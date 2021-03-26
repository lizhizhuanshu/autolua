//
// Created by lizhi on 2021/3/26.
//

#ifndef AUTOLUA_DISPLAY_H
#define AUTOLUA_DISPLAY_H
#include "jni.h"
#include "lua.h"
#ifdef __cplusplus
extern "C"
{
#endif

void onInitializeDisplayContext(JNIEnv*env);
void onReleaseDisplayContext(JNIEnv*env);
void pushDisplay(lua_State*L,JNIEnv*env,jobject display);
#ifdef __cplusplus
}
#endif
#endif //AUTOLUA_DISPLAY_H
