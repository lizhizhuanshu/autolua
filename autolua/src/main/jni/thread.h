//
// Created by lizhi on 2021/3/26.
//

#ifndef AUTOLUA2_THREAD_H
#define AUTOLUA2_THREAD_H
#include "autolua.h"
void onInitializeThreadContext(JNIEnv*env);
void onReleaseThreadContext(JNIEnv*env);
int luaopen_thread(lua_State*L);
#endif //AUTOLUA2_THREAD_H
