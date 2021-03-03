package top.lizhistudio.androidlua;


import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LuaContextTest {

    public static class Wife
    {
        private String name;
        private int age;
        public Wife(String name,int age)
        {
            this.name= name;
            this.age = age;
        }

        public Wife(String name)
        {
            this(name,18);
        }
        public static Wife where()
        {
            return new Wife("unknown");
        }

        public String getName()
        {
            return name;
        }

        public int getAge()
        {
            return age;
        }
    }

    @Test
    public void testRegister()
    {
        //这个工厂是用来创建 JavaObjectWrapper的，每个在lua中使用的java类型都需要注册到这里面
        JavaObjectWrapperFactoryImplement.Builder builder  = new JavaObjectWrapperFactoryImplement.Builder();
        builder.registerThrowable()
                .registerInterface(Wife.class);
        JavaObjectWrapperFactory factory = builder.build();

        LuaContext context = new LuaContextImplement(factory);
        //注册wife的对象
        context.push(new Wife("who"));
        //保存在lua的全局表中
        context.setGlobal("myWife");
        Object[] result = context.execute("return myWife:getName(),myWife:getAge()".getBytes(),"test");
        //验证一下结果
        assert "who".equals(result[0]);
        assertEquals(18L,result[1]);
        //注册wife的class
        context.push(Wife.class);
        //保存在lua的全局表中
        context.setGlobal("Wife");
        result = context.execute("return Wife:where()".getBytes(),"test");
        //返回的应该是Wife类型的对象
        Wife wife = (Wife)result[0];
        //验证一下结果
        assert  "unknown".equals(wife.getName());
        assertEquals(18L,wife.getAge());
    }



}
