//
// Created by lizhi on 2021/3/26.
//

#include "thread.h"
#define CLASS_NAME "java/lang/Thread"
#define INTERRUPT_ERROR "interrupt"
#include "unistd.h"

static jclass ThreadClassID = nullptr;
static jmethodID SleepMethodID = nullptr;

void onInitializeThreadContext(JNIEnv*env)
{
    jclass aClass = env->FindClass(CLASS_NAME);
    ThreadClassID = (jclass)env->NewGlobalRef(aClass);
    SleepMethodID = env->GetStaticMethodID(ThreadClassID,"sleep","(J)V");
    env->DeleteLocalRef(aClass);
}

void onReleaseThreadContext(JNIEnv*env)
{
    env->DeleteGlobalRef(ThreadClassID);
}


static int luaSleep(lua_State*L)
{
    jlong  time = luaL_checkinteger(L,1);
    JNIEnv *env = GetJNIEnv(L);
    env->CallStaticVoidMethod(ThreadClassID,SleepMethodID,time);
    if (env->ExceptionCheck())
    {
        env->ExceptionClear();
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

int luaopen_thread(lua_State*L)
{
    static luaL_Reg methods[]={
            {"sleep",luaSleep},
            {"usleep",luaUSleep},
            {nullptr,nullptr}
    };
    luaL_newlib(L,methods);
    lua_pushstring(L,INTERRUPT_ERROR);
    lua_setfield(L,-2,"INTERRUPT");
    return 1;
}