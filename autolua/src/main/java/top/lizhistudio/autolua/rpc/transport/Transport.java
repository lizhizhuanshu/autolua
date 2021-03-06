package top.lizhistudio.autolua.rpc.transport;


import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;

public interface Transport {
    void send(Response response);
    void send(Request request);
    Object receive();
    void close();
}