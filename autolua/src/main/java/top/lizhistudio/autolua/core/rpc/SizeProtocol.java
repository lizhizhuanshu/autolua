package top.lizhistudio.autolua.core.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SizeProtocol {
    private static final String TEXT_HEADER = "<-Call->";
    private final ByteBuffer writeBuffer;
    private final ByteBuffer readBuffer;
    public SizeProtocol()
    {
        writeBuffer = ByteBuffer.allocate(4);
        writeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        readBuffer = ByteBuffer.allocate(4);
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }
    public void writeSizeByText(OutputStream os,int size) throws IOException
    {
        os.write(TEXT_HEADER.getBytes());
        os.write(String.valueOf(size).getBytes());
        os.write('\n');
    }

    public void writeSizeByBinary(OutputStream os,int size)throws IOException
    {
        synchronized (writeBuffer)
        {
            writeBuffer.clear();
            writeBuffer.putInt(size);
            os.write(writeBuffer.array());
        }
    }

    public int readSizeByBinary(InputStream is) throws IOException
    {
        synchronized (readBuffer)
        {
            readBuffer.clear();
            Util.receive(is,readBuffer.array(),0,4);
            return readBuffer.getInt();
        }
    }

    public int readSizeByText(String text)
    {
        if (text.startsWith(TEXT_HEADER))
        {
            return Integer.parseInt(text.substring(TEXT_HEADER.length()));
        }
        return -1;
    }

}
