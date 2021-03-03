package top.lizhistudio.autolua;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    interface TestClass
    {
        int testMethod1();
        Integer testMethod2();
        void testMethod3();
    }


    @Test
    public void testClass() throws Throwable
    {
        Class<?> clazz = TestClass.class;
        Method method =  clazz.getMethod("testMethod1");
        System.out.println("result  = "+(method.getReturnType()==Integer.TYPE));
        System.out.println("result  = "+method.getReturnType().isPrimitive());
        System.out.println("result  = "+method.getReturnType().isAssignableFrom(Number.class));

    }
}