//
// Created by lizhi on 2021/3/3.
//

#include "luajava.h"
#include "Bitmap.h"
#include "lua.h"
#include "lauxlib.h"


#define SET_JAVA_METHOD(obj, methodName,classID,result,...) obj->methodName##MethodID = (*env)->GetMethodID(env,classID,#methodName,ARGS(__VA_ARGS__)result)
#define SET_JAVA_STATIC_METHOD(obj, methodName,classID,result,...) obj->methodName##MethodID = (*env)->GetStaticMethodID(env,classID,#methodName,ARGS(__VA_ARGS__)result)

#define SCREEN_CLASS_NAME "top/lizhistudio/autolua/extend/Screen"
#define DISPLAY_CLASS_NAME "top/lizhistudio/autolua/extend/Display"
#define BYTEBUFFER_CLASS_NAME "java/nio/ByteBuffer"

#define CLASS_ARG(className) "L" className ";"
#define ARGS(args) "(" args ")"


#if __ANDROID__
#define LUA_METHOD __attribute__((visibility("default")))
#else
#define LUA_METHOD __declspec(dllexport)
#endif // __ANDROID__

typedef struct ScreenClass{
    jlong id;
    jmethodID getWidthMethodID;
    jmethodID getHeightMethodID;
    jmethodID newDisplayMethodID;
    jmethodID getRotationMethodID;
    jmethodID getDensityMethodID;
    jmethodID getDirectionMethodID;
}LScreenClass;

typedef struct DisplayClass{
    jlong id;
    jmethodID resetMethodID;
    jmethodID checkDirectionMethodID;
    jmethodID getDisplayBufferMethodID ;
    jmethodID getHeightMethodID;
    jmethodID getRowStrideMethodID ;
    jmethodID getPixelStrideMethodID;
    jmethodID getWidthMethodID;
    jmethodID updateMethodID;
    jmethodID destroyMethodID;
}LDisplayClass;

typedef struct DisplayObject{
    struct Bitmap buffer;
    jlong id;
}LDisplayObject;


static int screen_getWidth(lua_State*L);
static int screen_getHeight(lua_State *L);
static int screen_getRotation(lua_State*L);
static int screen_getDensity(lua_State*L);
static int screen_getDirection(lua_State*L);
static int screen_newDisplay(lua_State*L);

static int displayReset(lua_State*L);
static int displayCheckDirection(lua_State*L);
static int displayGetWidthHeight(lua_State*L);
static int displayUpdate(lua_State*L);
static int displayGC(lua_State*L);
static int displayDestroy(lua_State*L);

static void setGcMetaTable(lua_State*L)
{
    static luaL_Reg methods[]={
            {"__gc",javaObjectDestroy},
            {NULL,NULL}
    };
    luaL_newlib(L,methods);
    lua_setmetatable(L,-2);
}


static void pushAndInitializeDisplayClass(lua_State*L)
{
    LDisplayClass * displayClass = lua_newuserdata(L,sizeof(LDisplayClass));
    const void *pID = lua_topointer(L,-1);
    displayClass->id = (jlong)pID;
    JNIEnv *env = GetJNIEnv(L);
    jclass aClass = JavaFindClass(env,DISPLAY_CLASS_NAME);
    CacheJavaObject(L,env,displayClass->id,aClass);
    SET_JAVA_METHOD(displayClass,reset, aClass, "V", "II");
    SET_JAVA_METHOD(displayClass,getDisplayBuffer, aClass, CLASS_ARG(BYTEBUFFER_CLASS_NAME));
    SET_JAVA_METHOD(displayClass,getHeight, aClass, "I");
    SET_JAVA_METHOD(displayClass,getWidth, aClass, "I");
    SET_JAVA_METHOD(displayClass,getRowStride, aClass,"I");
    SET_JAVA_METHOD(displayClass,getPixelStride, aClass, "I");
    SET_JAVA_METHOD(displayClass,checkDirection, aClass, "Z");
    SET_JAVA_METHOD(displayClass,update, aClass, "V");
    SET_JAVA_METHOD(displayClass,destroy,aClass,"V");
    FreeLocalObject(env,aClass);
    setGcMetaTable(L);
}

