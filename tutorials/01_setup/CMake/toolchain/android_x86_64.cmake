# Standard settings
set(CMAKE_SYSTEM_NAME Android)
set(CMAKE_SYSTEM_PROCESSOR x86_64)
set(CMAKE_ANDROID_API_MIN 21)
# the 2 following variables are used internally by building scripts (i.e. not related to CMake variables)
set(OS_ANDROID true CACHE STRING "Android operating system (used internally, not related to CMake variables)")
set(ARCH_X86_64 true CACHE STRING "x86_64 architecture (used internally, not related to CMake variables)")

set(cross_triple x86_64-linux-android)
set(CMAKE_ANDROID_STANDALONE_TOOLCHAIN /usr/${cross_triple}/)

# assembler
set(CMAKE_ASM_COMPILER /usr/${cross_triple}/bin/${cross_triple}-as)
set(CMAKE_ASM_PREPROCESSOR /usr/${cross_triple}/bin/${cross_triple}-clang CACHE FILEPATH "Preprocessor for Android x86_64")
set(CMAKE_ASM_COMPILE_OBJECT
    "${CMAKE_ASM_PREPROCESSOR} <DEFINES> <INCLUDES> $(C_FLAGS) $(C_DEFINES) -o <OBJECT>_pp.s -E <SOURCE>"
    "${CMAKE_ASM_COMPILER} ${CMAKE_ASM_FLAGS} -o <OBJECT> <OBJECT>_pp.s")
# c / c++ compilers
set(CMAKE_C_COMPILER /usr/${cross_triple}/bin/${cross_triple}-clang)
set(CMAKE_CXX_COMPILER /usr/${cross_triple}/bin/${cross_triple}-clang++)
# archiver
set(CMAKE_AR /usr/${cross_triple}/bin/${cross_triple}-ar CACHE FILEPATH "Archiver for Android x86_64")
# linker
set(CMAKE_LD /usr/${cross_triple}/bin/${cross_triple}-ld CACHE FILEPATH "Linker for Android x86_64")
# stripper
set(CMAKE_STRIP /usr/${cross_triple}/bin/${cross_triple}-strip CACHE FILEPATH "Stripper for Android x86_64")

set(CMAKE_FIND_ROOT_PATH /usr/${cross_triple})
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
set(CMAKE_SYSROOT /usr/${cross_triple}/sysroot)
set(CMAKE_IGNORE_PATH /usr/lib/x86_64-linux-gnu/ /usr/lib/x86_64-linux-gnu/lib/)

set(CC_NO_UNINITIALIZED_WARNING "-Wno-uninitialized" CACHE STRING "No uninitialized variable warning for Android x86_64 compiler")

# flags for Release build type or configuration
set(ANDROID_COMMON_C_FLAGS "-O2 -fpic -ffast-math -fno-exceptions -fno-strict-aliasing -Wformat -Werror=format-security -ffunction-sections -funwind-tables -no-canonical-prefixes -fomit-frame-pointer -Wall -W")
set(ANDROID_COMMON_CXX_FLAGS "${ANDROID_COMMON_C_FLAGS} -fno-rtti")

if (CMAKE_COMPILER_IS_GNUCC)
    set(CMAKE_C_FLAGS_RELEASE "${ANDROID_COMMON_C_FLAGS} -Wa,--noexecstack -finline-limit=300 -funswitch-loops" CACHE STRING "Compiler C flags used by release builds for Android x86_64")
    set(CMAKE_CXX_FLAGS_RELEASE "${ANDROID_COMMON_CXX_FLAGS} -Wa,--noexecstack -finline-limit=300 -funswitch-loops" CACHE STRING "Compiler C++ flags used by release builds for Android x86_64")
else()
    # clang does not support '-finline-limit' switch
    set(CMAKE_C_FLAGS_RELEASE "${ANDROID_COMMON_C_FLAGS} -Xclang -mnoexecstack" CACHE STRING "Compiler C flags used by release builds for Android x86_64")
    set(CMAKE_CXX_FLAGS_RELEASE "${ANDROID_COMMON_CXX_FLAGS} -Xclang -mnoexecstack" CACHE STRING "Compiler C++ flags used by release builds for Android x86_64")
endif()

# linker flags to be used to create shared libraries
set(CMAKE_SHARED_LINKER_FLAGS "-Wl,--no-undefined -Wl,-z,noexecstack -Wl,-z,relro -Wl,-z,now" CACHE STRING "Shared libraries linker flags for Android x86_64")
