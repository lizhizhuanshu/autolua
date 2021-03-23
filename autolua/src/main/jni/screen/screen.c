//
// Created by lizhi on 2021/3/3.
//

#include "luajava.h"
#include "Bitmap.h"
#include "lua.h"
#include "lauxlib.h"

#include "luaViewer.h"

#define SCREEN_CLASS_NAME "top/lizhistudio/autolua/extend/Screen"
#define DISPLAY_CLASS_NAME "top/lizhistudio/autolua/extend/Display"
#define BYTEBUFFER_CLASS_NAME "java/nio/ByteBuffer"

#define CLASS_ARG(className) "L" className ";"
#define ARGS(args) "(" args ")"
#define GLOBAL(env, obj) (*env)->NewGlobalRef(env,obj)
#define FreeGlobal(env,obj) (*env)->DeleteGlobalRef(env,obj)


static jclass ScreenClassID = NULL;
static jclass DisplayClassID = NULL;

static jmethodID getBaseWidthMethodID;
static jmethodID getBaseHeightMethodID;
static jmethodID getRotationMethodID;
static jmethodID getBaseDensityMethodID;
static jmethodID getBaseDirectionMethodID;
static jmethodID releaseDisplayMethodID;

static jmethodID resetMethodID;
static jmethodID isChangeDirectionMethodID;
static jmethodID getDisplayBufferMethodID ;
static jmethodID getHeightMethodID;
static jmethodID getRowStrideMethodID ;
static jmethodID getPixelStrideMethodID;
static jmethodID getWidthMethodID;
static jmethodID updateMethodID;

JNIEXPORT jint JNI_OnLoad(JavaVM * vm, void * reserved)
{
#define findGlobalClass(env,name) GLOBAL(env,JavaFindClass(env,name))
#define SET_JAVA_STATIC_METHOD(methodName,classID,result,...) methodName##MethodID = (*env)->GetStaticMethodID(env,classID,#methodName,ARGS(__VA_ARGS__)result)
#define SET_JAVA_METHOD(methodName,classID,result,...) methodName##MethodID = (*env)->GetMethodID(env,classID,#methodName,ARGS(__VA_ARGS__)result)

    JNIEnv * env = NULL;
    if ((*vm)->GetEnv(vm,(void**)&env, JNI_VERSION_1_6) != JNI_OK)
        return -1;
    ScreenClassID = findGlobalClass(env,SCREEN_CLASS_NAME);
    DisplayClassID = findGlobalClass(env,DISPLAY_CLASS_NAME);

    SET_JAVA_STATIC_METHOD(getBaseWidth,ScreenClassID,"I");
    SET_JAVA_STATIC_METHOD(getBaseHeight, ScreenClassID, "I");
    SET_JAVA_STATIC_METHOD(getBaseDirection,ScreenClassID,"I");
    SET_JAVA_STATIC_METHOD(getBaseDensity, ScreenClassID, "I");
    SET_JAVA_STATIC_METHOD(getRotation,ScreenClassID,"I");
    SET_JAVA_STATIC_METHOD(releaseDisplay,ScreenClassID,"V","J");

    SET_JAVA_METHOD(reset, DisplayClassID, "V", "II");
    SET_JAVA_METHOD(getDisplayBuffer, DisplayClassID, CLASS_ARG(BYTEBUFFER_CLASS_NAME));
    SET_JAVA_METHOD(getHeight, DisplayClassID, "I");
    SET_JAVA_METHOD(getWidth, DisplayClassID, "I");
    SET_JAVA_METHOD(getRowStride, DisplayClassID,"I");
    SET_JAVA_METHOD(getPixelStride, DisplayClassID, "I");
    SET_JAVA_METHOD(isChangeDirection, DisplayClassID, "Z");
    SET_JAVA_METHOD(update, DisplayClassID, "V");
    return  JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved){
    JNIEnv * env = NULL;
    if ((*vm)->GetEnv(vm,(void**)&env, JNI_VERSION_1_6) != JNI_OK)
        return ;
    FreeGlobal(env,ScreenClassID);
    FreeGlobal(env,DisplayClassID);
}

typedef struct Display{
    struct Bitmap buffer;
    jobject object;
}LDisplay;

static int getBaseWidthHeight(lua_State*L);
static int getRotation(lua_State*L);
static int getBaseDensity(lua_State*L);
static int getBaseDirection(lua_State*L);


