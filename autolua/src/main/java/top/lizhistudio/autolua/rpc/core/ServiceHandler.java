package top.lizhistudio.autolua.rpc;

import android.net.Uri;
import android.util.Log;
import android.view.WindowManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import top.lizhistudio.androidlua.Util;
import top.lizhistudio.autolua.annotation.RPCInterface;
import top.lizhistudio.autolua.annotation.RPCMethod;
import top.lizhistudio.autolua.exception.RemoteException;
import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;
import top.lizhistudio.autolua.rpc.transport.Transport;

public class ServiceHandler {
    private final LocalRPCInterfaceCache localRPCInterfaceCache = new LocalRPCInterfaceCache();
    private final RPCServiceCache rpcServices;
    private final Transport transport;

    public ServiceHandler(Transport transport)
    {
        this.transport = transport;
        this.rpcServices = new RPCServiceCache();
    }

    public ServiceHandler(Transport transport,RPCServiceCache rpcServices)
    {
        this.rpcServices = rpcServices;
        this.transport = transport;
    }

    public RPCService register(String name,Class<?> aInterface,Object service)
    {
        return rpcServices.put(name,new RPCService(aInterface,service));
    }

    public RPCService register(String name,RPCService rpcService)
    {
        return rpcServices.put(name, rpcService);
    }

    public void unRegister(String name)
    {
        rpcServices.remove(name);
    }

    private String[] allServiceName()
    {
        return rpcServices.allServiceName();
    }

    private void sendResponse(long callID,Class<?> aClass, Object result)
    {
        if (result !=null
                && aClass != null
                && aClass.isAnnotationPresent(RPCInterface.class)
                && aClass.isInstance(result))
        {
            long id = localRPCInterfaceCache.append(new RPCService(aClass,result));
            transport.send(new Response(callID,id));
        }else
        {
            transport.send(new Response(callID,result));
        }
    }

    private RPCService getService(Object serviceID)
    {
        if (serviceID instanceof String)
            return rpcServices.get((String)serviceID);
        else if (serviceID instanceof Long)
            return localRPCInterfaceCache.get((long)serviceID);
        return null;
    }


    public void onReceive(Request request)
    {
        if ( "newFloatView".equals(request.methodName))
        {
            for(Object o:(Object[])request.params)
            {
                System.err.println(o.toString());
            }
        }
        Object result;
        Class<?> returnClass = null;
        try {
            if (request.serviceID == null) {
                result = allServiceName();
            } else if (request.methodName == null)
            {
                if (request.serviceID instanceof String)
                {
                    result = rpcServices.get((String)request.serviceID)!=null;
                }else
                {
                    localRPCInterfaceCache.remove((long)request.serviceID);
                    result = true;
                }
            }
            else
            {
                RPCService service = getService(request.serviceID);
                assert service!=null;
                Method method = service.getMethod(request.methodName);
                returnClass = method.getReturnType();
                if (method.getAnnotation(RPCMethod.class).async())
                {
                    request.params[request.params.length-1] = new MyCallback(request.callID,returnClass);
                    method.invoke(service.getService(),request.params);
                    return;
                }else
                    result = method.invoke(service.getService(),request.params);
            }
        }catch (InvocationTargetException e)
        {
            result = new RemoteException(e.getCause());
        }catch (Throwable e)
        {
            result = new RemoteException(e);
        }
        sendResponse(request.callID,returnClass, result);
    }

    private class MyCallback implements Callback
    {
        private final long callID;
        private final Class<?> resultClass;
        public MyCallback(long callID,Class<?> resultClass)
        {
            this.callID = callID;
            this.resultClass = resultClass;
        }

        @Override
        public void onCompleted(Object result) {
            sendResponse(callID,resultClass,result);
        }

        @Override
        public void onError(Throwable throwable) {
            sendResponse(callID,null,throwable);
        }
    }
}
