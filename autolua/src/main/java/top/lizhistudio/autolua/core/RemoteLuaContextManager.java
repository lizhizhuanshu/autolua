package top.lizhistudio.autolua.core;

import android.content.Context;
import android.util.Log;
import android.view.contentcapture.DataRemovalRequest;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Scanner;


import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaFunctionAdapter;
import top.lizhistudio.androidlua.LuaObjectAdapter;
import top.lizhistudio.androidlua.exception.LuaError;
import top.lizhistudio.androidlua.exception.LuaRuntimeError;
import top.lizhistudio.androidlua.exception.LuaTypeError;
import top.lizhistudio.autolua.core.rpc.CommonRemoteHost;
import top.lizhistudio.autolua.core.rpc.Protocol;
import top.lizhistudio.autolua.core.rpc.RemoteHost;
import top.lizhistudio.autolua.core.rpc.SizeProtocol;
import top.lizhistudio.autolua.core.rpc.Util;

public class RemoteLuaContextManager implements RemoteHost.Handler ,LuaContextManager{
    private final String commandLine;
    private PrintListener outputPrintListener = null;
    private PrintListener errorPrintListener = null;
    private CommonRemoteHost remoteHost = null;
    private Process process = null;
    private Thread errorPrintThread = null;
    private STATE state = STATE.PREPARE;
    private final ObjectCache<LuaFunctionAdapter> luaFunctionAdapterCache;
    private final ObjectCache<LuaObjectAdapter> luaObjectAdapterCache;
    private final ObjectCache<WeakReference<LuaContext>> luaContextCache;
    private final ArrayList<LuaFunctionAdapter> initializeMethods;
    private RemoteLuaContextManager(String commandLine)
    {
        this.commandLine = commandLine;
        luaFunctionAdapterCache = new ObjectCache<>();
        luaObjectAdapterCache = new ObjectCache<>();
        luaContextCache = new ObjectCache<>();
        initializeMethods = new ArrayList<>();
    }


    private void pushLuaFunctionAdapter(long contextID,
                                       LuaFunctionAdapter luaFunctionAdapter)
    {
        long id = luaFunctionAdapterCache.add(luaFunctionAdapter);
        Protocol.PushLuaFunctionAdapter.Builder builder = Protocol.PushLuaFunctionAdapter.newBuilder()
                .setId(id);
        Protocol.Message.Builder messageBuilder = Protocol.Message.newBuilder()
                .setContextID(contextID)
                .setPushLuaFunctionAdapter(builder);
        try{
            callAndCheckException(messageBuilder);
        }catch (LuaError e)
        {
            luaFunctionAdapterCache.remove(id);
            throw  e;
        }
    }

    private void pushLuaObjectAdapter(long contextID,
                                     LuaObjectAdapter luaObjectAdapter)
    {
        long id = luaObjectAdapterCache.add(luaObjectAdapter);
        Protocol.PushLuaObjectAdapter.Builder builder = Protocol.PushLuaObjectAdapter.newBuilder()
                .addAllMethodName(luaObjectAdapter.getAllMethodName())
                .setId(id);
        Protocol.Message.Builder messageBuilder = Protocol.Message.newBuilder()
                .setContextID(contextID)
                .setPushLuaObjectAdapter(builder);
        try {
            callAndCheckException(messageBuilder);
        }catch (LuaError e)
        {
            luaObjectAdapterCache.remove(id);
            throw  e;
        }
    }

    private void destroyContext(long id)
    {
        luaContextCache.remove(id);
        Protocol.Destroy.Builder request = Protocol.Destroy.newBuilder();
        callAndCheckException(Protocol.Message.newBuilder().setContextID(id).setDestroy(request));
    }

