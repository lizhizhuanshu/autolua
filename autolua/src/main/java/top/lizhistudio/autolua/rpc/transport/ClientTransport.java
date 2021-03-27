package top.lizhistudio.autolua.rpc.transport;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.LinkedBlockingQueue;

import top.lizhistudio.androidlua.exception.LuaError;
import top.lizhistudio.autolua.exception.DisconnectException;
import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;

public class ClientTransport implements Transport{
    private final TSocket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final LinkedBlockingQueue<Object> sendQueue;
    private final Thread thread;
    public ClientTransport(TSocket socket) throws IOException
    {
        this.socket = socket;
        this.outputStream = socket.getOutputStream();
        this.inputStream = socket.getInputStream();
        sendQueue = new LinkedBlockingQueue<>();
        thread = new Thread()
        {
            @Override
            public void run() {
                try{
                    while (!Thread.currentThread().isInterrupted())
                    {
                        Object o = sendQueue.take();
                        outputStream.writeObject(o);
                        outputStream.flush();
                    }
                }catch (InterruptedException | IOException e)
                {
                }
            }
        };
        thread.start();
    }

    private void sendObject(Object o)
    {
        try{
            sendQueue.put(o);
        }catch (InterruptedException e)
        {
        }
    }

    @Override
    public void send(Response response) {
        sendObject(response);
    }

    @Override
    public void send(Request request) {
        sendObject(request);
    }

    @Override
    public Object receive() {
        synchronized (inputStream)
        {
            try{
                return inputStream.readObject();
            }catch (IOException | ClassNotFoundException e)
            {
                throw new DisconnectException(e);
            }
        }
    }

    @Override
    public void close() {
        synchronized (socket)
        {
            try{
                socket.close();
                thread.interrupt();
            }catch (IOException e)
            {
            }
        }
    }
}
