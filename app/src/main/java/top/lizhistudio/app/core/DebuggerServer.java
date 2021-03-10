package top.lizhistudio.autoluaapp.core;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.autoluaapp.core.implement.DebugServiceImplement;
import top.lizhistudio.autoluaapp.thrift.DebuggerService;

public class DebuggerServer {
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private THsHaServer server;

    private DebuggerServer()
    {
    }

    public boolean isServing()
    {
        return isRunning.get() && server != null && server.isServing();
    }

    private void startService(int port)
    {
        new Thread()
        {
            @Override
            public void run() {
                try{
                    TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(port);
                    TProcessor processor = new DebuggerService.Processor<>(new DebugServiceImplement());
                    THsHaServer.Args args = new THsHaServer.Args(serverTransport);
                    args.processor(processor);
                    args.transportFactory(new TFramedTransport.Factory());
                    args.protocolFactory(new TBinaryProtocol.Factory());
                    server = new THsHaServer(args);
                    server.serve();
                    serverTransport.close();
                    server = null;
                }catch (TTransportException e)
                {
                    e.printStackTrace();
                }
                finally {
                    isRunning.set(false);
                }
            }
        }.start();
    }


    public void start(int port)
    {
        if (isRunning.compareAndSet(false,true))
        {
            startService(port);
        }
    }

    public void stop()
    {
        if (isRunning.get())
        {
            THsHaServer server = this.server;
            if (server !=null && server.isServing())
            {
                server.stop();
            }
        }
    }

    private static class Stub{
        private static final DebuggerServer server = new DebuggerServer();
    }
    public static DebuggerServer getInstance()
    {
        return Stub.server;
    }
}
