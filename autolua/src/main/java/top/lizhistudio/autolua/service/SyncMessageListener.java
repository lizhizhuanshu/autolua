package top.lizhistudio.autolua.service;

public class SyncMessageListener implements MessageListener {
    private Protocol.Message message;
    @Override
    public synchronized void onReceive(Protocol.Message message) {
        this.message = message;
        this.notifyAll();
    }

    public synchronized Protocol.Message getMessage() throws InterruptedException {
        if (message == null)
            wait();
        return message;
    }
}
