# AutoJs6 YOLO NCNN

- Runtime action: `org.autojs.plugin.YOLO`
- Engine: `yolo`
- Variant: `ncnn`
- Initial target: CPU-only `arm64-v8a` detect

This R1 source scaffold intentionally reports that its native runtime is not
ready. A pinned NCNN runtime and fixed model fixture must be staged before the
provider can open a session.
