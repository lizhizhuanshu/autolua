package top.lizhistudio.autolua.core.rpc;

public interface RemoteHost {
    Protocol.Message call(Protocol.Message.Builder requestBuilder);
    Protocol.Message callAndCheckException(Protocol.Message.Builder requestBuilder);
    void call(Protocol.Message.Builder requestBuilder,Callback callback);
    void serve();
    void interrupt();
    void releaseAllCallback(Throwable throwable);

    interface Callback{
        void onCallback(Protocol.Message message);
        void onError(Throwable ioException);
    }

    interface Handler{
        void onHandle(Protocol.Message request, Protocol.Message.Builder responseBuilder)
                throws Throwable;
    }
}