    private void onInitializeLuaContext(LuaContext context)
    {
        int top = context.getTop();
        try{
            synchronized (initializeMethods)
            {
                for (LuaFunctionAdapter method:initializeMethods)
                {
                    method.onExecute(context);
                }
            }
        }catch (LuaError e)
        {
            throw e;
        }catch (Throwable e)
        {
            throw new LuaRuntimeError(e);
        }finally {
            context.setTop(top);
        }
    }

    @Override
    public LuaContext newLuaContext() {
        Protocol.Create.Builder request = Protocol.Create.newBuilder();
        Protocol.Message result = callAndCheckException(
                Protocol.Message.newBuilder().setCreate(request));
        long contextID = result.getContextID();
        LuaContextProxy luaContextProxy = new LuaContextProxy(contextID);
        luaContextCache.put(contextID,new WeakReference<>(luaContextProxy));
        onInitializeLuaContext(luaContextProxy);
        return luaContextProxy;
    }

    private void onCallLuaFunctionAdapter(long contextID,
                                          Protocol.CallLuaFunctionAdapter request,
                                          Protocol.Message.Builder response) throws Throwable {
        LuaContext context = luaContextCache.get(contextID).get();
        LuaFunctionAdapter functionAdapter = luaFunctionAdapterCache.get(request.getId());
        int result = functionAdapter.onExecute(context);
        response.setCallLuaFunctionAdapter(
                Protocol.CallLuaFunctionAdapter.newBuilder().setResult(result));
    }

    private void onCallLuaObjectAdapter(long contextID,
                                        Protocol.CallLuaObjectAdapter request,
                                        Protocol.Message.Builder response) throws Throwable {
        LuaContext context = luaContextCache.get(contextID).get();
        LuaObjectAdapter luaObjectAdapter = luaObjectAdapterCache.get(request.getId());
        String methodName = request.getMethodName();
        int result = luaObjectAdapter.call(methodName,context);
        response.setCallLuaObjectAdapter(
                Protocol.CallLuaObjectAdapter.newBuilder().setResult(result));
    }


    @Override
    public void onHandle(Protocol.Message request, Protocol.Message.Builder responseBuilder)
            throws Throwable{
        switch (request.getMessageCase()){
            case ERROR:
            case TOPOINTER:
            case TOLONG:
            case TODOUBLE:
            case TOSTRING:
            case TOBOOLEAN:
            case PUSHBASEVALUE:
            case PUSHLUAFUNCTIONADAPTER:
            case PUSHLUAOBJECTADAPTER:
            case GETTABLE:
            case SETTABLE:
            case GETGLOBAL:
            case SETGLOBAL:
            case RAWGET:
            case RAWSET:
            case GETTOP:
            case SETTOP:
            case POP:
            case GETTYPE:
            case ISINTEGER:
            case ISLUAOBJECTADAPTER:
            case LOADFILE:
            case LOADBUFFER:
            case PCALL:
            case DESTROY:
            case CREATE:
            case CREATETABLE:
            case INTERRUPT:
            case MESSAGE_NOT_SET:
                break;
            case CALLLUAFUNCTIONADAPTER:
                onCallLuaFunctionAdapter(request.getContextID(),
                        request.getCallLuaFunctionAdapter(),
                        responseBuilder);
                break;
            case CALLLUAOBJECTADAPTER:
                onCallLuaObjectAdapter(request.getContextID(),
                        request.getCallLuaObjectAdapter(),
                        responseBuilder);
                break;
            case RELEASELUAFUNCTIONADAPTER:
                LuaFunctionAdapter functionAdapter =
                        luaFunctionAdapterCache.removeOut(request.getReleaseLuaFunctionAdapter().getId());
                if (functionAdapter != null)
                    functionAdapter.onRelease();
                break;
            case RELEASELUAOBJECTADAPTER:
                LuaObjectAdapter objectAdapter =
                        luaObjectAdapterCache.removeOut(request.getReleaseLuaObjectAdapter().getId());
                if (objectAdapter!=null)
                    objectAdapter.onRelease();
                break;

        }
    }

