package top.lizhistudio.autolua.service;


import android.util.Log;

import com.google.protobuf.ByteString;

import top.lizhistudio.autolua.LuaInterpreter;

public class ServiceHandler implements Handler {
    private static final String TAG = "ServiceHandler";
    private final Transport transport;
    private final LuaInterpreter interpreter;

    public ServiceHandler(Transport transport,LuaInterpreter luaInterpreter)
    {
        this.transport = transport;
        this.interpreter = luaInterpreter;
    }

    private static Protocol.LuaValue JavaToLuaValue(Object o)
    {
        if (o instanceof Number)
        {
            Number n = (Number) o;
            if (o instanceof Double || o instanceof Float)
                return Protocol.LuaValue.newBuilder().setD(n.doubleValue()).build();
            else
                return Protocol.LuaValue.newBuilder().setL(n.longValue()).build();
        }else if(o instanceof Boolean)
            return Protocol.LuaValue.newBuilder().setZ((Boolean)o).build();
        else if(o instanceof String)
            return Protocol.LuaValue.newBuilder().setS((String)o).build();
        else if(o instanceof byte[])
            return Protocol.LuaValue.newBuilder().setB(ByteString.copyFrom((byte[])o)).build();
        return Protocol.LuaValue.newBuilder().build();
    }

    private static void setResult(Protocol.Message.Builder builder, Object[] result)
    {
        for (Object o:result)
        {
            builder.addData(JavaToLuaValue(o));
        }
    }

    private void sendThrowable(int id, Throwable throwable)
    {
        Protocol.Message.Builder builder = Protocol.Message.newBuilder().setId(id);
        Protocol.Message message = builder.addData(Protocol.LuaValue.newBuilder().setS(throwable.getMessage()).build())
                .setMethod(Protocol.Message.TYPE.ERROR)
                .build();
        Log.e(TAG,"server error",throwable);
        transport.send(message);
    }


    private LuaInterpreter.Callback newCallback(int id)
    {
        return new LuaInterpreter.Callback() {
            @Override
            public void onCompleted(Object[] result) {
                Protocol.Message.Builder builder = Protocol.Message.newBuilder()
                        .setId(id);
                setResult(builder,result);
                Protocol.Message message = builder.build();
                transport.send(message);
            }
            @Override
            public void onError(Throwable throwable) {
                sendThrowable(id,throwable);
            }
        };
    }


    private void onExecute(Protocol.Message message)
    {
        byte[] code = message.getData(0).getB().toByteArray();
        String chunkName = message.getData(1).getS();
        interpreter.execute(code, chunkName,newCallback(message.getId()));
    }

    private void onExecuteFile(Protocol.Message message)
    {
        String path = message.getData(0).getS();
        interpreter.executeFile(path,newCallback(message.getId()));
    }

    private void onIsRunning(Protocol.Message message)
    {
        int id = message.getId();
        boolean result = interpreter.isRunning();
        Protocol.Message.Builder builder = Protocol.Message.newBuilder()
                .setId(id)
                .addData(Protocol.LuaValue.newBuilder().setZ(result).build());
        transport.send(builder.build());
    }

    private void onVoidRespond(int id)
    {
        Protocol.Message message;
        message = Protocol.Message.newBuilder()
                .setId(id)
                .build();
        transport.send(message);
    }

    private void onReset(Protocol.Message message)
    {
        try{
            interpreter.reset();
        }catch (Throwable e)
        {
            sendThrowable(message.getId(),e);
            return;
        }
        onVoidRespond(message.getId());
    }

    public void onReceive(Protocol.Message message)
    {
        switch (message.getMethod())
        {
            case EXECUTE:
                onExecute(message);
                break;
            case EXECUTE_FILE:
                onExecuteFile(message);
                break;
            case IS_RUNNING:
                onIsRunning(message);
                break;
            case INTERRUPT:
                interpreter.interrupt();
                onVoidRespond(message.getId());
                break;
            case RESET:
                onReset(message);
                break;
            default:
                throw new RuntimeException("unknown command");
        }
    }

}
