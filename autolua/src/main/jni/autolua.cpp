// Created by lizhi on 2021/2/21.
//
#include "lua/lua.hpp"
#include <cstdlib>
#include <jni.h>
#include "display/display.h"
#include "view/luaview.h"
#include "thread/thread.h"
#include "input/input.h"

#define PACKAGE_NAME "top/lizhistudio/autolua"
#define ANDROID_LUA_PACKAGE_NAME "top/lizhistudio/androidlua"
#define LUA_CONTEXT_CLASS_NAME PACKAGE_NAME "/core/LuaContextImplement"
#define LUA_HANDLER_CLASS_NAME ANDROID_LUA_PACKAGE_NAME "/LuaHandler"
#define LUA_OBJECT_ADAPTER_CLASS_NAME ANDROID_LUA_PACKAGE_NAME "/LuaObjectAdapter"
#define PUSH_THROWABLE_ERROR "push java throwable error"


#define LUA_OBJECT_ADAPTER_NAME "LuaObjectAdapter"
#define LUA_HANDLER_NAME "LuaHandler"
#define GLOBAL(env, obj) env->NewGlobalRef(obj)
#define FreeGlobal(env,obj) env->DeleteGlobalRef(obj)
#define toLuaState(L) ((lua_State*)L)
#define GetLuaExtension(L) (*((LuaExtension**)lua_getextraspace(L)))
#define SetLuaExtension(L,p) (*((LuaExtension**)lua_getextraspace(L)) = p)
#define GetJavaLuaContext(L) ((*((LuaExtension**)lua_getextraspace(L)))->context)

typedef struct {
    jobject context;
}LuaExtension;

class LocalJavaString{
    JNIEnv*env;
    jstring javaString;
    const char* cString;
    jsize stringSize;
public:
    LocalJavaString(JNIEnv*env,jstring javaString)
    : env(env), javaString(javaString), cString(nullptr), stringSize(0)
    {
        if (javaString)
        {
            jboolean isCopy = 0;
            cString = env->GetStringUTFChars(javaString,&isCopy);
            stringSize=env->GetStringUTFLength(javaString);
        }
    }

    ~LocalJavaString(){
        if (javaString)
        {
            env->ReleaseStringUTFChars(javaString,cString);
            env->DeleteLocalRef(javaString);
        }
    }

    const char*str(){
        return cString;
    }

    jsize size() const{
        return stringSize;
    }
};

class LocalJavaBytes{
    JNIEnv*env;
    jbyteArray javaBytes;
    const char* cBytes;
    jsize bytesSize;
public:
    LocalJavaBytes(JNIEnv*env,jbyteArray javaBytes)
    : env(env), javaBytes(javaBytes),cBytes(nullptr), bytesSize(0)
    {
        if (javaBytes)
        {
            jboolean isCopy = 0;
            cBytes = (char*)env->GetByteArrayElements(javaBytes,&isCopy);
            bytesSize = env->GetArrayLength(javaBytes);
        }
    }

    ~LocalJavaBytes(){
        if (javaBytes)
        {
            env->ReleaseByteArrayElements(javaBytes,(jbyte*)cBytes,0);
            env->DeleteLocalRef(javaBytes);
        }
    }

    const char*str(){
        return cBytes;
    }

    jsize size() const{
        return bytesSize;
    }
};


template<class T>
class LocalReference
{
    JNIEnv*env;
    T o;
public:
    LocalReference(JNIEnv*env,T v)
    :env(env),o(v)
    {
    }
    ~LocalReference(){
        if (env && o)
        {
            env->DeleteLocalRef((jobject)o);
        }
    }
    T get(){
        return o;
    }
};




static JavaVM *GlobalJavaVm = nullptr;


static jclass LuaContextClass = nullptr;
static jclass LuaHandlerClass = nullptr;
static jclass LuaObjectAdapterClass = nullptr;


static jmethodID GetJavaObjectMethodID = nullptr;
static jmethodID CacheJavaObjectMethodID = nullptr;
static jmethodID RemoveJavaObjectMethodID = nullptr;

