package top.lizhistudio.autoluaapp.core.implement;

import top.lizhistudio.androidlua.DebugInfo;
import top.lizhistudio.androidlua.JavaObjectWrapperFactory;
import top.lizhistudio.androidlua.JavaObjectWrapperFactoryImplement;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaContextImplement;
import top.lizhistudio.androidlua.LuaHandler;
import top.lizhistudio.androidlua.exception.LuaInterruptError;
import top.lizhistudio.autoluaapp.core.DebugListener;
import top.lizhistudio.autoluaapp.javawrapper.IToast;
import top.lizhistudio.autolua.core.LuaContextFactory;
import top.lizhistudio.autolua.core.RPCJavaInterfaceWrapper;
import top.lizhistudio.autolua.core.Server;
import top.lizhistudio.autolua.extend.Controller;
import top.lizhistudio.autolua.rpc.ClientHandler;

//set print method


public class LuaContextFactoryImplement implements LuaContextFactory {

    private final JavaObjectWrapperFactory javaObjectWrapperFactory;
    public LuaContextFactoryImplement()
    {
        JavaObjectWrapperFactoryImplement.Builder builder = new JavaObjectWrapperFactoryImplement.Builder();
        builder.registerInterfaceByAnnotation(Controller.class)
                .register(new RPCJavaInterfaceWrapper(IToast.class))
                .register(new RPCJavaInterfaceWrapper(DebugListener.class));
        javaObjectWrapperFactory = builder.build();
    }

    private <T> T getService(String name,Class<? extends T> aInterface)
    {
        ClientHandler clientHandler = Server.getInstance().getClientHandler();
        try{
            return clientHandler.getService(name,aInterface);
        }catch (InterruptedException e)
        {
            throw  new LuaInterruptError();
        }
    }

    @Override
    public LuaContext newInstance() {
        IToast to = getService("Toast",IToast.class);
        LuaContextImplement luaContextImplement = new LuaContextImplement(javaObjectWrapperFactory);
        luaContextImplement.push(IToast.class,to);
        luaContextImplement.setGlobal("Toast");
        if (getService(DebugListener.SERVICE_NAME,DebugListener.class)!= null)
        {
            luaContextImplement.push(new PrintHandler());
            luaContextImplement.setGlobal("print");
        }
        return luaContextImplement;
    }

    private static class PrintHandler implements LuaHandler
    {
        private final DebugInfo debugInfo;
        PrintHandler()
        {
            debugInfo = new DebugInfo();
        }

        private String getMessage(LuaContext context)
        {
            StringBuilder builder = new StringBuilder();
            for (int i=1;i<=context.getTop();i++)
            {
                builder.append(context.toString(i));
                if (i!=context.getTop())
                {
                    builder.append("    ");
                }
            }
            return builder.toString();
        }


        @Override
        public int onExecute(LuaContext luaContext) throws Throwable {
            ClientHandler clientHandler = Server.getInstance().getClientHandler();
            DebugListener listener = clientHandler.getService(DebugListener.SERVICE_NAME,DebugListener.class);
            if (listener == null)
                return 0;
            String message = getMessage(luaContext);
            luaContext.getStack(2,debugInfo);
            luaContext.getInfo("Sl",debugInfo);
            String path = debugInfo.getSource();
            int currentLine = debugInfo.getCurrentLine();
            listener.onLog(message,path,currentLine);
            return 0;
        }
    }

}
