package top.lizhistudio.autolua.rpc;

import android.util.LongSparseArray;

public class ResponseListenerManager {
    private final LongSparseArray<ResponseListener> listenerSparseArray;
    public ResponseListenerManager()
    {
        listenerSparseArray = new LongSparseArray<>();
    }

    public synchronized void push(long id, ResponseListener listener)
    {
        listenerSparseArray.put(id,listener);
    }

    public synchronized ResponseListener remove(long id)
    {
        ResponseListener listener = listenerSparseArray.get(id);
        if (listener != null)
            listenerSparseArray.remove(id);
        return listener;
    }
}
