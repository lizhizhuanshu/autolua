package top.lizhistudio.androidlua.wrapper;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.annotation.LuaField;

public class JavaMixtureWrapper extends JavaClassWrapper{
    private final HashMap<String,Object> fieldsOrMethods;
    private JavaMixtureWrapper(Class<?> o,HashMap<String,Object> fieldsOrMethods) {
        super(o);
        this.fieldsOrMethods = fieldsOrMethods;
    }

    public static JavaMixtureWrapper newInstance(@NonNull Class<?> aClass,String[] methodNames,String[] fieldNames) throws NoSuchFieldException
    {
        HashMap<String,Object> fieldsOrMethods = new HashMap<>();
        pushMethod(aClass,methodNames,fieldsOrMethods);
        for (String name :
                fieldNames) {
            fieldsOrMethods.put(name, aClass.getField(name));
        }
        return new JavaMixtureWrapper(aClass,fieldsOrMethods);
    }


    public static JavaMixtureWrapper newInstanceByAnnotation(@NonNull Class<?> aClass)
    {
        HashMap<String,Object> fieldsOrMethods = new HashMap<>();
        pushMethodByAnnotation(aClass,fieldsOrMethods);
        for (Field field:aClass.getFields())
        {
            LuaField luaField = field.getAnnotation(LuaField.class);
            if (luaField != null)
            {
                String name = luaField.alias();
                if (name.equals(""))
                    name = field.getName();
                fieldsOrMethods.put(name,field);
            }
        }
        return new JavaMixtureWrapper(aClass,fieldsOrMethods);
    }



    @Override
    public int __index(LuaContext context, Object o) throws Throwable {
        String key = context.toString(2);
        Object fieldOrMethod = fieldsOrMethods.get(key);
        if (fieldOrMethod == null)
            return 0;
        if (fieldOrMethod instanceof Field)
        {
            Field field = (Field) fieldOrMethod;
            Object result = field.get(o);
            context.push(field.getType(),result);
        }else if (fieldOrMethod instanceof Method)
        {
            context.pushJavaObjectMethod();
        }
        return 1;
    }

    @Override
    public int __newIndex(LuaContext context, Object o) throws Throwable {
        String key = context.toString(2);
        Object fieldOrMethod = fieldsOrMethods.get(key);
        if (fieldOrMethod instanceof Field)
        {
            Field field = content.getField(context.toString(2));
            field.set(o,context.toJavaObject(3,field.getType()));
        }
        return 0;
    }

    @Override
    public int callMethod(LuaContext context, String methodName, Object o) throws Throwable {
        Object fieldOrMethod = fieldsOrMethods.get(methodName);
        if (fieldOrMethod == null || fieldOrMethod instanceof Field)
            return 0;
        Method method = (Method) fieldOrMethod;
        Object result;
        Class<?>[] classes = method.getParameterTypes();
        if (classes.length == 0)
            result = method.invoke(o);
        else if(classes.length == 1)
            result = method.invoke(o,
                    context.toJavaObject(2,classes[0]));
        else if(classes.length == 2)
            result = method.invoke(o,
                    context.toJavaObject(2,classes[0]),
                    context.toJavaObject(3,classes[1]));
        else
            result = method.invoke(o,context.toJavaObjects(2,classes));
        context.push(method.getReturnType(),result);
        return 1;
    }

    public static class Builder
    {
        private Class<?> aClass;
        private HashMap<String,Object> fieldOrMethod;
        public Builder reset(Class<?> aClass)
        {
            this.aClass = aClass;
            this.fieldOrMethod = new HashMap<>();
            return this;
        }

        public Builder(Class<?> aClass)
        {
            reset(aClass);
        }

        public Builder addMethod(String name,@NonNull String alias, Class<?> ... classes) throws NoSuchMethodException
        {
            Method method= aClass.getMethod(name,classes);
            fieldOrMethod.put(alias,method);
            return this;
        }

        public Builder addMethod(String name, Class<?> ... classes) throws NoSuchMethodException
        {
            Method method= aClass.getMethod(name,classes);
            fieldOrMethod.put(method.getName(),method);
            return this;
        }



        public Builder addField(String name) throws NoSuchFieldException
        {
            Field field = aClass.getField(name);
            fieldOrMethod.put(field.getName(),field);
            return this;
        }

        public Builder addField(String name,@NonNull String alias) throws NoSuchFieldException
        {
            Field field = aClass.getField(name);
            fieldOrMethod.put(alias,field);
            return this;
        }


        public JavaMixtureWrapper build()
        {
            return new JavaMixtureWrapper(aClass, fieldOrMethod);
        }

    }
}
