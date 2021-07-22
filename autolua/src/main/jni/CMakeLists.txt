cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../libs/${ANDROID_ABI})

add_definitions(-DLUA_DL_DLOPEN -DLUA_COMPAT_5_3 -DLUA_USE_LINUX)

#设置头文件搜索路径（和此txt同个路径的头文件无需设置）
INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/lua)

ADD_SUBDIRECTORY(${CMAKE_CURRENT_SOURCE_DIR}/lua)
ADD_SUBDIRECTORY(${CMAKE_CURRENT_SOURCE_DIR}/view)
ADD_SUBDIRECTORY(${CMAKE_CURRENT_SOURCE_DIR}/input)

add_library(autolua SHARED ./autolua.cpp ./display.cpp ./thread.cpp)

target_link_libraries(autolua
        lua
        view
        input)