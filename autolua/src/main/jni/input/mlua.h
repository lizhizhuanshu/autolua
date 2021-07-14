#pragma once
#include<lua.hpp>
#include<memory>
#define CLASS_METATABLE_NAME(aClass) "NativeObject:"#aClass
#define luaL_pushNewObject(aClass,...) lua::newObject<aClass>(CLASS_METATABLE_NAME(aClass),__VA_ARGS__)
#define luaL_checkObject(aClass,L,index) lua::checkObject<aClass>(L,index,CLASS_METATABLE_NAME(aClass))
#define luaL_newClassMetatable(aClass,L) luaL_newmetatable(L,CLASS_METATABLE_NAME(aClass))
#define luaL_testObject(aClass,L,index) lua::testObject<aClass>(L,index,CLASS_METATABLE_NAME(aClass))
namespace lua
{
	template <class T, class...Args>
	static T* newObject(const char * className, lua_State*L, Args&&...args);
	template <class T>
	static T* checkObject(lua_State*L, int index, const char * className);
	template <class T>
	static T* testObject(lua_State* L, int index, const char* className);
	template <class T>
	static int finish(lua_State*L);
}




template<class T, class ...Args>
inline T * lua::newObject(const char * className, lua_State * L, Args && ...args)
{
	T* obj = (T*)lua_newuserdata(L, sizeof(T));
	new(obj)T(std::forward<Args>(args)...);
	if (!luaL_getmetatable(L, className))
	{
		obj->~T();
		lua_pop(L, 2);
		luaL_error(L, "error create c++ object  this class: '%s' is not hava metatable", className);
	}
	lua_setmetatable(L, -2);
	return obj;
}

template<class T>
inline T * lua::checkObject(lua_State * L, int index, const char * className)
{
	return (T*)luaL_checkudata(L, index, className);
}

template<class T>
inline T* lua::testObject(lua_State* L, int index, const char* className)
{
	return (T*)luaL_testudata(L, index, className);
}


template<class T>
inline int lua::finish(lua_State * L)
{
	T* obj = (T*)lua_touserdata(L, 1);
	obj->~T();
	return 0;
}
