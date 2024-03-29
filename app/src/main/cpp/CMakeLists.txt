# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# 设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../../../jniLibs/${ANDROID_ABI})

# file 文件操作命令。
# GLOB 会根据后面表达式匹配文件组成列表，保存在变量中。
# 不推荐使用，因为：如果CMakeLists.txt文件没有改变，即便在该源码树中添加或删除文件，
# 产生的构建系统也不会知道何时该要求CMake重新产生构建文件。
file(GLOB bzip2 bzip2/*.c)

# 导入头文件就可以不使用路径+头文件了，并且可以使用<>代替""引入
include_directories(bzip2)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        bspatch

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp
        bspatch.c
        ${bzip2})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        bspatch

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})