package top.lizhistudio.autolua.exception;

public class DisconnectException extends RPCException{
    public DisconnectException(Throwable throwable) {
        super(throwable);
    }
}
