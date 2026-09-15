# NCNN 20260526 build input

The checked-in `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64` headers,
CMake packages, and static libraries come from
Tencent's official `ncnn-20260526-android.zip`. `provenance.lock.json` records
the release URL, upstream revision, archive/library hashes, and toolchain.

All four standard Android CPU slices are staged. Vulkan is disabled. The
provider explicitly links `c++_static`, leaving one self-contained provider
`.so` per ABI and no unpackaged `libc++_shared.so` dependency. The complete
upstream license and bundled third-party notices are preserved in
`LICENSE-20260526.txt`.

On 2026-09-12 the three additional slices were copied without modification from
the same 21,045,913-byte archive, after checking its recorded SHA-256. The
existing ARM64 slice was retained byte for byte. `staticLibraries` records each
ABI's archive path, byte count, and SHA-256; `arm64StaticLibrary` is retained for
compatibility with the original provenance record. CMake selects the package
under `20260526/${ANDROID_ABI}`. No NCNN rebuild or version upgrade is involved.
