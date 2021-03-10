package top.lizhistudio.autolua.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import top.lizhistudio.autolua.annotation.RPCMethod;
import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;
import top.lizhistudio.autolua.rpc.transport.Transport;

public class ClientHandler {
    private final Transport transport;
    private final CallbackCache listenerManager;
    private final AtomicLong id = new AtomicLong(1);

    public ClientHandler(Transport transport)
    {
        this.transport = transport;
        listenerManager = new CallbackCache();
    }

    public void onReceive(Response response)
    {
        Callback callback = listenerManager.remove(response.callID);
        if (response.result instanceof Throwable)
            callback.onError((Throwable) response.result);
        else
            callback.onCompleted(response.result);
    }

    private Object sendAndGetResult(Request request) throws InterruptedException
    {
        SyncCallback syncCallback = new SyncCallback();
        listenerManager.push(request.callID,syncCallback);
        transport.send(request);
        return syncCallback.getResult();
    }

    public void sendExitRequest()
    {
        transport.send(new Request());
    }


    public String[] allServiceName() throws InterruptedException
    {
        Request request = new Request();
        do{
            request.callID = id.getAndAdd(1);
        }while (request.callID == 0);
        request.serviceName = null;
        return (String[])sendAndGetResult(request);
    }

    private boolean hasService(String name) throws InterruptedException
    {
        Request request = new Request();
        request.serviceName = name;
        request.callID = id.getAndAdd(1);
        request.methodName = null;
        return (boolean)sendAndGetResult(request);
    }

    public <T> T getService(String name,Class<T> serviceInterface) throws InterruptedException
    {
        return hasService (name)?(T)(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader()
                ,new Class[]{serviceInterface}
                ,new ProxyHandler(name))):null;
    }

    private class ProxyHandler implements InvocationHandler
    {
        private final String serviceName;
        ProxyHandler(String serviceName)
        {
            this.serviceName = serviceName;
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Request request = new Request();
            request.callID = id.getAndAdd(1);
            request.serviceName = this.serviceName;
            RPCMethod rpcMethod = method.getAnnotation(RPCMethod.class);
            request.methodName = rpcMethod.alias();
            if (request.methodName.equals(""))
                request.methodName = method.getName();
            request.params = args;
            if (rpcMethod.async())
            {
                Callback callback = (Callback)args[args.length-1];
                args[args.length-1] = null;
                listenerManager.push(request.callID,callback);
                transport.send(request);
                return null;
            }
            return sendAndGetResult(request);
        }
    }
}
