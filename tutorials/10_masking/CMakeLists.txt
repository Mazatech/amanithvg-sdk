cmake_minimum_required(VERSION 3.2 FATAL_ERROR)
project(tutorial_10)
set(CMAKE_BUILD_TYPE Release)

function (get_os_subdir ret)
    if (DEFINED OS_WINDOWS)
        set(${ret} "win" PARENT_SCOPE)
    elseif (DEFINED OS_MACOSX)
        set(${ret} "macosx" PARENT_SCOPE)
    elseif (DEFINED OS_LINUX)
        set(${ret} "linux" PARENT_SCOPE)
    elseif (DEFINED OS_IOS)
        set(${ret} "ios" PARENT_SCOPE)
    elseif (DEFINED OS_ANDROID)
        set(${ret} "android" PARENT_SCOPE)
    elseif (DEFINED OS_QNX)
        set(${ret} "qnx" PARENT_SCOPE)
    else()
        set(${ret} "" PARENT_SCOPE)
    endif()
endfunction()

function (get_arch_subdir ret)

    if (DEFINED ARCH_X86_64)
        set(${ret} "x86_64" PARENT_SCOPE)
    elseif (DEFINED ARCH_X86)
        set(${ret} "x86" PARENT_SCOPE)
    elseif (DEFINED ARCH_UB)
        set(${ret} "ub" PARENT_SCOPE)
    elseif (DEFINED ARCH_PPC64)
        # supported by Linux only
        set(${ret} "ppc64el" PARENT_SCOPE)
    elseif (DEFINED ARCH_ARMV5)
        # supported by Linux and Android
        if (DEFINED OS_ANDROID)
            set(${ret} "armeabi" PARENT_SCOPE)
        else()
            set(${ret} "arm-gnueabi" PARENT_SCOPE)
        endif()
    elseif (DEFINED ARCH_ARMV6HF)
        # supported by Linux only
        set(${ret} "arm-gnueabihf-v6" PARENT_SCOPE)
    elseif (DEFINED ARCH_ARMV7)
        # supported by Linux, Android and QNX
        if (DEFINED OS_ANDROID)
            set(${ret} "armeabi-v7a" PARENT_SCOPE)
        elseif (DEFINED OS_QNX)
            set(${ret} "armv7-le" PARENT_SCOPE)
        else()
            set(${ret} "arm-gnueabi-v7a" PARENT_SCOPE)
        endif()
    elseif (DEFINED ARCH_ARMV8)
        # supported by Linux and Android
        if (DEFINED OS_ANDROID)
            set(${ret} "arm64-v8a" PARENT_SCOPE)
        else()
            set(${ret} "aarch64" PARENT_SCOPE)
        endif()
    elseif (DEFINED ARCH_MIPS64)
        # supported by Linux and Android
        if (DEFINED OS_ANDROID)
            set(${ret} "mips64" PARENT_SCOPE)
        else()
            set(${ret} "mips64el" PARENT_SCOPE)
        endif()
    elseif (DEFINED ARCH_MIPS)
        # supported by Linux and Android
        if (DEFINED OS_ANDROID)
            set(${ret} "mips" PARENT_SCOPE)
        else()
            set(${ret} "mipsel" PARENT_SCOPE)
        endif()
    else()
        set(${ret} "" PARENT_SCOPE)
    endif()
endfunction()

function (get_backend_subdir ret)
    if (DEFINED ENGINE_SRE)
        set(${ret} "sre" PARENT_SCOPE)
    elseif(DEFINED ENGINE_GLE)
        set(${ret} "gle" PARENT_SCOPE)
    else()
        set(${ret} "" PARENT_SCOPE)
    endif()
endfunction()

