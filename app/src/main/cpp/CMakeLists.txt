cmake_minimum_required(VERSION 3.22.1)
project("nethunter_core")

add_library(${CMAKE_PROJECT_NAME} SHARED
            native_gadget.cpp
            native_io.cpp
            hardware_detector.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME}
                      android
                      log)
