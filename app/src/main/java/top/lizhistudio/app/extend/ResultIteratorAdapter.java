package top.lizhistudio.app.extend;

import android.graphics.Rect;

import com.googlecode.tesseract.android.ResultIterator;

import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;

public class ResultIteratorAdapter extends CommonLuaObjectAdapter {
    private final ResultIterator resultIterator;
    public ResultIteratorAdapter(ResultIterator resultIterator)
    {
        this.resultIterator = resultIterator;
    }

    public int next(LuaContext context)
    {
        int level = (int)context.toLong(2);
        context.push(resultIterator.next(level));
        return 1;
    }

    public int getUTF8Text(LuaContext context)
    {
        int level = (int)context.toLong(2);
        context.push(resultIterator.getUTF8Text(level));
        return 1;
    }

    public int getBoundingRect(LuaContext context)
    {
        int level = (int)context.toLong(2);
        Rect rect = resultIterator.getBoundingRect(level);
        context.push(rect.left);
        context.push(rect.top);
        context.push(rect.right);
        context.push(rect.bottom);
        return 4;
    }

    public int delete(LuaContext context)
    {
        resultIterator.delete();
        return 0;
    }

    public int confidence(LuaContext context)
    {
        int level = (int)context.toLong(2);
        context.push(resultIterator.confidence(level));
        return 1;
    }

    public int isAtBeginningOf(LuaContext context)
    {
        int level = (int)context.toLong(2);
        context.push(resultIterator.isAtBeginningOf(level));
        return 1;
    }

    public int isAtFinalElement(LuaContext context)
    {
        int level = (int)context.toLong(2);
        int element = (int)context.toLong(3);
        context.push(resultIterator.isAtFinalElement(level,element));
        return 1;
    }

    @Override
    public void onRelease() {
        super.onRelease();
        resultIterator.delete();
    }
}
