package top.lizhistudio.autolua;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaFunctionAdapter;
import top.lizhistudio.androidlua.LuaObjectAdapter;
import top.lizhistudio.androidlua.exception.LuaError;
import top.lizhistudio.androidlua.exception.LuaRuntimeError;
import top.lizhistudio.androidlua.exception.LuaTypeError;
import top.lizhistudio.autolua.core.LuaContextImplement;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LuaContextTest {
    private final LuaContext luaContext;
    public LuaContextTest()
    {
        luaContext = new LuaContextImplement();
    }

    @Test
    public void testGetTopAndSetTopAndPop()
    {
        assertEquals(0,luaContext.getTop());
        luaContext.push(1);
        assertEquals(1,luaContext.getTop());
        luaContext.push(2);
        assertEquals(2,luaContext.getTop());
        luaContext.pop(1);
        assertEquals(1,luaContext.getTop());
        luaContext.push(1);
        luaContext.push(1);
        luaContext.setTop(0);
        assertEquals(0,luaContext.getTop());
    }

    @Test
    public void testPushBaseValueAndType()
    {
        luaContext.push(true);
        assertEquals(luaContext.type(-1),LuaContext.VALUE_TYPE.BOOLEAN.getCode());
        luaContext.push(111);
        assert luaContext.isInteger(-1);
        assertEquals(luaContext.type(-1),LuaContext.VALUE_TYPE.NUMBER.getCode());
        luaContext.push(111d);
        assert !luaContext.isInteger(-1);
        assertEquals(luaContext.type(-1),LuaContext.VALUE_TYPE.NUMBER.getCode());
        luaContext.push("lizhi");
        assertEquals(luaContext.type(-1),LuaContext.VALUE_TYPE.STRING.getCode());
        luaContext.push("lizhi".getBytes());
        assertEquals(luaContext.type(-1),LuaContext.VALUE_TYPE.STRING.getCode());
        luaContext.setTop(0);
    }

    @Test
    public void testPushLuaObjectAdapter()
    {
        CommonLuaObjectAdapter commonLuaObjectAdapter = new CommonLuaObjectAdapter(new Object());
        luaContext.push(commonLuaObjectAdapter);
        assertEquals(luaContext.type(-1),LuaContext.VALUE_TYPE.USERDATA.getCode());
        assert luaContext.isLuaObjectAdapter(-1);
        LuaObjectAdapter luaObjectAdapter = luaContext.toLuaObjectAdapter(-1);
        assertEquals(commonLuaObjectAdapter,luaObjectAdapter);
        luaContext.setTop(0);
    }

    @Test
    public void testLuaHandler()
    {
        luaContext.setTop(0);
        luaContext.push(new LuaFunctionAdapter() {
            @Override
            public int onExecute(LuaContext luaContext) throws Throwable {
                long a = luaContext.toLong(1);
                long b = luaContext.toLong(2);
                luaContext.push(a+b);
                return 1;
            }
        });
        long a = 150;
        long b = 156;
        luaContext.push(a);
        luaContext.push(b);
        luaContext.pCall(2,1,0);
        assertEquals(a+b,luaContext.toLong(-1));
        assertEquals(1,luaContext.getTop());
        luaContext.pop(1);

        luaContext.push(new LuaFunctionAdapter() {
            @Override
            public int onExecute(LuaContext luaContext) throws Throwable {
                throw new LuaRuntimeError("test error");
            }
        });
        try{
            luaContext.pCall(0,0,0);
            assert false;
        }catch (LuaError e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetSetGlobal()
    {
        luaContext.push("lizhi");
        luaContext.setGlobal("name");
        assertEquals(luaContext.getGlobal("name"),LuaContext.VALUE_TYPE.STRING.getCode());
        assertEquals("lizhi",luaContext.toString(-1));
        luaContext.setTop(0);
    }


    @Test
    public void testToLong()
    {
        long aLong = 111;
        luaContext.push(aLong);
        assertEquals(aLong,luaContext.toLong(-1));
        luaContext.push(111d);
        assertEquals(aLong,luaContext.toLong(-1));
        try{
            luaContext.push("lizhi");
            luaContext.toLong(-1);
            assert false;
        }catch (LuaTypeError e)
        {
            e.printStackTrace();
        }
        try{
            luaContext.push(true);
            luaContext.toLong(-1);
            assert false;
        }catch (LuaTypeError e)
        {
            e.printStackTrace();
        }
        luaContext.setTop(0);
    }

    @Test
    public void testToDouble()
    {
        double aDouble = 0.5564;
        luaContext.push(aDouble);
        assertEquals(aDouble,luaContext.toDouble(-1),0);
        luaContext.push(111);
        assertEquals(111d,luaContext.toDouble(-1),0);
        luaContext.setTop(0);
    }

    @Test
    public void testToString()
    {
        String str = "lizhi";
        luaContext.push(str);
        assertEquals(str,luaContext.toString(-1));
        luaContext.push(str.getBytes());
        assertEquals(str,luaContext.toString(-1));
        assertArrayEquals(str.getBytes(),luaContext.toBytes(-1));
        int number = 1111;
        luaContext.push(number);
        assertEquals(String.valueOf(number),luaContext.toString(-1));
        luaContext.setTop(0);
    }


}
