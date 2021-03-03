package top.lizhistudio.autolua.service;

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageManager {
    private final AtomicInteger id ;

    public MessageManager()
    {
        id = new AtomicInteger(1);
    }

    public static Protocol.Message buildExit()
    {
        Protocol.Message.Builder builder = Protocol.Message.newBuilder()
                .setId(0)
                .setMethod(Protocol.Message.TYPE.EXIT);
        return builder.build();
    }

    public static Protocol.Message buildVerify(String password)
    {
        Protocol.Message.Builder builder = Protocol.Message.newBuilder()
                .setId(0);
        if (password!= null)
            builder.addData(0, Protocol.LuaValue.newBuilder().setS(password).build());
        return builder.build();
    }


    private Protocol.Message.Builder getBuilder()
    {
        return Protocol.Message.newBuilder().setId(id.addAndGet(1));
    }

    public Protocol.Message buildExecute(byte[] code, String chunkName)
    {
        Protocol.Message.Builder builder = getBuilder();
        return builder.setMethod(Protocol.Message.TYPE.EXECUTE)
                .addData(Protocol.LuaValue.newBuilder().setB(ByteString.copyFrom(code)).build())
                .addData(Protocol.LuaValue.newBuilder().setS(chunkName))
                .build();
    }

    public Protocol.Message buildExecuteFIle(String path)
    {

        Protocol.Message.Builder builder = getBuilder();
        return builder.setMethod(Protocol.Message.TYPE.EXECUTE_FILE)
                .addData(Protocol.LuaValue.newBuilder().setS(path))
                .build();
    }

    public Protocol.Message buildIsRunning()
    {
        Protocol.Message.Builder builder = getBuilder();
        return builder.setMethod(Protocol.Message.TYPE.IS_RUNNING)
                .build();
    }

    public Protocol.Message buildReset()
    {
        Protocol.Message.Builder builder = getBuilder();
        return builder.setMethod(Protocol.Message.TYPE.RESET)
                .build();
    }

    public Protocol.Message buildInterrupt()
    {
        Protocol.Message.Builder builder = getBuilder();
        return builder.setMethod(Protocol.Message.TYPE.INTERRUPT)
                .build();
    }

}
