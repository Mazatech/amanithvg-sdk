# Standard settings QNX 6.6
set(CMAKE_SYSTEM_NAME QNX)
set(CMAKE_SYSTEM_VERSION 6.6)
# the 2 following variables are used internally by building scripts (i.e. not related to CMake variables)
set(OS_QNX true CACHE STRING "QNX operating system (used internally, not related to CMake variables)")
set(ARCH_ARMV7 true CACHE STRING "armv7 architecture (used internally, not related to CMake variables)")

if (NOT DEFINED ENV{QNX_HOST})
    message(FATAL_ERROR "\nQNX_HOST environment variable is not set! Did you set QNX environment variables? (e.g. source /usr/qnx660/qnx660-env.sh)\n")
else()
    set(QNX_HOST $ENV{QNX_HOST})
endif()

if (NOT DEFINED ENV{QNX_TARGET})
    message(FATAL_ERROR "\nQNX_TARGET environment variable is not set! Did you set QNX environment variables? (e.g. source /usr/qnx660/qnx660-env.sh)\n")
else()
    set(QNX_TARGET $ENV{QNX_TARGET})
endif()

set(arch gcc_ntoarmv7le)

set(CMAKE_C_COMPILER ${QNX_HOST}/usr/bin/qcc)
set(CMAKE_C_COMPILER_TARGET ${arch})
set(CMAKE_CXX_COMPILER ${QNX_HOST}/usr/bin/qcc)
set(CMAKE_CXX_COMPILER_TARGET ${arch})
set(CMAKE_AR ${QNX_HOST}/usr/bin/ntoarmv7-ar CACHE FILEPATH "Archiver for QNX armv7")
set(CMAKE_LD ${QNX_HOST}/usr/bin/ntoarmv7-ld CACHE FILEPATH "Linker for QNX armv7")
set(CMAKE_STRIP ${QNX_HOST}/usr/bin/ntoarmv7-strip CACHE FILEPATH "Stripper for QNX armv7")

set(CMAKE_FIND_ROOT_PATH ${QNX_HOST}/usr ${QNX_TARGET})
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

set(CMAKE_IGNORE_PATH /usr/lib/x86_64-linux-gnu/ /usr/lib/x86_64-linux-gnu/lib/)

set(CC_NO_UNINITIALIZED_WARNING "-Wno-uninitialized" CACHE STRING "No uninitialized variable warning for QNX armv7 compiler")