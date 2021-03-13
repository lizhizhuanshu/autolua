package top.lizhistudio.app.core.implement;

import com.immomo.mls.annotation.LuaBridge;
import com.immomo.mls.annotation.LuaClass;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

@LuaClass(gcByLua = false)
public class UserdataUI {
    public static final String LUA_CLASS_NAME = "UI";

    public UserdataUI(Globals g, LuaValue[] init)
    {

    }
    @LuaBridge
    public void sendSignal(LuaValue[] luaValues)
    {
        Object[] signal = new Object[luaValues.length];
        LuaValue luaValue;
        for (int i=0;i<luaValues.length;i++)
        {
            luaValue = luaValues[i];
            if (luaValue.isNil())
                signal[i] = null;
            else if(luaValue.isBoolean())
                signal[i] = luaValue.toBoolean();
            else if(luaValue.isString())
                signal[i] = luaValue.toString();
            else if(luaValue.isInt())
                signal[i] = luaValue.toLong();
            else if(luaValue.isNumber())
                signal[i] = luaValue.toDouble();
            else
                signal[i] = null;
        }
        try{
            UIImplement.getInstance().putSignal(signal);
        }catch (InterruptedException e)
        {

        }
    }
}
