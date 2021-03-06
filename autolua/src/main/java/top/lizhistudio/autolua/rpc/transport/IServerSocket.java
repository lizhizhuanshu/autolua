package top.lizhistudio.autolua.rpc.transport;

import android.net.LocalServerSocket;

import java.io.IOException;
import java.net.ServerSocket;

public interface IServerSocket {
    TSocket accept() throws IOException;
    void close() throws IOException;
    class NetSocketServer implements IServerSocket
    {
        private final ServerSocket socket;
        public NetSocketServer(int port) throws IOException
        {
            socket = new ServerSocket(port);
        }

        @Override
        public TSocket accept() throws IOException {
            return new TSocket.NetSocket(socket.accept());
        }
        @Override
        public void close() throws IOException {
            socket.close();
        }
    }

    class LocalSocketServer implements IServerSocket
    {
        private final LocalServerSocket socket;
        public LocalSocketServer(String feature) throws IOException
        {
            socket = new LocalServerSocket(feature);
        }

        @Override
        public TSocket accept() throws IOException {
            return new TSocket.LocalTSocket(socket.accept());
        }
        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}


