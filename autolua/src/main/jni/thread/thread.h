//
// Created by lizhi on 2021/3/26.
//

#ifndef AUTOLUA_THREAD_H
#define AUTOLUA_THREAD_H
#include "lua.h"
#include "jni.h"

void onInitializeThreadContext(JNIEnv*env);
void onReleaseThreadContext(JNIEnv*env);
void pushThreadMethodTable(lua_State*L);
#endif //AUTOLUA_THREAD_H
