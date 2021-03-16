package top.lizhistudio.autolua.rpc;


import java.util.concurrent.ConcurrentHashMap;

public class RPCServiceCache {
    private final ConcurrentHashMap<String,RPCService> serviceCache;

    public RPCServiceCache()
    {
        serviceCache = new ConcurrentHashMap<>();
    }

    public RPCService put(String name,RPCService service)
    {
        return serviceCache.put(name,service);
    }

    public RPCService remove(String name)
    {
        return serviceCache.remove(name);
    }

    public RPCService get(String name)
    {
        return serviceCache.get(name);
    }

    public String[] allServiceName()
    {
        return (String[])serviceCache.keySet().toArray();
    }
}