# get windowing system SDK (includes and libs)
function (get_windowing_system_sdk ws_inc_dir ws_libs_dir ws_libs)

    # set empty defaults
    set(${ws_inc_dir} "" PARENT_SCOPE)
    set(${ws_libs_dir} "" PARENT_SCOPE)
    set(${ws_libs} "" PARENT_SCOPE)

    if (DEFINED OS_LINUX)

        set(base_sdk_dir "${PROJECT_BINARY_DIR}/../../3rdparts/x11_sdk")
        if (EXISTS ${base_sdk_dir})
            # we are cross compiling within our private building virtual machine
            set(base_sdk_inc "${base_sdk_dir}/include/linux")
            set(base_sdk_lib "${base_sdk_dir}/lib/linux")
            if (DEFINED ARCH_X86_64)
                set(${ws_inc_dir} "${base_sdk_inc}/x86_64" PARENT_SCOPE)
                set(${ws_libs_dir} "${base_sdk_lib}/x86_64" PARENT_SCOPE)
                set(${ws_libs} "-lX11 -Wl,--unresolved-symbols=ignore-in-shared-libs" PARENT_SCOPE)
            elseif (DEFINED ARCH_X86)
                set(${ws_inc_dir} "${base_sdk_inc}/x86" PARENT_SCOPE)
                set(${ws_libs_dir} "${base_sdk_lib}/x86" PARENT_SCOPE)
                set(${ws_libs} "-lX11 -Wl,--unresolved-symbols=ignore-in-shared-libs" PARENT_SCOPE)
            endif()
        else()
            # we are compiling on a real user machine (so we assume that X11 development libraries are already installed within the system)
            set(${ws_libs} "X11" PARENT_SCOPE)
        endif()

    endif()
endfunction()    

# get OpenGL / OpenGL ES SDK (includes and libs)
function (get_gles_sdk gles_inc_dir gles_libs_dir gles_libs)

    # set empty defaults
    set(${gles_inc_dir} "" PARENT_SCOPE)
    set(${gles_libs_dir} "" PARENT_SCOPE)
    set(${gles_libs} "" PARENT_SCOPE)

    if (DEFINED ENGINE_GLE)

        # AmanithVG GLE uses OpenGL / OpenGL ES as rendering backend

        if (DEFINED OS_WINDOWS)

            set(${gles_libs} "opengl32" PARENT_SCOPE)

        elseif (DEFINED OS_MACOSX)

            set(${gles_libs} "-framework OpenGL" PARENT_SCOPE)

        elseif (DEFINED OS_LINUX)

            set(base_sdk_dir "${PROJECT_BINARY_DIR}/../../3rdparts/gl_gles_sdk")
            if (EXISTS ${base_sdk_dir})
                # we are cross compiling within our private building virtual machine
                set(base_sdk_lib "${base_sdk_dir}/lib/linux")
                set(${gles_inc_dir} "${base_sdk_dir}/include" PARENT_SCOPE)
                if (DEFINED ARCH_X86_64)
                    set(${gles_libs_dir} "${base_sdk_lib}/x86_64" PARENT_SCOPE)
                    set(${gles_libs} "GL" PARENT_SCOPE)
                elseif (DEFINED ARCH_X86)
                    set(${gles_libs_dir} "${base_sdk_lib}/x86" PARENT_SCOPE)
                    set(${gles_libs} "GL" PARENT_SCOPE)
                endif()
            else()
                # we are compiling on a real user machine (so we assume that OpenGL development libraries are already installed within the system)
                if (DEFINED ARCH_X86_64 OR DEFINED ARCH_X86 OR DEFINED ARCH_PPC64)
                    set(${gles_libs} "GL" PARENT_SCOPE)
                else()
                    set(${gles_libs} "GLES_CM" PARENT_SCOPE)
                endif()
            endif()

        elseif (DEFINED OS_IOS)

            set(${gles_libs} "-framework OpenGLES" PARENT_SCOPE)

        elseif (DEFINED OS_ANDROID)

            if (CMAKE_COMPILER_IS_GNUCC OR CMAKE_C_COMPILER_ID MATCHES "Clang" OR CMAKE_C_COMPILER_ID MATCHES "QCC")
                # the gcc-like way to link against shared libraries
                set(${gles_libs} "-lGLESv1_CM -lEGL" PARENT_SCOPE)
            endif()

        elseif (DEFINED OS_QNX)

            if (CMAKE_COMPILER_IS_GNUCC OR CMAKE_C_COMPILER_ID MATCHES "Clang" OR CMAKE_C_COMPILER_ID MATCHES "QCC")
                # the gcc-like way to link against shared libraries
                set(${gles_libs} "-lGLESv1_CM -lEGL" PARENT_SCOPE)
            endif()

        endif()

    elseif (DEFINED ENGINE_SRE)

        # even when using AmanithVG SRE, we need OpenGL / OpenGL ES in order to blit OpenVG drawing surface pixels
        if (DEFINED OS_MACOSX)
            set(${gles_libs} "-framework OpenGL" PARENT_SCOPE)
        elseif (DEFINED OS_IOS)
            set(${gles_libs} "-framework OpenGLES" PARENT_SCOPE)
        endif()

    endif()

