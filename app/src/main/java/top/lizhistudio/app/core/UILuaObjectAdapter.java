package top.lizhistudio.app.core;


import android.view.WindowManager;

import java.lang.reflect.Field;

import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.Util;
import top.lizhistudio.autolua.core.value.LuaValue;

public class UILuaObjectAdapter extends CommonLuaObjectAdapter{
    private final UserInterface ui;
    public UILuaObjectAdapter()
    {
        this(UserInterfaceImplement.getDefault());
    }

    public UILuaObjectAdapter(UserInterface ui)
    {
        this.ui = ui;
    }


    private void setIntField(LuaContext context,
                             String fieldName,
                             WindowManager.LayoutParams layoutParams)
            throws NoSuchFieldException, IllegalAccessException {
        context.push(fieldName);
        int type = context.getTable(4);
        if (type == LuaContext.VALUE_TYPE.NUMBER.getCode())
        {
            Field field = WindowManager.LayoutParams.class.getField(fieldName);
            field.set(layoutParams,(int)context.toLong(-1));
        }
        context.pop(1);
    }

    private void setFloatField(LuaContext context,
                               String fieldName,
                               WindowManager.LayoutParams layoutParams)
            throws NoSuchFieldException, IllegalAccessException {
        context.push(fieldName);
        int type = context.getTable(4);
        if (type == LuaContext.VALUE_TYPE.NUMBER.getCode())
        {
            Field field = WindowManager.LayoutParams.class.getField(fieldName);
            field.set(layoutParams,(float)context.toDouble(-1));
        }
        context.pop(1);
    }

    public int newFloatView(LuaContext context) throws NoSuchFieldException, IllegalAccessException {
        String name = context.toString(2);
        String uri = context.toString(3);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (context.type(4) == LuaContext.VALUE_TYPE.TABLE.getCode())
        {
            setIntField(context,"x",layoutParams);
            setIntField(context,"y",layoutParams);
            setIntField(context,"width",layoutParams);
            setIntField(context,"height",layoutParams);
            setIntField(context,"gravity",layoutParams);
            setIntField(context,"flags",layoutParams);
            setIntField(context,"format",layoutParams);
            setFloatField(context,"alpha",layoutParams);
        }
        UserInterface.FloatView floatView = ui.newFloatView(name,uri,layoutParams);
        context.push(new FloatViewLuaObjectAdapter(floatView));
        return 1;
    }

    public int showMessage(LuaContext context)
    {
        String message = context.toString(2);
        int time = (int)context.toLong(3);
        ui.showMessage(message,time);
        return 0;
    }


    public int takeSignal(LuaContext context) throws InterruptedException {
        LuaValue[] luaValues = ui.takeSignal();
        Util.pushLuaValues(context,luaValues);
        return luaValues.length;
    }

    public int getFloatView(LuaContext context)
    {
        String name = context.toString(2);
        UserInterface.FloatView floatView = ui.getFloatView(name);
        if (floatView != null){
            context.push(new FloatViewLuaObjectAdapter(floatView));
        }else
            context.pushNil();
        return 1;
    }
}
