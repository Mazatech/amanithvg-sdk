# Darwin 19
set(CMAKE_SYSTEM_NAME Darwin)
set(CMAKE_SYSTEM_VERSION 19)
set(APPLE true CACHE STRING "APPLE target")
# the 2 following variables are used internally by building scripts (i.e. not related to CMake variables)
set(OS_MACOSX true CACHE STRING "MacOS X operating system (used internally, not related to CMake variables)")
set(ARCH_UB true CACHE STRING "Universal binaries x86/x86_64 (used internally, not related to CMake variables)")

if (NOT "${CMAKE_HOST_SYSTEM_NAME}" STREQUAL "Darwin")

    # cross compiling for MacOS X on a non-Darwin host system (e.g. Linux)
    
    set(OSX_TOOLCHAIN /usr/osxcross)
    set(OSX_SDK /usr/osxcross/SDK/MacOSX10.15.sdk)
    set(OSX_BINARIES_PREFIX x86_64-apple-darwin${CMAKE_SYSTEM_VERSION}-)

    set(CMAKE_C_COMPILER ${OSX_TOOLCHAIN}/bin/${OSX_BINARIES_PREFIX}clang)
    set(CMAKE_CXX_COMPILER ${OSX_TOOLCHAIN}/bin/${OSX_BINARIES_PREFIX}clang++)
    set(CMAKE_AR ${OSX_TOOLCHAIN}/bin/${OSX_BINARIES_PREFIX}ar CACHE FILEPATH "Archiver for MacOS X")
    set(CMAKE_LD ${OSX_TOOLCHAIN}/bin/${OSX_BINARIES_PREFIX}ld CACHE FILEPATH "Linker for MacOS X")
    set(CMAKE_LIPO ${OSX_TOOLCHAIN}/bin/${OSX_BINARIES_PREFIX}lipo CACHE FILEPATH "Lipo tool for MacOS X")
    set(CMAKE_STRIP ${OSX_TOOLCHAIN}/bin/${OSX_BINARIES_PREFIX}strip CACHE FILEPATH "Strip tool for MacOS X")
    set(CMAKE_INSTALL_NAME_TOOL ${OSX_TOOLCHAIN}/bin/${OSX_BINARIES_PREFIX}install_name_tool CACHE FILEPATH "install_name_tool for MacOS X")

    set(CMAKE_OSX_SYSROOT ${OSX_SDK})
    set(CMAKE_FIND_ROOT_PATH ${OSX_TOOLCHAIN} ${OSX_SDK}/usr)
    set(CMAKE_IGNORE_PATH /usr/lib/x86_64-linux-gnu/ /usr/lib/x86_64-linux-gnu/lib/)

else()

    # compiling for MacOS X on a MacOS X host system

    set(OSX_TOOLCHAIN /Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr)
    set(OSX_SDK /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk)

    set(CMAKE_C_COMPILER ${OSX_TOOLCHAIN}/bin/clang)
    set(CMAKE_CXX_COMPILER ${OSX_TOOLCHAIN}/bin/clang++)
    set(CMAKE_AR ${OSX_TOOLCHAIN}/bin/ar CACHE FILEPATH "Archiver for MacOS X")
    set(CMAKE_LD ${OSX_TOOLCHAIN}/bin/ld CACHE FILEPATH "Linker for MacOS X")
    set(CMAKE_LIPO ${OSX_TOOLCHAIN}/bin/lipo CACHE FILEPATH "Lipo tool for MacOS X")
    set(CMAKE_STRIP ${OSX_TOOLCHAIN}/bin/strip CACHE FILEPATH "Strip tool for MacOS X")
    set(CMAKE_INSTALL_NAME_TOOL ${OSX_TOOLCHAIN}/bin/install_name_tool CACHE FILEPATH "install_name_tool for MacOS X")

    set(CMAKE_OSX_SYSROOT ${OSX_SDK})
    set(CMAKE_FIND_ROOT_PATH ${OSX_TOOLCHAIN} ${OSX_SDK}/usr)

endif()

set(CMAKE_OSX_ARCHITECTURES "x86_64" CACHE STRING "Build architectures for MacOS X")
set(CMAKE_OSX_DEPLOYMENT_TARGET "10.12" CACHE STRING "Deployment target for MacOS X")

set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM BOTH)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

set(CC_NO_UNINITIALIZED_WARNING "-Wno-uninitialized" CACHE STRING "No uninitialized variable warning for MacOS X compiler")

# common flags
set(MACOSX_COMMON_C_FLAGS "-O2 -ffast-math -fno-exceptions -fno-strict-aliasing -fomit-frame-pointer -Wall -W")
set(MACOSX_COMMON_CXX_FLAGS "${MACOSX_COMMON_C_FLAGS} -fno-rtti")

# flags for Release build type or configuration
set(CMAKE_C_FLAGS_RELEASE "${MACOSX_COMMON_C_FLAGS}" CACHE STRING "Compiler C flags used by release builds for MacOS X")
set(CMAKE_CXX_FLAGS_RELEASE "${MACOSX_COMMON_CXX_FLAGS}" CACHE STRING "Compiler C++ flags used by release builds for MacOS X")
