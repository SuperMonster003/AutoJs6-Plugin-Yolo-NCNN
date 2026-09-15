******

### Release History

******

# v0.1.2

###### 2026/09/13

* `Fix` Version dates use a consistent English format
* `Fix` Add the standard Wake activation entry and report native ABIs from the installed APK, with complete localized descriptions
* `Improvement` Validate release APK versions, signing and the complete variant set before creating download artifacts
* `Improvement` Extend native ABI packaging and plugin metadata to arm64-v8a, armeabi-v7a, x86 and x86_64, with matching universal and per-ABI APKs

# v0.1.1

###### 2026/09/13

* `Improvement` Build verification of 16 KB page alignment for 64-bit native libraries, including manifest contract checks and JSON reports

# v0.1.0

###### 2026/08/13

* `Hint` First release (version code 2, no version code 1 predecessor); requires AutoJs6 with a version code of at least 5275 (6.8.0+) signed with the same certificate as the plugin
* `Hint` Currently in a private staging phase: public release and the official plugin index submission follow the formal release of the compatible host; the capability scope is CPU / arm64-v8a / object detection
* `Feature` Offline `tools/generate_yolo_ncnn_manifest.py` converts Ultralytics YOLO11 NCNN metadata into `model.json`, validates the fixed metadata/label/graph profile, and emits artifact hashes
* `Feature` Process-isolated YOLO object detection provider: the separate `:provider` process serves `org.autojs.plugin.YOLO` inference and `org.autojs.plugin.INFO` discovery, both protected by the `org.autojs.permission.PLUGIN` permission
* `Feature` Built-in NCNN 20260526 CPU inference backend with the `ultralytics-detect` decoder, supporting YOLO11 detect models and manifest-declared custom class counts (1 to 256)
* `Feature` Model Manifest v1 contract in place: session open verifies declared lengths and SHA-256, runtime verifies the output shape, and mismatches are rejected with stable error codes
* `Feature` Models arrive from the host as read-only file descriptors and the whole open shares one monotonic deadline; the plugin APK ships no models and makes no network calls
* `Feature` Session lifecycle protection: single-request serial inference per session (zero queue), callback-death detection, idempotent close, and deferred native resource release
* `Fix` Stale model sessions are cleaned up on startup, avoiding leftover native resource usage after an abnormal host exit
* `Improvement` Release and TEST-SIGNED RC builds are admitted through R8 and resource shrinking, with ELF 16 KiB alignment packaging
* `Improvement` The APK fully bundles the MPL-2.0, Kotlin Apache-2.0, and NCNN licenses plus provenance locks, with a third-party notice index
* `Improvement` New offline source/build/package gate `tools/verify-r6-provider-source.ps1`: rejects drift across 22 generated README/CHANGELOG artifacts, starts from clean sources, records test and artifact hashes, and verifies the APK against a five-file asset allowlist
* `Dependency` Pinned NCNN 20260526 (BSD-3-Clause, with provenance and hash locks), Kotlin 2.2.21, and YOLO protocol AARs 1.0 (handed off from a frozen AutoJs6 source revision)