    @Override
    public boolean addInitializeMethod(LuaFunctionAdapter method) {
        synchronized (initializeMethods)
        {
            return initializeMethods.add(method);
        }
    }

    @Override
    public boolean removeInitializeMethod(LuaFunctionAdapter method) {
        synchronized (initializeMethods)
        {
            return initializeMethods.remove(method);
        }
    }

    private enum STATE{
        PREPARE,
        STARTED
    }


    private void checkProcessorPrepare(InputStream is) throws IOException
    {
        while (true)
        {
            String str = Util.readLine(is);
            if (str.startsWith(LuaContextManagerProcessor.RESULT_HEADER))
            {
                str = str.substring(LuaContextManagerProcessor.RESULT_HEADER.length());
                if (Boolean.parseBoolean(str))
                {
                    break;
                }
                throw new LuaRuntimeError(str);
            }else if(outputPrintListener != null)
            {
                outputPrintListener.onPrint(str);
            }
        }
    }

    private void onStartProcess()
    {
        try{
            process = Runtime.getRuntime().exec("su");
            OutputStream os = process.getOutputStream();
            os.write(commandLine.getBytes());
            os.flush();
            InputStream is = process.getInputStream();
            checkProcessorPrepare(is);
            if (outputPrintListener == null)
                outputPrintListener = PrintListener.EMPTY;
            remoteHost = new ProxyRemoteHost(process,outputPrintListener);
            remoteHost.setHandler(this);
            new Thread() {
                @Override
                public void run() {
                    remoteHost.serve();
                }
            }.start();
            if (errorPrintListener != null)
                startListenErrorPrint();
        }catch (Exception e)
        {
            throw  new LuaRuntimeError(e);
        }
    }

    private void startListenErrorPrint()
    {
        errorPrintThread = new Thread()
        {
            @Override
            public void run() {
                Scanner scanner = new Scanner(process.getErrorStream());
                while (!Thread.interrupted() &&
                        scanner.hasNextLine())
                {
                    errorPrintListener.onPrint(scanner.nextLine());
                }
            }
        };
        errorPrintThread.start();
    }

    private void checkStart()
    {
        synchronized (this)
        {
            if (state == STATE.STARTED)
                return;
            onStartProcess();
            state = STATE.STARTED;
        }
    }


    private Protocol.Message callAndCheckException(Protocol.Message.Builder requestBuilder)
    {
        checkStart();
        return remoteHost.callAndCheckException(requestBuilder);
    }

    private Protocol.Message call(Protocol.Message.Builder request)
    {
        checkStart();
        return remoteHost.call(request);
    }

    public void destroy()
    {
        synchronized (this)
        {
            if (state == STATE.STARTED)
            {
                if (errorPrintThread !=null){
                    errorPrintThread.interrupt();
                    errorPrintThread = null;
                }
                remoteHost.interrupt();
                remoteHost.releaseAllCallback(new LuaRuntimeError("interrupt"));
                remoteHost = null;
                process.destroy();
                process = null;
                luaFunctionAdapterCache.clear();
                luaObjectAdapterCache.clear();
                luaContextCache.clear();
                state = STATE.PREPARE;
            }
        }
    }



    private static class ProxyRemoteHost extends CommonRemoteHost {
        private final PrintListener printListener;
        private final SizeProtocol sizeProtocol = new SizeProtocol();
        public ProxyRemoteHost(Process process,
                               @NonNull PrintListener printListener) {
            super(process.getInputStream(), process.getOutputStream());
            this.printListener = printListener;
        }

        @Override
        protected void onSendMessageSize(int size, OutputStream os) throws IOException {
            sizeProtocol.writeSizeByBinary(os,size);
        }

        @Override
        protected int onReceiveMessageSize(InputStream inputStream) throws IOException {
            while (true)
            {
                String str = Util.readLine(inputStream);
                int size = sizeProtocol.readSizeByText(str);
                if (size > -1)
                    return size;
                printListener.onPrint(str);
            }
        }
    }




