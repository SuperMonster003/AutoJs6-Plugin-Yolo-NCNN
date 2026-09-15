<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-yolo-ncnn-ic-launcher" border="0" width="128" />
  </p>

  <h1>AutoJs6 Plugin: YOLO NCNN</h1>

  <p>Process-isolated, fully offline YOLO object detection for AutoJs6 (NCNN 20260526 backend)</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/commit/01b9093c55c7c1a78f39246c671e928df244483f"><img alt="Created" src="https://img.shields.io/date/1786442764?color=2e7d32&label=Created"/></a>
    <br>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2023.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2023.3+-EE4677"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Languages

******

The current README.md supports the following languages:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-TW.md)
- English [en] # current
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ar.md)

******

### Introduction

******

The AutoJs6 YOLO NCNN plugin lets AutoJs6 scripts run YOLO object detection entirely on-device: pass in an image and get back an array of detections with labels, confidences, and pixel-coordinate bounding boxes. Inference is performed by Tencent NCNN 20260526 in a separate `:provider` process, with no network access and no data upload; model files are supplied by the user and the plugin APK ships no models.

```text
application ID: io.github.supermonster003.autojs6.plugin.yolo.ncnn
plugin / engine / variant: yolo-ncnn / yolo / ncnn
provider ID: autojs6-yolo-ncnn
discovery actions: org.autojs.plugin.INFO / org.autojs.plugin.YOLO
runtime process: :provider
protocol version: 1.0
backend / task / decoder: ncnn / detect / ultralytics-detect
supported ABI: arm64-v8a, armeabi-v7a, x86, x86_64
minimum host build: 5275 (AutoJs6 6.8.0+)
```

The identity above is what the host uses to discover and bind this plugin. Models are handed over by the host through read-only file descriptors and always remain external resources.

******

### Features

******

- Offline YOLO11 object detection: feed an AutoJs6 `images` module image object, get labels + confidences + bounding boxes, computed entirely on-device.
- Process isolation: inference runs in the separate `:provider` process, so native-layer failures never take down the AutoJs6 main process; services are protected by the `org.autojs.permission.PLUGIN` permission and signature checks.
- NCNN 20260526 CPU inference backend: adjustable thread count (default 4, up to 64), for `arm64-v8a, armeabi-v7a, x86, x86_64` devices.
- Manifest-driven model compatibility: `model.json` declares inputs, outputs, and labels, supporting 1 to 256 custom classes; official YOLO11 and self-trained models work alike.
- Model safety validation: session open verifies the declared length and SHA-256 of all three model files, and the actual NCNN graph output shape is verified at runtime; mismatches are rejected, never guessed at.
- Stable error categories: missing component, provider unavailable, model rejected, unsupported capability, and similar cases all surface decidable error codes that scripts can handle precisely.
- Request timeouts and cleanup through `detector.close()`; native work uses cooperative cancellation and may finish its current call before releasing resources.
- README and CHANGELOG are available in ten languages: Simplified Chinese, Traditional Chinese (HK/TW), English, French, Spanish, Japanese, Korean, Russian, and Arabic.

******

### Quick Start

******

- **Install** — This plugin is currently in a private staging phase (see the Project Status section below): public downloads and the official plugin index entry arrive only after the compatible host AutoJs6 6.8.0 (build 5275) is formally released. Until then you can build a TEST-SIGNED candidate yourself as described in the Build section and pair it with an AutoJs6 test APK using the same debug certificate; host and plugin must be signed with the same certificate.
- **Enable** — Installing the plugin does not switch YOLO on by itself: the AutoJs6 host keeps explicit selection, trust, and enable switches (the YOLO route is off by default), so enable and trust this provider in the host. On the script side, `yolo.load` also requires the explicit component string in `options.component`; there is no implicit fallback.
- **Run** — Prepare a model directory containing the three files `model.json`, `model.ncnn.param`, and `model.ncnn.bin` (see the Model Preparation section below), open a detector with `yolo.load(modelDir, options)`, get the detection array with `detector.detect(image, options)`, and release it with `detector.close()` when done.
- **Troubleshoot** — Exceptions thrown by `yolo.load` and `detector.detect` carry stable error categories: `COMPONENT_REQUIRED` (no component given), `PROVIDER_UNAVAILABLE` (host cannot find or does not trust the provider), `MODEL_REJECTED` (model or manifest failed validation; details carry prefixes such as `MANIFEST_*`), `UNSUPPORTED_CAPABILITY` (a capability outside CPU/detect was requested), `SESSION_CLOSED`, `DETECT_FAILED`, and so on. Check the Boundaries section below and the [model manifest specification](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) when debugging.

******

### Usage Example

******

A minimal ready-to-run example (see also `sample/yolo/detect.js` in the host repository):

