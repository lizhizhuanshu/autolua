cmake_minimum_required(VERSION 3.4.1)

aux_source_directory(. DIR_SRCS)
list(REMOVE_ITEM DIR_SRCS ./lua.c ./luac.c)

add_library(lua SHARED ${DIR_SRCS})
