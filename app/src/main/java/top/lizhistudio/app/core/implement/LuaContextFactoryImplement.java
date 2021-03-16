package top.lizhistudio.app.core.implement;


import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import top.lizhistudio.androidlua.DebugInfo;
import top.lizhistudio.androidlua.JavaObjectWrapperFactory;
import top.lizhistudio.androidlua.JavaObjectWrapperFactoryImplement;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaContextImplement;
import top.lizhistudio.androidlua.LuaHandler;
import top.lizhistudio.androidlua.exception.LuaInterruptError;
import top.lizhistudio.androidlua.wrapper.JavaStructWrapper;
import top.lizhistudio.app.core.DebugListener;
import top.lizhistudio.app.core.FloatView;
import top.lizhistudio.app.core.UI;
import top.lizhistudio.app.core.wrapper.UIWrapper;
import top.lizhistudio.autolua.core.LuaContextFactory;
import top.lizhistudio.autolua.core.RPCJavaInterfaceWrapper;
import top.lizhistudio.autolua.core.Server;
import top.lizhistudio.autolua.extend.Controller;
import top.lizhistudio.autolua.rpc.ClientHandler;



public class LuaContextFactoryImplement implements LuaContextFactory {

    private final JavaObjectWrapperFactory javaObjectWrapperFactory;
    public LuaContextFactoryImplement()
    {
        JavaObjectWrapperFactoryImplement.Builder builder = new JavaObjectWrapperFactoryImplement.Builder();
        builder.registerInterfaceByAnnotation(Controller.class)
                .registerThrowable()
                .registerStruct(PixelFormat.class)
                .registerStruct(WindowManager.LayoutParams.class)
                .registerStruct(Gravity.class)
                .register(new JavaStructWrapper( UI.LayoutParams.class))
                .registerLuaAdapter(UIWrapper.class)
                .register(new RPCJavaInterfaceWrapper(FloatView.class))
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
        LuaContextImplement luaContextImplement = new LuaContextImplement(javaObjectWrapperFactory);
        luaContextImplement.setGlobal(UI.SERVICE_NAME,UIWrapper.class,new UIWrapper(Server.getInstance().getClientHandler()));
        luaContextImplement.setGlobal("PixelFormat",PixelFormat.class);
        luaContextImplement.setGlobal("LayoutParamsFlags",WindowManager.LayoutParams.class);
        luaContextImplement.setGlobal("Gravity",Gravity.class);
        luaContextImplement.getGlobal("package");
        luaContextImplement.getField(-1,"loaded");
        luaContextImplement.push(Controller.getDefault());
        luaContextImplement.setField(-2,"controller");
        luaContextImplement.pop(2);
        if (getService(DebugListener.SERVICE_NAME,DebugListener.class)!= null)
        {
            luaContextImplement.setGlobal("print",new PrintHandler());
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
                builder.append(context.coerceToString(i));
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
            luaContext.getStack(1,debugInfo);
            luaContext.getInfo("Sl",debugInfo);
            String path = debugInfo.getSource();
            int currentLine = debugInfo.getCurrentLine();
            listener.onLog(message,path,currentLine);
            return 0;
        }
    }
}
