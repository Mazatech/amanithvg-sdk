set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_VERSION 3)
set(CMAKE_SYSTEM_PROCESSOR armv7-a)
# the 2 following variables are used internally by building scripts (i.e. not related to CMake variables)
set(OS_LINUX true CACHE string "Linux operating system (used internally, not related to CMake variables)")
set(ARCH_ARMV7 true CACHE string "armv7 architecture (used internally, not related to CMake variables)")

# assembler
set(CMAKE_ASM_COMPILER /usr/bin/arm-linux-gnueabi-as)
set(CMAKE_ASM_PREPROCESSOR /usr/bin/arm-linux-gnueabi-gcc-6 CACHE FILEPATH "Preprocessor for Linux armv7")
set(CMAKE_ASM_COMPILE_OBJECT
    "${CMAKE_ASM_PREPROCESSOR} <DEFINES> <INCLUDES> $(C_FLAGS) $(C_DEFINES) -o <OBJECT>_pp.s -E <SOURCE>"
    "${CMAKE_ASM_COMPILER} ${CMAKE_ASM_FLAGS} -o <OBJECT> <OBJECT>_pp.s")
set(CMAKE_C_COMPILER /usr/bin/arm-linux-gnueabi-gcc-6)
set(CMAKE_CXX_COMPILER /usr/bin/arm-linux-gnueabi-g++-6)
set(CMAKE_AR /usr/bin/arm-linux-gnueabi-ar CACHE FILEPATH "Archiver for Linux armv7")
set(CMAKE_LD /usr/bin/arm-linux-gnueabi-ld CACHE FILEPATH "Linker for Linux armv7")
set(CMAKE_STRIP /usr/bin/arm-linux-gnueabi-strip CACHE FILEPATH "Stripper for Linux armv7")

set(CMAKE_FIND_ROOT_PATH /usr/arm-linux-gnueabi)
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

set(CMAKE_IGNORE_PATH /usr/lib/x86_64-linux-gnu/ /usr/lib/x86_64-linux-gnu/lib/)

set(CC_NO_UNINITIALIZED_WARNING "-Wno-uninitialized" CACHE string "No uninitialized variable warning for Linux armv7 compiler")

# common flags
set(LINUX_COMMON_C_FLAGS "-O2 -ffast-math -fno-exceptions -fno-strict-aliasing -fomit-frame-pointer -Wall -W")
set(LINUX_COMMON_CXX_FLAGS "${LINUX_COMMON_C_FLAGS} -fno-rtti")

# architecture specific flags
set(LINUX_ARCH_C_FLAGS "-march=armv7-a -msoft-float -mfloat-abi=softfp -mfpu=vfpv3-d16 -Wa,-march=armv7-a -Wa,-mfpu=vfpv3-d16 -mlittle-endian")

# flags for Release build type or configuration
set(CMAKE_C_FLAGS_RELEASE "${LINUX_COMMON_C_FLAGS} ${LINUX_ARCH_C_FLAGS}" CACHE string "Compiler C flags used by release builds for Linux armv7")
set(CMAKE_CXX_FLAGS_RELEASE "${LINUX_COMMON_CXX_FLAGS} ${LINUX_ARCH_C_FLAGS}" CACHE string "Compiler C++ flags used by release builds for Linux armv7")

# linker flags to be used to create shared libraries
set(CMAKE_SHARED_LINKER_FLAGS "-Wl,--no-undefined" CACHE string "Shared libraries linker flags for Linux armv7")