package top.lizhistudio.app.core;

import android.util.Log;

import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;

public class FloatViewLuaObjectAdapter extends CommonLuaObjectAdapter {
    private final UserInterface.FloatView floatView;
    public FloatViewLuaObjectAdapter(UserInterface.FloatView floatView)
    {
        this.floatView = floatView;
    }

    public int show(LuaContext context)
    {
        floatView.show();
        return 0;
    }

    public int conceal(LuaContext context)
    {
        floatView.conceal();
        return 0;
    }

    public int getXY(LuaContext context)
    {
        context.push(floatView.getX());
        context.push(floatView.getY());
        return 2;
    }

    public int setXY(LuaContext context)
    {
        floatView.setXY((int)context.toLong(2),(int)context.toLong(3));
        return 0;
    }

    public int getWidthHeight(LuaContext context)
    {
        context.push(floatView.getWidth());
        context.push(floatView.getHeight());
        return 2;
    }

    public int setWidthHeight(LuaContext context)
    {
        floatView.setWidthHeight((int)context.toLong(2),(int)context.toLong(3));
        return 0;
    }

    public int destroy(LuaContext context)
    {
        floatView.destroy();
        return 0;
    }

    public int getName(LuaContext context)
    {
        context.push(floatView.getName());
        return 1;
    }

    public int reload(LuaContext context)
    {
        context.push(floatView.reload(context.toString(2)));
        return 1;
    }

    @Override
    public void onRelease() {
        floatView.destroy();
    }
}
