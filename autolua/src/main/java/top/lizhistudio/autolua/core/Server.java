package top.lizhistudio.autolua.core;


import androidx.annotation.NonNull;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import top.lizhistudio.autolua.rpc.ClientHandler;
import top.lizhistudio.autolua.rpc.ServiceHandler;
import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;
import top.lizhistudio.autolua.rpc.transport.IServerSocket;
import top.lizhistudio.autolua.rpc.transport.ServerTransport;
import top.lizhistudio.autolua.rpc.transport.TSocket;
import top.lizhistudio.autolua.rpc.transport.Transport;

public class Server {
    private static final String TAG = "Server";
    public static final String AUTO_LUA_SERVICE_NAME = "AutoLua";
    public static final String LISTENER_SERVICE_NAME = "PrintListener";
    private final Transport transport;
    private final ClientHandler clientHandler;
    private final ServiceHandler serviceHandler;
    private final ExecutorService executorService;
    private static Server self = null;


    public Server(Transport transport, ClientHandler clientHandler,ServiceHandler serviceHandler)
    {
        this.transport = transport;
        this.clientHandler = clientHandler;
        this.serviceHandler = serviceHandler;
        executorService= Executors.newCachedThreadPool();
    }

    public void serve()
    {
        while (true)
        {
            Object message = transport.receive();
            if (message instanceof Request &&
                    ((Request)message).serviceID == null &&
                    ((Request)message).callID == 0)
                break;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (message instanceof Request)
                        serviceHandler.onReceive((Request)message);
                    else if(message instanceof Response)
                        clientHandler.onReceive((Response)message);
                    else
                        throw new RuntimeException("unknown message");
                }
            });
        }
    }



    private static class PasswordProcurator implements ServerTransport.Procurator
    {
        private final String password;

        public PasswordProcurator(@NonNull String password)
        {
            this.password = password;
        }

        @Override
        public boolean isAccept(TSocket socket) {
            try{
                socket.setSoTimeout(2000);
                Object o = socket.getInputStream().readObject();
                boolean result = password.equals(o);
                socket.getOutputStream().writeObject(result);
                socket.setSoTimeout(0);
                return result;
            }catch (Throwable e)
            {
                return false;
            }

        }
    }


    public static void main(String[] args)
    {
        Options options = new Options();
        options.addOption("p","port",true,"server port");
        options.addOption("l","local-server",true,"local server feature");
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
                    serverSocket = new IServerSocket.NetSocketServer(Integer.parseInt(commandLine.getOptionValue('p')));
                else
                    serverSocket = new IServerSocket.LocalSocketServer(commandLine.getOptionValue('l'));
                Transport transport = new ServerTransport(serverSocket,
                        new PasswordProcurator( commandLine.getOptionValue('v')));
                ClientHandler clientHandler = new ClientHandler(transport);
                ServiceHandler serviceHandler = new ServiceHandler(transport);
                UserInterface userInterface = clientHandler.getService(UserInterface.LUA_CLASS_NAME,UserInterface.class);
                LuaInterpreter.PrintListener printListener = clientHandler.getService(
                        LISTENER_SERVICE_NAME , LuaInterpreter.PrintListener.class);
                LuaInterpreter luaInterpreter = new LuaInterpreterImplement(userInterface,
                        printListener);
                serviceHandler.register(AUTO_LUA_SERVICE_NAME,LuaInterpreter.class,luaInterpreter);
                self = new Server(transport,clientHandler,serviceHandler);
                System.out.println(1);
                self.serve();
                System.exit(0);
            }
        }catch (Throwable e)
        {
            e.printStackTrace(System.err);
            e.printStackTrace(System.out);
            System.out.println(0);
        }
    }

    public static Server getInstance()
    {
        return self;
    }

    public ClientHandler getClientHandler()
    {
        return clientHandler;
    }
}
