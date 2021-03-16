package top.lizhistudio.app.core.wrapper;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.Util;
import top.lizhistudio.app.core.FloatView;
import top.lizhistudio.app.core.UI;
import top.lizhistudio.autolua.rpc.ClientHandler;

public class UIWrapper {
    private final ClientHandler clientHandler;
    public UIWrapper(ClientHandler clientHandler)
    {
        this.clientHandler = clientHandler;
    }
    private UI getUI() throws InterruptedException
    {
        return clientHandler.getService(UI.SERVICE_NAME,UI.class);
    }



    public int newFloatView(LuaContext context) throws InterruptedException, IllegalAccessException {
        UI ui = getUI();
        if (ui == null)
        {
            context.pushNil();
            return 1;
        }
        String name = context.toString(2);
        String uri = context.toString(3);
        UI.LayoutParams layoutParams = null;
        if (context.type(4) == LuaContext.LUA_TTABLE)
        {
            layoutParams = new UI.LayoutParams();
            context.tableToStruct(4,layoutParams);
        }
        FloatView floatView = ui.newFloatView(name,uri,layoutParams);
        context.push(FloatView.class,floatView);
        return 1;
    }

    public int takeSignal(LuaContext context) throws InterruptedException
    {
        UI ui = getUI();
        if (ui == null)
            return 0;
        Object[] objects = (Object[])(ui.takeSignal());
        if (objects == null)
            return 0;
        for(Object obj :objects)
        {
            context.push(obj);
        }
        return objects.length;
    }

    public int getFloatView(LuaContext context) throws InterruptedException
    {
        UI ui = getUI();
        if (ui == null)
            return 0;
        String name = context.toString(2);
        context.push(FloatView.class,ui.getFloatView(name));
        return 1;
    }

    public int showMessage(LuaContext context) throws InterruptedException
    {
        UI ui = getUI();
        if (ui == null)
            return 0;
        String message = context.toString(2);
        int time = 0;
        if (context.isInteger(3))
        {
            time = (int)context.toInteger(3);
        }
        ui.showMessage(message,time);
        return 0;
    }
}
