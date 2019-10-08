set(CMAKE_SYSTEM_NAME Windows)
set(CMAKE_SYSTEM_VERSION 5)
set(CMAKE_SYSTEM_PROCESSOR i686)
# the 2 following variables are used internally by building scripts (i.e. not related to CMake variables)
set(OS_WINDOWS true CACHE STRING "Windows operating system (used internally, not related to CMake variables)")
set(ARCH_X86 true CACHE STRING "x86 architecture (used internally, not related to CMake variables)")

if (NOT "${CMAKE_HOST_SYSTEM_NAME}" STREQUAL "Windows")

    # cross compiling for Windows on a non-Windows host system (e.g. Linux)

    set(CMAKE_C_COMPILER /usr/bin/i686-w64-mingw32-gcc)
    set(CMAKE_CXX_COMPILER /usr/bin/i686-w64-mingw32-g++)
    set(CMAKE_RC_COMPILER /usr/bin/i686-w64-mingw32-windres)
    set(CMAKE_RANLIB /usr/bin/i686-w64-mingw32-ranlib)
    set(CMAKE_AR /usr/bin/i686-w64-mingw32-ar CACHE FILEPATH "Archiver for Windows x86")
    set(CMAKE_LD /usr/bin/i686-w64-mingw32-ld CACHE FILEPATH "Linker for Windows x86")
    set(CMAKE_STRIP /usr/bin/i686-w64-mingw32-strip CACHE FILEPATH "Strip for Windows x86")
    set(CMAKE_DLLTOOL /usr/bin/i686-w64-mingw32-dlltool CACHE FILEPATH "DLLTool for Windows x86")
    set(CMAKE_DLLTOOL_ARGS "-m i386" CACHE STRING "DLLTool arguments for Windows x86")
    
    set(CMAKE_FIND_ROOT_PATH /usr/i686-w64-mingw32)
    set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
    set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
    set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

    set(CMAKE_IGNORE_PATH /usr/lib/x86_64-linux-gnu/ /usr/lib/x86_64-linux-gnu/lib/)

    set(CC_NO_UNINITIALIZED_WARNING "-Wno-uninitialized" CACHE STRING "No uninitialized variable warning for Windows x86 compiler")

    # common flags
    set(WINDOWS_COMMON_C_FLAGS "-O2 -ffast-math -fno-exceptions -fno-strict-aliasing -fomit-frame-pointer -Wall -W")
    set(WINDOWS_COMMON_CXX_FLAGS "${WINDOWS_COMMON_C_FLAGS} -fno-rtti")
    # architecture specific flags
    set(WINDOWS_ARCH_C_FLAGS "-m32 -march=i686 -mtune=i686")

else()

    # compiling for Windows on a Windows host system, with Visual Studio
    set(CMAKE_C_COMPILER cl)
    set(CMAKE_CXX_COMPILER cl)

    set(CC_NO_UNINITIALIZED_WARNING "")

    # common flags
    set(WINDOWS_COMMON_C_FLAGS "/O2 /Ob2 /Ot /Oy /fp:fast /MD /GS- /nologo /W3")
    set(WINDOWS_COMMON_CXX_FLAGS "${WINDOWS_COMMON_C_FLAGS} /GR-")

    # linker flags to be used to create shared libraries
    set(WINDOWS_COMMON_SHARED_LINKER_FLAGS "/NOLOGO /SUBSYSTEM:WINDOWS /OPT:REF /OPT:ICF /INCREMENTAL:NO")
    set(WINDOWS_ARCH_SHARED_LINKER_FLAGS "/MACHINE:X86")
    set(CMAKE_SHARED_LINKER_FLAGS "${WINDOWS_COMMON_SHARED_LINKER_FLAGS} ${WINDOWS_ARCH_SHARED_LINKER_FLAGS} " CACHE STRING "Shared libraries linker flags for Windows x86")

endif()

# flags for Release build type or configuration
set(CMAKE_C_FLAGS_RELEASE "${WINDOWS_COMMON_C_FLAGS} ${WINDOWS_ARCH_C_FLAGS}" CACHE STRING "Compiler C flags used by release builds for Windows x86")
set(CMAKE_CXX_FLAGS_RELEASE "${WINDOWS_COMMON_CXX_FLAGS} ${WINDOWS_ARCH_C_FLAGS}" CACHE STRING "Compiler C++ flags used by release builds for Windows x86")
