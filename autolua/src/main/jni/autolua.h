//
// Created by lizhi on 2021/6/23.
//

#ifndef AUTOLUA2_LUAJNI_H
#define AUTOLUA2_LUAJNI_H
#include<jni.h>
#include "lua.hpp"

#define JavaFindClass(env,str) env->FindClass(str)
#define FreeLocalObject(env,obj)  env->DeleteLocalRef(obj)
#define toLuaState(L) ((lua_State*)L)
#define GLOBAL(env, obj) env->NewGlobalRef(obj)
#define FreeGlobal(env,obj) env->DeleteGlobalRef(obj)
#define toLuaState(L) ((lua_State*)L)
#define GetLuaExtension(L) (*((LuaExtension**)lua_getextraspace(L)))
#define SetLuaExtension(L,p) (*((LuaExtension**)lua_getextraspace(L)) = p)
#define GetJavaLuaContext(L) ((*((LuaExtension**)lua_getextraspace(L)))->context)

JNIEnv *GetJNIEnv(lua_State*L);

typedef struct {
    jobject context;
}LuaExtension;

template<class T>
class LocalReference
{
    JNIEnv*env;
    T o;
public:
    LocalReference(JNIEnv*env,T v)
            :env(env),o(v)
    {
    }
    ~LocalReference(){
        if (env && o)
        {
            env->DeleteLocalRef((jobject)o);
        }
    }
    T get(){
        return o;
    }
};


#endif //AUTOLUA2_LUAJNI_H
