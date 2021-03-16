//
// Created by lizhi on 2021/3/16.
//
#include "lua.h"
#include "lauxlib.h"
#include "unistd.h"

#if __ANDROID__
#define LUA_METHOD __attribute__((visibility("default")))
#else
#define LUA_METHOD __declspec(dllexport)
#endif // __ANDROID__


static int thread_usleep(lua_State*L)
{
    lua_Integer time = luaL_checkinteger(L, 1);
    usleep(time);
    return 0;
}

static int thread_sleep(lua_State*L)
{
    lua_Integer time = luaL_checkinteger(L, 1);
    usleep(time*1000);
    return 0;
}


LUA_METHOD int luaopen_thread(lua_State*L)
{
    luaL_Reg methods[]={
            {"uSleep", thread_usleep},
            {"sleep",  thread_sleep},
            {NULL,NULL}
    };
    luaL_newlib(L, methods);
    return 1;
}
