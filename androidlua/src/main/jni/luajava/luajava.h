//
// Created by lizhi on 2021/3/2.
//

#ifndef AUTOLUA_LUAJAVA_H
#define AUTOLUA_LUAJAVA_H

#include <jni.h>
#include "lua.h"
#define JavaFindClass(env,str) (*env)->FindClass(env,str)
#define FreeLocalObject(env,obj)  (*env)->DeleteLocalRef(env,obj)
JNIEnv* GetJNIEnv(lua_State*L);
void CacheJavaObject(lua_State*L, JNIEnv*env, jlong id, jobject object);
jobject GetJavaObject(lua_State*L,JNIEnv*env,jlong id);
void ReleaseJavaObject(lua_State*L, JNIEnv*env, jlong id);
int javaObjectDestroy(lua_State*L);
#endif //AUTOLUA_LUAJAVA_H
