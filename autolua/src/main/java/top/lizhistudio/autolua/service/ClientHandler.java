package top.lizhistudio.autolua.service;


import top.lizhistudio.autolua.LuaInterpreter;
import top.lizhistudio.androidlua.exception.LuaError;

public class ClientHandler implements LuaInterpreter, Handler {
    private static final String TAG = "ClientHandler";
    private final Transport transport;
    private final MessageManager messageManager;
    private final MessageListenerManager listenerManager;

    public ClientHandler(Transport transport)
    {
        this.transport = transport;
        this.messageManager = new MessageManager();
        this.listenerManager = new MessageListenerManager();
    }

    private Object luaValueToJavaValue(Protocol.LuaValue v)
    {
        switch (v.getValueCase())
        {
            case B:return v.getB().toByteArray();
            case D:return v.getD();
            case L:return v.getL();
            case S:return v.getS();
            case Z:return v.getZ();
        }
        return null;
    }

    private void checkException(Protocol.Message message)
    {
        if (message.getMethod() == Protocol.Message.TYPE.ERROR)
            throw new LuaError(message.getData(0).getS());
    }

    private Object[] getResult(Protocol.Message message)
    {
        checkException(message);
        Object[] r = new Object[message.getDataCount()];
        for (int i = 0; i < r.length; i++) {
            r[i] = luaValueToJavaValue(message.getData(i));
        }
        return r;
    }


    private Protocol.Message getMessage(SyncMessageListener listener)
    {
        try{
            return listener.getMessage();
        }catch (InterruptedException e)
        {
            throw new LuaError(e);
        }
    }

    private Object[] executeResult(Protocol.Message message)
    {
        SyncMessageListener listener = new SyncMessageListener();
        listenerManager.push(message.getId(),listener);
        transport.send(message);
        return getResult(getMessage(listener));
    }


    @Override
    public Object[] execute(byte[] code, String chunkName) {
        Protocol.Message message = messageManager.buildExecute(code,chunkName);
        return executeResult(message);
    }

    @Override
    public Object[] executeFile(String path) {
        Protocol.Message message = messageManager.buildExecuteFIle(path);
        return executeResult(message);
    }

    @Override
    public boolean isRunning() {
        Protocol.Message message = messageManager.buildIsRunning();
        final SyncMessageListener listener = new SyncMessageListener();
        listenerManager.push(message.getId(),listener);
        transport.send(message);
        message = getMessage(listener);
        checkException(message);
        return message.getData(0).getZ();
    }

    private void noResultCall(Protocol.Message message)
    {
        final SyncMessageListener listener = new SyncMessageListener();
        listenerManager.push(message.getId(),listener);
        transport.send(message);
        message = getMessage(listener);
        checkException(message);
    }

    @Override
    public void reset() {
        Protocol.Message message = messageManager.buildReset();
        noResultCall(message);
    }

    @Override
    public void interrupt() {
        Protocol.Message message = messageManager.buildInterrupt();
        noResultCall(message);
    }


    @Override
    public void execute(byte[] code, String chunkName, Callback callback) {
        new Thread()
        {
            @Override
            public void run() {
                try{
                    callback.onCompleted(execute(code,chunkName));
                }catch (Throwable e)
                {
                    callback.onError(e);
                }
            }
        }.start();
    }

    @Override
    public void executeFile(String path, Callback callback) {
        new Thread()
        {
            @Override
            public void run() {
                try{
                    callback.onCompleted(executeFile(path));
                }catch (Throwable e)
                {
                    callback.onError(e);
                }
            }
        }.start();
    }


    public void onReceive(Protocol.Message message)
    {
        int id = message.getId();
        MessageListener listener = listenerManager.remove(id);
        listener.onReceive(message);
    }
}
