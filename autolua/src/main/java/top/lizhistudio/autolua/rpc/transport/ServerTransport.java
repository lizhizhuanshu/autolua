package top.lizhistudio.autolua.rpc.transport;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;

public class ServerTransport implements Transport {
    private final IServerSocket serverSocket;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final Object sendMutex = new Object();
    private final Object receiveMutex = new Object();
    private final Procurator procurator;

    private TSocket session;

    public interface Procurator
    {
        boolean isAccept(TSocket socket);
    }

    public ServerTransport(IServerSocket iServerSocket,@NonNull Procurator procurator)
    {
        serverSocket = iServerSocket;
        session = null;
        this.procurator= procurator;
    }

    private TSocket getSession()
    {
        synchronized (isRunning)
        {
            try{
                while (session == null)
                {
                    TSocket socket = serverSocket.accept();
                    if (procurator.isAccept(socket))
                    {
                        session = socket;
                    }else
                        socket.close();
                }

            }catch (IOException e)
            {
                throw  new RuntimeException(e);
            }
            return session;
        }
    }

    private void closeSession()
    {
        synchronized (isRunning)
        {
            if (session != null)
            {
                try{
                    session.close();
                }catch (IOException e)
                {
                }
                session = null;
            }

        }
    }

    private void sendObject(Object o)
    {
        while (isRunning.get())
        {
            TSocket socket = getSession();
            try{
                synchronized (sendMutex)
                {
                    socket.getOutputStream().writeObject(o);
                    socket.getOutputStream().flush();
                    break;
                }
            }catch (IOException e)
            {
                closeSession();
            }
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
        while (isRunning.get())
        {
            TSocket socket = getSession();
            try{
                synchronized (receiveMutex)
                {
                    return socket.getInputStream().readObject();
                }
            }
            catch (IOException | ClassNotFoundException e)
            {
                closeSession();
            }
        }
        return null;
    }

    @Override
    public void close() {
        synchronized (isRunning)
        {
            try {
                if (isRunning.compareAndSet(true,false))
                    serverSocket.close();
            }catch (IOException e)
            {
            }
        }
    }
}