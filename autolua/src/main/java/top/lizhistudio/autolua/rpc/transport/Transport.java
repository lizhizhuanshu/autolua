package top.lizhistudio.autolua.rpc;

public interface Transport {
    void send(Response response);
    void send(Request request);
    void send(RegisterNotice registerNotice);
    Object receive();
}