package top.lizhistudio.autolua.service;


import android.net.LocalServerSocket;
import android.util.Log;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;


import top.lizhistudio.androidlua.JavaObjectWrapperFactoryImplement;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaContextImplement;
import top.lizhistudio.autolua.LuaInterpreter;
import top.lizhistudio.autolua.LuaInterpreterImplement;
import top.lizhistudio.autolua.extend.Controller;
import top.lizhistudio.autolua.extend.Screen;


public class Server {
    private final static String TAG  = "Server";
    private final Transport transport;
    private final ServiceHandler serviceHandler;
    private final ClientHandler clientHandler;
    private final AtomicBoolean isRunning;

    private static volatile Server self;

    static {
        System.loadLibrary("screen");
    }

    public Server(Transport transport, ServiceHandler serviceHandler, ClientHandler clientHandler)
    {
        this.transport = transport;
        this.serviceHandler = serviceHandler;
        this.clientHandler = clientHandler;
        this.isRunning = new AtomicBoolean(true);
    }

    public void serve()
    {
        while (isRunning.get())
        {
            Protocol.Message message = transport.receive();
            Protocol.Message.TYPE type = message.getMethod();
            if (type == Protocol.Message.TYPE.EXIT)
                stop();
            else if (type == Protocol.Message.TYPE.RETURN || type == Protocol.Message.TYPE.ERROR)
                clientHandler.onReceive(message);
            else
                serviceHandler.onReceive(message);
        }
    }


    public void stop()
    {
        if (isRunning.compareAndSet(true,false))
        {
            transport.close();
        }
    }

    private static class MyLog
    {
        public static int e(LuaContext context)
        {
            StringBuilder builder = new StringBuilder();
            for (int i=2;i<=context.getTop();i++)
            {
                builder.append(context.toString(i));
                builder.append("  ");
            }
            Log.e(TAG,builder.toString());
            return 0;
        }
    }


    private static LuaInterpreter createAutoEngine(LuaInterpreter clientProxy)
    {
        JavaObjectWrapperFactoryImplement.Builder builder = new JavaObjectWrapperFactoryImplement.Builder();
        builder.registerThrowable()
                .registerInterfaceByAnnotation(LuaInterpreter.class)
                .registerLuaAdapter(Controller.class)
                .registerLuaAdapter(MyLog.class);

        LuaInterpreterImplement luaInterpreter = new LuaInterpreterImplement(builder.build());
        return luaInterpreter.append(new LuaInterpreterImplement.Constructor() {
            @Override
            public void onConstruct(LuaContextImplement luaContext) {
                luaContext.push(LuaInterpreter.class,clientProxy);
                luaContext.setGlobal("UserInterface");
                luaContext.push(Controller.class,Controller.getDefault());
                luaContext.setGlobal("Controller");
                luaContext.push(MyLog.class);
                luaContext.setGlobal("Log");
            }
        });
    }


    public static void main(String[] args)
    {
        Options options = new Options();
        options.addOption("p","port",true,"server port");
        options.addOption("f","feature",true,"local server feature");
        options.addOption("h","help",false,"print options");
        options.addOption("v","verify",true,"verify password");
        try{
            CommandLine commandLine = new DefaultParser().parse(options,args);
            if (commandLine.hasOption('h'))
            {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("options",options);
            }else
            {
                IServerSocket serverSocket;
                if (commandLine.hasOption('p'))
                    serverSocket = new NetSocketServer(Integer.parseInt(commandLine.getOptionValue('p')));
                else
                    serverSocket = new LocalSocketServer(commandLine.getOptionValue('f'));
                Transport transport = new SocketTransport(serverSocket,commandLine.getOptionValue('v'));
                ClientHandler clientHandler = new ClientHandler(transport);
                ServiceHandler serviceHandler = new ServiceHandler(transport,createAutoEngine(clientHandler));
                Server server = new Server(transport,serviceHandler,clientHandler);
                self = server;
                System.out.println(1);
                server.serve();
                Screen.getDefault().destroy();
                System.exit(0);
            }
        }catch (Throwable e)
        {
            System.out.println(0);
        }
    }


    public static Server getDefault()
    {
        return self;
    }


    private interface IServerSocket
    {
        TSocket accept() throws IOException;
        void close() throws IOException;
    }


    private static class NetSocketServer implements IServerSocket
    {
        private final ServerSocket socket;
        private NetSocketServer(int port) throws IOException
        {
            socket = new ServerSocket(port);
        }

        @Override
        public TSocket accept() throws IOException {
            return new TSocket.NetSocket(socket.accept());
        }
        @Override
        public void close() throws IOException {
            socket.close();
        }
    }

    private static class LocalSocketServer implements IServerSocket
    {
        private final LocalServerSocket socket;
        private LocalSocketServer(String port) throws IOException
        {
            socket = new LocalServerSocket(port);
        }

        @Override
        public TSocket accept() throws IOException {
            return new TSocket.LocalTSocket(socket.accept());
        }
        @Override
        public void close() throws IOException {
            socket.close();
        }
    }


    private static class SocketTransport implements Transport {
        private final IServerSocket serverSocket;
        private final AtomicBoolean isRunning = new AtomicBoolean(true);
        private final Object sendMutex = new Object();
        private final Object receiveMutex = new Object();
        private final String password;

        private TSocket session;

        public SocketTransport(IServerSocket iServerSocket,String password)
        {
            serverSocket = iServerSocket;
            session = null;
            this.password = password;
        }

        private boolean checkPassword( TSocket socket)
        {
            try{
                socket.setSoTimeout(2000);
                Protocol.Message message = Protocol.Message.parseDelimitedFrom(socket.getInputStream());
                boolean result;
                if (password == null)
                    result = message.getDataCount() == 0;
                else if (message.getDataCount() == 0)
                    result = false;
                else
                    result = password.equals(message.getData(0).getS());
                message = Protocol.Message.newBuilder()
                        .addData(0, Protocol.LuaValue.newBuilder().setZ(result).build())
                        .build();
                message.writeDelimitedTo(socket.getOutputStream());
                socket.setSoTimeout(0);
                return result;
            }catch (IOException e)
            {
                return false;
            }
        }


        private TSocket getSession()
        {
            synchronized (isRunning)
            {
                try{
                    while (session == null)
                    {
                        TSocket socket = serverSocket.accept();
                        if (checkPassword(socket))
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

        @Override
        public void send(Protocol.Message message) {
            while (isRunning.get())
            {
                TSocket socket = getSession();
                try{
                    synchronized (sendMutex)
                    {
                        OutputStream out = socket.getOutputStream();
                        message.writeDelimitedTo(out);
                        out.flush();
                        break;
                    }
                }catch (IOException e)
                {
                    closeSession();
                }
            }
        }

        @Override
        public Protocol.Message receive() {
            while (isRunning.get())
            {
                TSocket socket = getSession();
                try{
                    synchronized (receiveMutex)
                    {
                        return Protocol.Message.parseDelimitedFrom(socket.getInputStream());
                    }
                }
                catch (IOException e)
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
}
