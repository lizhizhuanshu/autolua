package top.lizhistudio.autolua.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import top.lizhistudio.autolua.annotation.RPCMethod;
import top.lizhistudio.autolua.exception.RPCException;
import top.lizhistudio.autolua.exception.RemoteException;
import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;
import top.lizhistudio.autolua.rpc.transport.Transport;

public class ServiceHandler {
    private final ConcurrentHashMap<String,RPCService> rpcServices;
    private final Transport transport;
    public ServiceHandler(Transport transport)
    {
        rpcServices = new ConcurrentHashMap<>();
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

    private HashMap<String,Class<?>> getServices()
    {
        HashMap<String,Class<?>> services = new HashMap<>();
        for (String key:rpcServices.keySet())
        {
            services.put(key,rpcServices.get(key).getInterface());
        }
        return services;
    }


    public void onReceive(Request request)
    {
        Object result;
        try{
            if (request.serviceName == null)
            {
                result = getServices();
            }else
            {
                RPCService service = rpcServices.get(request.serviceName);
                Method method = service.getMethod(request.methodName);
                if (method.getAnnotation(RPCMethod.class).async())
                {
                    request.params[request.params.length-1] = new MyCallback(request.callID,transport);
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
        transport.send(new Response(request.callID,result));
    }

    private static class MyCallback implements Callback
    {
        private final long callID;
        private final Transport transport;
        public MyCallback(long callID,Transport transport)
        {
            this.callID = callID;
            this.transport = transport;
        }

        @Override
        public void onCompleted(Object result) {
            transport.send(new Response(callID,result));
        }

        @Override
        public void onError(Throwable throwable) {
            transport.send(new Response(callID,throwable));
        }
    }
}
