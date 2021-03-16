package top.lizhistudio.autolua.rpc;

import android.util.LongSparseArray;

public class CallbackCache {
    private final LongSparseArray<Callback> listenerSparseArray;
    public CallbackCache()
    {
        listenerSparseArray = new LongSparseArray<>();
    }

    public synchronized void push(long id, Callback listener)
    {
        listenerSparseArray.put(id,listener);
    }

    public synchronized Callback remove(long id)
    {
        Callback listener = listenerSparseArray.get(id);
        if (listener != null)
            listenerSparseArray.remove(id);
        return listener;
    }
}
