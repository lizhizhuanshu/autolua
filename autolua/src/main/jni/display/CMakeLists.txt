cmake_minimum_required(VERSION 3.0)
project(display)

INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/../viewer
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../androidlua/src/main/jni/luajava)

aux_source_directory(. DIR_SRCS)

add_library(${PROJECT_NAME} STATIC ${DIR_SRCS})
unset(DIR_SRCS)

