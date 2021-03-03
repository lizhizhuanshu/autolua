package top.lizhistudio.autolua;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class LuaContextTest {
    private LuaContext context;
    public LuaContextTest()
    {
        context = new LuaContextImplement(new JavaObjectWrapFactoryImplement());
    }

    private Object[] execute(String code)
    {
        return context.execute(code.getBytes(),"test");
    }

    @Test
    public void testExecute()
    {
        assertEquals(0,context.getTop());
        Object[] result = execute("return 1,2,true,'lizhi'");
        assertEquals(1L,result[0]);
        assertEquals(2L,result[1]);
        assert (boolean)result[2];
        assert "lizhi".equals(result[3]);
        assertEquals(0,context.getTop());
    }

    private static class TestClass
    {
        public static void testStaticVoid()
        {
        }

        public boolean testStaticBoolean(boolean v)
        {
            return v;
        }

        public String testStaticString(String v)
        {
            return v;
        }

        public static int testStaticInt(int i)
        {
            return i;
        }
        public static short testStaticShort(short v)
        {
            return v;
        }

        public static float testStaticFloat(float v)
        {
            return v;
        }

        public static double testStaticDouble(double v)
        {
            return v;
        }

        public static long testStaticLong(long v)
        {
            return v;
        }

        public  void testVoid()
        {
        }

        public boolean testBoolean(boolean v)
        {
            return v;
        }

        public String testString(String v)
        {
            return v;
        }

        public  int testInt(int i)
        {
            return i;
        }
        public  short testShort(short v)
        {
            return v;
        }

        public  float testFloat(float v)
        {
            return v;
        }

        public  double testDouble(double v)
        {
            return v;
        }

        public  long testLong(long v)
        {
            return v;
        }

    }

    @Test
    public void testArray()
    {
        int[] arr = new int[]{1,22,3,4};
        context.push(arr);
        context.setGlobal("arr");
        Object[] result = execute("return arr.length");
        assertEquals((long)(arr.length),result[0]);
        execute("arr[0] = 100");
        assertEquals(arr[0],100);
        result = execute("return arr[2]");
        assertEquals((long)arr[2],result[0]);
    }


    @Test
    public void testClass()
    {

    }

}
