package top.lizhistudio.app.core;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;

import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.app.core.implement.DebugServiceImplement;
import top.lizhistudio.app.thrift.DebuggerService;

public class DebuggerServer extends Observable {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private THsHaServer server;

    private DebuggerServer()
    {
    }



    public boolean isServing()
    {
        return isRunning.get() && server != null && server.isServing();
    }


    private void update(boolean isRun)
    {
        synchronized (isRunning)
        {
            isRunning.set(isRun);
            setChanged();
            notifyObservers(isRun);
        }
    }

    private boolean compareAndSet(boolean exception,boolean set)
    {
        synchronized (isRunning)
        {
            if (isRunning.compareAndSet(exception,set))
            {

                setChanged();
                notifyObservers(set);
                return true;
            }
        }
        return false;
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
                }catch (Throwable e)
                {
                    e.printStackTrace();
                }
                finally {
                    update(false);
                }
            }
        }.start();
    }


    public void start(int port)
    {
        if (compareAndSet(false,true))
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
