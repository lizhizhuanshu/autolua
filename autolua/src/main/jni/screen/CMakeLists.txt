cmake_minimum_required(VERSION 3.0)
project(screen)

INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/../viewer
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../androidlua/src/main/jni/luajava)

aux_source_directory(. DIR_SRCS)

add_library(${PROJECT_NAME} SHARED ${DIR_SRCS})
target_link_libraries(${PROJECT_NAME} lua luajava)
unset(DIR_SRCS)

