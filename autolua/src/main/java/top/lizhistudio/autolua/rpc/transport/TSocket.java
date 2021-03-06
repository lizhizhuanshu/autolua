package top.lizhistudio.autolua.rpc.transport;

import android.net.LocalSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public interface TSocket {
    ObjectInputStream getInputStream() throws IOException;
    ObjectOutputStream getOutputStream() throws IOException;
    void setSoTimeout(int time) throws IOException;
    void close() throws IOException;

    class NetSocket implements TSocket
    {
        private final Socket socket;
        private final ObjectInputStream inputStream;
        private final ObjectOutputStream outputStream;
        private final AtomicBoolean closed ;
        public NetSocket(Socket socket) throws IOException
        {
            this.socket = socket;
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            closed = new AtomicBoolean(false);
        }

        @Override
        public ObjectInputStream getInputStream(){
            return inputStream;
        }

        @Override
        public ObjectOutputStream getOutputStream(){
            return outputStream;
        }

        public void setSoTimeout(int time) throws IOException
        {
            socket.setSoTimeout(time);
        }

        @Override
        public void close() throws IOException {
            if (closed.compareAndSet(false,true))
            {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            }
        }
    }

    class LocalTSocket implements TSocket
    {
        private final LocalSocket socket;
        private final ObjectInputStream inputStream;
        private final ObjectOutputStream outputStream;
        private final AtomicBoolean closed ;
        public LocalTSocket(LocalSocket socket) throws IOException
        {
            this.socket = socket;
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            closed = new AtomicBoolean(false);
        }

        @Override
        public ObjectInputStream getInputStream(){
            return inputStream;
        }

        @Override
        public ObjectOutputStream getOutputStream(){
            return outputStream;
        }

        public void setSoTimeout(int time) throws IOException
        {
            socket.setSoTimeout(time);
        }

        @Override
        public void close() throws IOException {
            if (closed.compareAndSet(false,true))
            {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            }
        }
    }
}