```javascript
"use strict";

const providerComponent = "io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService";
const modelDirectory = files.path("./models/yolo11n");

let detector = null;
let image = null;

try {
    detector = yolo.load(modelDirectory, {
        component: providerComponent,
        device: "cpu",
        threads: 4,
        decoderId: "ultralytics-detect",
        timeoutMillis: 120000,
    });
    image = images.read(files.path("./bus.jpg"), true);

    const detections = detector.detect(image, {
        confidence: 0.25,
        iouThreshold: 0.45,
        maxDetections: 100,
        timeoutMillis: 30000,
    });

    detections.forEach((detection) => {
        const bounds = detection.bounds;
        console.log(
            detection.label + " " + detection.confidence.toFixed(4)
            + " [" + bounds.left + ", " + bounds.top + ", " + bounds.right + ", " + bounds.bottom + "]",
        );
    });
} finally {
    if (image !== null) {
        images.recycle(image);
    }
    if (detector !== null) {
        detector.close();
    }
}
```

Each detection carries `classId`, `label`, `confidence`, and `bounds` (an Android `RectF` with `left` / `top` / `right` / `bottom` fields plus `centerX()` / `centerY()` methods); coordinates are in input-image pixels. A detector is a single-request serial session: the inference queue length is 0, so a second concurrent `detect` on the same detector fails immediately instead of queueing.

******

### Model Preparation

******

A model directory always contains exactly three files, and file names select roles:

```text
models/yolo11n/
|-- model.json
|-- model.ncnn.param
`-- model.ncnn.bin
```

Official or self-trained Ultralytics YOLO11 detect models can be exported with `yolo export format=ncnn imgsz=640`, which produces `model.ncnn.param` and `model.ncnn.bin` (see the [Ultralytics NCNN export guide](https://docs.ultralytics.com/integrations/ncnn/)). `model.json` is a Model Manifest v1 document: it declares the input (`in0`, RGB NCHW, 640x640 letterbox), the output (`out0`, `ultralytics-detect` decoder, shape `[1, 4 + N, 8400]` where N is the class count), and the label list; a complete example is [fixtures/yolo11n/model-manifest-v1.json](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/fixtures/yolo11n/model-manifest-v1.json).

Run `python tools/generate_yolo_ncnn_manifest.py <export-directory>` to generate `model.json` directly from the Ultralytics `metadata.yaml`. The offline standard-library helper validates the fixed YOLO11/detect/640/batch/label profile and the NCNN `in0`/`out0` graph structure before writing; use `--check` to reject drift without writing. See the [model conversion guide](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-conversion.md) for the exact export command, validation boundary, and troubleshooting.

The manifest is a compatibility contract, not a relabeling tool: session open verifies the declared lengths and SHA-256 of all three files, and the actual NCNN graph output shape is verified at runtime; mismatches are rejected with `MODEL_REJECTED` (detail prefixes such as `MANIFEST_SHAPE_INVALID`, `MODEL_GRAPH_REJECTED`). Models keep the license and usage conditions of their source; converting to NCNN does not change them, and the plugin grants no redistribution rights. See the [model manifest specification](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) and the [model license policy](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md).

******

### Script API

******

`yolo.load(modelDir, options)` opens a detection session and returns a `YoloDetector`. `options.component` is required (a package/class component string; for this plugin it is `io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService`). Optional: `device` (only `"cpu"` is accepted currently), `threads` (default 4, up to 64), `decoderId` (default `ultralytics-detect`), and `timeoutMillis` (total model-open timeout, default 120000 ms, up to 600000 ms).

`detector.detect(image, options)` runs detection synchronously on one image and returns the detection array. `image` is an AutoJs6 `images` module image object (from `images.read`, a screenshot, etc.). Optional: `confidence` (confidence threshold, default 0.25), `iouThreshold` (NMS IoU threshold, default 0.45), `maxDetections` (default 100, up to 400), and `timeoutMillis` (default 30000 ms).

`detector.close()` releases the session and native resources and is safe to call repeatedly; AutoJs6 also closes detectors when the script ends, but an explicit `try...finally` release is recommended. Calling `detect` after close returns `SESSION_CLOSED`.

******

### Boundaries

******

To keep behavior predictable, requests outside the following scope are rejected explicitly rather than silently falling back:

- CPU inference only: Vulkan/GPU is unsupported and `options.device` accepts only `"cpu"`.
- Supported ABIs: `arm64-v8a, armeabi-v7a, x86, x86_64`; the universal APK includes the native library for each ABI.
- Object detection (detect) only: segmentation, pose, OBB, classification, and tracking are all unsupported.
- Only the `ultralytics-detect` decoder is registered: an unknown `decoderId` is rejected instead of falling back to another decoder.
- Input is preprocessed as a 640x640 letterbox (the fixed manifest v1 profile) with RGBA_8888 pixels.
- Single-request serial inference per session: the queue limit is 0, so a second concurrent `detect` on the same session fails.
- Model open accepts read-only descriptors of regular files only (no pipes or sockets); all three files must be readable.
- Installing this plugin does not enable YOLO by itself: enable, trust, and selection state always belong to the AutoJs6 host.

******

### Security & Isolation

******

The plugin is designed fail-closed; the following mechanisms are always in effect:

- Inference runs in the separate `:provider` process, isolated from the AutoJs6 main process; services are protected by the `org.autojs.permission.PLUGIN` permission and signature checks.
- Models arrive from the host as read-only `ParcelFileDescriptor` instances; the plugin reads no storage on its own and makes no network requests.
- Before a session opens, declared lengths, EOF, and SHA-256 are verified; the whole open shares one monotonic deadline, and an expired session is never published.
- If the NCNN runtime cannot be loaded or initialized, the plugin fails closed instead of degrading.
- Malformed ingress is isolated from active requests; when a request identity can be recovered, an exact failure terminal is published instead of hanging.
- Callback death and stale sessions are detected and cleaned up; native resources are released deferred and close operations are idempotent.

******

### Compatibility

******

Requires AutoJs6 with a version code of at least 5275 (that is, 6.8.0 or later) signed with the same certificate as the plugin; Android 24+ (Android 7.0), targetSdk 36; the device must be `arm64-v8a, armeabi-v7a, x86, x86_64`. Plugin protocol version 1.0; current provider version 0.1.2 (version code 2).

******

### Project Status

******

This repository is currently a private staging archive: the compatible host AutoJs6 6.8.0 (5275) has not been formally released, and this plugin is neither publicly released nor listed in the official plugin index; the GitHub badges above may not render until the repository goes public. Without `sign.properties`, `assembleRelease` produces an unsigned APK that is source/build evidence only, not a publishable artifact. The first release is 0.1.2 (version code 2, with no version code 1 predecessor); defects are fixed forward as version code 3, never by rollback. Production signing, final on-device verification, and publication status are bound by the external R6 evidence archive; see the [engineering notes](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md).

******

### Build

******

JDK 21+ is recommended; the Android SDK must provide platforms 24 and 36, plus NDK 29.0.14206865 and CMake 3.22.1 (needed to compile the NCNN JNI). Common commands:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRc
.\gradlew.bat :app:assembleRelease
```