    public interface PrintListener{
        void onPrint(String message);
        PrintListener EMPTY  = new PrintListener() {
            @Override
            public void onPrint(String message) {

            }
        };
    }

    public static class  Builder
    {
        private String packagePath = null;
        private String scriptPath = null;
        private String processName = null;
        private boolean isUse64Bit = false;
        private final StringBuilder otherArgsBuilder;
        private PrintListener outputPrintListener = null;
        private PrintListener errorPrintListener = null;
        private final ArrayList<Class<? extends LuaFunctionAdapter>> classes;
        public Builder()
        {
            otherArgsBuilder = new StringBuilder();
            classes = new ArrayList<>();
        }

        public Builder outputPrintListener(PrintListener printListener)
        {
            this.outputPrintListener = printListener;
            return this;
        }

        public Builder errorPrintListener(PrintListener printListener)
        {
            this.errorPrintListener = printListener;
            return this;
        }

        public Builder addLuaContextInitializeMethod(Class<? extends LuaFunctionAdapter> clazz)
        {
            synchronized (classes)
            {
                classes.add(clazz);
                return this;
            }
        }

        public Builder scriptLoadPath(String path)
        {
            scriptPath = path;
            return this;
        }

        public Builder processName(String name)
        {
            processName = name;
            return this;
        }

        public Builder use64Bit()
        {
            isUse64Bit = true;
            return this;
        }


        public Builder append(String arg)
        {
            append(otherArgsBuilder,arg);
            return this;
        }

        public Builder appendArg(String key, String value)
        {
            appendArg(otherArgsBuilder,key,value);
            return this;
        }

        private void append(StringBuilder builder,String arg)
        {
            builder.append("  ").append(arg);
        }

        private void appendArg(StringBuilder builder, String k,String v)
        {
            append(builder,k);
            append(builder,v);
        }

        private String getLibraryPath()
        {
            return packagePath.substring(0,packagePath.lastIndexOf('/')+1)+"lib/";
        }

        private String buildCommandLine()
        {
            StringBuilder command = new StringBuilder();
            String libPathHead = getLibraryPath();
            File file = new File(libPathHead);
            String libPath = null;
            if(isUse64Bit)
            {
                for(String s:file.list()) {
                    if (s.indexOf("64") > 0) {
                        libPath = libPathHead + s;
                        break;
                    }
                }
            }else
                libPath = libPathHead + file.list()[0];

            if(scriptPath != null)
            {
                command.append("export LUA_PATH=\"")
                        .append(scriptPath)
                        .append("\"\n");
            }

            command.append("export LUA_CPATH=\"")
                    .append( libPath)
                    .append("/lib?.so;")
                    .append(libPath)
                    .append("/?/init.so")
                    .append("\"\n");

            command.append("export LD_LIBRARY_PATH=\"")
                    .append(libPath)
                    .append(":" )
                    .append(System.getProperty("java.library.path"))
                    .append("\"\n");

            command.append("export CLASSPATH=")
                    .append(packagePath)
                    .append("\n");

            if(isUse64Bit ||(libPath != null && libPath.contains("64")) )
                command.append("/system/bin/app_process64 ");
            else
                command.append("/system/bin/app_process32 ");

            command.append("/system/bin ");

            if(processName != null)
            {
                command.append("--nice-name=");
                command.append(processName);
            }


            append(command, LuaContextManagerProcessor.class.getName());
            if (!classes.isEmpty())
            {
                append(command,LuaContextManagerProcessor.buildInitializeMethodOption(classes));
            }

            append(command,otherArgsBuilder.toString());
            command.append('\n');
            return command.toString();
        }


        public RemoteLuaContextManager build(Context context)
        {
            packagePath = context.getPackageCodePath();
            String command = buildCommandLine();
            Log.d("RemoteLuaContextManager",command);
            RemoteLuaContextManager result = new RemoteLuaContextManager(command);
            result.outputPrintListener = outputPrintListener;
            result.errorPrintListener = errorPrintListener;
            return result;
        }
    }

