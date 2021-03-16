package top.lizhistudio.autolua.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

import top.lizhistudio.androidlua.Util;
import top.lizhistudio.autolua.annotation.RPCInterface;
import top.lizhistudio.autolua.annotation.RPCMethod;
import top.lizhistudio.autolua.exception.RemoteException;
import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;
import top.lizhistudio.autolua.rpc.transport.Transport;

public class ClientHandler {
    private final Transport transport;
    private final CallbackCache callbackCache;
    private final AtomicLong id = new AtomicLong(1);

    public ClientHandler(Transport transport)
    {
        this.transport = transport;
        callbackCache = new CallbackCache();
    }

    public void onReceive(Response response)
    {
        Callback callback = callbackCache.remove(response.callID);
        if (response.result instanceof Throwable)
            callback.onError((Throwable) response.result);
        else
        {
            callback.onCompleted(response.result);
        }
    }

    private Object sendAndGetResult(Request request) throws InterruptedException
    {
        return sendAndGetResult(request,null);
    }

    private Object sendAndGetResult(Request request,Class<?> aClass) throws InterruptedException
    {
        SyncCallback syncCallback = new SyncCallback();
        if (aClass != null && aClass.isAnnotationPresent(RPCInterface.class))
            callbackCache.push(request.callID,new RPCInterfaceCallback(aClass,syncCallback));
        else
            callbackCache.push(request.callID,syncCallback);
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
        request.serviceID = null;
        return (String[])sendAndGetResult(request);
    }

    private boolean hasService(String name) throws InterruptedException
    {
        Request request = new Request();
        request.serviceID = name;
        request.callID = id.getAndAdd(1);
        request.methodName = null;
        return (boolean)sendAndGetResult(request);
    }


    private <ID,T> T newService(ID serviceID,Class<T> serviceInterface)
    {
        return (T)(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader()
                ,new Class[]{serviceInterface}
                ,new ProxyHandler(serviceID,serviceInterface)));
    }

    public <T> T getService(String name,Class<T> serviceInterface) throws InterruptedException
    {
        return hasService (name)?newService(name,serviceInterface):null;
    }

    private void releaseLocalRPCService(long serviceID)throws InterruptedException
    {
        Request request = new Request();
        request.serviceID = serviceID;
        request.callID = id.getAndAdd(1);
        request.methodName = null;
        sendAndGetResult(request);
    }


    private class ProxyHandler<T> implements InvocationHandler
    {
        private final T serviceID;
        private final Class<?> aClass;

        ProxyHandler(T serviceID,Class<?> aClass)
        {
            this.serviceID = serviceID;
            this.aClass = aClass;
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Request request = new Request();
            request.callID = id.getAndAdd(1);
            request.serviceID = this.serviceID;
            RPCMethod rpcMethod = method.getAnnotation(RPCMethod.class);
            if (rpcMethod == null){
                if (method.getName().equals("toString"))
                    return aClass.getName()+"@"+this.hashCode();
                else if(method.getName().equals("hashCode"))
                    return this.hashCode();
                else
                    return null;
            }
            request.methodName = rpcMethod.alias();
            if (request.methodName.equals(""))
                request.methodName = method.getName();
            request.params = args;
            Class<?> returnClass = method.getReturnType();
            if (rpcMethod.async())
            {
                Callback callback = (Callback)args[args.length-1];
                if (returnClass.isAnnotationPresent(RPCInterface.class))
                {
                    callback = new RPCInterfaceCallback(returnClass,callback);
                }
                args[args.length-1] = null;
                callbackCache.push(request.callID,callback);
                transport.send(request);
                return null;
            }
            return sendAndGetResult(request,returnClass);
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if (serviceID instanceof Long)
                releaseLocalRPCService((Long)serviceID);
        }
    }

    private class RPCInterfaceCallback implements Callback{
        private final Class<?> aClass;
        private final Callback callback;
        RPCInterfaceCallback(Class<?> aClass,Callback callback){
            this.aClass = aClass;
            this.callback = callback;
        }
        @Override
        public void onCompleted(Object result) {
            if (result != null)
                result = newService((Long)result,aClass);
            callback.onCompleted(result);
        }
        @Override
        public void onError(Throwable throwable) {
            callback.onError(throwable);
        }
    }


    private static class SyncCallback implements Callback{
        private boolean completed = false;
        private Object object;
        private boolean hasThrowable = false;

        public synchronized Object getResult() throws InterruptedException {
            if (!completed)
                wait();
            if (hasThrowable)
            {
                if (object instanceof RemoteException)
                    throw (RemoteException)object;
                else
                    throw new RuntimeException((Throwable)object);
            }
            return object;
        }

        @Override
        public synchronized void onCompleted(Object result) {
            object = result;
            completed = true;
            notifyAll();
        }

        @Override
        public synchronized void onError(Throwable throwable) {
            hasThrowable = true;
            completed = true;
            onCompleted(throwable);
        }
    }

}
