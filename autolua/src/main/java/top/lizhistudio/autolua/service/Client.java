package top.lizhistudio.autolua.service;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.autolua.AutoLuaEngine;
import top.lizhistudio.autolua.LuaInterpreter;
import top.lizhistudio.androidlua.exception.LuaError;

public class Client implements AutoLuaEngine {
    private static  final String TAG = "Client";
    private final Transport transport;
    private final ClientHandler clientHandler;
    private final ServiceHandler serviceHandler;
    private final Thread thread;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private Client(Transport transport, LuaInterpreter luaInterpreter)
    {
        this.clientHandler = new ClientHandler(transport);
        this.transport = transport;
        serviceHandler = new ServiceHandler(transport,luaInterpreter);
        this.thread = new Thread()
        {
            @Override
            public void run() {
                talk();
            }
        };
        thread.start();
    }

    public static Client newInstance(String ip,int port,String password,LuaInterpreter luaInterpreter) throws IOException
    {
        Socket socket = new Socket(ip,port);
        return newInstance(new TSocket.NetSocket(socket),password, luaInterpreter);
    }

    private static boolean verifyPassword(Transport transport,String password)
    {
        Protocol.Message message = MessageManager.buildVerify(password);
        transport.send(message);
        message = transport.receive();
        return message.getData(0).getZ();
    }

    private static Client newInstance(TSocket socket,String password, LuaInterpreter luaInterpreter) throws IOException
    {
        BaseTransport transport = new BaseTransport( socket);
        if (!verifyPassword(transport,password))
        {
            throw new RuntimeException("password error");
        }
        return new Client(transport,luaInterpreter);
    }

    public static Client newInstance(String feature,String password,LuaInterpreter luaInterpreter) throws IOException
    {
        LocalSocket socket = new LocalSocket();
        socket.connect(new LocalSocketAddress(feature));
        return newInstance(new TSocket.LocalTSocket(socket),password, luaInterpreter);
    }



    private void talk()
    {
        while (isRunning.get())
        {
            Protocol.Message message = transport.receive();
            Protocol.Message.TYPE type = message.getMethod();
            if (type == Protocol.Message.TYPE.RETURN || type == Protocol.Message.TYPE.ERROR)
                clientHandler.onReceive(message);
            else
                serviceHandler.onReceive(message);
        }
    }


    public void destroy()
    {
        if (isRunning.compareAndSet(true,false))
        {
            transport.close();
            thread.interrupt();
        }
    }



    @Override
    public Object[] execute(byte[] code, String chunkName) {
        return clientHandler.execute(code, chunkName);
    }

    @Override
    public Object[] executeFile(String path) {
        return clientHandler.executeFile(path);
    }

    @Override
    public boolean isRunning() {
        return clientHandler.isRunning();
    }

    @Override
    public void reset() {
        clientHandler.reset();
    }

    @Override
    public void interrupt() {
        clientHandler.interrupt();
    }

    @Override
    public void execute(byte[] code, String chunkName, Callback callback) {
        clientHandler.execute(code, chunkName, callback);
    }

    @Override
    public void executeFile(String path, Callback callback) {
        clientHandler.executeFile(path, callback);
    }

    @Override
    public void exit() {
        transport.send(MessageManager.buildExit());
    }


    public static class BaseTransport implements Transport
    {
        private final TSocket socket;
        private final OutputStream out;
        private final InputStream in;
        public BaseTransport(TSocket socket) throws IOException
        {
            this.socket = socket;
            this.out = socket.getOutputStream();
            this.in = socket.getInputStream();
        }
        @Override
        public void send(Protocol.Message message) {
            synchronized (out)
            {
                try{
                    message.writeDelimitedTo(out);
                    out.flush();
                }catch (IOException e)
                {
                    throw  new LuaError(e);
                }

            }
        }

        @Override
        public Protocol.Message receive() {
            synchronized (in)
            {
                try{
                    return Protocol.Message.parseDelimitedFrom(in);
                }catch (IOException e)
                {
                    throw  new LuaError(e);
                }
            }
        }

        public void close(){
            try{
                socket.close();
            }catch (IOException e)
            {
                throw  new LuaError(e);
            }
        }
    }

}
