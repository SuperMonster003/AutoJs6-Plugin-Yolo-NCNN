# Third-Party Notices

This file identifies third-party components used to build, or distributed in,
AutoJs6 Plugin: YOLO NCNN 0.1.0. It is a notice index; the applicable license
texts and sources are identified below.

## NCNN

- Component: Tencent NCNN
- Version: 20260526
- Upstream revision: e54f7b1f88434e1d844ea0551b880a1cfb079ce1
- Primary license identifier: BSD-3-Clause
- Complete upstream license and bundled third-party notices:
  `assets/licenses/ncnn-20260526.txt`
- Reproducible source/archive identity:
  `assets/third_party/ncnn/provenance.lock.json`

The complete NCNN notice also covers the differently licensed third-party
components identified by the upstream binary distribution.

## Kotlin runtime

- Components: Kotlin standard library and Kotlin Parcelize runtime
- Version requested by this project: 2.2.21
- Copyright: JetBrains s.r.o. and Kotlin contributors
- License: Apache License 2.0
- Complete license text: `assets/licenses/Apache-2.0.txt`

The Maven POMs for `org.jetbrains.kotlin:kotlin-stdlib:2.2.21` and
`org.jetbrains.kotlin:kotlin-parcelize-runtime:2.2.21` both identify
`Apache-2.0` and the upstream Apache License 2.0 text.

The Parcelize runtime is declared explicitly because the local protocol AAR has
no POM from which Gradle could recover its annotation dependency. R8 may remove
the annotation classes from a given release artifact; the dependency remains a
release build input.

## AutoJs6 protocol AARs

`common-plugin-api.aar`, `protocol-wire-api.aar`, and `yolo-api.aar` are
pinned AutoJs6 protocol artifacts. Their source is licensed under MPL-2.0; the
APK includes `assets/licenses/MPL-2.0.txt`.

## Models and validation images

Model weights, training data, exporter packages, and validation images are not
distributed in the Provider APK. Users are responsible for establishing their
right to use and distribute every external model and related input. Their
original licenses and usage conditions continue to apply.
