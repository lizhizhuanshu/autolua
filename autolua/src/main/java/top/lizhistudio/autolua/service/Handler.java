package top.lizhistudio.autolua.service;

public interface Handler {
    void onReceive(Protocol.Message message);
}
