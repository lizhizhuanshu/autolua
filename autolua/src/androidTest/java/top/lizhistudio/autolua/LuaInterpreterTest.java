package top.lizhistudio.autolua;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;


import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.autolua.core.LuaContextFactoryImplement;
import top.lizhistudio.autolua.core.LuaInterpreter;
import top.lizhistudio.autolua.core.rpc.Protocol;
import top.lizhistudio.autolua.core.value.LuaValue;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LuaInterpreterTest {
    private final LuaInterpreter interpreter;
    public LuaInterpreterTest()
    {
        interpreter = new AutoLuaEngine(new LuaContextFactoryImplement());
    }

    @Test
    public void testInterpreterExecute()
    {
        String code = "return 1,'lizhi',true,1.3";
        LuaValue[] result = interpreter.execute(code.getBytes(),"test", LuaContext.CODE_TYPE.TEXT);
        assertEquals(1,result[0].toLong());
        assertEquals("lizhi",result[1].toString());
        assertTrue(result[2].toBoolean());
        assertEquals(1.3,result[3].toDouble(),0);
    }
    @Test
    public void testInterpreterSetScriptPath()
    {
        String path = "/sdcard/lizhi/autolua";
        interpreter.setScriptLoadPath(path);
        interpreter.destroyNowLuaContext();
        String code = "return package.path";
        LuaValue[] result = interpreter.execute(code.getBytes(),"test",LuaContext.CODE_TYPE.TEXT);
        assertEquals(path,result[0].toString());
    }
}
