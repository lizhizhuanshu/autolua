cmake_minimum_required(VERSION 3.0)
project(thread)

aux_source_directory(. DIR_SRCS)

INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/../../../../../androidlua/src/main/jni/luajava)

add_library(${PROJECT_NAME} STATIC ${DIR_SRCS})
unset(DIR_SRCS)

