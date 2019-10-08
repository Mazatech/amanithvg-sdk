set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_VERSION 3)
set(CMAKE_SYSTEM_PROCESSOR armv6kz)
# the 2 following variables are used internally by building scripts (i.e. not related to CMake variables)
set(OS_LINUX true CACHE STRING "Linux operating system (used internally, not related to CMake variables)")
set(ARCH_ARMV6HF true CACHE STRING "armv6hf architecture (used internally, not related to CMake variables)")

# assembler
set(CMAKE_ASM_COMPILER /usr/bin/arm-linux-gnueabihf-as)
set(CMAKE_ASM_PREPROCESSOR /usr/bin/arm-linux-gnueabihf-gcc-6 CACHE FILEPATH "Preprocessor for Linux armv6hf")
set(CMAKE_ASM_COMPILE_OBJECT
    "${CMAKE_ASM_PREPROCESSOR} <DEFINES> <INCLUDES> $(C_FLAGS) $(C_DEFINES) -o <OBJECT>_pp.s -E <SOURCE>"
    "${CMAKE_ASM_COMPILER} ${CMAKE_ASM_FLAGS} -o <OBJECT> <OBJECT>_pp.s")
set(CMAKE_C_COMPILER /usr/bin/arm-linux-gnueabihf-gcc-6)
set(CMAKE_CXX_COMPILER /usr/bin/arm-linux-gnueabihf-g++-6)
set(CMAKE_AR /usr/bin/arm-linux-gnueabihf-ar CACHE FILEPATH "Archiver for Linux armv6hf")
set(CMAKE_LD /usr/bin/arm-linux-gnueabihf-ld CACHE FILEPATH "Linker for Linux armv6hf")
set(CMAKE_STRIP /usr/bin/arm-linux-gnueabihf-strip CACHE FILEPATH "Stripper for Linux armv6hf")

set(CMAKE_FIND_ROOT_PATH /usr/arm-linux-gnueabihf)
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

set(CMAKE_IGNORE_PATH /usr/lib/x86_64-linux-gnu/ /usr/lib/x86_64-linux-gnu/lib/)

set(CC_NO_UNINITIALIZED_WARNING "-Wno-uninitialized" CACHE STRING "No uninitialized variable warning for Linux armv6hf compiler")

# common flags
set(LINUX_COMMON_C_FLAGS "-O2 -ffast-math -fno-exceptions -fno-strict-aliasing -fomit-frame-pointer -Wall -W")
set(LINUX_COMMON_CXX_FLAGS "${LINUX_COMMON_C_FLAGS} -fno-rtti")

# architecture specific flags
set(LINUX_ARCH_C_FLAGS "-march=armv6kz -mtune=arm1176jzf-s -mhard-float -mfloat-abi=hard -mfpu=vfp -Wa,-march=armv6kz -Wa,-mfpu=vfp -mlittle-endian -marm")

# flags for Release build type or configuration
set(CMAKE_C_FLAGS_RELEASE "${LINUX_COMMON_C_FLAGS} ${LINUX_ARCH_C_FLAGS}" CACHE STRING "Compiler C flags used by release builds for Linux armv6hf")
set(CMAKE_CXX_FLAGS_RELEASE "${LINUX_COMMON_CXX_FLAGS} ${LINUX_ARCH_C_FLAGS}" CACHE STRING "Compiler C++ flags used by release builds for Linux armv6hf")

# linker flags to be used to create shared libraries
set(CMAKE_SHARED_LINKER_FLAGS "-Wl,--no-undefined" CACHE STRING "Shared libraries linker flags for Linux armv6hf")