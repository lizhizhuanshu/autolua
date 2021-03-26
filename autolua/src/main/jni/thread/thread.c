//
// Created by lizhi on 2021/3/26.
//
#include "unistd.h"
#include "thread.h"
#include "lauxlib.h"
#include "luajava.h"
#define CLASS_NAME "java/lang/Thread"
#define INTERRUPT_ERROR "interrupt"
static jclass ThreadClassID = NULL;
static jmethodID SleepMethodID = NULL;
void onInitializeThreadContext(JNIEnv*env)
{
    jclass aClass = (*env)->FindClass(env,CLASS_NAME);
    ThreadClassID = (*env)->NewGlobalRef(env, aClass);
    SleepMethodID = (*env)->GetStaticMethodID(env,ThreadClassID,"sleep","(J)V");
    (*env)->DeleteLocalRef(env,aClass);
}
void onReleaseThreadContext(JNIEnv*env)
{
    (*env)->DeleteGlobalRef(env,ThreadClassID);
}


static int luaSleep(lua_State*L)
{
    jlong  time = luaL_checkinteger(L,1);
    JNIEnv *env = GetJNIEnv(L);
    (*env)->CallStaticVoidMethod(env,ThreadClassID,SleepMethodID,time);
    if ((*env)->ExceptionCheck(env))
    {
        (*env)->ExceptionClear(env);
        lua_pushstring(L,INTERRUPT_ERROR);
        lua_error(L);
    }
    return 0;
}

static int luaUSleep(lua_State*L)
{
    jlong  time = luaL_checkinteger(L,1);
    usleep(time);
    return 0;
}

void pushThreadMethodTable(lua_State*L)
{
    static luaL_Reg methods[]={
            {"sleep",luaSleep},
            {"usleep",luaUSleep},
            {NULL,NULL}
    };
    luaL_newlib(L,methods);
    lua_pushstring(L,INTERRUPT_ERROR);
    lua_setfield(L,-2,"INTERRUPT");
}