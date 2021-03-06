package top.lizhistudio.autolua.exception;

import java.io.Serializable;

public class RPCException extends RuntimeException{
    public RPCException(Throwable throwable)
    {
        super(throwable);
    }

}