endfunction()

# base output directories for binaries
set(OUTPUT_BASE_DIR ${CMAKE_SOURCE_DIR}/../../bin)

# get target operating system, architecture and AmanithVG backend engine type
get_os_subdir(TARGET_OS)
get_arch_subdir(TARGET_ARCH)
get_backend_subdir(TARGET_ENGINE)

# windowing system SDK
get_windowing_system_sdk(WS_INC_DIR WS_LIBS_DIR WS_LIBS)

# OpenGL / OpenGL ES SDK
get_gles_sdk(GLES_INC_DIR GLES_LIBS_DIR GLES_LIBS)

# OpenVG SDK
set(OPENVG_BASE_DIR ${CMAKE_SOURCE_DIR}/../..)
set(OPENVG_INC_DIR ${OPENVG_BASE_DIR}/include)
set(OPENVG_LIBS_DIR ${OPENVG_BASE_DIR}/lib/${TARGET_OS}/${TARGET_ARCH}/${TARGET_ENGINE}/standalone)

if (MSVC)
    # use the 'lib' prefix for the import library, in order to match *nix systems
    set(OPENVG_LIB "libAmanithVG")
else()
    set(OPENVG_LIB "AmanithVG")
endif()

# application platform dependent source code
set(APP_SRC_PLATFORM_DIR ${CMAKE_SOURCE_DIR}/platform/${TARGET_OS})
# application platform independent source code
set(APP_SRC_DIR ${CMAKE_SOURCE_DIR}/src)

# application output directory
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${OUTPUT_BASE_DIR}/${TARGET_OS}/${TARGET_ARCH}/${TARGET_ENGINE}/standalone)

if (${CMAKE_SYSTEM_NAME} MATCHES "Windows")

    # application source code files
    set(APP_SOURCE_FILES
        ${APP_SRC_PLATFORM_DIR}/main.c
        ${APP_SRC_DIR}/${PROJECT_NAME}.c)
    # application icon file
    set(APP_ICON ${APP_SRC_PLATFORM_DIR}/tutorial.rc)

