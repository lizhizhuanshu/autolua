package top.lizhistudio.autolua.core;

import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicLong;

public class ObjectCache<T> {
    private final LongSparseArray<T> cache;
    private final AtomicLong id ;
    public ObjectCache()
    {
        cache = new LongSparseArray<>();
        id = new AtomicLong(0);
    }

    public void put(long id,@NonNull T object)
    {
        synchronized (cache)
        {
            cache.put(id,object);
        }
    }

    public long add(@NonNull T object)
    {
        long id;
        while (true)
        {
            id = newID();
            synchronized (cache)
            {
                if (cache.get(id) == null)
                {
                    cache.put(id,object);
                    break;
                }
            }
        }
        return id;
    }

    public T get(long id)
    {
        synchronized (cache)
        {
            return cache.get(id);
        }
    }

    public void remove(long id)
    {
        synchronized (cache)
        {
            cache.remove(id);
        }
    }

    public T removeOut(long id)
    {
        synchronized (cache)
        {
            T object = cache.get(id);
            if (object != null)
            {
                cache.remove(id);
            }
            return object;
        }
    }

    public long remove(@NonNull T object) throws NoSuchObjectException
    {
        synchronized (cache)
        {
            int index= cache.indexOfValue(object);
            if (index<0)
                throw new NoSuchObjectException(object);
            long id = cache.keyAt(index);
            cache.removeAt(index);
            return id;
        }
    }

    public void clear()
    {
        synchronized (cache)
        {
            cache.clear();
        }
    }

    private long newID()
    {
        return id.getAndAdd(1);
    }

    public static final class NoSuchObjectException extends Exception{
        public NoSuchObjectException(Object o)
        {
            super("ObjectCache don't have "+o);
        }
    }
}
