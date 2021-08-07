package top.lizhistudio.app.extend;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.nio.ByteBuffer;

import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.autolua.core.Display;
import top.lizhistudio.autolua.core.LuaContextImplement;

public class TessBaseAPIAdapter extends CommonLuaObjectAdapter {
    private final TessBaseAPI tessBaseAPI;
    public TessBaseAPIAdapter()
    {
        tessBaseAPI = new TessBaseAPI();
    }

    public int init(LuaContext context)
    {
        String dataPath = context.toString(2);
        String language = context.toString(3);
        boolean result;
        if (context.type(4)== LuaContext.VALUE_TYPE.NUMBER.getCode())
        {
            result = tessBaseAPI.init(dataPath,language,(int)context.toLong(4));
        }else{
            result = tessBaseAPI.init(dataPath,language);
        }
        context.push(result);
        return 1;
    }

    public int getUTF8Text(LuaContext context)
    {
        String text = tessBaseAPI.getUTF8Text();
        context.push(text);
        return 1;
    }

    public int setImageFromDisplay(LuaContext context)
    {
        Display display = ((LuaContextImplement)context).getDisplay();
        Bitmap source = Bitmap.createBitmap(display.getRowStride() / display.getPixelStride(),
                display.getHeight(), Bitmap.Config.ARGB_8888);
        ByteBuffer byteBuffer = display.getDisplayBuffer();
        byteBuffer.clear();
        source.copyPixelsFromBuffer(byteBuffer);
        tessBaseAPI.setImage(source);
        return 0;
    }

    public int setImageFromFile(LuaContext context)
    {
        String path = context.toString(2);
        File file = new File(path);
        tessBaseAPI.setImage(file);
        return 0;
    }

    public int clear(LuaContext context)
    {
        tessBaseAPI.clear();
        return 0;
    }

    public int release(LuaContext context)
    {
        tessBaseAPI.end();
        return 0;
    }

    public int getBoxText(LuaContext context)
    {
        int page = (int)context.toLong(2);
        String text = tessBaseAPI.getBoxText(page);
        context.push(text);
        return 1;
    }

    public int setRectangle(LuaContext context)
    {
        int left = (int)context.toLong(2);
        int top = (int)context.toLong(3);
        int width = (int)context.toLong(4);
        int height =(int)context.toLong(5);
        tessBaseAPI.setRectangle(left,top,width,height);
        return 0;
    }



    public int getResultIterator(LuaContext context)
    {
        ResultIterator resultIterator = tessBaseAPI.getResultIterator();
        if (resultIterator!= null)
            context.push(new ResultIteratorAdapter(resultIterator));
        else
            context.pushNil();
        return 1;
    }


    public int setScope(LuaContext context)
    {
        int x = (int)context.toLong(2);
        int y = (int)context.toLong(3);
        int x1 = (int)context.toLong(4);
        int y1 =(int)context.toLong(5);
        tessBaseAPI.setRectangle(x,y,x1-x,y1-y);
        tessBaseAPI.getResultIterator();
        return 0;
    }

    public int wordConfidences(LuaContext context)
    {
        int[] confidences = tessBaseAPI.wordConfidences();
        context.createTable(confidences.length,0);
        for (int i = 0; i < confidences.length; i++) {
            context.push(confidences[i]);
            context.push(i+1);
            context.setTable(-3);
        }
        return 1;
    }


    public int getPageSegMode(LuaContext context)
    {
        context.push(tessBaseAPI.getPageSegMode());
        return 1;
    }

    public int setPageSegMode(LuaContext context)
    {
        int mode = (int)context.toLong(2);
        tessBaseAPI.setPageSegMode(mode);
        return 0;
    }

    public int setVariable(LuaContext context)
    {
        String key = context.toString(2);
        String value = context.toString(3);
        context.push(tessBaseAPI.setVariable(key,value));
        return 1;
    }
}
