package top.lizhistudio.app.core.implement;

import android.os.Parcel;
import android.view.WindowManager;

import java.io.Serializable;

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
                .registerThrowable()
                .register(new JavaStructWrapper(WindowManager.LayoutParams.class))
                .register(new RPCJavaInterfaceWrapper(UI.class))
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
        luaContextImplement.setGlobal(UI.SERVICE_NAME,UI.class,new UIProxy(getService(UI.SERVICE_NAME,UI.class)));
        luaContextImplement.setGlobal("LayoutParams", WindowManager.LayoutParams.class);
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

    private static class UIProxy implements UI {
        private final UI ui;
        UIProxy(UI ui )
        {
            this.ui = ui;
        }
        @Override
        public FloatView newFloatView(String name, String uri, byte[] layoutParams) {

            return ui.newFloatView(name, uri, layoutParams);
        }

        @Override
        public FloatView newFloatView(String name, String uri, WindowManager.LayoutParams layoutParams) {
            Parcel parcel = Parcel.obtain();
            try{
                parcel.writeValue(layoutParams);
                return newFloatView(name, uri, parcel.marshall());
            }finally {
                parcel.recycle();
            }
        }

        @Override
        public Object takeSignal() throws InterruptedException {
            return ui.takeSignal();
        }

        @Override
        public FloatView getFloatView(String name) {
            return ui.getFloatView(name);
        }

        @Override
        public void showMessage(String message, int time) {
            ui.showMessage(message, time);
        }

        @Override
        public void putSignal(Object message) throws InterruptedException {
            ui.putSignal(message);
        }
    }
}
