//
// Created by lizhi on 2021/6/23.
//
#include "display.h"
#include "Bitmap.h"

#define DISPLAY_CLASS_NAME "top/lizhistudio/autolua/extend/Display"
#define BYTEBUFFER_CLASS_NAME "java/nio/ByteBuffer"

#define CLASS_ARG(className) "L" className ";"
#define ARGS(args) "(" args ")"
#define GLOBAL(env, obj) env->NewGlobalRef(obj)
#define FreeGlobal(env,obj) env->DeleteGlobalRef(obj)

struct Display{
    Bitmap buffer;
    jobject object;
};

static jclass DisplayClassID = nullptr;

static jmethodID getBaseWidthMethodID;
static jmethodID getBaseHeightMethodID;
static jmethodID getRotationMethodID;
static jmethodID getBaseDensityMethodID;
static jmethodID getBaseDirectionMethodID;
static jmethodID getDefaultMethodID;

static jmethodID initializeMethodID;
static jmethodID isChangeDirectionMethodID;
static jmethodID getDisplayBufferMethodID ;
static jmethodID getHeightMethodID;
static jmethodID getRowStrideMethodID ;
static jmethodID getPixelStrideMethodID;
static jmethodID getWidthMethodID;
static jmethodID updateMethodID;


void onInitializeDisplayContext(JNIEnv*env)
{
#define findGlobalClass(env,name) (jclass)GLOBAL(env,JavaFindClass(env,name))
#define SET_JAVA_STATIC_METHOD(methodName,classID,result,...) methodName##MethodID = env->GetStaticMethodID(classID,#methodName,ARGS(__VA_ARGS__)result)
#define SET_JAVA_METHOD(methodName,classID,result,...) methodName##MethodID = env->GetMethodID(classID,#methodName,ARGS(__VA_ARGS__)result)

    DisplayClassID = findGlobalClass(env,DISPLAY_CLASS_NAME);

    SET_JAVA_STATIC_METHOD(getBaseWidth,DisplayClassID,"I");
    SET_JAVA_STATIC_METHOD(getBaseHeight, DisplayClassID, "I");
    SET_JAVA_STATIC_METHOD(getBaseDirection,DisplayClassID,"I");
    SET_JAVA_STATIC_METHOD(getBaseDensity, DisplayClassID, "I");
    SET_JAVA_STATIC_METHOD(getRotation,DisplayClassID,"I");
    SET_JAVA_STATIC_METHOD(getDefault,DisplayClassID,CLASS_ARG(DISPLAY_CLASS_NAME));

    SET_JAVA_METHOD(initialize, DisplayClassID, "V", "II");
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
static int initialize(lua_State*L);
static int isChangeDirection(lua_State*L);
static int destroy(lua_State*L);
static int update(lua_State*L);

int luaopen_display(lua_State*L)
{
#define ONE_METHOD(name) {#name,name}
    auto *display = (Display*)lua_newuserdata(L,sizeof(Display));
    JNIEnv*env = GetJNIEnv(L);
    jobject obj = env->CallStaticObjectMethod(DisplayClassID,getDefaultMethodID);
    display->object = env->NewWeakGlobalRef(obj);
    FreeLocalObject(env,obj);
    luaL_Reg  method[] = {
            ONE_METHOD(getBaseWidthHeight),
            ONE_METHOD(getRotation),
            ONE_METHOD(getBaseDensity),
            ONE_METHOD(getBaseDirection),
            ONE_METHOD(getWidthHeight),
            ONE_METHOD(isChangeDirection),
            ONE_METHOD(initialize),
            ONE_METHOD(destroy),
            ONE_METHOD(update),
            {"__gc",destroy},
            {nullptr,nullptr}
    };
    luaL_newlib(L,method);
    lua_pushvalue(L,-1);
    lua_setfield(L,-2,"__index");
    lua_setmetatable(L,-2);
    return 1;
}



int getBaseWidthHeight(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    int width = env->CallStaticIntMethod(DisplayClassID,getBaseWidthMethodID);
    int height = env->CallStaticIntMethod(DisplayClassID,getBaseHeightMethodID);
    lua_pushinteger(L,width);
    lua_pushinteger(L,height);
    return 2;
}


#define StaticMethod(name) int   name(lua_State*L) \
{\
    JNIEnv *env = GetJNIEnv(L);\
    int result =env->CallStaticIntMethod(DisplayClassID,name##MethodID);\
    lua_pushinteger(L,result);\
    return 1;\
}

StaticMethod(getRotation)
StaticMethod(getBaseDensity)
StaticMethod(getBaseDirection)

int getWidthHeight(lua_State*L)
{
    auto * display = (Display*)lua_touserdata(L,1);
    lua_pushinteger(L,display->buffer.width);
    lua_pushinteger(L,display->buffer.height);
    return 2;
}

int isChangeDirection(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    auto * display = (Display*)lua_touserdata(L,1);
    int result = env->CallBooleanMethod(display->object,isChangeDirectionMethodID);
    lua_pushboolean(L,result);
    return 1;
}

static void syncDisplayInfo(lua_State*L, JNIEnv*env, jobject javaDisplay, Bitmap *bitmap)
{
#define CallIntMethod(env,method) env->CallIntMethod(javaDisplay,method)
    bitmap->width = CallIntMethod(env,getWidthMethodID);
    bitmap->height = CallIntMethod(env,getHeightMethodID);
    bitmap->pixelStride = CallIntMethod(env,getPixelStrideMethodID);
    bitmap->rowShift = CallIntMethod(env,getRowStrideMethodID);
    jobject buffer = env->CallObjectMethod(javaDisplay,getDisplayBufferMethodID);
    bitmap->origin = (unsigned char*)env->GetDirectBufferAddress(buffer);
    FreeLocalObject(env, buffer);
}

int update(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    auto * display = (Display*)lua_touserdata(L,1);
    env->CallVoidMethod(display->object,updateMethodID);
    syncDisplayInfo(L,env,display->object,&(display->buffer));
    return 0;
}

int initialize(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    auto * display = (Display*)lua_touserdata(L,1);
    jint width = luaL_checkinteger(L,2);
    jint height = luaL_checkinteger(L,3);
    env->CallVoidMethod(display->object,initializeMethodID,width,height);
    syncDisplayInfo(L, env, display->object, &(display->buffer));
    return 0;
}


int destroy(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    auto * display = (Display*)lua_touserdata(L,1);
    if (display->object)
    {
        env->DeleteWeakGlobalRef(display->object);
        display->object = nullptr;
    }
    return 0;
}