static int getWidthHeight(lua_State*L);
static int reset(lua_State*L);
static int isChangeDirection(lua_State*L);
static int update(lua_State*L);
static int destroy(lua_State*L);



int getBaseWidthHeight(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    int width = (*env)->CallStaticIntMethod(env,ScreenClassID,getBaseWidthMethodID);
    int height = (*env)->CallStaticIntMethod(env,ScreenClassID,getBaseHeightMethodID);
    lua_pushinteger(L,width);
    lua_pushinteger(L,height);
    return 2;
}


#define ScreenMethod(name) int   name(lua_State*L) \
{\
    JNIEnv *env = GetJNIEnv(L);\
    int result =(*env)->CallStaticIntMethod(env,ScreenClassID,name##MethodID);\
    lua_pushinteger(L,result);\
    return 1;\
}

ScreenMethod(getRotation)
ScreenMethod(getBaseDensity)
ScreenMethod(getBaseDirection)


int getWidthHeight(lua_State*L)
{
    LDisplay * display = lua_touserdata(L,1);
    lua_pushinteger(L,display->buffer.width);
    lua_pushinteger(L,display->buffer.height);
    return 2;
}

int isChangeDirection(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    LDisplay * display = lua_touserdata(L,1);
    int result = (*env)->CallBooleanMethod(env,display->object,isChangeDirectionMethodID);
    lua_pushboolean(L,result);
    return 1;
}


static void syncDisplayInfo(lua_State*L, JNIEnv*env, jobject javaDisplay, LBitmap *bitmap)
{
#define CallIntMethod(env,method) (*env)->CallIntMethod(env,javaDisplay,method)
    bitmap->width = CallIntMethod(env,getWidthMethodID);
    bitmap->height = CallIntMethod(env,getHeightMethodID);
    bitmap->pixelStride = CallIntMethod(env,getPixelStrideMethodID);
    bitmap->rowShift = CallIntMethod(env,getRowStrideMethodID);
    jobject buffer = (*env)->CallObjectMethod(env,javaDisplay,getDisplayBufferMethodID);
    bitmap->origin = (unsigned char*)(*env)->GetDirectBufferAddress(env,buffer);
    FreeLocalObject(env, buffer);
}

int update(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    LDisplay * display = lua_touserdata(L,1);
    (*env)->CallVoidMethod(env,display->object,updateMethodID);
    syncDisplayInfo(L,env,display->object,&(display->buffer));
    return 0;
}

int initialize(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    LDisplay * display = lua_touserdata(L,1);
    jint width = luaL_checkinteger(L,2);
    jint height = luaL_checkinteger(L,3);
    (*env)->CallVoidMethod(env,display->object,resetMethodID,width,height);
    syncDisplayInfo(L, env, display->object, &(display->buffer));
    return 0;
}


int destroy(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    LDisplay * display = lua_touserdata(L,1);
    if (display->object)
    {
        (*env)->DeleteWeakGlobalRef(env,display->object);
        display->object = NULL;
        (*env)->CallStaticVoidMethod(env,ScreenClassID,releaseDisplayMethodID,(jlong)L);
    }
    return 0;
}

JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_extend_Screen_injectModel(JNIEnv *env, jclass clazz, jlong native_lua,
                                                       jobject display) {
#define ONE_METHOD(name) {#name,name}
    lua_State *L = (lua_State*)native_lua;
    LDisplay * cDisplay = lua_newuserdata(L,sizeof(LDisplay));
    cDisplay->object = (*env)->NewWeakGlobalRef(env,display);
    luaopen_viewer(L);
    luaL_Reg  method[] = {
            ONE_METHOD(getBaseWidthHeight),
            ONE_METHOD(getRotation),
            ONE_METHOD(getBaseDensity),
            ONE_METHOD(getBaseDirection),
            ONE_METHOD(getWidthHeight),
            ONE_METHOD(isChangeDirection),
            ONE_METHOD(initialize),
            ONE_METHOD(update),
            ONE_METHOD(destroy),
            {"__gc",destroy},
            {NULL,NULL}
    };
    luaL_setfuncs(L,method,0);
    lua_pushvalue(L,-1);
    lua_setfield(L,-2,"__index");
    lua_setmetatable(L,-2);
    lua_setglobal(L,"Screen");
}