elseif (${CMAKE_SYSTEM_NAME} MATCHES "Darwin")

    set(CMAKE_MACOSX_BUNDLE TRUE)
    string(REPLACE "_" "" APP_NAME "${PROJECT_NAME}")
    set(APP_BUNDLE_IDENTIFIER "com.mazatech.${APP_NAME}")
    set(MACOSX_BUNDLE_INFO_STRING ${APP_BUNDLE_IDENTIFIER})
    set(MACOSX_BUNDLE_GUI_IDENTIFIER ${APP_BUNDLE_IDENTIFIER})
    set(MACOSX_BUNDLE_EXECUTABLE_NAME ${PROJECT_NAME})
    set(MACOSX_BUNDLE_BUNDLE_NAME ${APP_NAME})
    set(MACOSX_BUNDLE_DISPLAY_NAME ${APP_NAME})
    string(TIMESTAMP CURRENT_YEAR "%Y")
    set(MACOSX_BUNDLE_COPYRIGHT "Copyright (C) 2004-${CURRENT_YEAR} Mazatech S.r.l. All rights reserved.")

    if (DEFINED IOS OR DEFINED iOS)
        # application source code files
        set(APP_SOURCE_FILES
            ${APP_SRC_PLATFORM_DIR}/main.m
            ${APP_SRC_PLATFORM_DIR}/AppDelegate.m
            ${APP_SRC_PLATFORM_DIR}/View.m
            ${APP_SRC_PLATFORM_DIR}/ViewController.m
            ${APP_SRC_DIR}/${PROJECT_NAME}.c)
        set(APP_ICON
            # icons
            ${APP_SRC_PLATFORM_DIR}/icons/Icon.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon@2x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon@3x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-120.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-152.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-16.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-167.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-180.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-24.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-32.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-40.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-40@2x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-40@3x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-60.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-60@2x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-60@3x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-64.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-72.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-72@2x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-76.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-76@2x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-Small.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-Small@2x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-Small@3x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-Small-40.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-Small-40@2x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-Small-40@3x.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-Small-50.png
            ${APP_SRC_PLATFORM_DIR}/icons/Icon-Small-50@2x.png
            # launch images (see plist.in)
            # Retina, 640x960
            ${APP_SRC_PLATFORM_DIR}/images/iphone/Default@2x.png
            # Retina 4-inch, 640x1136
            ${APP_SRC_PLATFORM_DIR}/images/iphone/Default-568h@2x.png
            # Portrait Non-Retina, 768x1024
            ${APP_SRC_PLATFORM_DIR}/images/ipad/Default-Portrait~ipad.png
            # Portrait Retina, 1536x2048
            ${APP_SRC_PLATFORM_DIR}/images/ipad/Default-Portrait@2x~ipad.png
            # Landscape Non-Retina, 1024x768
            ${APP_SRC_PLATFORM_DIR}/images/ipad/Default-Landscape~ipad.png
            # Landscape Retina, 2048x1536
            ${APP_SRC_PLATFORM_DIR}/images/ipad/Default-Landscape@2x~ipad.png
            )
    else()
        # don't include the path in MACOSX_BUNDLE_ICON_FILE, this is the property added to Info.plist
        set(MACOSX_BUNDLE_ICON_FILE tutorial.icns)
        # application source code files
        set(APP_SOURCE_FILES
            ${APP_SRC_PLATFORM_DIR}/main.m
            ${APP_SRC_DIR}/${PROJECT_NAME}.c)
        # application icon file
        set(APP_ICON ${APP_SRC_PLATFORM_DIR}/${MACOSX_BUNDLE_ICON_FILE})
        # this part tells CMake where to find and install the file itself
        set_source_files_properties(${APP_ICON} PROPERTIES MACOSX_PACKAGE_LOCATION "Resources")
    endif()

elseif (${CMAKE_SYSTEM_NAME} MATCHES "Linux")

    # application source code files
    set(APP_SOURCE_FILES
        ${APP_SRC_PLATFORM_DIR}/main.c
        ${APP_SRC_DIR}/${PROJECT_NAME}.c)
    # generate the executable such that the loader will search for OpenVG library within the same executable folder
    set(CMAKE_INSTALL_RPATH "$ORIGIN")
    set(CMAKE_BUILD_WITH_INSTALL_RPATH TRUE)

endif()

link_directories(${WS_LIBS_DIR} ${GLES_LIBS_DIR} ${OPENVG_LIBS_DIR})

add_executable(${PROJECT_NAME} ${APP_SOURCE_FILES} ${APP_ICON})

# let the tutorial source code know the used AmanithVG rendering backend (SRE or GLE)
if (DEFINED ENGINE_SRE)
    target_compile_definitions(${PROJECT_NAME} PUBLIC AM_SRE)
elseif(DEFINED ENGINE_GLE)
    target_compile_definitions(${PROJECT_NAME} PUBLIC AM_GLE)
endif()

# include directories (windowing system, OpenGL / OpenGL ES, OpenVG, application)
target_include_directories(${PROJECT_NAME}
                           PUBLIC ${WS_INC_DIR}
                           PUBLIC ${GLES_INC_DIR}
                           PUBLIC ${OPENVG_INC_DIR}
                           PUBLIC ${APP_SRC_PLATFORM_DIR}
                           PUBLIC ${APP_SRC_DIR})

# external needed libraries (windowing system, OpenGL / OpenGL ES, OpenVG)
target_link_libraries(${PROJECT_NAME}
                      PUBLIC ${WS_LIBS}
                      PUBLIC ${GLES_LIBS}
                      PUBLIC ${OPENVG_LIB})

