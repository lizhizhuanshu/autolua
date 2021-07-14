cmake_minimum_required(VERSION 3.4.1)

INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/../lua
        ${CMAKE_CURRENT_SOURCE_DIR}/..)

aux_source_directory(. DIR_SRCS)


add_library(display STATIC ${DIR_SRCS})