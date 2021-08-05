package top.lizhistudio.app.extend;

import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.NotReleaseLuaFunctionAdapter;

public class TessLogger extends NotReleaseLuaFunctionAdapter {
    private final TessFactory tessFactory;
    public TessLogger()
    {
        this.tessFactory = new TessFactory();
    }

    @Override
    public int onExecute(LuaContext luaContext) throws Throwable {
        luaContext.push(tessFactory);
        luaContext.setGlobal("TessFactory");
        return 0;
    }

    public static class TessFactory extends CommonLuaObjectAdapter {
        public int create(LuaContext context)
        {
            context.push(new TessAdapter());
            return 1;
        }
    }
}
