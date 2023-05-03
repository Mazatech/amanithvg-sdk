# AmanithVG Tutorial 07: Alpha blending

According to OpenVG specifications, the pipeline mechanism by which primitives are rendered consists in the following steps:

 - **path/image transformation** to the drawing surface reference system (the current path-user-to-surface transformation is applied to the path geometry, producing drawing surface coordinates; for an image, the outline of the image is transformed using the image-user-to-surface transformation)

 - **rasterization** (a coverage value is computed at pixels affected by the drawn path/image)

 - **clipping and masking** (pixels not lying within the bounds of the drawing surface, or not lying within the union of the current set of active scissor rectangles, are assigned a coverage value of 0)

 - **paint generation** (at each pixel of the drawing surface, the relevant current paint is used to define a color and an alpha value)

 - **blending** (at each pixel, the source color and alpha value from the preceding stage is converted into the destination color space; the resulting color is blended with the corresponding destination color and alpha value according to the current blending rule)

Previous tutorials have introduced all the pipeline stages except the last one: now, with this tutorial, it's time to take a look at *blending*.

Full description [here](https://www.amanithvg.com/docs/tut/007-blending.html).

# How to build

If you don't have `CMake` installed on your system, please install it by following instructions at: [https://cmake.org/install](https://cmake.org/install/).
Then, according to your platform, select a toolchain file and the backend OpenVG engine, then generate the build project.

## Windows

```
// AmanithVG SRE backend, Windows x86_64, Visual Studio 2022 solution
<open x64 Native Tools Command Prompt for VS 2022>
cmake -DENGINE_SRE=1 -DCMAKE_TOOLCHAIN_FILE=./CMake/toolchain/win_x86_64.cmake --no-warn-unused-cli -G "Visual Studio 17 2022" -A x64 .
<open the generated .sln solution>

// AmanithVG GLE backend, Windows x86_64, Visual Studio 2022 solution
<open x64 Native Tools Command Prompt for VS 2022>
cmake -DENGINE_GLE=1 -DCMAKE_TOOLCHAIN_FILE=./CMake/toolchain/win_x86_64.cmake --no-warn-unused-cli -G "Visual Studio 17 2022" -A x64 .
<open the generated .sln solution>
```

## MacOS X

```
// AmanithVG SRE backend, MacOS X, Xcode project
<open a command prompt>
cmake -DENGINE_SRE=1 -DCMAKE_TOOLCHAIN_FILE=./CMake/toolchain/osx_ub.cmake --no-warn-unused-cli -G "Xcode" .
<open the generated .xcodeproj project>

// AmanithVG GLE backend, MacOS X, Xcode project
<open a command prompt>
cmake -DENGINE_GLE=1 -DCMAKE_TOOLCHAIN_FILE=./CMake/toolchain/osx_ub.cmake --no-warn-unused-cli -G "Xcode" .
<open the generated .xcodeproj project>
```

## Linux

```
// AmanithVG SRE backend, Linux x86_64, standard Makefile
<open a command prompt>
cmake -DENGINE_SRE=1 -DCMAKE_TOOLCHAIN_FILE=./CMake/toolchain/linux_x86_64.cmake --no-warn-unused-cli -G "Unix Makefiles" .
make

// AmanithVG GLE backend, Linux x86_64, standard Makefile
<open a command prompt>
cmake -DENGINE_GLE=1 -DCMAKE_TOOLCHAIN_FILE=./CMake/toolchain/linux_x86_64.cmake --no-warn-unused-cli -G "Unix Makefiles" .
make
```

## Android

The viewer for Android can be compiled directly with [Android Studio](https://developer.android.com/studio) (you don't need to use CMake).
Open project located in `platform/android` subdirectory.

AmanithVG SRE (software rendering) is the default, you can switch to AmanithVG GLE (OpenGL rendering), editing `platform/android/app/src/main/res/values/bools.xml`:

```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <bool name="sreBackend">false</bool>
</resources>
```

## iOS

The viewer for iOS can be compiled using Xcode only.

```
// AmanithVG SRE backend, iOS, Xcode project
<open a command prompt>
cmake -DENGINE_SRE=1 -DCMAKE_TOOLCHAIN_FILE=./CMake/toolchain/ios_ub.cmake --no-warn-unused-cli -G "Xcode" .
<open the generated .xcodeproj project>

// AmanithVG GLE backend, iOS, Xcode project
<open a command prompt>
cmake -DENGINE_GLE=1 -DCMAKE_TOOLCHAIN_FILE=./CMake/toolchain/ios_ub.cmake --no-warn-unused-cli -G "Xcode" .
<open the generated .xcodeproj project>
```
