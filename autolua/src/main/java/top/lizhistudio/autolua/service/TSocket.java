package top.lizhistudio.autolua.service;

import android.net.LocalSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public interface TSocket {
    InputStream getInputStream() throws IOException;
    OutputStream getOutputStream() throws IOException;
    void setSoTimeout(int time) throws IOException;
    void close() throws IOException;

    class NetSocket implements TSocket
    {
        private final Socket socket;
        public NetSocket(Socket socket)
        {
            this.socket = socket;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return socket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }

        public void setSoTimeout(int time) throws IOException
        {
            socket.setSoTimeout(time);
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }

    class LocalTSocket implements TSocket
    {
        private final LocalSocket socket;
        public LocalTSocket(LocalSocket socket)
        {
            this.socket = socket;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return socket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }

        public void setSoTimeout(int time) throws IOException
        {
            socket.setSoTimeout(time);
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
