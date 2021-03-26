//
// Created by lizhi on 2021/3/3.
//

#include "luajava.h"
#include "Bitmap.h"
#include "lauxlib.h"
#include "display.h"
#include "luaViewer.h"


#define DISPLAY_CLASS_NAME "top/lizhistudio/autolua/extend/Display"
#define BYTEBUFFER_CLASS_NAME "java/nio/ByteBuffer"

#define CLASS_ARG(className) "L" className ";"
#define ARGS(args) "(" args ")"
#define GLOBAL(env, obj) (*env)->NewGlobalRef(env,obj)
#define FreeGlobal(env,obj) (*env)->DeleteGlobalRef(env,obj)


typedef struct Display{
    struct Bitmap buffer;
    jobject object;
}LDisplay;


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


void onInitializeDisplayContext(JNIEnv*env)
{
#define findGlobalClass(env,name) GLOBAL(env,JavaFindClass(env,name))
#define SET_JAVA_STATIC_METHOD(methodName,classID,result,...) methodName##MethodID = (*env)->GetStaticMethodID(env,classID,#methodName,ARGS(__VA_ARGS__)result)
#define SET_JAVA_METHOD(methodName,classID,result,...) methodName##MethodID = (*env)->GetMethodID(env,classID,#methodName,ARGS(__VA_ARGS__)result)

    DisplayClassID = findGlobalClass(env,DISPLAY_CLASS_NAME);

    SET_JAVA_STATIC_METHOD(getBaseWidth,DisplayClassID,"I");
    SET_JAVA_STATIC_METHOD(getBaseHeight, DisplayClassID, "I");
    SET_JAVA_STATIC_METHOD(getBaseDirection,DisplayClassID,"I");
    SET_JAVA_STATIC_METHOD(getBaseDensity, DisplayClassID, "I");
    SET_JAVA_STATIC_METHOD(getRotation,DisplayClassID,"I");
    SET_JAVA_STATIC_METHOD(releaseDisplay,DisplayClassID,"V","J");

    SET_JAVA_METHOD(reset, DisplayClassID, "V", "II");
    SET_JAVA_METHOD(getDisplayBuffer, DisplayClassID, CLASS_ARG(BYTEBUFFER_CLASS_NAME));
    SET_JAVA_METHOD(getHeight, DisplayClassID, "I");
    SET_JAVA_METHOD(getWidth, DisplayClassID, "I");
    SET_JAVA_METHOD(getRowStride, DisplayClassID,"I");
    SET_JAVA_METHOD(getPixelStride, DisplayClassID, "I");
    SET_JAVA_METHOD(isChangeDirection, DisplayClassID, "Z");
    SET_JAVA_METHOD(update, DisplayClassID, "V");
}

void onReleaseDisplayContext(JNIEnv*env)
{
    FreeGlobal(env,DisplayClassID);
}




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
    int width = (*env)->CallStaticIntMethod(env,DisplayClassID,getBaseWidthMethodID);
    int height = (*env)->CallStaticIntMethod(env,DisplayClassID,getBaseHeightMethodID);
    lua_pushinteger(L,width);
    lua_pushinteger(L,height);
    return 2;
}


#define StaticMethod(name) int   name(lua_State*L) \
{\
    JNIEnv *env = GetJNIEnv(L);\
    int result =(*env)->CallStaticIntMethod(env,DisplayClassID,name##MethodID);\
    lua_pushinteger(L,result);\
    return 1;\
}

StaticMethod(getRotation)
StaticMethod(getBaseDensity)
StaticMethod(getBaseDirection)


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
        (*env)->CallStaticVoidMethod(env,DisplayClassID,releaseDisplayMethodID,(jlong)L);
    }
    return 0;
}


void pushDisplay(lua_State*L,JNIEnv*env,jobject object)
{
#define ONE_METHOD(name) {#name,name}
    LDisplay * cDisplay = lua_newuserdata(L,sizeof(LDisplay));
    cDisplay->object = (*env)->NewWeakGlobalRef(env,object);
    pushViewerMethodTable(L);
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
}
