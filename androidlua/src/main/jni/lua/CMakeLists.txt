cmake_minimum_required(VERSION 3.4.1)


add_definitions(-DLUA_DL_DLOPEN -DLUA_COMPAT_5_3 -DLUA_USE_C89 -DLUA_USE_LINUX)
aux_source_directory(. DIR_SRCS)
list(REMOVE_ITEM DIR_SRCS ./lua.c ./luac.c)

add_library(lua SHARED ${DIR_SRCS})