static void pushAndInitializeScreenClass(lua_State*L)
{
    LScreenClass * screenClass = lua_newuserdata(L,sizeof(LScreenClass));
    const void *pID = lua_topointer(L,-1);
    screenClass->id = (jlong)pID;
    JNIEnv *env = GetJNIEnv(L);
    jclass aClass = JavaFindClass(env,SCREEN_CLASS_NAME);
    CacheJavaObject(L,env,(jlong)pID,aClass);
    SET_JAVA_STATIC_METHOD(screenClass,getWidth,aClass,"I");
    SET_JAVA_STATIC_METHOD(screenClass,getHeight, aClass, "I");
    SET_JAVA_STATIC_METHOD(screenClass,getDirection,aClass,"I");
    SET_JAVA_STATIC_METHOD(screenClass,getDensity, aClass, "I");
    SET_JAVA_STATIC_METHOD(screenClass,getRotation,aClass,"I");
    SET_JAVA_STATIC_METHOD(screenClass,newDisplay,aClass,CLASS_ARG(DISPLAY_CLASS_NAME),"II");
    FreeLocalObject(env,aClass);
    setGcMetaTable(L);
}


LUA_METHOD int luaopen_screen(lua_State*L)
{
    if (luaL_newmetatable(L,DISPLAY_CLASS_NAME))
    {
        static luaL_Reg methods[]={
                {"reset",displayReset},
                {"checkDirection",displayCheckDirection},
                {"getWidthHeight",displayGetWidthHeight},
                {"update",displayUpdate},
                {"destroy",displayDestroy},
                {"__gc",displayGC},
                {NULL,NULL}
        };
        luaL_setfuncs(L,methods,0);
        lua_pushvalue(L,-1);
        lua_setfield(L,-2,"__index");
    }
    lua_pop(L,1);

    pushAndInitializeScreenClass(L);
    static luaL_Reg methods[]={
            {"getWidth",screen_getWidth},
            {"getHeight",screen_getHeight},
            {"getRotation",screen_getRotation},
            {"getDirection",screen_getDirection},
            {"getDensity",screen_getDensity},
            {NULL,NULL}
    };
    lua_createtable(L,0,6);
    lua_pushvalue(L,-2);
    luaL_setfuncs(L,methods,1);
    lua_pushvalue(L,-2);
    pushAndInitializeDisplayClass(L);
    lua_pushcclosure(L,screen_newDisplay,2);
    lua_setfield(L,-2,"newDisplay");
    return 1;
}


