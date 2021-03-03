package top.lizhistudio.androidlua;

import androidx.annotation.NonNull;

import java.util.HashMap;

import top.lizhistudio.androidlua.wrapper.JavaArrayWrapper;
import top.lizhistudio.androidlua.wrapper.JavaClassWrapper;
import top.lizhistudio.androidlua.wrapper.JavaInterfaceWrapper;
import top.lizhistudio.androidlua.wrapper.JavaMixtureWrapper;
import top.lizhistudio.androidlua.wrapper.JavaObjectWrapperImplement;
import top.lizhistudio.androidlua.wrapper.JavaStructWrapper;
import top.lizhistudio.androidlua.wrapper.LuaAdapterWrapper;

public class JavaObjectWrapperFactoryImplement implements JavaObjectWrapperFactory {
    private final HashMap<Class<?>,JavaClassWrapper> javaClassWrapperHashMap;

    private JavaObjectWrapperFactoryImplement(HashMap<Class<?>,JavaClassWrapper> javaClassWrapperHashMap)
    {
        this.javaClassWrapperHashMap = javaClassWrapperHashMap;
    }

    public JavaObjectWrapperFactoryImplement()
    {
        this.javaClassWrapperHashMap = new HashMap<>();
    }

    @Override
    public JavaObjectWrapper newObjectWrapper(Class<?> aClass, Object o) {
        if (aClass.isArray())
        {
            return new JavaArrayWrapper(o,aClass);
        }
        JavaClassWrapper javaClassWrapper = javaClassWrapperHashMap.get(aClass);
        if (javaClassWrapper == null)
            throw new RuntimeException("don't has this class "+aClass);
        return new JavaObjectWrapperImplement(o,javaClassWrapper);
    }

    @Override
    public JavaClassWrapper getClassWrapper(Class<?> aClass) {
        return javaClassWrapperHashMap.get(aClass);
    }

    @Override
    public JavaObjectWrapperFactory register(@NonNull JavaClassWrapper javaClassWrapper) {
        javaClassWrapperHashMap.put((Class<?>) javaClassWrapper.getContent(),javaClassWrapper);
        return this;
    }

    public static class Builder
    {
        private HashMap<Class<?>,JavaClassWrapper> javaClassWrapperHashMap;
        public Builder reset()
        {
            javaClassWrapperHashMap = new HashMap<>();
            return this;
        }
        public Builder()
        {
            reset();
        }

        public Builder register(@NonNull JavaClassWrapper javaClassWrapper)
        {
            javaClassWrapperHashMap.put((Class<?>) javaClassWrapper.getContent(),javaClassWrapper);
            return this;
        }

        public Builder registerInterface(@NonNull Class<?> aClass,String[] methodNames)
        {
            javaClassWrapperHashMap.put(aClass, JavaInterfaceWrapper.newInstance(aClass,methodNames));
            return this;
        }

        public Builder registerInterface(@NonNull Class<?> aClass)
        {
            javaClassWrapperHashMap.put(aClass, JavaInterfaceWrapper.newInstance(aClass));
            return this;
        }

        public Builder registerInterfaceByAnnotation(@NonNull Class<?> aClass)
        {
            javaClassWrapperHashMap.put(aClass, JavaInterfaceWrapper.newInstanceByAnnotation(aClass));
            return this;
        }

        public Builder registerLuaAdapter(@NonNull Class<?> aClass)
        {

            javaClassWrapperHashMap.put(aClass,new LuaAdapterWrapper(aClass));
            return this;
        }

        public Builder registerStruct(@NonNull Class<?> aClass)
        {
            javaClassWrapperHashMap.put(aClass,new JavaStructWrapper(aClass));
            return this;
        }

        public Builder registerByAnnotation(@NonNull Class<?> aClass)
        {
            javaClassWrapperHashMap.put(aClass, JavaMixtureWrapper.newInstanceByAnnotation(aClass));
            return this;
        }

        public Builder register(@NonNull Class<?> aClass,String[] methodNames,String[] fieldNames) throws NoSuchFieldException
        {
            javaClassWrapperHashMap.put(aClass, JavaMixtureWrapper.newInstance(aClass,methodNames,fieldNames));
            return this;
        }

        public Builder registerThrowable()
        {
            return registerInterface(Throwable.class).registerInterface(StackTraceElement.class);
        }


        public JavaObjectWrapperFactoryImplement build()
        {
            return new JavaObjectWrapperFactoryImplement(javaClassWrapperHashMap);
        }
    }


}
