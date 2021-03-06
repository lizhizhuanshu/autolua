package top.lizhistudio.autolua.rpc;

import top.lizhistudio.autolua.service.Protocol;

public class SyncResponseListener implements ResponseListener{
    private Response message;
    @Override
    public synchronized void onReceived(Response message) {
        this.message = message;
        this.notifyAll();
    }


    public synchronized Response getResponse() throws InterruptedException {
        if (message == null)
            wait();
        return message;
    }
}
