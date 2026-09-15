cmake_minimum_required(VERSION 3.22.1)

project(autojs_yolo LANGUAGES CXX)

set(supported_abis arm64-v8a armeabi-v7a x86 x86_64)
if(NOT ANDROID_ABI IN_LIST supported_abis)
    message(FATAL_ERROR "Unsupported Android ABI: ${ANDROID_ABI}")
endif()

set(ncnn_DIR
    "${CMAKE_CURRENT_LIST_DIR}/../../../../third_party/ncnn/20260526/${ANDROID_ABI}/lib/cmake/ncnn"
)
find_package(ncnn 20260526 EXACT REQUIRED CONFIG)

add_library(autojs_yolo SHARED yolo_jni.cpp)

target_compile_features(autojs_yolo PRIVATE cxx_std_17)
target_compile_options(autojs_yolo PRIVATE
    -Wall
    -Wextra
    -Wformat=2
    -Wshadow
)

target_link_libraries(autojs_yolo PRIVATE ncnn android log)

# Keep 16 KB alignment explicit when changing toolchains.
target_link_options(autojs_yolo PRIVATE
    "-Wl,-z,max-page-size=16384"
    "-Wl,-z,common-page-size=16384"
)
