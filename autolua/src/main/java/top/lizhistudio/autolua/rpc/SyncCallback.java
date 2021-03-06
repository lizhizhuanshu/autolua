package top.lizhistudio.autolua.rpc;


import top.lizhistudio.autolua.exception.RemoteException;

public class SyncCallback implements Callback{
    private Object object;
    private boolean hasThrowable = false;

    public synchronized Object getResult() throws InterruptedException {
        if (object == null)
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
        notifyAll();
    }

    @Override
    public synchronized void onError(Throwable throwable) {
        hasThrowable = true;
        onCompleted(throwable);
    }
}
