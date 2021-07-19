package top.lizhistudio.app.core;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import top.lizhistudio.autolua.debugger.proto.DebugMessage;

public class Transport {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private final ByteBuffer sendHeader;
    private final ByteBuffer receiveHeader;

    public Transport(Socket socket) throws IOException
    {
        this.socket = socket;
        this.outputStream = socket.getOutputStream();
        this.inputStream = socket.getInputStream();
        sendHeader = ByteBuffer.allocate(4);
        receiveHeader = ByteBuffer.allocate(4);
        sendHeader.order(ByteOrder.LITTLE_ENDIAN);
        receiveHeader.order(ByteOrder.LITTLE_ENDIAN);
    }


    public void send(DebugMessage.Message message) throws IOException
    {
        synchronized (sendHeader)
        {
            sendHeader.clear();
            sendHeader.putInt(message.getSerializedSize());
            outputStream.write(sendHeader.array());
            message.writeTo(outputStream);
            outputStream.flush();
        }
    }

    private void receive(byte[] out) throws IOException
    {
        int completed = 0;
        int now;
        while (completed<out.length)
        {
            now = inputStream.read(out,completed,out.length-completed);
            if (now <= 0)
            {
                throw new IOException();
            }
            completed += now;
        }
    }

    public DebugMessage.Message receive() throws IOException
    {
        synchronized (receiveHeader)
        {
            receiveHeader.clear();
            receive(receiveHeader.array());
            int size = receiveHeader.getInt();
            byte[] data = new byte[size];
            receive(data);
            return DebugMessage.Message.parseFrom(data);
        }
    }

    public synchronized void close()
    {
        try{
            if (socket != null)
            {
                inputStream.close();
                outputStream.close();
                socket.close();
                socket = null;
                inputStream = null;
                outputStream = null;
            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
