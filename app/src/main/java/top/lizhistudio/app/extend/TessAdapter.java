package top.lizhistudio.app.extend;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.autolua.core.Display;
import top.lizhistudio.autolua.core.LuaContextImplement;

public class TessAdapter extends CommonLuaObjectAdapter {
    private final TessBaseAPI tessBaseAPI;
    public TessAdapter()
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
        source.copyPixelsFromBuffer(display.getDisplayBuffer());
        tessBaseAPI.setImage(source);
        return 0;
    }

    public int clear(LuaContext context)
    {
        tessBaseAPI.clear();
        return 0;
    }

    public int end(LuaContext context)
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

    public int setScope(LuaContext context)
    {
        int x = (int)context.toLong(2);
        int y = (int)context.toLong(3);
        int x1 = (int)context.toLong(4);
        int y1 =(int)context.toLong(5);
        tessBaseAPI.setRectangle(x,y,x1-x,y1-y);
        return 0;
    }

    public int getHOCRText(LuaContext context)
    {
        int page = (int)context.toLong(2);
        String text = tessBaseAPI.getHOCRText(page);
        context.push(text);
        return 1;
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

    public int getWords(LuaContext context)
    {
        context.push(new PixaAdapter(tessBaseAPI.getWords()));
        return 1;
    }

    public int getTextLines(LuaContext context)
    {
        context.push(new PixaAdapter(tessBaseAPI.getTextlines()));
        return 1;
    }

    public int getRegions(LuaContext context)
    {
        context.push(new PixaAdapter(tessBaseAPI.getRegions()));
        return 1;
    }

    public int getStrips(LuaContext context)
    {
        context.push(new PixaAdapter(tessBaseAPI.getStrips()));
        return 1;
    }

    public int getConnectedComponents(LuaContext context)
    {
        context.push(new PixaAdapter(tessBaseAPI.getConnectedComponents()));
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
