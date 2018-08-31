# Standard settings
set(CMAKE_SYSTEM_NAME Darwin)
set(CMAKE_SYSTEM_VERSION 11)
set(APPLE true CACHE string "APPLE target")
set(iOS true CACHE string "iOS target")
set(IOS true CACHE string "IOS target")
# the 2 following variables are used internally by building scripts (i.e. not related to CMake variables)
set(OS_IOS true CACHE string "iOS operating system (used internally, not related to CMake variables)")
set(ARCH_UB true CACHE string "Universal binaries armv7/arm64 (used internally, not related to CMake variables)")

if (NOT "${CMAKE_HOST_SYSTEM_NAME}" STREQUAL "Darwin")

    # cross compiling for iOS on a non-Darwin host system (e.g. Linux)

    set(IOS_TOOLCHAIN /usr/ioscross/cctools-port/usage_examples/ios_toolchain/target)
    set(IOS_SDK ${IOS_TOOLCHAIN}/SDK/iPhoneOS9.3.sdk)

    set(CMAKE_C_COMPILER ${IOS_TOOLCHAIN}/bin/arm-apple-darwin11-clang)
    set(CMAKE_CXX_COMPILER ${IOS_TOOLCHAIN}/bin/arm-apple-darwin11-clang++)
    set(CMAKE_AR ${IOS_TOOLCHAIN}/bin/arm-apple-darwin11-ar CACHE FILEPATH "Archiver for iOS")
    set(CMAKE_LD ${IOS_TOOLCHAIN}/bin/arm-apple-darwin11-ld CACHE FILEPATH "Linker for iOS")
    set(CMAKE_LIPO ${IOS_TOOLCHAIN}/bin/arm-apple-darwin11-lipo CACHE FILEPATH "Lipo tool for iOS")
    set(CMAKE_STRIP ${IOS_TOOLCHAIN}/bin/arm-apple-darwin11-strip CACHE FILEPATH "Strip tool for iOS")
    set(CMAKE_INSTALL_NAME_TOOL ${IOS_TOOLCHAIN}/bin/arm-apple-darwin11-install_name_tool CACHE FILEPATH "install_name_tool for iOS")

    set(CMAKE_OSX_SYSROOT ${IOS_SDK})
    set(CMAKE_FIND_ROOT_PATH ${IOS_TOOLCHAIN} ${IOS_SDK}/usr)
    set(CMAKE_IGNORE_PATH /usr/lib/x86_64-linux-gnu/ /usr/lib/x86_64-linux-gnu/lib/)

else()

    # compiling for iOS on a MacOS X host system

    set(IOS_TOOLCHAIN /Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr)
    set(IOS_SDK /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk)

    set(CMAKE_C_COMPILER ${IOS_TOOLCHAIN}/bin/clang)
    set(CMAKE_CXX_COMPILER ${IOS_TOOLCHAIN}/bin/clang++)
    set(CMAKE_AR ${IOS_TOOLCHAIN}/bin/ar CACHE FILEPATH "Archiver for iOS")
    set(CMAKE_LD ${IOS_TOOLCHAIN}/bin/ld CACHE FILEPATH "Linker for iOS")
    set(CMAKE_LIPO ${IOS_TOOLCHAIN}/bin/lipo CACHE FILEPATH "Lipo tool for iOS")
    set(CMAKE_STRIP ${IOS_TOOLCHAIN}/bin/strip CACHE FILEPATH "Strip tool for iOS")
    set(CMAKE_INSTALL_NAME_TOOL ${IOS_TOOLCHAIN}/bin/install_name_tool CACHE FILEPATH "install_name_tool for iOS")

    set(CMAKE_OSX_SYSROOT ${IOS_SDK})
    set(CMAKE_FIND_ROOT_PATH ${IOS_TOOLCHAIN} ${IOS_SDK}/usr)

    # in order to check compilers, cmake will try (as default) to build test applications
    # that for iOS need code signing; so we skip the generation of test executables
    # we could have used CMakeForceCompiler module, but the modern way is to use
    # the CMAKE_TRY_COMPILE_TARGET_TYPE within the toolchain file
    set(CMAKE_TRY_COMPILE_TARGET_TYPE "STATIC_LIBRARY")

endif()

set(CMAKE_OSX_ARCHITECTURES "arm64" CACHE string "Build architectures for iOS")
set(CMAKE_OSX_DEPLOYMENT_TARGET "9.3" CACHE string "Deployment target for iOS")
set(CMAKE_XCODE_ATTRIBUTE_IPHONEOS_DEPLOYMENT_TARGET "9.3" CACHE string "Xcode deployment target for iOS")

set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM BOTH)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

set(CC_NO_UNINITIALIZED_WARNING "-Wno-uninitialized" CACHE string "No uninitialized variable warning for iOS compiler")

# common flags
set(IOS_COMMON_C_FLAGS "-O2 -ffast-math -fno-exceptions -fno-strict-aliasing -fomit-frame-pointer -fembed-bitcode -Wall -W")
set(IOS_COMMON_CXX_FLAGS "${IOS_COMMON_C_FLAGS} -fno-rtti")

# flags for Release build type or configuration
set(CMAKE_C_FLAGS_RELEASE "${IOS_COMMON_C_FLAGS}" CACHE string "Compiler C flags used by release builds for iOS")
set(CMAKE_CXX_FLAGS_RELEASE "${IOS_COMMON_CXX_FLAGS}" CACHE string "Compiler C++ flags used by release builds for iOS")
