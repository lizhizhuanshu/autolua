//
// Created by lizhi on 2021/6/23.
//

#ifndef AUTOLUA2_LUAJNI_H
#define AUTOLUA2_LUAJNI_H
#include<jni.h>
#include "lua.hpp"

#define JavaFindClass(env,str) env->FindClass(str)
#define FreeLocalObject(env,obj)  env->DeleteLocalRef(obj)
#define GetAutoLua(L) (*((jobject*)lua_getextraspace(L)))
#define SetAutoLua(L,p) (*((jobject*)lua_getextraspace(L)) = p)
#define toLuaState(L) ((lua_State*)L)

JNIEnv *GetJNIEnv(lua_State*L);
#endif //AUTOLUA2_LUAJNI_H
