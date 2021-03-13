package top.lizhistudio.app.core.implement;

import top.lizhistudio.androidlua.DebugInfo;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaHandler;
import top.lizhistudio.androidlua.exception.LuaInterruptError;
import top.lizhistudio.app.core.DebugListener;
import top.lizhistudio.autolua.core.LuaContextFactory;
import top.lizhistudio.autolua.core.LuaInterpreter;
import top.lizhistudio.autolua.core.LuaInterpreterFactory;
import top.lizhistudio.autolua.core.LuaInterpreterImplement;
import top.lizhistudio.autolua.core.Server;
import top.lizhistudio.autolua.rpc.ClientHandler;

public class LuaInterpreterFactoryImplement implements LuaInterpreterFactory {
    private final LuaContextFactory luaContextFactory;
    public LuaInterpreterFactoryImplement()
    {
        luaContextFactory = new LuaContextFactoryImplement();
    }
    @Override
    public LuaInterpreter newInstance() {
        return new LuaInterpreterImplement(luaContextFactory,new ErrorHandler());
    }
    private static class ErrorHandler implements LuaHandler
    {

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
        public int onExecute(LuaContext luaContext) throws Throwable {
            DebugListener debugListener = getService(DebugListener.SERVICE_NAME,DebugListener.class);
            if (debugListener != null)
            {
                DebugInfo debugInfo = new DebugInfo();
                luaContext.getStack(1,debugInfo);
                luaContext.getInfo("Sl",debugInfo);
                String path = debugInfo.getSource();
                int currentLine = debugInfo.getCurrentLine();
                String message = luaContext.coerceToString(-1);
                debugListener.onError(message,path,currentLine);
            }
            return 1;
        }
    }
}
