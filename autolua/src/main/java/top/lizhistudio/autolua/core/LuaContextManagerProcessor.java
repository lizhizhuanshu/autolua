package top.lizhistudio.autolua.core;

import com.google.protobuf.ByteString;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaFunctionAdapter;
import top.lizhistudio.androidlua.LuaObjectAdapter;
import top.lizhistudio.androidlua.exception.LuaRuntimeError;
import top.lizhistudio.autolua.core.rpc.CommonRemoteHost;
import top.lizhistudio.autolua.core.rpc.Protocol;
import top.lizhistudio.autolua.core.rpc.RemoteHost;
import top.lizhistudio.autolua.core.rpc.SourceRemoteHost;

public class LuaContextManagerProcessor implements RemoteHost.Handler {
    static final String LUA_CONTEXT_FACTORY_OPTION = "luaContextFactory";
    static final String RESULT_HEADER = "<start result>";
    private final LuaContextFactory luaContextFactory;
    private final ObjectCache<LuaContext> luaContextCache;
    private WeakReference<RemoteHost>  remoteHost;
    private LuaContextManagerProcessor(LuaContextFactory luaContextFactory)
    {
        this.luaContextFactory = luaContextFactory;
        this.luaContextCache = new ObjectCache<>();
    }


    private static LuaContextFactory createLuaContextFactory(String[] args)
    {
        Options options = new Options();
        options.addOption(null,LUA_CONTEXT_FACTORY_OPTION,true,"luaContext factory class name");
        CommandLine commandLine;
        try {
            commandLine = new DefaultParser().parse(options,args);
        }catch (ParseException e)
        {
            e.printStackTrace(System.err);
            return null;
        }
        if (commandLine.hasOption(LUA_CONTEXT_FACTORY_OPTION))
        {
            try{
                Class<?> clazz = Class.forName(commandLine.getOptionValue(LUA_CONTEXT_FACTORY_OPTION));
                Constructor<?> constructor = clazz.getConstructor(String[].class);
                return (LuaContextFactory)constructor.newInstance((Object) args);
            }catch (Throwable e)
            {
                e.printStackTrace(System.err);
            }
            return null;
        }
        return new LuaContextFactoryImplement(args);
    }

    private static void printPrepareResult(boolean r)
    {
        System.out.println(RESULT_HEADER+r);
    }


    public static void main(String[] args)
    {
        LuaContextFactory luaContextFactory = createLuaContextFactory(args);
        printPrepareResult(luaContextFactory!= null);
        if (luaContextFactory != null)
        {
            LuaContextManagerProcessor handler = new LuaContextManagerProcessor(luaContextFactory);
            CommonRemoteHost remoteHost = new SourceRemoteHost();
            remoteHost.setHandler(handler);
            handler.remoteHost = new WeakReference<>(remoteHost);
            remoteHost.serve();
        }
        System.exit(0);
    }





    @Override
    public void onHandle(Protocol.Message request, Protocol.Message.Builder responseBuilder) throws Throwable {
        LuaContext context = luaContextCache.get(request.getContextID());
        switch (request.getMessageCase())
        {
            case CALLLUAFUNCTIONADAPTER:
            case CALLLUAOBJECTADAPTER:
            case RELEASELUAFUNCTIONADAPTER:
            case RELEASELUAOBJECTADAPTER:
            case ERROR:
            case MESSAGE_NOT_SET:
                return;
            case TOPOINTER:
                onToPointer(context,request.getToPointer(),responseBuilder);
                break;
            case TOLONG:
                onToLong(context,request.getToLong(),responseBuilder);
                break;
            case TODOUBLE:
                onToDouble(context,request.getToDouble(),responseBuilder);
                break;
            case TOSTRING:
                onToString(context,request.getToString(),responseBuilder);
                break;
            case TOBOOLEAN:
                onToBoolean(context,request.getToBoolean(),responseBuilder);
                break;
            case PUSHBASEVALUE:
                onPushBaseValue(context,request.getPushBaseValue());
                break;
            case PUSHLUAFUNCTIONADAPTER:
                onPushLuaFunctionAdapter(context,
                        request.getContextID(),
                        request.getPushLuaFunctionAdapter());
                break;
            case PUSHLUAOBJECTADAPTER:
                onPushLuaObjectAdapter(context,
                        request.getContextID(),
                        request.getPushLuaObjectAdapter());
                break;
            case GETTABLE:
                onGetTable(context,request.getGetTable(),responseBuilder);
                break;
            case SETTABLE:
                onSetTable(context,request.getSetTable());
                break;
            case GETGLOBAL:
                onGetGlobal(context,request.getGetGlobal(),responseBuilder);
                break;
            case SETGLOBAL:
                onSetGlobal(context,request.getSetGlobal());
                break;
            case RAWGET:
                onRawGet(context,request.getRawGet(),responseBuilder);
                break;
            case RAWSET:
                onRawSet(context,request.getRawSet());
                break;
            case GETTOP:
                onGetTop(context,responseBuilder);
                break;
            case SETTOP:
                onSetTop(context,request.getSetTop());
                break;
            case POP:
                onPop(context,request.getPop());
                break;
            case GETTYPE:
                onGetType(context,request.getGetType(),responseBuilder);
                break;
            case ISINTEGER:
                onIsInteger(context,request.getIsInteger(),responseBuilder);
                break;
            case ISLUAOBJECTADAPTER:
                onIsLuaObjectAdapter(context,request.getIsLuaObjectAdapter(),responseBuilder);
                break;
            case LOADFILE:
                onLoadFile(context,request.getLoadFile());
                break;
            case LOADBUFFER:
                onLoadBuffer(context,request.getLoadBuffer());
                break;
            case PCALL:
                onPCall(context,request.getPCall());
                break;
            case DESTROY:
                onDestroy(request);
                break;
            case CREATE:
                onCreate(responseBuilder);
                return;
        }
        responseBuilder.setContextID(request.getContextID());
    }

