# NCNN 20260526 build input

The checked-in `arm64-v8a` headers, CMake package, and static library come from
Tencent's official `ncnn-20260526-android.zip`. `provenance.lock.json` records
the release URL, upstream revision, archive/library hashes, and toolchain.

Only the CPU `arm64-v8a` slice is staged. Vulkan and other ABIs remain outside
R1. The provider explicitly links `c++_static`, leaving one self-contained
provider `.so` and no unpackaged `libc++_shared.so` dependency. The complete
upstream license and bundled third-party notices are preserved in
`LICENSE-20260526.txt`.