`assembleRc` produces an installable universal TEST-SIGNED candidate: it inherits the release R8 and resource shrinking, uses the standard debug signing, and its version name ends in `-rc-test-signed`; it is meant to pair with an AutoJs6 test APK using the same certificate for on-device verification. `assembleRelease` is the release build and stays unsigned when signing material is absent.

The maintainer gate `tools/verify-r6-provider-source.ps1` first runs `--check` over all 22 generated README/CHANGELOG artifacts for 10 languages and fails immediately on drift. It then starts from clean sources by default: after `:app:clean` it runs the focused tests and both APK assemblies, records test XML and artifact hashes, and verifies the APK against a five-file asset allowlist. Local verification and doc generation both run offline by default (zero network calls) to stay clear of Cloudflare 502/524/529 noise on the development network.

Before any Gradle build, the same gate also runs the eight standard-library tests for `tools/generate_yolo_ncnn_manifest.py` and records their source hashes and receipt, so model-toolchain regressions fail the release preflight offline.

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

##### For more release history, see

* [CHANGELOG-en.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/assets/doc/CHANGELOG-en.md)

******

### License

******

The plugin source is released under [MPL-2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE). The APK bundles the complete NCNN license and notices (BSD-3-Clause plus its upstream third-party notices), the full Apache-2.0 text for the Kotlin runtime, and the third-party notice index; see [THIRD_PARTY_NOTICES](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md). Models are external resources: they keep the license and usage conditions of their source, and the plugin ships no models and grants no redistribution rights.

******

### Further Reading

******

- [Model manifest specification (Model Manifest v1)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)
- [Model manifest JSON Schema](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/schemas/model-manifest-v1.schema.json)
- [Model and validation-asset license policy](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md)
- [0.1.0 release notes (engineering wording)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/release-notes/0.1.0.md)
- [Engineering notes (the former audit-style README in full)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md)
- [Project roadmap (history milestones and future plans)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/ROADMAP.md)

******

### Resource Layout

******

```text
.readme/common.json
.readme/lang_*.json
.readme/template_readme.md
.changelog/lang_*.json
.changelog/template_changelog.md
.python/generate_markdown.py
```

README and CHANGELOG are generated offline by `.python/generate_markdown.py` from the JSON sources above (standard library only, zero network). To change the docs, edit the JSON sources rather than the generated Markdown, then regenerate with the commands below; `--check` verifies that the outputs match the sources:

```powershell
python .\.python\generate_markdown.py
python .\.python\generate_markdown.py --check
```

******

### Links

******

- AutoJs6 project home: https://github.com/SuperMonster003/AutoJs6
- Tencent NCNN: https://github.com/Tencent/ncnn
- Ultralytics NCNN export guide: https://docs.ultralytics.com/integrations/ncnn/
- Third-party notices: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md
- License: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/16kb.md)
