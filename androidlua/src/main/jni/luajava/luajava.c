#include <limits.h>
//
// Created by lizhi on 2021/2/21.
//
#include "lualib.h"
#include "lauxlib.h"
#include <stdlib.h>
#include <stdio.h>
#include "luajava.h"

#define LUA_JAVA_MODULE_NAME "luajava"
#define PACKAGE_NAME "top/lizhistudio/androidlua"
#define TYPE_ERROR_CLASS_NAME PACKAGE_NAME "/exception/LuaTypeError"
#define INVOKE_ERROR_CLASS_NAME PACKAGE_NAME "/exception/LuaInvokeError"
#define JAVA_INTERFACE_WRAP_CLASS_NAME PACKAGE_NAME "/JavaObjectWrapper"
#define LUA_CONTEXT_CLASS_NAME PACKAGE_NAME "/LuaContext"
#define PUSH_THROWABLE_ERROR "push java throwable error"
#define THROWABLE_CLASS_NAME "java/lang/Throwable"

#define JAVA_OBJECT_NAME "JavaObject"
#define GLOBAL(env, obj) (*env)->NewGlobalRef(env,obj)
#define FreeLocal(env,obj)  (*env)->DeleteLocalRef(env,obj)
#define JavaFindClass(env,str) (*env)->FindClass(env,str)
#define FreeGlobal(env,obj) (*env)->DeleteGlobalRef(env,obj)


static jclass LuaContextClass = NULL;
static jclass LuaTypeErrorClass = NULL;
static jclass LuaInvokeErrorClass = NULL;
static jclass JavaObjectWrapperClass = NULL;
static jclass ThrowableClass = NULL;

static jmethodID IndexMethodID = NULL;
static jmethodID NewIndexMethodID = NULL;
static jmethodID CallMethodID = NULL;
static jmethodID EqualMethodID = NULL;
static jmethodID LenMethodID = NULL;

static jmethodID CallMethodMethodID = NULL;


static jmethodID GetJavaObjectWrapperID = NULL;
static jmethodID ToJavaObjectID = NULL;
static jmethodID PushJavaObjectWrapperID = NULL;
static jmethodID PushJavaObjectID = NULL;
static jmethodID RemoveJavaObjectWrapperID = NULL;


JNIEXPORT jint JNI_OnLoad(JavaVM * vm, void * reserved)
{
#define findGlobalClass(env,name) GLOBAL(env,JavaFindClass(env,name))
    JNIEnv * env = NULL;
    if ((*vm)->GetEnv(vm,(void**)&env, JNI_VERSION_1_6) != JNI_OK)
        return -1;
    LuaTypeErrorClass = findGlobalClass(env, TYPE_ERROR_CLASS_NAME);
    LuaContextClass = findGlobalClass(env, LUA_CONTEXT_CLASS_NAME);
    LuaInvokeErrorClass = findGlobalClass(env, INVOKE_ERROR_CLASS_NAME);
    JavaObjectWrapperClass = findGlobalClass(env, JAVA_INTERFACE_WRAP_CLASS_NAME);
    ThrowableClass = findGlobalClass(env,THROWABLE_CLASS_NAME);

    CallMethodID = (*env)->GetMethodID(env,JavaObjectWrapperClass,"__call", "(Ltop/lizhistudio/androidlua/LuaContext;)I");
    NewIndexMethodID = (*env)->GetMethodID(env,JavaObjectWrapperClass,"__newIndex",
                                           "(Ltop/lizhistudio/androidlua/LuaContext;)I");
    IndexMethodID = (*env)->GetMethodID(env,JavaObjectWrapperClass,"__index",
                                        "(Ltop/lizhistudio/androidlua/LuaContext;)I");
    EqualMethodID = (*env)->GetMethodID(env,JavaObjectWrapperClass,"__equal",
                                        "(Ltop/lizhistudio/androidlua/LuaContext;)I");
    LenMethodID = (*env)->GetMethodID(env,JavaObjectWrapperClass,"__len",
                                      "(Ltop/lizhistudio/androidlua/LuaContext;)I");


    CallMethodMethodID = (*env)->GetMethodID(env, JavaObjectWrapperClass, "callMethod",
                                             "(Ltop/lizhistudio/androidlua/LuaContext;Ljava/lang/String;)I");



    GetJavaObjectWrapperID = (*env)->GetMethodID(env,LuaContextClass,"getWrapper",
                                                 "(J)Ltop/lizhistudio/androidlua/JavaObjectWrapper;");
    ToJavaObjectID = (*env)->GetMethodID(env, LuaContextClass, "toJavaObject", "(I)Ljava/lang/Object;");
    PushJavaObjectWrapperID = (*env)->GetMethodID(env,LuaContextClass,"pushWrapper","(JLtop/lizhistudio/androidlua/JavaObjectWrapper;)V");
    RemoveJavaObjectWrapperID = (*env)->GetMethodID(env,LuaContextClass,"removeWrapper", "(J)V");
    PushJavaObjectID = (*env)->GetMethodID(env,LuaContextClass,"push", "(Ljava/lang/Class;Ljava/lang/Object;)V");
    return  JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved){
    JNIEnv * env = NULL;
    if ((*vm)->GetEnv(vm,(void**)&env, JNI_VERSION_1_6) != JNI_OK)
        return ;
    FreeGlobal(env,LuaContextClass);
    FreeGlobal(env,LuaInvokeErrorClass);
    FreeGlobal(env,LuaTypeErrorClass);
    FreeGlobal(env, JavaObjectWrapperClass);
    FreeGlobal(env,ThrowableClass);
}


