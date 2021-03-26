//
// Created by lizhi on 2021/3/26.
//

#include "display.h"
#include "luaViewer.h"
#include "thread.h"

JNIEXPORT jint JNI_OnLoad(JavaVM * vm, void * reserved)
{
    #define findGlobalClass(env,name) GLOBAL(env,JavaFindClass(env,name))
    JNIEnv * env = NULL;
    if ((*vm)->GetEnv(vm,(void**)&env, JNI_VERSION_1_6) != JNI_OK)
        return -1;
    onInitializeDisplayContext(env);
    onInitializeThreadContext(env);
    return  JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved){
    JNIEnv * env = NULL;
    if ((*vm)->GetEnv(vm,(void**)&env, JNI_VERSION_1_6) != JNI_OK)
        return ;
    onReleaseDisplayContext(env);
    onReleaseThreadContext(env);
}

JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_extend_Core_inject(JNIEnv *env, jclass clazz, jlong native_lua,
                                                jobject display) {
    lua_State *L = (lua_State*) native_lua;
    pushDisplay(L,env,display);
    lua_setglobal(L,"Display");
    pushDotMatrixConstructor(L);
    lua_setglobal(L,"DotMatrix");
    pushThreadMethodTable(L);
    lua_setglobal(L,"Thread");
}
