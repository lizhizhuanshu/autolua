package top.lizhistudio.autolua.rpc.transport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import top.lizhistudio.androidlua.exception.LuaError;
import top.lizhistudio.autolua.exception.DisconnectException;
import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;

public class ClientTransport implements Transport{
    private final TSocket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    public ClientTransport(TSocket socket) throws IOException
    {
        this.socket = socket;
        this.outputStream = socket.getOutputStream();
        this.inputStream = socket.getInputStream();
    }

    private void sendObject(Object o)
    {
        synchronized (outputStream)
        {
            try{
                outputStream.writeObject(o);
                outputStream.flush();
            }catch (IOException e)
            {
                throw new DisconnectException(e);
            }
        }
    }

    @Override
    public void send(Response response) {
        sendObject(response);
    }

    @Override
    public void send(Request request) {
        sendObject(request);
    }

    @Override
    public Object receive() {
        synchronized (inputStream)
        {
            try{
                return inputStream.readObject();
            }catch (IOException | ClassNotFoundException e)
            {
                throw new DisconnectException(e);
            }
        }
    }

    @Override
    public void close() {
        synchronized (socket)
        {
            try{
                socket.close();
            }catch (IOException e)
            {

            }
        }
    }
}
