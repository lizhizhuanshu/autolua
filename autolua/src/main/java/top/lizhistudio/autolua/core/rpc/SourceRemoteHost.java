package top.lizhistudio.autolua.core.rpc;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SourceRemoteHost extends CommonRemoteHost{
    private final SizeProtocol sizeProtocol;
    public SourceRemoteHost() {
        super(System.in, System.out);
        sizeProtocol = new SizeProtocol();
    }

    @Override
    protected void onSendMessageSize(int size, OutputStream os) throws IOException {
        sizeProtocol.writeSizeByText(os,size);
    }

    @Override
    protected int onReceiveMessageSize(InputStream inputStream) throws IOException {
        return sizeProtocol.readSizeByBinary(inputStream);
    }
}
