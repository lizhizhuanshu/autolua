package top.lizhistudio.app.extend;

import com.googlecode.leptonica.android.Box;
import com.googlecode.leptonica.android.Pixa;

import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;

public class PixaAdapter extends CommonLuaObjectAdapter {
    private final Pixa pixa;
    public PixaAdapter(Pixa pixa)
    {
        this.pixa = pixa;
    }

    public int getBoxRectangle(LuaContext context)
    {
        Box box = pixa.getBox((int)context.toLong(2));
        context.push(box.getX());
        context.push(box.getY());
        context.push(box.getWidth());
        context.push(box.getHeight());
        box.recycle();
        return 4;
    }

    public int getBoxScope(LuaContext context)
    {
        Box box = pixa.getBox((int)context.toLong(2));
        int x= box.getX();
        int y = box.getY();
        context.push(x);
        context.push(y);
        context.push(x+box.getWidth());
        context.push(y+box.getHeight());
        box.recycle();
        return 4;
    }

    public int getBoxGeometry(LuaContext context)
    {
        Box box = pixa.getBox((int)context.toLong(2));
        int[] geometry  =box.getGeometry();
        context.createTable(geometry.length,0);
        for (int i = 0; i < geometry.length; i++) {
            context.push(geometry[i]);
            context.push(i+1);
            context.setTable(-3);
        }
        box.recycle();
        return 1;
    }

    public int getWidth(LuaContext context)
    {
        context.push(pixa.getWidth());
        return 1;
    }

    public int getHeight(LuaContext context)
    {
        context.push(pixa.getHeight());
        return 1;
    }

    public int size(LuaContext context)
    {
        context.push(pixa.size());
        return 1;
    }

    @Override
    public void onRelease() {
        super.onRelease();
        pixa.recycle();
    }
}
