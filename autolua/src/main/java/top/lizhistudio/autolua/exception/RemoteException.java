package top.lizhistudio.autolua.exception;

import java.io.Serializable;

public class RemoteException extends RPCException implements Serializable {
    public RemoteException(Throwable throwable)
    {
        super(throwable);
    }
}