static jmethodID LuaHandlerCallID = nullptr;
static jmethodID LuaObjectAdapterHasMethodMethodID = nullptr;
static jmethodID LuaObjectAdapterCallMethodID = nullptr;

JNIEnv *GetJNIEnv(lua_State*L)
{
    JNIEnv *env;
    if (GlobalJavaVm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        //TODO 没想好非java线程该怎么样处理，所以此处直接报错
        luaL_error(L,"now thread is not java thread");
    }
    return env;
}


extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM * vm, void * reserved)
{
#define findGlobalClass(env,name) (jclass)GLOBAL(env,env->FindClass(name))
    GlobalJavaVm = vm;
    JNIEnv * env = nullptr;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK)
        return -1;
    LuaHandlerClass = findGlobalClass(env,LUA_HANDLER_CLASS_NAME);
    LuaContextClass = findGlobalClass(env,LUA_CONTEXT_CLASS_NAME);
    LuaObjectAdapterClass = findGlobalClass(env, LUA_OBJECT_ADAPTER_CLASS_NAME);

    LuaHandlerCallID = env->GetMethodID(LuaHandlerClass,"onExecute",
                                        "(Ltop/lizhistudio/androidlua/LuaContext;)I");

    LuaObjectAdapterHasMethodMethodID = env->GetMethodID(LuaObjectAdapterClass,
            "hasMethod", "(Ljava/lang/String;)Z");
    LuaObjectAdapterCallMethodID = env->GetMethodID(LuaObjectAdapterClass,
            "call", "(Ljava/lang/String;Ltop/lizhistudio/androidlua/LuaContext;)I");

    GetJavaObjectMethodID = env->GetMethodID(LuaContextClass, "getJavaObject",
                                                "(J)Ljava/lang/Object;");
    CacheJavaObjectMethodID = env->GetMethodID(LuaContextClass, "cacheJavaObject",
                                                  "(JLjava/lang/Object;)V");
    RemoveJavaObjectMethodID = env->GetMethodID(LuaContextClass, "removeJavaObject", "(J)V");

    onInitializeDisplayContext(env);
    onInitializeThreadContext(env);
    return  JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved){
    JNIEnv * env = nullptr;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK)
        return ;
    FreeGlobal(env,LuaContextClass);
    FreeGlobal(env, LuaObjectAdapterClass);

    onReleaseDisplayContext(env);
    onReleaseThreadContext(env);
}


static void throwTypeError(JNIEnv *env, lua_State*L,int index,int exception)
{
    char buffer[1024];
    const char* exceptionName = lua_typename(L,exception);
    const char* nowName = lua_typename(L,lua_type(L,index));
    sprintf(buffer,"luaState %p  index %d exception type %s now type %s",L,index,exceptionName,nowName);
    LocalReference<jclass> clazz(env,env->FindClass("top/lizhistudio/androidlua/exception/LuaTypeError"));
    env->ThrowNew(clazz.get(),buffer);
}

static int pushJavaThrowable(JNIEnv* env,lua_State*L,jthrowable throwable)
{
    LocalReference<jclass> clazz(env,env->FindClass("java/lang/Throwable"));
    jmethodID method = env->GetMethodID(clazz.get(),"getMessage", "()Ljava/lang/String;");
    auto message = (jstring)env->CallObjectMethod(throwable,method);
    if (!env->ExceptionCheck())
    {
        LocalJavaString cMessage(env, message);
        lua_pushlstring(L, cMessage.str(), cMessage.size());
        return 1;
    }
    env->ExceptionDescribe();
    env->ExceptionClear();
    return 0;
}

static int catchAndPushJavaThrowable(JNIEnv *env, lua_State*L)
{
    LocalReference<jthrowable> throwable(env,env->ExceptionOccurred());
    if (throwable.get() != nullptr)
    {
        env->ExceptionDescribe();
        env->ExceptionClear();
        if (!pushJavaThrowable(env,L,throwable.get()))
            lua_pushstring(L,PUSH_THROWABLE_ERROR);
        return 1;
    }
    return 0;
}



