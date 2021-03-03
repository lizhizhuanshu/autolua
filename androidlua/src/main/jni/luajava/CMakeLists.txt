cmake_minimum_required(VERSION 3.4.1)


aux_source_directory(. DIR_SRCS)
add_library(luajava SHARED ${DIR_SRCS})

target_link_libraries(luajava lua)