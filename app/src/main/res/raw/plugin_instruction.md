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

This CPU/arm64 candidate does not by itself claim production signing, upgrade or
rollback coverage, or successful native loading on a 16 KiB page-size device.
