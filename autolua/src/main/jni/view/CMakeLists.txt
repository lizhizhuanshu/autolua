cmake_minimum_required(VERSION 3.0)
set(CMAKE_CXX_STANDARD 17)
project(view)

INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/../lua
        ${CMAKE_CURRENT_SOURCE_DIR}/..)

aux_source_directory(. DIR_SRCS)

add_library(${PROJECT_NAME} STATIC ${DIR_SRCS})
unset(DIR_SRCS)

