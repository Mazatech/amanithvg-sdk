# CHANGES BETWEEN 5.0.0 and 6.0.2

- AmanithVG is now thread safe: all the exposed functions can be called from multiple threads at the same time

- Some parts of the rasterizer have been rewritten by implementing better data structures, increasing efficiency in terms of memory consumption and performance

- Configuration via external file has been removed, in favor of an equivalent (and more powerful) runtime API (`vgConfigGetMZT` / `vgConfigSetMZT`)

- New image filters have been implemented (as OpenVG extension): `vgGaussianBlurMZT`, `vgLightingMZT`, `vgMorphologyMZT`, `vgTurbulenceMZT`, `vgDisplacementMapMZT`, `vgCompositeMZT`

- Removed builds relative to deprecated Android ABI (ARMv5, 32 and 64 bit MIPS)

- Removed builds relative to 32 bit architectures on iOS (ARMv7) and MacOS X (x86): now both iOS and MacOS X builds are Universal Binaries for arm64 (Apple Silicon) and x86_64 architectures

- Tutorials have been have been improved from the point of view of compatibility with various building systems (CMake, Gradle)

- Lot of minor fixes and improvements
