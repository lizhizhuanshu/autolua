package top.lizhistudio.autolua.service;

import android.util.SparseArray;

public class MessageListenerManager {
    private final SparseArray<MessageListener> listenerSparseArray;
    public MessageListenerManager()
    {
        listenerSparseArray = new SparseArray<>();
    }

    public synchronized void push(int id, MessageListener listener)
    {
        listenerSparseArray.put(id,listener);
    }

    public synchronized MessageListener remove(int id)
    {
        MessageListener listener = listenerSparseArray.get(id);
        if (listener != null)
            listenerSparseArray.remove(id);
        return listener;
    }
}
