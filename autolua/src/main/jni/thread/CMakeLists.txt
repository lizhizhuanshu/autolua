cmake_minimum_required(VERSION 3.0)
project(lizhi)

aux_source_directory(. DIR_SRCS)

add_library(${PROJECT_NAME} SHARED ${DIR_SRCS})
target_link_libraries(${PROJECT_NAME} lua)
unset(DIR_SRCS)

