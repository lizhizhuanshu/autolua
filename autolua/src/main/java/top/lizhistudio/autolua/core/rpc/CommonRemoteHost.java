package top.lizhistudio.autolua.core.rpc;

import android.util.Log;
import android.util.LongSparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import top.lizhistudio.androidlua.exception.LuaError;
import top.lizhistudio.androidlua.exception.LuaRuntimeError;


public abstract class CommonRemoteHost implements RemoteHost{
    private final String TAG = "CommonRemoteHost";
    private final InputStream is;
    private final OutputStream os;
    private final AtomicLong methodID = new AtomicLong(0);
    private final LongSparseArray<Callback> callbackCache= new LongSparseArray<>();
    private final ExecutorService executorService;
    private WeakReference<Handler> handler = null;
    private volatile Thread serveThread = null;

    public CommonRemoteHost(InputStream is,OutputStream os)
    {
        this.is = is;
        this.os = os;
        executorService = Executors.newCachedThreadPool();
    }

    public void setHandler(Handler handler)
    {
        this.handler = new WeakReference<>(handler);
    }

    protected abstract void onSendMessageSize(int size,OutputStream os) throws IOException;
    protected abstract int onReceiveMessageSize(InputStream inputStream) throws IOException;

    protected void sendMessage(Protocol.Message message) throws IOException {
        synchronized (os)
        {
            onSendMessageSize(message.getSerializedSize(),os);
            message.writeTo(os);
            os.flush();
        }
    }

    private Protocol.Message receive() throws IOException
    {
        synchronized (is)
        {
            int size = onReceiveMessageSize(is);
            byte[] buffer = new byte[size];
            Util.receive(is,buffer,0,size);
            return Protocol.Message.parseFrom(buffer);
        }
    }

    @Override
    public Protocol.Message call(Protocol.Message.Builder requestBuilder) {
        long id = newID();
        requestBuilder.setId(id)
                .setType(Protocol.Message.TYPE.REQUEST);
        SyncCallback callback = new SyncCallback();
        synchronized (callbackCache)
        {
            callbackCache.put(id,callback);
        }
        try{
            sendMessage(requestBuilder.build());
            return callback.getMessage();
        }catch (IOException |InterruptedException e)
        {
            throw new LuaRuntimeError(e);
        }
        finally {
            synchronized (callbackCache)
            {
                callbackCache.remove(id);
            }
        }
    }

    public Protocol.Message callAndCheckException(Protocol.Message.Builder requestBuilder)
    {
        Protocol.Message message = call(requestBuilder);
        Util.checkException(message);
        return message;
    }

    @Override
    public void call(Protocol.Message.Builder requestBuilder, Callback callback)
    {
        long id = newID();
        requestBuilder.setId(id)
                .setType(Protocol.Message.TYPE.REQUEST);
        synchronized (callbackCache)
        {
            callbackCache.put(id,callback);
        }
        try{
            sendMessage(requestBuilder.build());
        }catch (IOException e)
        {
            synchronized (callbackCache)
            {
                callbackCache.remove(id);
            }
            throw new LuaRuntimeError(e);
        }
    }

    private static class SyncCallback implements Callback
    {
        private Protocol.Message message = null;
        private Throwable exception = null;
        public Protocol.Message getMessage() throws InterruptedException
        {
            synchronized (this)
            {
                if (message == null && exception == null)
                    this.wait();
                if (exception != null)
                {
                    if (exception instanceof LuaError)
                    {
                        throw (LuaError)exception;
                    }else
                    {
                        throw new LuaRuntimeError(exception);
                    }
                }
                return message;
            }
        }


        @Override
        public void onCallback(Protocol.Message message) {
            synchronized (this)
            {
                this.message = message;
                this.notifyAll();
            }
        }

        @Override
        public void onError(Throwable ioException) {
            synchronized (this)
            {
                exception = ioException;
                this.notifyAll();
            }
        }

    }
    private long newID()
    {
        return methodID.getAndAdd(1);
    }


    @Override
    public void serve() {

        serveThread = Thread.currentThread();
        while (!Thread.interrupted())
        {
            try{
                Protocol.Message message = receive();
                if (message == null)
                    break;
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        onHandle(message);
                    }
                });
            }catch (IOException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }


    private void onHandle(Protocol.Message message)
    {
        long id = message.getId();
        if (message.getType() == Protocol.Message.TYPE.RESPONSE)
        {
            Callback callback = getCallback(id);
            callback.onCallback(message);
            removeCallback(id);
        }else
        {
            Protocol.Message.Builder builder = Protocol.Message.newBuilder();
            try{
                handler.get().onHandle(message,builder);
            }catch (Throwable throwable)
            {
                builder.clear();
                builder.setError(Protocol.LuaError.newBuilder()
                        .setType(throwable.getClass().getName())
                        .setMessage(throwable.getMessage()));
            }
            builder.setId(id);
            builder.setType(Protocol.Message.TYPE.RESPONSE);
            try{
                sendMessage(builder.build());
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private Callback getCallback(long id){
        synchronized (callbackCache)
        {
            return callbackCache.get(id);
        }
    }

    private void removeCallback(long id)
    {
        synchronized (callbackCache)
        {
            callbackCache.remove(id);
        }
    }

    @Override
    public void interrupt() {
        Thread thread = serveThread;
        if (thread != null)
        {
            thread.interrupt();
        }
    }

    @Override
    public void releaseAllCallback(Throwable throwable) {
        synchronized (callbackCache)
        {
            for (int i = 0; i < callbackCache.size(); i++) {
                Callback callback = callbackCache.valueAt(i);
                callback.onError(throwable);
            }
            callbackCache.clear();
        }
    }
}
