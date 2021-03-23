//
// Created by lizhi on 2021/3/22.
//

#ifndef AUTOLUA_LUAVIEWER_H
#define AUTOLUA_LUAVIEWER_H
#include "lua.h"
#if __ANDROID__
#define LUA_METHOD __attribute__((visibility("default")))
#else
#define LUA_METHOD __declspec(dllexport)
#endif // __ANDROID__
#ifdef __cplusplus
extern "C"
{
    LUA_METHOD int luaopen_viewer(lua_State* L);
}
#else
LUA_METHOD int luaopen_viewer(lua_State* L);
#endif


#endif //AUTOLUA_LUAVIEWER_H
