//
// Created by lizhi on 2021/3/2.
//

#ifndef AUTOLUA_LUAJAVA_H
#define AUTOLUA_LUAJAVA_H

#include <jni.h>
#include "lua.h"

typedef struct {
    JNIEnv *env;
    jobject context;
}LuaExtension;

#define toLuaState(L) ((lua_State*)L)
#define GetLuaExtension(L) (*((LuaExtension**)lua_getextraspace(L)))
#define SetLuaExtension(L,p) (*((LuaExtension**)lua_getextraspace(L)) = p)
#define SetJNIEnv(L,p) ((*((LuaExtension**)lua_getextraspace(L)))->env = p)
#define GetJNIEnv(L) (GetLuaExtension(L)->env)

#endif //AUTOLUA_LUAJAVA_H