    private void onToPointer(LuaContext context,
                             Protocol.ToPointer request,
                             Protocol.Message.Builder response)
    {
        long result = context.toPointer(request.getIndex());
        response.setToPointer(Protocol.ToPointer.newBuilder().setResult(result));
    }

    private void onToLong(LuaContext context,
                          Protocol.ToLong request,
                          Protocol.Message.Builder response)
    {
        long result = context.toLong(request.getIndex());
        response.setToLong(Protocol.ToLong.newBuilder().setResult(result));
    }

    private void onToDouble(LuaContext context,
                            Protocol.ToDouble request,
                            Protocol.Message.Builder response)
    {
        double result = context.toDouble(request.getIndex());
        response.setToDouble(Protocol.ToDouble.newBuilder().setResult(result));
    }

    private void onToString(LuaContext context,
                            Protocol.ToString request,
                            Protocol.Message.Builder response)
    {
        byte[] result = context.toBytes(request.getIndex());
        response.setToString(Protocol.ToString.newBuilder().setResult(ByteString.copyFrom(result)));
    }

    private void onToBoolean(LuaContext context,
                             Protocol.ToBoolean request,
                             Protocol.Message.Builder response)
    {
        boolean result = context.toBoolean(request.getIndex());
        response.setToBoolean(Protocol.ToBoolean.newBuilder().setResult(result));
    }

    private void onPushBaseValue(LuaContext context,
                                 Protocol.PushBaseValue request)
    {
        switch (request.getVCase())
        {

            case B:
                context.push(request.getB().toByteArray());
                break;
            case L:
                context.push(request.getL());
                break;
            case D:
                context.push(request.getD());
                break;
            case Z:
                context.push(request.getZ());
                break;
            case V_NOT_SET:
                context.pushNil();
                break;
        }
    }

    private void onPushLuaFunctionAdapter(LuaContext context,long contextID,
                                          Protocol.PushLuaFunctionAdapter request)
    {
        long id = request.getId();
        LuaFunctionAdapter luaFunctionAdapter =
                new LuaFunctionAdapterProxy(contextID,id);
        context.push(luaFunctionAdapter);
    }

    private void onPushLuaObjectAdapter(LuaContext context, long contextID,
                                        Protocol.PushLuaObjectAdapter request)
    {
        long id = request.getId();
        List<String> methodNames = request.getMethodNameList();
        LuaObjectAdapterProxy luaObjectAdapterProxy =
                new LuaObjectAdapterProxy(contextID,id,methodNames);
        context.push(luaObjectAdapterProxy);
    }


    private class LuaFunctionAdapterProxy implements LuaFunctionAdapter{
        private final long contextID;
        private final long id;
        private LuaFunctionAdapterProxy(long contextID, long id)
        {
            this.contextID = contextID;
            this.id = id;
        }
        @Override
        public int onExecute(LuaContext luaContext) throws Throwable {
            Protocol.CallLuaFunctionAdapter.Builder request = Protocol.CallLuaFunctionAdapter.newBuilder()
                    .setId(id);
            Protocol.Message.Builder builder = Protocol.Message.newBuilder()
                    .setContextID(contextID)
                    .setCallLuaFunctionAdapter(request);
            return remoteHost.get()
                    .callAndCheckException(builder)
                    .getCallLuaFunctionAdapter()
                    .getResult();
        }

        @Override
        protected void finalize() throws Throwable {
            Protocol.ReleaseLuaFunctionAdapter.Builder request = Protocol.ReleaseLuaFunctionAdapter.newBuilder()
                    .setId(id);
            remoteHost.get().call(Protocol.Message.newBuilder().setReleaseLuaFunctionAdapter(request));
            super.finalize();
        }
    }

    private class LuaObjectAdapterProxy implements LuaObjectAdapter{
        private final long contextID;
        private final long id;
        private final HashSet<String> methods;
        private LuaObjectAdapterProxy(long contextID,long id,List<String> methods)
        {
            this.contextID = contextID;
            this.id = id;
            this.methods = new HashSet<>(methods);
        }
        @Override
        public boolean hasMethod(String name) {
            return methods.contains(name);
        }

