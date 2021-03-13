package top.lizhistudio.autolua.rpc;

import android.util.LongSparseArray;

public class LocalRPCInterfaceCache {
    private final LongSparseArray<RPCService> rpcServices;
    private long id;
    public LocalRPCInterfaceCache()
    {
        id = 0;
        rpcServices = new LongSparseArray<>();
    }

    public long append(RPCService rpcService)
    {
        synchronized (rpcServices)
        {
            long now  = id++;
            rpcServices.append(now,rpcService);
            return now;
        }
    }

    public void remove(long id)
    {
        synchronized (rpcServices)
        {
            rpcServices.remove(id);
        }
    }

    public RPCService get(long id)
    {
        synchronized (rpcServices)
        {
            return rpcServices.get(id);
        }
    }

    public void clear()
    {
        synchronized (rpcServices)
        {
            rpcServices.clear();
        }
    }
}
