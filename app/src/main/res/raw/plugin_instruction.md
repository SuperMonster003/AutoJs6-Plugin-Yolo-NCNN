# AutoJs6 YOLO NCNN

- Runtime action: `org.autojs.plugin.YOLO`
- Engine: `yolo`
- Variant: `ncnn`
- Supported runtime: CPU-only `arm64-v8a` detection
- Supported model profile: NCNN YOLO11 detect with the manifest-declared
  `ultralytics-detect` decoder

Vulkan, other ABIs, segmentation, pose, OBB, tracking, and unknown decoders are
not supported. Requests outside the advertised capabilities are rejected rather
than silently falling back.

Models are external resources supplied by AutoJs6 and are not included in this
plugin. Model authors and users remain responsible for the license and usage
terms of each model. The main APK source set contains the plugin's MPL-2.0
license and the pinned NCNN license and provenance under `assets/licenses` and
`assets/third_party/ncnn`; candidate packaging is verified separately.

This CPU/arm64 candidate does not by itself claim production signing. A 16 KiB
page-size native load and API 36 arm64 runtime are non-blocking limitations:
`NATIVE_LOAD_16K_DEVICE=NOT_RUN_NO_16K_DEVICE` and
`API36_ARM64_RUNTIME=NOT_RUN_NO_AVAILABLE_ENVIRONMENT`. Provider `0.1.0` version
code `2` is the first release and has no version code `1` predecessor; upgrade
runtime and version rollback are `NOT_RUN_BY_PRODUCT_DECISION`, not R6 release
gates. Recovery uses Provider disable/index withdrawal and a version code `3`
forward fix. A verified archived exact version code `2` reinstall is
same-version recovery, not rollback.
