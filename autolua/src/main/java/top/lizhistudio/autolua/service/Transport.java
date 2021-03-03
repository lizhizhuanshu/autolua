package top.lizhistudio.autolua.service;

public interface Transport {
    void send(Protocol.Message message);
    Protocol.Message receive();
    void close();
}
