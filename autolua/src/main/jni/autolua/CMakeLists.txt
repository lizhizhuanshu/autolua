cmake_minimum_required(VERSION 3.0)
project(autolua)

INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/../viewer
        ${CMAKE_CURRENT_SOURCE_DIR}/../display
        ${CMAKE_CURRENT_SOURCE_DIR}/../thread)

aux_source_directory(. DIR_SRCS)

add_library(${PROJECT_NAME} SHARED ${DIR_SRCS})
target_link_libraries(${PROJECT_NAME} lua luajava viewer display thread)
unset(DIR_SRCS)