static void throwTypeError(JNIEnv *env, lua_State*L,int index,int exception)
{
    char buffer[1024];
    const char* exceptionName = lua_typename(L,exception);
    const char* nowName = lua_typename(L,lua_type(L,index));
    sprintf(buffer,"luaState %p  index %d exception type %s now type %s",L,index,exceptionName,nowName);
    (*env)->ThrowNew(env, LuaTypeErrorClass,buffer);
}

static int pushJavaThrowable(JNIEnv* env,lua_State*L,jthrowable throwable)
{
    LuaExtension *extension = GetLuaExtension(L);
    (*env)->CallVoidMethod(env,extension->context,PushJavaObjectID,ThrowableClass,throwable);
    if ((*env)->ExceptionCheck(env))
    {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        return 0;
    }
    return 1;
}

static int catchAndPushJavaThrowable(JNIEnv *env, lua_State*L)
{
    jthrowable throwable = (*env)->ExceptionOccurred(env);
    if (throwable != NULL)
    {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        if (!pushJavaThrowable(env,L,throwable))
            lua_pushstring(L,PUSH_THROWABLE_ERROR);
        (*env)->DeleteLocalRef(env,throwable);
        return 1;
    }
    return 0;

}


JNIEXPORT jlong JNICALL
Java_top_lizhistudio_androidlua_LuaJava_toInteger(JNIEnv *env, jclass clazz, jlong native_lua,
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

JNIEXPORT jdouble JNICALL
Java_top_lizhistudio_androidlua_LuaJava_toNumber(JNIEnv *env, jclass clazz, jlong native_lua,
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

JNIEXPORT jboolean JNICALL
Java_top_lizhistudio_androidlua_LuaJava_toBoolean(JNIEnv *env, jclass clazz, jlong native_lua,
                                               jint index) {
    lua_State *L =toLuaState(native_lua);
    return lua_toboolean(L,index);
}

JNIEXPORT jbyteArray JNICALL
Java_top_lizhistudio_androidlua_LuaJava_toBytes(JNIEnv *env, jclass clazz, jlong native_lua,
                                             jint index) {
    lua_State *L =toLuaState(native_lua);
    if(lua_isstring(L,index))
    {
        size_t len = 0;
        const char* str = lua_tolstring(L,index,&len);
        jbyteArray result = (*env)->NewByteArray(env,len);
        if ((*env)->ExceptionCheck(env))
            return NULL;
        (*env)->SetByteArrayRegion(env,result,0,len,(jbyte*)str);
        return result;
    } else{
        throwTypeError(env,L,index,LUA_TSTRING);
    }
    return NULL;
}

JNIEXPORT jstring JNICALL
Java_top_lizhistudio_androidlua_LuaJava_toString(JNIEnv *env, jclass clazz, jlong native_lua,
                                              jint index) {
    lua_State *L =toLuaState(native_lua);
    if(lua_isstring(L,index))
    {
        size_t len = 0;
        const char* str = lua_tolstring(L,index,&len);
        jstring result = (*env)->NewStringUTF(env,str);
        return result;
    } else{
        throwTypeError(env,L,index,LUA_TSTRING);
    }
    return NULL;
}

JNIEXPORT void JNICALL
Java_top_lizhistudio_androidlua_LuaJava_push__JZ(JNIEnv *env, jclass clazz, jlong native_lua,
                                              jboolean v) {
    lua_pushboolean(toLuaState(native_lua),v);
}

JNIEXPORT void JNICALL
Java_top_lizhistudio_androidlua_LuaJava_push__J_3B(JNIEnv *env, jclass clazz, jlong native_lua,
                                                jbyteArray v) {
    lua_State *L =toLuaState(native_lua);
    jboolean isCopy = 0;
    jbyte * bytes = (*env)->GetByteArrayElements(env,v,&isCopy);
    jsize size = (*env)->GetArrayLength(env,v);
    lua_pushlstring(L,(const char*)bytes,size);
    (*env)->ReleaseByteArrayElements(env,v,bytes,0);
}

JNIEXPORT void JNICALL
Java_top_lizhistudio_androidlua_LuaJava_pushNil(JNIEnv *env, jclass clazz, jlong native_lua) {
    lua_pushnil(toLuaState(native_lua));
}

JNIEXPORT void JNICALL
Java_top_lizhistudio_androidlua_LuaJava_push__JJ(JNIEnv *env, jclass clazz, jlong native_lua,
                                              jlong v) {
    lua_pushinteger(toLuaState(native_lua),v);
}

JNIEXPORT void JNICALL
Java_top_lizhistudio_androidlua_LuaJava_push__JD(JNIEnv *env, jclass clazz, jlong native_lua,
                                              jdouble v) {
    lua_pushnumber(toLuaState(native_lua),v);
}

JNIEXPORT jint JNICALL
Java_top_lizhistudio_androidlua_LuaJava_type(JNIEnv *env, jclass clazz, jlong native_lua, jint index) {
    return lua_type(toLuaState(native_lua),index);
}

JNIEXPORT jboolean JNICALL
Java_top_lizhistudio_androidlua_LuaJava_isInteger(JNIEnv *env, jclass clazz, jlong native_value,
                                               jint index) {
    return lua_isinteger(toLuaState(native_value),index);
}

static int isJavaObject(lua_State*L,int index)
{
    return luaL_testudata(L, index, JAVA_OBJECT_NAME) >0;
}

static jobject getJavaObjectWrapper(LuaExtension *extension,lua_State*L,int index)
{
    JNIEnv *env = extension->env;
    jlong* pId = (jlong *)lua_touserdata(L,index);
    return (*env)->CallObjectMethod(env,extension->context,GetJavaObjectWrapperID,*pId);
}

static jobject toJavaObject(JNIEnv *env,jobject context, int index)
{
    return (*env)->CallObjectMethod(env,context, ToJavaObjectID, (jint)index);
}



static int executeLua(JNIEnv*env,jobject context, lua_State*L,int loadResult)
{
    const char* message = "unknown error";
    int oldTop = lua_gettop(L)-1;
    if (loadResult == LUA_OK)
    {
        if (lua_pcall(L,0,LUA_MULTRET,0) == LUA_OK)
        {
            return lua_gettop(L)-oldTop;
        } else
        {
            if (lua_isstring(L,-1))
                message = lua_tostring(L,-1);
            else if(isJavaObject(L,-1))
            {
                (*env)->Throw(env,toJavaObject(env,context,-1));
                lua_pop(L,1);
                return 0;
            }
        }
    } else{
        message = lua_tostring(L,-1);
    }
    (*env)->ThrowNew(env,LuaInvokeErrorClass,message);
    lua_pop(L,1);
    return 0;
}

JNIEXPORT __unused  jint JNICALL
Java_top_lizhistudio_androidlua_LuaJava_execute(JNIEnv *env, jclass clazz, jlong native_lua,
                                             jbyteArray code, jstring chunk_name) {
    jboolean isCopy = 0;
    jbyte* cCode = (*env)->GetByteArrayElements(env,code,&isCopy);
    jsize codeSize = (*env)->GetArrayLength(env,code);
    isCopy = 0;
    const char* chunkName = (*env)->GetStringUTFChars(env,chunk_name,&isCopy);
    lua_State *L = toLuaState(native_lua);
    SetJNIEnv(L,env);
    lua_settop(L,0);
    int result = luaL_loadbuffer(L,(const char*)cCode,codeSize,chunkName);
    (*env)->ReleaseByteArrayElements(env,code,cCode,0);
    (*env)->ReleaseStringUTFChars(env,chunk_name,chunkName);
    LuaExtension *extension =GetLuaExtension(L);
    return executeLua(env,extension->context,L,result);
}

JNIEXPORT __unused  jint JNICALL
Java_top_lizhistudio_androidlua_LuaJava_executeFile(JNIEnv *env, jclass clazz, jlong native_lua,
                                                 jstring path) {

    jboolean isCopy = 0;
    const char* codePath = (*env)->GetStringUTFChars(env,path,&isCopy);
    lua_State *L = toLuaState(native_lua);
    SetJNIEnv(L,env);
    lua_settop(L,0);
    int result = luaL_loadfile(L,codePath);
    (*env)->ReleaseStringUTFChars(env,path,codePath);
    LuaExtension *extension =GetLuaExtension(L);
    return executeLua(env,extension->context,L,result);
}



JNIEXPORT void JNICALL
Java_top_lizhistudio_androidlua_LuaJava_setGlobal(JNIEnv *env, jclass clazz, jlong native_lua,
                                               jstring key) {
    lua_State *L = toLuaState(native_lua);
    SetJNIEnv(L,env);
    jboolean isCopy = 0;
    const char* globalKey =(*env)->GetStringUTFChars(env,key,&isCopy);
    lua_setglobal(L,globalKey);
    (*env)->ReleaseStringUTFChars(env,key,globalKey);
}




static int javaObjectMethod(lua_State*L)
{
    LuaExtension *extension = GetLuaExtension(L);
    JNIEnv *env = extension->env;
    const char* methodName = lua_tostring(L,lua_upvalueindex(2));
    jstring jMethodName = (*env)->NewStringUTF(env,methodName);
    jobject object = getJavaObjectWrapper(extension,L,lua_upvalueindex(1));
    jint result = (*env)->CallIntMethod(env,object,CallMethodMethodID,extension->context,jMethodName);
    FreeLocal(env,jMethodName);
    FreeLocal(env,object);
    if (catchAndPushJavaThrowable(env,L) || result == -1)
    {
        lua_error(L);
    }
    return result;
}

static int javaObjectDestroy(lua_State*L)
{
    LuaExtension *extension = GetLuaExtension(L);
    JNIEnv *env = extension->env;
    jlong* pId = (jlong *)lua_touserdata(L,1);
    if (*pId)
    {
        (*env)->CallVoidMethod(env,extension->context,RemoveJavaObjectWrapperID,*pId);
        *pId = 0;
    }
    return 0;
}


static int callJavaObject(lua_State*L,jmethodID id)
{
    LuaExtension *extension = GetLuaExtension(L);
    jobject object =  getJavaObjectWrapper(extension,L,1);
    JNIEnv *env = extension->env;
    int result = (*env)->CallIntMethod(env,object,id,extension->context);
    FreeLocal(env,object);
    if (catchAndPushJavaThrowable(env,L))
        lua_error(L);
    return result;
}

#define JAVA_METHOD_DEFINE(n) static int javaObject##n(lua_State*L)\
{\
    return callJavaObject(L,n##MethodID);\
}

JAVA_METHOD_DEFINE(Index)
JAVA_METHOD_DEFINE(NewIndex)
JAVA_METHOD_DEFINE(Call)
JAVA_METHOD_DEFINE(Len)
JAVA_METHOD_DEFINE(Equal)


static int destroyJavaObject(lua_State*L)
{
    if (luaL_testudata(L, 1, JAVA_OBJECT_NAME))
    {
        javaObjectDestroy(L);
        lua_pushboolean(L,1);
    } else
        lua_pushboolean(L,0);
    return 1;
}


static int luaopen_luajava(lua_State*L)
{
    if (luaL_newmetatable(L, JAVA_OBJECT_NAME))
    {
        luaL_Reg methods[] = {
                {"__gc",javaObjectDestroy},
                {"__index",javaObjectIndex},
                {"__newindex",javaObjectNewIndex},
                {"__len",javaObjectLen},
                {"__eq",javaObjectEqual},
                {"__call",javaObjectCall},
                {NULL,NULL}
        };
        luaL_setfuncs(L,methods,0);
    }
    lua_pop(L,1);
    luaL_Reg methods[] = {
            {"destroy",destroyJavaObject},
            {NULL,NULL}
    };
    luaL_newlib(L,methods);
    return 1;
}

JNIEXPORT jlong JNICALL
Java_top_lizhistudio_androidlua_LuaJava_newLuaState(JNIEnv *env, jclass clazz, jobject context) {
    LuaExtension * extension = malloc(sizeof(LuaExtension));
    extension->context = (*env)->NewWeakGlobalRef(env,context);
    extension->env = env;
    lua_State *L = luaL_newstate();
    SetLuaExtension(L,extension);
    luaL_requiref(L, LUA_JAVA_MODULE_NAME, luaopen_luajava, 1);
    luaL_openlibs(L);
    lua_pop(L,1);
    return (jlong)L;
}

JNIEXPORT void JNICALL
Java_top_lizhistudio_androidlua_LuaJava_closeLuaState(JNIEnv *env, jclass clazz, jlong native_lua) {
    lua_State *L = toLuaState(native_lua);
    LuaExtension * extension = GetLuaExtension(native_lua);
    extension->env = env;
    lua_close(L);
    (*env)->DeleteWeakGlobalRef(env,extension->context);
    free(extension);
}


static void pushJavaObjectWrapper(JNIEnv *env,  jlong native_lua, jobject object_wrapper)
{
    lua_State *L = toLuaState(native_lua);
    LuaExtension *extension = GetLuaExtension(L);
    extension->env = env;
    jlong* pId = lua_newuserdata(L,sizeof(jlong));
    *pId = (jlong)lua_topointer(L,-1);
    (*env)->CallVoidMethod(env,extension->context,PushJavaObjectWrapperID,*pId,object_wrapper);
}


JNIEXPORT void JNICALL
Java_top_lizhistudio_androidlua_LuaJava_push__JLtop_lizhistudio_androidlua_JavaObjectWrapper_2(
        JNIEnv *env, jclass clazz, jlong native_lua, jobject object_wrapper) {
    pushJavaObjectWrapper(env,native_lua,object_wrapper);
    luaL_setmetatable(toLuaState(native_lua), JAVA_OBJECT_NAME);
}

JNIEXPORT jobject JNICALL
Java_top_lizhistudio_androidlua_LuaJava_toJavaObject(JNIEnv *env, jclass clazz, jlong native_lua,
                                                  jint index) {
    lua_State *L = toLuaState(native_lua);
    LuaExtension * extension = GetLuaExtension(L);
    extension->env = env;
    jlong * pId = (jlong*)luaL_testudata(L, index, JAVA_OBJECT_NAME);
    return pId?getJavaObjectWrapper(extension,L,index):NULL ;
}

JNIEXPORT jboolean JNICALL
Java_top_lizhistudio_androidlua_LuaJava_isJavaObjectWrapper(JNIEnv *env, jclass clazz,
                                                         jlong native_lua, jint index) {

    lua_State *L = toLuaState(native_lua);
    return luaL_testudata(L, index, JAVA_OBJECT_NAME) >0;
}

JNIEXPORT jint JNICALL
Java_top_lizhistudio_androidlua_LuaJava_getTop(JNIEnv *env, jclass clazz, jlong native_lua) {
    return lua_gettop(toLuaState(native_lua));
}

JNIEXPORT void JNICALL
Java_top_lizhistudio_androidlua_LuaJava_setTop(JNIEnv *env, jclass clazz, jlong native_lua,
                                            jint index) {
    lua_settop(toLuaState(native_lua),index);
}

JNIEXPORT void JNICALL
Java_top_lizhistudio_androidlua_LuaJava_pushJavaObjectMethod(JNIEnv *env, jclass clazz,
                                                             jlong native_lua) {
    lua_State *L = toLuaState(native_lua);
    if (luaL_testudata(L,-2,JAVA_OBJECT_NAME))
    {
        if (lua_isstring(L,-1))
        {
            lua_pushcclosure(L,javaObjectMethod,2);
        } else
            throwTypeError(env,L,-1,LUA_TSTRING);
    } else
        throwTypeError(env,L,-2,LUA_TUSERDATA);

}