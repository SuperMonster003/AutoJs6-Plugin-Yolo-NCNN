cmake_minimum_required(VERSION 3.22.1)

project(autojs_yolo LANGUAGES CXX)

if(NOT ANDROID_ABI STREQUAL "arm64-v8a")
    message(FATAL_ERROR "R1 supports arm64-v8a only (requested ${ANDROID_ABI})")
endif()

set(ncnn_DIR
    "${CMAKE_CURRENT_LIST_DIR}/../../../../third_party/ncnn/20260526/arm64-v8a/lib/cmake/ncnn"
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
