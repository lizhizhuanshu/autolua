package top.lizhistudio.app.core;

import com.immomo.mls.annotation.LuaBridge;
import com.immomo.mls.annotation.LuaClass;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

import top.lizhistudio.autolua.core.value.LuaNumber;
import top.lizhistudio.autolua.core.value.LuaString;

@LuaClass(gcByLua = false)
public class UserdataUI {
    public static final String LUA_CLASS_NAME = "UI";

    public UserdataUI(Globals g, LuaValue[] init)
    {

    }
    @LuaBridge
    public void sendSignal(LuaValue[] luaValues){
        top.lizhistudio.autolua.core.value.LuaValue[] signal =
                new top.lizhistudio.autolua.core.value.LuaValue[luaValues.length];
        LuaValue luaValue;
        for (int i=0;i<luaValues.length;i++)
        {
            luaValue = luaValues[i];
            if (luaValue.isNil())
                signal[i] = top.lizhistudio.autolua.core.value.LuaValue.NIL();
            else if(luaValue.isBoolean())
                signal[i] = luaValue.toBoolean()? top.lizhistudio.autolua.core.value.LuaValue.TRUE():
                        top.lizhistudio.autolua.core.value.LuaValue.FALSE();
            else if(luaValue.isString())
                signal[i] = new LuaString(luaValue.toString());
            else if(luaValue.isInt())
                signal[i] = LuaNumber.valueOf(luaValue.toLong());
            else if(luaValue.isNumber())
                signal[i] = LuaNumber.valueOf(luaValue.toDouble());
            else
                signal[i] = top.lizhistudio.autolua.core.value.LuaValue.NIL();
        }
        try{
            UserInterfaceImplement.getDefault().putSignal(signal);
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }
}
