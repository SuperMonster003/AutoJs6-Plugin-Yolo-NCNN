# AutoJs6 Plugin: YOLO NCNN

Independent, process-isolated YOLO provider for AutoJs6.

The Gradle wrapper, build logic, and version-selection structure were reused
from the clean local sibling `AutoJs6-Plugin-DEX-Compiler@98037a3e`; provider
identity and runtime code are YOLO-specific.

## Current deployment state

`R1-SOURCE`, `R1-BUILD`, and the direct-engine `R1-NATIVE` smoke are complete.
The repository contains the pinned protocol AARs, NCNN 20260526 CPU/arm64 inputs,
JNI implementation, strict YOLO11n manifest/decoder contract, and the debug APK
build. The local-only model and test-image bytes remain ignored and are not shipped
in the provider APK.

Synchronous model-open accepts regular-file PFDs only. One monotonic session-open
deadline covers descriptor duplication/materialization, exact length, EOF, SHA-256,
and native engine construction; a session is never published after expiry. Native
model construction currently checks the deadline before and after the call but is
not cooperatively abortable, so R1 makes no hard-timeout claim.

The device smoke exercises model materialization and the native engine directly.
AutoJs6 Host to provider Binder/PFD end-to-end integration remains R2, and installing
this APK alone does not add or enable a production YOLO route in AutoJs6.

This sibling Git repository is independent and has no remote. The canonical R1
receipt records both the deterministic pre-commit source snapshot and the local
checkpoint identity; neither implies that anything was pushed or published.

## Fixed identity

| Field | Value |
| --- | --- |
| Application ID | `io.github.supermonster003.autojs6.plugin.yolo.ncnn` |
| Plugin ID | `yolo-ncnn` |
| Engine | `yolo` |
| Variant | `ncnn` |
| Provider ID | `autojs6-yolo-ncnn` |
| Runtime action | `org.autojs.plugin.YOLO` |
| Info action | `org.autojs.plugin.INFO` |
| Runtime process | `:provider` |

Models remain external resources supplied by the AutoJs6 host through read-only
`ParcelFileDescriptor` instances. They are not embedded into this APK.

See [ROADMAP.md](ROADMAP.md) for the source/build/native evidence boundary.
