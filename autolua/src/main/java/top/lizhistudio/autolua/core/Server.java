package top.lizhistudio.autolua;

import android.util.Log;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.autolua.extend.Screen;
import top.lizhistudio.autolua.rpc.ClientHandler;
import top.lizhistudio.autolua.rpc.ServiceHandler;
import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;
import top.lizhistudio.autolua.rpc.transport.IServerSocket;
import top.lizhistudio.autolua.rpc.transport.ServerTransport;
import top.lizhistudio.autolua.rpc.transport.Transport;

public class Server {
    private final Transport transport;
    private final ClientHandler clientHandler;
    private final ServiceHandler serviceHandler;
    private final AtomicBoolean isRunning;

    public Server(Transport transport, ClientHandler clientHandler,ServiceHandler serviceHandler)
    {
        this.transport = transport;
        this.clientHandler = clientHandler;
        this.serviceHandler = serviceHandler;
        isRunning = new AtomicBoolean(true);
    }

    public void serve()
    {
        while (isRunning.get())
        {
            Object message = transport.receive();
            if (message instanceof Request)
            {
                Request request = (Request) message;
                if (request.serviceName == null && request.callID == 0)
                    break;
                serviceHandler.onReceive((Request)message);
            }
            else if(message instanceof Response)
                clientHandler.onReceive((Response)message);
            else
                throw new RuntimeException("unknown message");
        }
    }

    private static LuaContextFactory createFactory(String className)
            throws ClassNotFoundException,
            IllegalAccessException,
            InstantiationException
    {
        Class<?> aClass = Class.forName(className);
        return (LuaContextFactory) aClass.newInstance();
    }


    public static void main(String[] args)
    {
        Options options = new Options();
        options.addOption("p","port",true,"server port");
        options.addOption("l","local-server",true,"local server feature");
        options.addOption("h","help",false,"print options");
        options.addOption("v","verify",true,"verify password");
        options.addOption("f","factory",true,"lua context factory class name");
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
                Transport transport = new ServerTransport(serverSocket,commandLine.getOptionValue('v'));
                ClientHandler clientHandler = new ClientHandler(transport);
                ServiceHandler serviceHandler = new ServiceHandler(transport);
                LuaContextFactory  luaContextFactory = createFactory(commandLine.getOptionValue('f'));
                LuaInterpreter luaInterpreter = new LuaInterpreterImplement(luaContextFactory);
                serviceHandler.register("AutoLua",LuaInterpreter.class,luaInterpreter);
                Server server = new Server(transport,clientHandler,serviceHandler);
                System.out.println(1);
                server.serve();
                Screen.getDefault().destroy();
                System.exit(0);
            }
        }catch (Throwable e)
        {
            Log.e("Server","error",e);
            System.out.println(0);
        }
    }


}
