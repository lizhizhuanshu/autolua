package top.lizhistudio.autolua.rpc;

public interface Callback {
    void onCompleted(Object result);
    void onError(Throwable throwable);
}