extern "C" JNIEXPORT jlong JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_toInteger(JNIEnv *env, jclass clazz, jlong native_lua,
                                                                jint index) {
    lua_State *L =toLuaState(native_lua);
    if (lua_isnumber(L,index))
    {
        return (jlong)lua_tointeger(L,index);
    } else{
        throwTypeError(env,L,index,LUA_TNUMBER);
    }
    return 0;
}

extern "C" JNIEXPORT jdouble JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_toNumber(JNIEnv *env, jclass clazz, jlong native_lua,
                                                               jint index) {
    lua_State *L =toLuaState(native_lua);
    if (lua_isnumber(L,index))
    {
        return (jdouble)lua_tonumber(L,index);
    }else{
        throwTypeError(env,L,index,LUA_TNUMBER);
    }
    return 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_toBoolean(JNIEnv *env, jclass clazz, jlong native_lua,
                                                                jint index) {
    lua_State *L =toLuaState(native_lua);
    return lua_toboolean(L,index);
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_toBytes(JNIEnv *env, jclass clazz, jlong native_lua,
                                                              jint index) {
    lua_State *L =toLuaState(native_lua);
    if(lua_isstring(L,index))
    {
        size_t len = 0;
        const char* str = lua_tolstring(L,index,&len);
        jbyteArray result = env->NewByteArray(len);
        if (env->ExceptionCheck())
            return nullptr;
        env->SetByteArrayRegion(result,0,len,(jbyte*)str);
        return result;
    } else{
        throwTypeError(env,L,index,LUA_TSTRING);
    }
    return nullptr;
}

extern "C" JNIEXPORT jstring JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_toString(JNIEnv *env, jclass clazz, jlong native_lua,
                                                               jint index) {
    lua_State *L =toLuaState(native_lua);
    if(lua_isstring(L,index))
    {
        size_t len = 0;
        const char* str = lua_tolstring(L,index,&len);
        jstring result = env->NewStringUTF(str);
        return result;
    } else{
        throwTypeError(env,L,index,LUA_TSTRING);
    }
    return nullptr;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_toLuaObjectAdapter(JNIEnv *env, jclass clazz,
                                                                         jlong native_lua,
                                                                         jint index) {
    auto* pID = (jlong*)luaL_testudata(toLuaState(native_lua),index,LUA_OBJECT_ADAPTER_NAME);
    if (pID)
    {
        jobject context = GetJavaLuaContext(native_lua);
        return env->CallObjectMethod(context,GetJavaObjectMethodID,*pID);
    }
    throwTypeError(env,toLuaState(native_lua),index,LUA_TUSERDATA);
    return nullptr;
}


extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_pushNil(JNIEnv *env, jclass clazz, jlong native_lua) {
    lua_pushnil(toLuaState(native_lua));
}

extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_push__JJ(JNIEnv *env, jclass clazz, jlong native_lua,
                                                               jlong v) {
    lua_pushinteger(toLuaState(native_lua),v);
}

extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_push__JD(JNIEnv *env, jclass clazz, jlong native_lua,
                                                               jdouble v) {
    lua_pushnumber(toLuaState(native_lua),v);
}


extern "C"
JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_push__J_3B(JNIEnv *env, jclass clazz,
                                                                 jlong native_lua, jbyteArray v) {

    LocalJavaBytes localJavaBytes(env,v);
    lua_pushlstring(toLuaState(native_lua),localJavaBytes.str(),localJavaBytes.size());
}

extern "C"
JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_push__JZ(JNIEnv *env, jclass clazz,
                                                               jlong native_lua, jboolean v) {
    lua_pushboolean(toLuaState(native_lua),v);
}

extern "C" JNIEXPORT jint JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_type(JNIEnv *env, jclass clazz, jlong native_lua, jint index) {
    return lua_type(toLuaState(native_lua),index);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_isInteger(JNIEnv *env, jclass clazz, jlong native_value,
                                                                jint index) {
    return lua_isinteger(toLuaState(native_value),index);
}

jobject GetJavaObject(lua_State*L,JNIEnv*env,jlong id)
{
    jobject result =env->CallObjectMethod(GetJavaLuaContext(L), GetJavaObjectMethodID, id);
    if (catchAndPushJavaThrowable(env,L))
        lua_error(L);
    return result;
}



void ReleaseJavaObject(lua_State*L, JNIEnv*env, jlong id)
{
    env->CallVoidMethod(GetJavaLuaContext(L), RemoveJavaObjectMethodID, id);
    if (catchAndPushJavaThrowable(env,L))
        lua_error(L);
}


static int luaObjectAdapterMethod(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    jobject context = GetJavaLuaContext(L);
    const char* methodName = lua_tostring(L,lua_upvalueindex(2));
    LocalReference<jstring> jMethodName(env,env->NewStringUTF(methodName));
    auto * pid = (jlong*)lua_touserdata(L,lua_upvalueindex(1));
    LocalReference<jobject> adapter(env,GetJavaObject(L,env,*pid));
    jint result = env->CallIntMethod(adapter.get(),LuaObjectAdapterCallMethodID,jMethodName.get(),context);
    if (catchAndPushJavaThrowable(env,L) || result == -1)
    {
        lua_error(L);
    }
    return result;
}

static int luaObjectAdapterIndex(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    const char* methodName = luaL_checkstring(L,2);
    auto * pid = (jlong*)luaL_checkudata(L,1,LUA_OBJECT_ADAPTER_NAME);
    LocalReference<jobject> adapter(env,GetJavaObject(L,env,*pid));
    LocalReference<jstring> jMethodName(env,env->NewStringUTF(methodName));
    jboolean result = env->CallBooleanMethod(adapter.get(),LuaObjectAdapterHasMethodMethodID,jMethodName.get());
    if (catchAndPushJavaThrowable(env,L))
        lua_error(L);
    else if (result)
        lua_pushcclosure(L,luaObjectAdapterMethod,2);
    else
        lua_pushnil(L);
    return 1;
}


int javaObjectDestroy(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    auto* pId = (jlong *)lua_touserdata(L,1);
    ReleaseJavaObject(L,env,*pId);
    return 0;
}


extern "C" JNIEXPORT jlong JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_newLuaState(JNIEnv *env, jclass clazz, jobject context) {
    auto * extension = (LuaExtension*)malloc(sizeof(LuaExtension));
    extension->context = env->NewWeakGlobalRef(context);
    lua_State *L = luaL_newstate();
    SetLuaExtension(L,extension);
    luaL_openlibs(L);
    if (luaL_newmetatable(L, LUA_OBJECT_ADAPTER_NAME))
    {
        luaL_Reg methods[] = {
                {"__gc",javaObjectDestroy},
                {"__index",luaObjectAdapterIndex},
                {nullptr,nullptr}
        };
        luaL_setfuncs(L,methods,0);
    }
    lua_pop(L,1);

    if (luaL_newmetatable(L,LUA_HANDLER_NAME))
    {
        luaL_Reg methods[] = {
                {"__gc",javaObjectDestroy},
                {nullptr,nullptr}
        };
        luaL_setfuncs(L,methods,0);
    }
    lua_pop(L,1);
    return (jlong)L;
}

extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_closeLuaState(JNIEnv *env, jclass clazz, jlong native_lua) {
    lua_State *L = toLuaState(native_lua);
    LuaExtension * extension = GetLuaExtension(native_lua);
    lua_close(L);
    env->DeleteWeakGlobalRef(extension->context);
    free(extension);
}


static bool pushJavaObject(JNIEnv *env, jlong native_lua, jobject object_wrapper)
{
    lua_State *L = toLuaState(native_lua);
    jobject context = GetJavaLuaContext(L);
    auto* pId = (jlong*)lua_newuserdata(L,sizeof(jlong));
    *pId = (jlong)lua_topointer(L,-1);
    env->CallVoidMethod(context, CacheJavaObjectMethodID, *pId, object_wrapper);
    if (env->ExceptionCheck())
    {
        lua_pop(L,1);
        return false;
    }
    return true;
}


static int luaHandlerCall(lua_State*L)
{
    JNIEnv *env = GetJNIEnv(L);
    auto * pid = (jlong*)lua_touserdata(L,lua_upvalueindex(1));
    LocalReference<jobject> handler (env,GetJavaObject(L,env,*pid));
    jint result = env->CallIntMethod(handler.get(),LuaHandlerCallID,GetJavaLuaContext(L));
    if (catchAndPushJavaThrowable(env,L))
        lua_error(L);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_push__JLtop_lizhistudio_androidlua_LuaHandler_2(JNIEnv *env,
                                                                                                      jclass clazz,
                                                                                                      jlong native_lua,
                                                                                                      jobject lua_handler) {
    if(lua_handler != nullptr )
    {
        if (pushJavaObject(env, native_lua, lua_handler)){
            luaL_setmetatable(toLuaState(native_lua), LUA_HANDLER_NAME);
            lua_pushcclosure(toLuaState(native_lua),luaHandlerCall,1);
        }
    }else{
        lua_pushnil(toLuaState(native_lua));
    }
}

extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_push__JLtop_lizhistudio_androidlua_LuaObjectAdapter_2(
        JNIEnv *env, jclass clazz, jlong native_lua, jobject object_wrapper) {
    lua_State *L = toLuaState(native_lua);
    if (object_wrapper != nullptr)
    {
        if (pushJavaObject(env,native_lua,object_wrapper))
        {
            luaL_setmetatable(L,LUA_OBJECT_ADAPTER_NAME);
        }
    } else{
        lua_pushnil(toLuaState(native_lua));
    }
}


extern "C" JNIEXPORT jint JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_getTop(JNIEnv *env, jclass clazz, jlong native_lua) {
    return lua_gettop(toLuaState(native_lua));
}

extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_setTop(JNIEnv *env, jclass clazz, jlong native_lua,
                                                             jint index) {
    lua_settop(toLuaState(native_lua),index);
}



static const char* getCodeMode(int t)
{
    const char* mode;
    if (t == 0)
        mode = "bt";
    else if(t == 1)
        mode = "t";
    else
        mode = "b";
    return mode;
}

static bool checkLuaLoadOrCallError(lua_State*L,JNIEnv*env,int code)
{
    if (code == LUA_OK)
        return false;
    jclass clazz;
    switch (code) {
        case LUA_ERRRUN:
            clazz = env->FindClass("top/lizhistudio/androidlua/exception/LuaRuntimeError");
            break;
        case LUA_ERRSYNTAX:
            clazz = env->FindClass("top/lizhistudio/androidlua/exception/LuaSyntaxError");
            break;
        case LUA_ERRERR:
            clazz = env->FindClass("top/lizhistudio/androidlua/exception/LuaErrorHandlerError");
            break;
        case LUA_ERRGCMM:
            clazz = env->FindClass("top/lizhistudio/androidlua/exception/LuaGCError");
            break;
        case LUA_ERRMEM:
            clazz = env->FindClass("top/lizhistudio/androidlua/exception/LuaMemberError");
            break;
        case LUA_ERRFILE:
            clazz = env->FindClass("top/lizhistudio/androidlua/exception/LuaErrorFileError");
            break;
        default:
            clazz  = env->FindClass("top/lizhistudio/androidlua/exception/LuaError");
            break;
    }
    const char* message;
    if (lua_isstring(L,-1))
        message = lua_tostring(L,-1);
    else
        message = "unknown error";
    env->ThrowNew(clazz,message);
    return true;
}

extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_loadBuffer(JNIEnv *env, jclass clazz, jlong native_lua,
                                                                 jbyteArray code, jstring chunk_name,
                                                                 jint code_type) {
    LocalJavaBytes codeWrapper(env,code);
    LocalJavaString chunkName(env,chunk_name);
    lua_State *L = toLuaState(native_lua);
    int result= luaL_loadbufferx(L,
            codeWrapper.str(), codeWrapper.size(), chunkName.str(), getCodeMode(code_type));
    checkLuaLoadOrCallError(L,env,result);
}


extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_loadFile(JNIEnv *env, jclass clazz, jlong native_lua,
                                                               jstring file_name, jint code_type) {
    LocalJavaString fileName(env,file_name);
    lua_State *L = toLuaState(native_lua);
    int result = luaL_loadfilex(L, fileName.str(), getCodeMode(code_type));
    checkLuaLoadOrCallError(L,env,result);
}


extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_pCall(JNIEnv *env, jclass clazz, jlong native_lua,
                                                            jint arg_number, jint result_number,
                                                            jint error_function_index) {
    lua_State *L = toLuaState(native_lua);
    int result = lua_pcall(L,arg_number,result_number,error_function_index);
    checkLuaLoadOrCallError(L,env,result);
}

extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_pop(JNIEnv *env, jclass clazz, jlong native_lua, jint n) {
    lua_pop(toLuaState(native_lua), n);
}


extern "C" JNIEXPORT jlong JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_toPointer(JNIEnv *env, jclass clazz, jlong native_lua,
                                                                jint index) {
    return (jlong)lua_topointer(toLuaState(native_lua),index);
}

static int lua_setTable(lua_State*L)
{
    int tableIndex = luaL_checkinteger(L,1);
    lua_settable(L,tableIndex);
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_setTable(JNIEnv *env, jclass clazz, jlong native_lua,
                                                               jint table_index) {
    lua_State *L = toLuaState(native_lua);
    lua_pushcfunction(L,lua_setTable);
    lua_pushinteger(L,table_index);
    lua_pushvalue(L,-2);
    lua_pushvalue(L,-1);
    int result = lua_pcall(L,3,0,0);
    checkLuaLoadOrCallError(L,env,result);
    lua_pop(L,2);
}

static int lua_getTable(lua_State*L)
{
    int tableIndex = luaL_checkinteger(L,1);
    int result = lua_gettable(L,tableIndex);
    lua_pushinteger(L,result);
    return 1;
}

extern "C" JNIEXPORT jint
JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_getTable(JNIEnv *env, jclass clazz,
                                                               jlong native_lua, jint table_index) {
    lua_State *L = toLuaState(native_lua);
    lua_pushcfunction(L,lua_getTable);
    lua_pushinteger(L,table_index);
    lua_pushvalue(L,-2);
    lua_pushvalue(L,-1);
    int result = lua_pcall(L,3,1,0);
    jint r = 0;
    if(!checkLuaLoadOrCallError(L,env,result))
    {
        r = lua_tointeger(L,-1);
        lua_pop(L,3);
    } else{
        lua_pop(L,2);
    }
    return r;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_top_lizhistudio_autolua_core_LuaContextImplement_isLuaObjectAdapter(JNIEnv *env, jclass clazz,
                                                                         jlong native_lua,
                                                                         jint index) {
    return luaL_testudata(toLuaState(native_lua),index,LUA_OBJECT_ADAPTER_NAME) != nullptr;
}

static void injectMode(lua_State*L,const char* name,lua_CFunction method)
{
    luaL_requiref(L,name,method,1);
    lua_pop(L,1);
}


extern "C" JNIEXPORT void JNICALL
Java_top_lizhistudio_autolua_core_LuaContextFactoryImplement_injectAutoLua(JNIEnv *env,
                                                                           jclass clazz,
                                                                           jlong native_lua) {
    lua_State *L = toLuaState(native_lua);
    injectMode(L,"display",luaopen_display);
    injectMode(L,"view",luaopen_view);
    injectMode(L,"thread",luaopen_thread);
    injectMode(L,"input",luaopen_input);
}


