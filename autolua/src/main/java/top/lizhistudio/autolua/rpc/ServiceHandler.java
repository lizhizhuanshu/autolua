package top.lizhistudio.autolua.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import top.lizhistudio.autolua.annotation.RPCMethod;
import top.lizhistudio.autolua.exception.RemoteException;
import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;
import top.lizhistudio.autolua.rpc.transport.Transport;

public class ServiceHandler {
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


    public void onReceive(Request request)
    {
        Object result;
        try {
            if (request.serviceName == null) {
                result = allServiceName();
            } else if (request.methodName == null)
            {
                result = rpcServices.get(request.serviceName)!=null;
            }
            else
            {
                RPCService service = rpcServices.get(request.serviceName);
                assert service!=null;
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
