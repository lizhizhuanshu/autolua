package top.lizhistudio.autolua.core.wrapper;

import top.lizhistudio.androidlua.DebugInfo;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.autolua.core.UserInterface;

public class UserInterfaceWrapper {
    private UserInterface ui;
    public UserInterfaceWrapper(UserInterface ui)
    {
        this.ui = ui;
    }

    public int newFloatView(LuaContext context) throws IllegalAccessException {
        String name = context.toString(2);
        String uri = context.toString(3);
        UserInterface.LayoutParams layoutParams = null;
        if (context.type(4) == LuaContext.LUA_TTABLE)
        {
            layoutParams = new UserInterface.LayoutParams();
            context.tableToStruct(4,layoutParams);
        }
        UserInterface.FloatView floatView = ui.newFloatView(name,uri,layoutParams);
        context.push(UserInterface.FloatView.class,floatView);
        return 1;
    }

    public int takeSignal(LuaContext context) throws InterruptedException {
        Object[] objects = (Object[])(ui.takeSignal());
        if (objects == null)
            return 0;
        for(Object obj :objects)
        {
            context.push(obj);
        }
        return objects.length;
    }

    public int getFloatView(LuaContext context)
    {
        String name = context.toString(2);
        context.push(UserInterface.FloatView.class,ui.getFloatView(name));
        return 1;
    }

    public int showMessage(LuaContext context)
    {
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
