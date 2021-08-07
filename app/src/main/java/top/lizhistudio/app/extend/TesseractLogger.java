package top.lizhistudio.app.extend;

import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.NotReleaseLuaFunctionAdapter;

public class TesseractLogger extends NotReleaseLuaFunctionAdapter {
    private final TessBaseAPIFactory tessBaseAPIFactory;
    public TesseractLogger()
    {
        this.tessBaseAPIFactory = new TessBaseAPIFactory();
    }

    @Override
    public int onExecute(LuaContext luaContext) throws Throwable {
        luaContext.push(tessBaseAPIFactory);
        luaContext.setGlobal("TessBaseAPIFactory");
        return 0;
    }

    public static class TessBaseAPIFactory extends CommonLuaObjectAdapter {
        public int create(LuaContext context)
        {
            context.push(new TessBaseAPIAdapter());
            return 1;
        }
    }
}
