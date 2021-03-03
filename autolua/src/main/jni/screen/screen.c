//
// Created by lizhi on 2021/3/3.
//

#include "luajava.h"
#include "Bitmap.h"
#include "lua.h"
#include "lauxlib.h"


#define SET_JAVA_METHOD(methodName,classID,result,...) methodName##MethodID = (*env)->GetMethodID(env,classID,#methodName,ARGS(__VA_ARGS__)result)
#define SCREEN_CLASS_NAME "top/lizhistudio/autolua/extend/Screen"
#define BYTEBUFFER_CLASS_NAME "java/nio/ByteBuffer"
#define GLOBAL(env, obj) (*env)->NewGlobalRef(env,obj)
#define FreeLocal(env,obj)  (*env)->DeleteLocalRef(env,obj)
#define JavaFindClass(env,str) (*env)->FindClass(env,str)
#define FreeGlobal(env,obj) (*env)->DeleteGlobalRef(env,obj)
#define CLASS_ARG(className) "L" className ";"
#define ARGS(args) "(" args ")"
#define CallIntMethod(env,method) (*env)->CallIntMethod(env,ObjectID,method)

#if __ANDROID__
#define LUA_METHOD __attribute__((visibility("default")))
#else
#define LUA_METHOD __declspec(dllexport)
#endif // __ANDROID__

jclass javaClassID = NULL;
jobject ObjectID = NULL;
jmethodID initializeMethodID = NULL;
jmethodID checkDirectionMethodID = NULL;
jmethodID getDisplayBufferMethodID = NULL;
jmethodID getHeightMethodID = NULL;
jmethodID getRowStrideMethodID = NULL;
jmethodID getPixelStrideMethodID = NULL;
jmethodID getWidthMethodID = NULL;
jmethodID updateMethodID = NULL;

JNIEXPORT jint JNI_OnLoad(JavaVM * vm, void * reserved)
{
#define findGlobalClass(env,name) GLOBAL(env,JavaFindClass(env,name))
    JNIEnv * env = NULL;
    if ((*vm)->GetEnv(vm,(void**)&env, JNI_VERSION_1_6) != JNI_OK)
        return -1;
    javaClassID = findGlobalClass(env,SCREEN_CLASS_NAME);
    jmethodID getDefaultMethodID = (*env)->GetStaticMethodID(env,javaClassID, "getDefault", "()" CLASS_ARG(SCREEN_CLASS_NAME));
    jobject  obj = (*env)->CallObjectMethod(env,javaClassID,getDefaultMethodID);
    ObjectID = GLOBAL(env,obj);
    FreeLocal(env,obj);
    SET_JAVA_METHOD(initialize, javaClassID, "V", "II");
    SET_JAVA_METHOD(getDisplayBuffer, javaClassID, CLASS_ARG(BYTEBUFFER_CLASS_NAME));
    SET_JAVA_METHOD(getHeight, javaClassID, "I");
    SET_JAVA_METHOD(getWidth, javaClassID, "I");
    SET_JAVA_METHOD(getRowStride, javaClassID, "I");
    SET_JAVA_METHOD(getPixelStride, javaClassID, "I");
    SET_JAVA_METHOD(checkDirection, javaClassID, "Z");
    SET_JAVA_METHOD(update, javaClassID, "V");
    return  JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved){
    JNIEnv * env = NULL;
    if ((*vm)->GetEnv(vm,(void**)&env, JNI_VERSION_1_6) != JNI_OK)
        return ;
    FreeGlobal(env,javaClassID);
}



static int checkDirection(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    jboolean r = (*env)->CallBooleanMethod(env,ObjectID,checkDirectionMethodID);
    lua_pushboolean(L,r);
    return 1;
}

static int getWidthHeight(lua_State*L)
{
    LBitmap * bitmap = lua_touserdata(L,1);
    lua_pushinteger(L,bitmap->width);
    lua_pushinteger(L,bitmap->height);
    return 2;
}

static int update(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    (*env)->CallVoidMethod(env,ObjectID,updateMethodID);
    LBitmap * bitmap = lua_touserdata(L,1);
    bitmap->width = CallIntMethod(env,getWidthMethodID);
    bitmap->height = CallIntMethod(env,getHeightMethodID);
    bitmap->pixelStride = CallIntMethod(env,getPixelStrideMethodID);
    bitmap->rowShift = CallIntMethod(env,getRowStrideMethodID);
    jobject buffer = (*env)->CallObjectMethod(env,ObjectID,getDisplayBufferMethodID);
    bitmap->origin = (unsigned char*)(*env)->GetDirectBufferAddress(env,buffer);
    FreeLocal(env,buffer);
    return 0;
}


static int initialize(lua_State*L)
{
    jint width = lua_tointeger(L,2);
    jint height = lua_tointeger(L,3);
    JNIEnv *env = GetJNIEnv(L);
    (*env)->CallVoidMethod(env,ObjectID,initializeMethodID,width,height);
    update(L);
    return 0;
}


LUA_METHOD int luaopen_screen(lua_State*L)
{
    lua_newuserdata(L,sizeof(LBitmap));
    luaL_Reg methods[] = {
            {"initialize",initialize},
            {"getWidthHeight",getWidthHeight},
            {"checkDirection",checkDirection},
            {"update",update},
            {NULL,NULL}
    };
    luaL_newlib(L,methods);
    lua_pushvalue(L,-1);
    lua_setfield(L,-2,"__index");
    lua_setmetatable(L,-2);
    return 1;
}
