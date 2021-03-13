package top.lizhistudio.autolua;

import android.content.Context;
import android.view.WindowManager;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.http.util.ByteArrayBuffer;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext()  throws Throwable{
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("top.lizhistudio.autolua", appContext.getPackageName());
        LayoutParams layoutParams = new LayoutParams();
        layoutParams.width = 1000;
        layoutParams.height = 1000;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(100000);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(layoutParams);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream  objectInputStream = new ObjectInputStream(byteArrayInputStream);
        LayoutParams layoutParams1 = (LayoutParams)objectInputStream.readObject();
        assertEquals(layoutParams.width,layoutParams1.width);
        assertEquals(layoutParams.height,layoutParams1.height);
    }

    public static class LayoutParams  implements Serializable
    {
        private static final long serialVersionUID = 423652362342L;
        public static final String CLASS_NAME = "LayoutParams";
        public int width;
        public int height;
    }

}