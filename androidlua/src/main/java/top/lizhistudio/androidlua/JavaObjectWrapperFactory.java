package top.lizhistudio.androidlua;

import androidx.annotation.NonNull;

import top.lizhistudio.androidlua.wrapper.JavaClassWrapper;

public interface JavaObjectWrapperFactory {
    JavaObjectWrapper newObjectWrapper(Class<?> aClass, Object o);
    JavaClassWrapper getClassWrapper(Class<?> aClass);
    JavaObjectWrapperFactory register(@NonNull JavaClassWrapper javaClassWrapper);
}


