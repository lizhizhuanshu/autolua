package top.lizhistudio.autolua.rpc;

import java.io.Serializable;

public class RemoteException extends RuntimeException implements Serializable {
    public RemoteException(Throwable throwable)
    {
        super(throwable);
    }
}
