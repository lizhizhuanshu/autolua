package top.lizhistudio.autolua;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static final String TAG = "LuaContextTest";
    LuaContext context;
    public ExampleInstrumentedTest()
    {
        context = new LuaContextImplement(new JavaObjectWrapFactoryImplement());
    }
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("top.lizhistudio.autolua.test", appContext.getPackageName());
    }

    private static class TestClass
    {
        public static void logError(String message)
        {
            Log.e(TAG,message);
        }
        public void log(String message)
        {
            Log.e(TAG,message);
        }
        public int test(int a,int b)
        {
            return a+b;
        }
    }


    @Test
    public void testLuaContext()
    {


    }
}