#define ScreenMethod(name) int screen_##name(lua_State*L) \
{\
    LScreenClass * screen = lua_touserdata(L,lua_upvalueindex(1));\
    JNIEnv *env = GetJNIEnv(L);\
    jobject obj = GetJavaObject(L,env,screen->id);\
    int result =(*env)->CallStaticIntMethod(env,obj,screen->name##MethodID);\
    FreeLocalObject(env,obj);\
    lua_pushinteger(L,result);\
    return 1;\
}

ScreenMethod(getWidth)
ScreenMethod(getHeight)
ScreenMethod(getRotation)
ScreenMethod(getDensity)
ScreenMethod(getDirection)

static void syncDisplayInfo(lua_State*L, JNIEnv*env, jobject javaDisplay, LBitmap *bitmap, LDisplayClass* displayClass)
{
#define CallIntMethod(env,method) (*env)->CallIntMethod(env,javaDisplay,method)
    bitmap->width = CallIntMethod(env,displayClass->getWidthMethodID);
    bitmap->height = CallIntMethod(env,displayClass->getHeightMethodID);
    bitmap->pixelStride = CallIntMethod(env,displayClass->getPixelStrideMethodID);
    bitmap->rowShift = CallIntMethod(env,displayClass->getRowStrideMethodID);
    jobject buffer = (*env)->CallObjectMethod(env,javaDisplay,displayClass->getDisplayBufferMethodID);
    bitmap->origin = (unsigned char*)(*env)->GetDirectBufferAddress(env,buffer);
    FreeLocalObject(env, buffer);
}


int screen_newDisplay(lua_State*L)
{
    jint width = luaL_checkinteger(L,1);
    jint height = luaL_checkinteger(L,2);
    LScreenClass * screen = lua_touserdata(L,lua_upvalueindex(1));
    LDisplayClass * displayClass = lua_touserdata(L,lua_upvalueindex(2));
    JNIEnv *env = GetJNIEnv(L);
    jobject javaScreen = GetJavaObject(L,env,screen->id);
    jobject javaDisplay = (*env)->CallStaticObjectMethod(env,javaScreen,screen->newDisplayMethodID,width,height);
    FreeLocalObject(env,javaScreen);
    LDisplayObject * display= lua_newuserdata(L,sizeof(LDisplayObject));
    display->id = (jlong)lua_topointer(L,-1);
    CacheJavaObject(L,env,display->id,javaDisplay);
    syncDisplayInfo(L, env, javaDisplay, &(display->buffer), displayClass);
    FreeLocalObject( env,javaDisplay);
    lua_pushvalue(L,lua_upvalueindex(2));
    lua_setuservalue(L,-2);
    luaL_setmetatable(L,DISPLAY_CLASS_NAME);
    return 1;
}



int displayUpdate(lua_State*L)
{
    LDisplayObject  * display = lua_touserdata(L,1);
    lua_getuservalue(L,1);
    LDisplayClass * displayClass = lua_touserdata(L,-1);
    JNIEnv *env = GetJNIEnv(L);
    jobject javaDisplay = GetJavaObject(L,env,display->id);
    LBitmap *bitmap = &(display->buffer);
    (*env)->CallVoidMethod(env,javaDisplay,displayClass->updateMethodID);
    syncDisplayInfo(L, env, javaDisplay, bitmap, displayClass);
    FreeLocalObject(env,javaDisplay);
    return 0;
}

int displayReset(lua_State*L)
{
    LDisplayObject  * display = lua_touserdata(L,1);
    jint width = luaL_checkinteger(L,2);
    jint height = luaL_checkinteger(L,3);
    lua_getuservalue(L,1);
    LDisplayClass * displayClass = lua_touserdata(L,-1);
    JNIEnv *env = GetJNIEnv(L);
    jobject javaDisplay = GetJavaObject(L,env,display->id);
    (*env)->CallVoidMethod(env,javaDisplay,displayClass->resetMethodID,width,height);
    syncDisplayInfo(L, env, javaDisplay, &(display->buffer), displayClass);
    FreeLocalObject(env,javaDisplay);
    return 0;
}




static int displayCheckDirection(lua_State*L)
{
    LDisplayObject  * display = lua_touserdata(L,1);
    lua_getuservalue(L,1);
    LDisplayClass * displayClass = lua_touserdata(L,-1);
    JNIEnv *env = GetJNIEnv(L);
    jobject javaDisplay = GetJavaObject(L,env,display->id);
    jboolean r = (*env)->CallBooleanMethod(env,javaDisplay,displayClass->checkDirectionMethodID);
    FreeLocalObject(env,javaDisplay);
    lua_pushboolean(L,r);
    return 1;
}

static int displayGetWidthHeight(lua_State*L)
{
    LDisplayObject  * display = lua_touserdata(L,1);
    LBitmap *bitmap = &(display->buffer);
    lua_pushinteger(L,bitmap->width);
    lua_pushinteger(L,bitmap->height);
    return 2;
}

static int displayGC(lua_State*L)
{
    LDisplayObject  * display = lua_touserdata(L,1);
    JNIEnv *env = GetJNIEnv(L);
    ReleaseJavaObject(L,env,display->id);
    return 0;
}

static int displayDestroy(lua_State*L)
{
    LDisplayObject  * display = lua_touserdata(L,1);
    lua_getuservalue(L,1);
    LDisplayClass * displayClass = lua_touserdata(L,-1);
    JNIEnv *env = GetJNIEnv(L);
    jobject javaDisplay = GetJavaObject(L,env,display->id);
    (*env)->CallVoidMethod(env,javaDisplay,displayClass->destroyMethodID);
    FreeLocalObject(env,javaDisplay);
    return 0;
}
