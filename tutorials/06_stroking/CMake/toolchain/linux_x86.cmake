set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_VERSION 3)
set(CMAKE_SYSTEM_PROCESSOR i686)
# the 2 following variables are used internally by building scripts (i.e. not related to CMake variables)
set(OS_LINUX true CACHE string "Linux operating system (used internally, not related to CMake variables)")
set(ARCH_X86 true CACHE string "x86 architecture (used internally, not related to CMake variables)")

set(CMAKE_C_COMPILER gcc)
set(CMAKE_CXX_COMPILER g++)
set(CMAKE_AR ar CACHE FILEPATH "Archiver for Linux x86")
set(CMAKE_LD ld CACHE FILEPATH "Linker for Linux x86")
set(CMAKE_STRIP strip CACHE FILEPATH "Stripper for Linux x86")

set(CMAKE_LD_ARGS -m elf_i386 CACHE string "Linker additional arguments for Linux x86")

set(CMAKE_IGNORE_PATH /usr/lib/x86_64-linux-gnu/ /usr/lib/x86_64-linux-gnu/lib/)

set(CC_NO_UNINITIALIZED_WARNING "-Wno-uninitialized" CACHE string "No uninitialized variable warning for Linux x86 compiler")

# common flags
set(LINUX_COMMON_C_FLAGS "-O2 -ffast-math -fno-exceptions -fno-strict-aliasing -fomit-frame-pointer -Wall -W")
set(LINUX_COMMON_CXX_FLAGS "${LINUX_COMMON_C_FLAGS} -fno-rtti")

# architecture specific flags
set(LINUX_ARCH_C_FLAGS "-m32 -march=i686 -mtune=i686")

# flags for Release build type or configuration
set(CMAKE_C_FLAGS_RELEASE "${LINUX_COMMON_C_FLAGS} ${LINUX_ARCH_C_FLAGS}" CACHE string "Compiler C flags used by release builds for Linux x86")
set(CMAKE_CXX_FLAGS_RELEASE "${LINUX_COMMON_CXX_FLAGS} ${LINUX_ARCH_C_FLAGS}" CACHE string "Compiler C++ flags used by release builds for Linux x86")

# linker flags to be used to create shared libraries
set(CMAKE_SHARED_LINKER_FLAGS "-Wl,--no-undefined" CACHE string "Shared libraries linker flags for Linux x86")