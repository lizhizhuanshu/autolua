package top.lizhistudio.autolua.service;

public interface MessageListener {
    void onReceive(Protocol.Message message);
}
