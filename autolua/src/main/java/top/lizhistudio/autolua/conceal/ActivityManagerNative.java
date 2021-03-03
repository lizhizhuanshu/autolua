package top.lizhistudio.autolua.conceal;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Process;

import java.lang.reflect.Method;

public class ActivityManagerNative {
    private Class clazz;
    private Object self;
    private ActivityManagerNative()
    {
        try{
            clazz = Class.forName("android.app.ActivityManagerNative");
            self = clazz.getMethod("getDefault").invoke(clazz);
        }catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    public static ActivityManagerNative getDefault()
    {
        return Stub.instance;
    }

    public ComponentName startService(Intent intent)
    {
        try{
            if(Build.VERSION.SDK_INT >= 26)
            {
                Method method = clazz.getMethod("startService",
                        Class.forName("android.app.IApplicationThread"),
                        Intent.class,
                        String.class,
                        boolean.class,
                        String.class,
                        int.class);
                return (ComponentName) method.invoke(self,null,intent,intent.getType(),false,"com.android.shell",  Process.myUid());
            }else if(Build.VERSION.SDK_INT >= 23)
            {
                Method method = clazz.getMethod("startService",
                        Class.forName("android.app.IApplicationThread"),
                        Intent.class,
                        String.class,
                        String.class,
                        int.class);
                return (ComponentName) method.invoke(self,null,intent,intent.getType(),"com.android.shell",  Process.myUid());
            }else{
                Method method = clazz.getMethod("startService",
                        Class.forName("android.app.IApplicationThread"),
                        Intent.class,
                        String.class,
                        int.class);
                return (ComponentName) method.invoke(self,null,intent,intent.getType(), Process.myUid());
            }
        }catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
        return null;
    }


    public void attachApplication(Object o)
    {
        try
        {
            Method method = clazz.getMethod("attachApplication",
                    Class.forName("android.app.IApplicationThread"));
            method.invoke(self,o);
        }catch (Exception e)
        {
            throw  new RuntimeException(e);
        }


    }


    private static final class Stub{
        static final ActivityManagerNative instance = new ActivityManagerNative();
    }
}