    private class LuaContextProxy implements LuaContext{
        private final long id;
        private LuaContextProxy(long id)
        {
            this.id = id;
        }
        @Override
        public long toPointer(int index) {
            Protocol.ToPointer.Builder builder = Protocol.ToPointer.newBuilder()
                    .setIndex(index);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setToPointer(builder))
                    .getToPointer().getResult();
        }

        @Override
        public long toLong(int index) throws LuaTypeError {
            Protocol.ToLong.Builder request = Protocol.ToLong.newBuilder()
                    .setIndex(index);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setToLong(request))
                    .getToLong().getResult();
        }

        @Override
        public double toDouble(int index) throws LuaTypeError {
            Protocol.ToDouble.Builder request = Protocol.ToDouble.newBuilder()
                    .setIndex(index);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setToDouble(request))
                    .getToDouble().getResult();
        }

        @Override
        public String toString(int index) throws LuaTypeError {
            Protocol.ToString.Builder request = Protocol.ToString.newBuilder()
                    .setIndex(index);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setToString(request))
                    .getToString().getResult().toStringUtf8();
        }

        @Override
        public byte[] toBytes(int index) throws LuaTypeError {
            Protocol.ToString.Builder request = Protocol.ToString.newBuilder()
                    .setIndex(index);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setToString(request))
                    .getToString().getResult().toByteArray();
        }

        @Override
        public boolean toBoolean(int index) {
            Protocol.ToBoolean.Builder request = Protocol.ToBoolean.newBuilder()
                    .setIndex(index);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setToBoolean(request))
                    .getToBoolean().getResult();
        }

        @Override
        public LuaObjectAdapter toLuaObjectAdapter(int index) throws LuaTypeError {
            throw new LuaRuntimeError("proxy object can't call toLuaObjectAdapter");
        }

        @Override
        public void push(long v) {
            Protocol.PushBaseValue.Builder request = Protocol.PushBaseValue.newBuilder()
                    .setL(v);
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setPushBaseValue(request));
        }

        @Override
        public void push(double v) {
            Protocol.PushBaseValue.Builder request = Protocol.PushBaseValue.newBuilder()
                    .setD(v);
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setPushBaseValue(request));
        }

        @Override
        public void push(String v) {
            Protocol.PushBaseValue.Builder request = Protocol.PushBaseValue.newBuilder()
                    .setB(ByteString.copyFrom(v.getBytes()));
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setPushBaseValue(request));
        }

        @Override
        public void push(byte[] v) {
            Protocol.PushBaseValue.Builder request = Protocol.PushBaseValue.newBuilder()
                    .setB(ByteString.copyFrom(v));
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setPushBaseValue(request));
        }

        @Override
        public void push(boolean v) {
            Protocol.PushBaseValue.Builder request = Protocol.PushBaseValue.newBuilder()
                    .setZ(v);
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setPushBaseValue(request));
        }

        @Override
        public void push(LuaFunctionAdapter v) {
            pushLuaFunctionAdapter(id,v);
        }

        @Override
        public void push(LuaObjectAdapter v) {
            pushLuaObjectAdapter(id,v);
        }

        @Override
        public void pushNil() {
            Protocol.PushBaseValue.Builder request = Protocol.PushBaseValue.newBuilder();
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setPushBaseValue(request));
        }

        @Override
        public int getTable(int tableIndex) {
            Protocol.GetTable.Builder request = Protocol.GetTable.newBuilder()
                    .setTableIndex(tableIndex);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setGetTable(request))
                    .getGetTable().getResult();
        }

        @Override
        public void setTable(int tableIndex) {
            Protocol.SetTable.Builder request = Protocol.SetTable.newBuilder()
                    .setTableIndex(tableIndex);
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setSetTable(request));
        }

        @Override
        public int getGlobal(String key) {
            Protocol.GetGlobal.Builder request = Protocol.GetGlobal.newBuilder()
                    .setKey(key);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setGetGlobal(request))
                    .getGetGlobal().getResult();
        }

        @Override
        public void setGlobal(String key) {
            Protocol.SetGlobal.Builder request = Protocol.SetGlobal.newBuilder()
                    .setKey(key);
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setSetGlobal(request));
        }

        @Override
        public int rawGet(int tableIndex) {
            Protocol.RawGet.Builder request = Protocol.RawGet.newBuilder()
                    .setTableIndex(tableIndex);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setRawGet(request))
                    .getRawGet().getResult();
        }

        @Override
        public void rawSet(int tableIndex) {
            Protocol.RawSet.Builder request = Protocol.RawSet.newBuilder()
                    .setTableIndex(tableIndex);
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setRawSet(request));
        }

        @Override
        public int getTop() {
            Protocol.GetTop.Builder request = Protocol.GetTop.newBuilder();
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setGetTop(request))
                    .getGetTop().getResult();
        }

        @Override
        public void setTop(int n) {
            Protocol.SetTop.Builder request = Protocol.SetTop.newBuilder()
                    .setN(n);
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setSetTop(request));
        }

        @Override
        public void pop(int n) {
            Protocol.Pop.Builder request = Protocol.Pop.newBuilder()
                    .setN(n);
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setPop(request));
        }

        @Override
        public int type(int index) {
            Protocol.GetType.Builder request = Protocol.GetType.newBuilder()
                    .setIndex(index);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setGetType(request))
                    .getGetType().getResult();
        }

        @Override
        public boolean isInteger(int index) {
            Protocol.IsInteger.Builder request = Protocol.IsInteger.newBuilder()
                    .setIndex(index);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setIsInteger(request))
                    .getIsInteger().getResult();
        }

        @Override
        public boolean isLuaObjectAdapter(int index) {
            Protocol.IsLuaObjectAdapter.Builder request = Protocol.IsLuaObjectAdapter.newBuilder()
                    .setIndex(index);
            return callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setIsLuaObjectAdapter(request))
                    .getIsLuaObjectAdapter().getResult();
        }

        @Override
        public void loadFile(String filePath, CODE_TYPE mode) {
            Protocol.LoadFile.Builder request = Protocol.LoadFile.newBuilder()
                    .setPath(filePath)
                    .setCodeType(Protocol.CODE_TYPE.valueOf(mode.name()));
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setLoadFile(request));
        }

        @Override
        public void loadBuffer(byte[] code, String chunkName, CODE_TYPE mode) {
            Protocol.LoadBuffer.Builder request = Protocol.LoadBuffer.newBuilder()
                    .setCode(ByteString.copyFrom(code))
                    .setChunkName(chunkName)
                    .setCodeType(Protocol.CODE_TYPE.valueOf(mode.name()));
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setLoadBuffer(request));
        }

        @Override
        public void pCall(int argNumber, int resultNumber, int errorFunctionIndex) {
            Protocol.PCall.Builder request = Protocol.PCall.newBuilder()
                    .setArgNumber(argNumber)
                    .setResultNumber(resultNumber)
                    .setErrorFunctionIndex(errorFunctionIndex);
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setPCall(request));
        }

        @Override
        public void destroy() {
            destroyContext(id);
        }

        @Override
        public void interrupt() {
            Protocol.Interrupt.Builder request = Protocol.Interrupt.newBuilder();
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setInterrupt(request));
        }

        @Override
        public void createTable(int arraySize, int dictionarySize) {
            Protocol.CreateTable.Builder request = Protocol.CreateTable.newBuilder()
                    .setArraySize(arraySize)
                    .setDictionarySize(dictionarySize);
            callAndCheckException(Protocol.Message.newBuilder()
                    .setContextID(id)
                    .setCreateTable(request));
        }
    }
}
