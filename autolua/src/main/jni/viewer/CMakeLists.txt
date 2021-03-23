cmake_minimum_required(VERSION 3.0)
set(CMAKE_CXX_STANDARD 17)
project(viewer)

aux_source_directory(. DIR_SRCS)

add_library(${PROJECT_NAME} STATIC ${DIR_SRCS})
target_link_libraries(${PROJECT_NAME} lua)
unset(DIR_SRCS)