if (${CMAKE_SYSTEM_NAME} MATCHES "Windows")

    if (MSVC)
        # do not let Visual Studio generate an automatic manifest file; the right manifest has been included within resource file (i.e. tutorial.rc)
        set_target_properties(${PROJECT_NAME} PROPERTIES LINK_FLAGS "/MANIFEST:NO /SUBSYSTEM:WINDOWS /SAFESEH:NO")
        # specify the default startup project in a Visual Studio solution
        set_property(DIRECTORY PROPERTY VS_STARTUP_PROJECT ${PROJECT_NAME})
    elseif(MINGW)
        # build a Win32 GUI app (without a console); the flag is equivalent to /SUBSYSTEM:WINDOWS under Visual Studio
        # '-s' option will strip final executable
        set_target_properties(${PROJECT_NAME} PROPERTIES LINK_FLAGS "-mwindows -s")
    endif()

    # fix the application bundle/executable: copy the OpenVG library in order to
    # run the application as standalone, directly within its directory
    add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
                       COMMAND ${CMAKE_COMMAND} -E copy_if_different
                       ${OPENVG_LIBS_DIR}/libAmanithVG${CMAKE_SHARED_LIBRARY_SUFFIX}
                       $<TARGET_FILE_DIR:${PROJECT_NAME}>)

elseif (${CMAKE_SYSTEM_NAME} MATCHES "Darwin")

    if (DEFINED IOS OR DEFINED iOS)
        set_target_properties(${PROJECT_NAME} PROPERTIES
                              MACOSX_BUNDLE_INFO_PLIST "${APP_SRC_PLATFORM_DIR}/info.plist.in"
                              XCODE_ATTRIBUTE_COMBINE_HIDPI_IMAGES "NO"
                              XCODE_ATTRIBUTE_CODE_SIGN_IDENTITY "iPhone Developer"
                              # Universal application for both iPad and iPhone
                              XCODE_ATTRIBUTE_TARGETED_DEVICE_FAMILY "1,2"
                              XCODE_ATTRIBUTE_DEVICES "Universal"
                              RESOURCE "${APP_ICON}")
        # include basic frameworks needed by the tutorial application (iOS)
        target_link_libraries(${PROJECT_NAME} PUBLIC "-framework CoreGraphics -framework QuartzCore -framework UIKit")
    else()
        set_target_properties(${PROJECT_NAME} PROPERTIES
                              MACOSX_BUNDLE_INFO_PLIST "${APP_SRC_PLATFORM_DIR}/info.plist.in")
        # include basic frameworks needed by the tutorial application (MacOS X)
        target_link_libraries(${PROJECT_NAME} PUBLIC "-framework QuartzCore -framework Cocoa")
    endif()

    # cross compiling for MacOS X on a non-Darwin host system (e.g. Linux)
    if (NOT "${CMAKE_HOST_SYSTEM_NAME}" STREQUAL "Darwin")

        # the MacOS X cross compiler could point to some "uncomplete" SDK, so some frameworks may miss some architectures
        target_link_libraries(${PROJECT_NAME} PUBLIC "-Wl,-undefined,suppress -flat_namespace")

        # fix the application bundle/executable: copy the OpenVG library in order to
        # run the application as standalone, directly within its directory
        # NB: on a native MacOS X system this step is not required: the Xcode generator
        # will take care of AmanithVG "dependency fix" (i.e. install_name_tool)
        add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD COMMAND ${CMAKE_COMMAND}
                           -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
                           -DTARGET_EXECUTABLE_DIR=${CMAKE_RUNTIME_OUTPUT_DIRECTORY}/${PROJECT_NAME}.app/Contents/MacOS
                           -DTARGET_EXECUTABLE_FNAME=${PROJECT_NAME}${CMAKE_EXECUTABLE_SUFFIX}
                           -DOPENVG_LIBS_DIR=${OPENVG_LIBS_DIR}
                           -P CMake/fix-bundle.cmake)

    endif()

elseif (${CMAKE_SYSTEM_NAME} MATCHES "Linux")

    # fix the application bundle/executable: copy the OpenVG library in order to
    # run the application as standalone, directly within its directory
    add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
                       COMMAND ${CMAKE_COMMAND} -E copy_if_different
                       ${OPENVG_LIBS_DIR}/libAmanithVG${CMAKE_SHARED_LIBRARY_SUFFIX}.?
                       $<TARGET_FILE_DIR:${PROJECT_NAME}>)

endif()
