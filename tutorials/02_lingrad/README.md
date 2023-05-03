# AmanithVG Tutorial 02: Linear gradients

Using OpenVG API it is possible to draw vector paths, specifying a paint for the fill (i.e.: the bounded inside region defined by its contours) and a paint for the stroke (i.e. the "widening" of path edges using a straight-line pen). The are a total of four possible paint types: plain colors, linear gradients, radial gradients and bitmap patterns.

This tutorial will introduce OpenVG linear gradients.

Full description [here](https://www.amanithvg.com/docs/tut/002-linear-gradients.html).

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
