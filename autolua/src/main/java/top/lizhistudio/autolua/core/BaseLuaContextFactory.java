package top.lizhistudio.autolua.core;

import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import top.lizhistudio.androidlua.DebugInfo;
import top.lizhistudio.androidlua.JavaObjectWrapperFactoryImplement;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaContextImplement;
import top.lizhistudio.androidlua.LuaHandler;
import top.lizhistudio.androidlua.exception.LuaInterruptError;
import top.lizhistudio.autolua.core.wrapper.UserInterfaceWrapper;
import top.lizhistudio.autolua.extend.Core;
import top.lizhistudio.autolua.extend.Input;
import top.lizhistudio.autolua.rpc.ClientHandler;

public class BaseLuaContextFactory implements LuaContextFactory{
    public static final String AUTO_LUA_UI_NAME = "UI";
    public static final String AUTO_LUA_PRINT_LISTENER_NAME = "PrintListener";
    public static final String AUTO_LUA_ASSETS_PROVIDER_NAME = "AssetsProvider";

    private JavaObjectWrapperFactoryImplement javaObjectWrapperFactory;
    private final ClientHandler  clientHandler;

    protected JavaObjectWrapperFactoryImplement.Builder createBuilder()
    {
        JavaObjectWrapperFactoryImplement.Builder builder = new JavaObjectWrapperFactoryImplement.Builder();
        builder.registerThrowable()
                .registerStruct(PixelFormat.class)
                .registerStruct(WindowManager.LayoutParams.class)
                .registerStruct(Gravity.class)
                .registerLuaAdapter(UserInterfaceWrapper.class)
                .registerInterface(UserInterface.FloatView.class)
                .registerLuaAdapter(Input.class)
                .registerInterface(AssetsProvider.class);
        return builder;
    }

    private void initializeJavaObjectWrapperFactory()
    {
        javaObjectWrapperFactory = createBuilder().build();
    }



    public BaseLuaContextFactory()
    {
        clientHandler = Server.getInstance().getClientHandler();
        initializeJavaObjectWrapperFactory();
    }


    @Override
    public LuaContext newInstance() {

        LuaContextImplement luaContext = new LuaContextImplement(javaObjectWrapperFactory);
        try{
            if (clientHandler.hasService(AUTO_LUA_UI_NAME))
            {
                UserInterface userInterface = clientHandler.getService(AUTO_LUA_UI_NAME,UserInterface.class);
                luaContext.setGlobal(AUTO_LUA_UI_NAME,
                        UserInterfaceWrapper.class,
                        new UserInterfaceWrapper(userInterface));
            }
            if (clientHandler.hasService(AUTO_LUA_PRINT_LISTENER_NAME))
            {
                luaContext.setGlobal("print",
                        new PrintHandler(clientHandler.getService(AUTO_LUA_PRINT_LISTENER_NAME,PrintListener.class)));
            }
            if (clientHandler.hasService(AUTO_LUA_ASSETS_PROVIDER_NAME))
            {
                AssetsProvider assetsProvider = clientHandler.getService(AUTO_LUA_ASSETS_PROVIDER_NAME,AssetsProvider.class);
                luaContext.setGlobal(AUTO_LUA_ASSETS_PROVIDER_NAME,AssetsProvider.class,assetsProvider);
            }

        }catch (InterruptedException e)
        {
            throw new LuaInterruptError();
        }
        luaContext.setGlobal("PixelFormat", PixelFormat.class);
        luaContext.setGlobal("LayoutParamsFlags", WindowManager.LayoutParams.class);
        luaContext.setGlobal("Gravity", Gravity.class);
        luaContext.setGlobal("Input", Input.class, Input.getDefault());
        Core.inject(luaContext.getNativeLua());
        return luaContext;
    }


    private static class PrintHandler implements  LuaHandler
    {
        private DebugInfo debugInfo = new DebugInfo();
        private PrintListener printListener;
        PrintHandler(PrintListener printListener)
        {
            this.printListener = printListener;
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
            String message = getMessage(luaContext);
            luaContext.getStack(1,debugInfo);
            luaContext.getInfo("Sl",debugInfo);
            String path = debugInfo.getSource();
            int currentLine = debugInfo.getCurrentLine();
            printListener.onPrint(path,currentLine,message);
            return 0;
        }
    }
}