        @Override
        public List<String> getAllMethodName() {
            return new ArrayList<String>(methods);
        }

        @Override
        public int call(String methodName, LuaContext luaContext) throws Throwable {
            Protocol.CallLuaObjectAdapter.Builder builder = Protocol.CallLuaObjectAdapter.newBuilder()
                    .setId(id)
                    .setMethodName(methodName);
            Protocol.Message.Builder request = Protocol.Message.newBuilder()
                    .setContextID(contextID)
                    .setCallLuaObjectAdapter(builder);
            return remoteHost.get().callAndCheckException(request)
                    .getCallLuaObjectAdapter()
                    .getResult();
        }

        @Override
        public Object getJavaObject() {
            throw new LuaRuntimeError("proxy object can't get java object");
        }

        @Override
        protected void finalize() throws Throwable {
            Protocol.ReleaseLuaObjectAdapter.Builder request = Protocol.ReleaseLuaObjectAdapter.newBuilder()
                    .setId(id);
            Protocol.Message.Builder builder = Protocol.Message.newBuilder()
                    .setReleaseLuaObjectAdapter(request);
            remoteHost.get().call(builder);
            super.finalize();
        }
    }


    private void onGetTable(LuaContext context,
                            Protocol.GetTable request,
                            Protocol.Message.Builder response)
    {
        int result = context.getTable(request.getTableIndex());
        response.setGetTable(Protocol.GetTable.newBuilder().setResult(result));
    }

    private void onSetTable(LuaContext context,
                            Protocol.SetTable request)
    {
        context.setTable(request.getTableIndex());
    }

    private void onGetGlobal(LuaContext context,
                             Protocol.GetGlobal request,
                             Protocol.Message.Builder response)
    {
        int result = context.getGlobal(request.getKey());
        response.setGetGlobal(Protocol.GetGlobal.newBuilder().setResult(result));
    }

    private void onSetGlobal(LuaContext context,
                             Protocol.SetGlobal request)
    {
        context.setGlobal(request.getKey());
    }

    private void onRawGet(LuaContext context,
                          Protocol.RawGet request,
                          Protocol.Message.Builder response)
    {
        int result = context.rawGet(request.getTableIndex());
        response.setRawGet(Protocol.RawGet.newBuilder().setResult(result));
    }

    private void onRawSet(LuaContext context,
                          Protocol.RawSet request)
    {
        context.rawSet(request.getTableIndex());
    }

    private void onGetTop(LuaContext context,
                          Protocol.Message.Builder response)
    {
        int result = context.getTop();
        response.setGetTop(Protocol.GetTop.newBuilder().setResult(result));
    }

    private void onSetTop(LuaContext context,
                          Protocol.SetTop request)
    {
        context.setTop(request.getN());
    }

    private void onPop(LuaContext context,
                       Protocol.Pop request)
    {
        context.pop(request.getN());
    }

    private void onGetType(LuaContext context,
                           Protocol.GetType request,
                           Protocol.Message.Builder response)
    {
        int result = context.type(request.getIndex());
        response.setGetType(Protocol.GetType.newBuilder().setResult(result));
    }

    private void onIsInteger(LuaContext context,
                             Protocol.IsInteger request,
                             Protocol.Message.Builder response)
    {
        boolean result = context.isInteger(request.getIndex());
        response.setIsInteger(Protocol.IsInteger.newBuilder().setResult(result));
    }

    private void onIsLuaObjectAdapter(LuaContext context,
                                      Protocol.IsLuaObjectAdapter request,
                                      Protocol.Message.Builder response)
    {
        boolean result = context.isLuaObjectAdapter(request.getIndex());
        response.setIsLuaObjectAdapter(Protocol.IsLuaObjectAdapter.newBuilder().setResult(result));
    }

    private void onLoadBuffer(LuaContext context,
                              Protocol.LoadBuffer request)
    {
        context.loadBuffer(request.getCode().toByteArray(),
                request.getChunkName(),
                LuaContext.CODE_TYPE.valueOf(request.getCodeType().name()));
    }

    private void onLoadFile(LuaContext context,
                            Protocol.LoadFile request)
    {
        context.loadFile(request.getPath(),
                LuaContext.CODE_TYPE.valueOf(request.getCodeType().name()));
    }

    private void onPCall(LuaContext context,
                         Protocol.PCall request)
    {
        context.pCall(request.getArgNumber(),request.getResultNumber(),request.getErrorFunctionIndex());
    }

    private void onCreate(Protocol.Message.Builder response)
    {
        LuaContext context = luaContextFactory.newLuaContext();
        long id = luaContextCache.add(context);
        response.setContextID(id);
    }

    private void onDestroy(Protocol.Message request)
    {
        long contextID = request.getContextID();
        LuaContext context = luaContextCache.get(contextID);
        if (context != null)
        {
            luaContextCache.remove(contextID);
            context.destroy();
        }
    }
}
