# YOLO NCNN Provider Roadmap

## R1-SOURCE

- [x] Create an independent sibling Git repository with one `:app` module.
- [x] Freeze application, plugin, engine, variant, provider, action, and process identities.
- [x] Add `org.autojs.plugin.INFO` and `org.autojs.plugin.YOLO` services guarded by
  `org.autojs.permission.PLUGIN`; both support actionless explicit-component binding.
- [x] Add provider/session ownership, callback-death, bounded serial control
  admission, single-in-flight with zero inference queue, deferred native cleanup,
  and idempotent close.
- [x] Add bounded model descriptor materialization with declared length, EOF,
  producer error, SHA-256, regular-file, and one whole-open monotonic deadline.
- [x] Keep malformed ingress isolated from any active request; when a valid
  request ID and sequence can be recovered, publish its exact failure terminal.
- [x] Fail closed when the pinned NCNN runtime cannot be loaded or initialized.
- [x] Freeze the AutoJs6 R0 source revision and stage three protocol AARs with hashes.
- [x] Freeze NCNN version/source/hash/license and stage its build inputs.
- [x] Freeze the YOLO11n NCNN fixture, manifest, test image, hashes, and license.

## R1-BUILD

- [x] Compile AIDL/Kotlin and package an arm64-v8a debug APK.
- [x] Compile and link the JNI/NCNN shared library.

## R1-NATIVE

- [x] Check the whole-open deadline before and after native model construction and
  reject a session that expires during construction.
- [x] On an explicitly authorized, non-protected arm64 Android target, run one
  real fixed-image inference and record the exact APK/model/environment identity.

R1 does not claim Host/provider Binder-PFD end-to-end or production readiness.
Native model construction is not yet cooperatively abortable, so no hard-timeout
runtime guarantee is claimed; that hardening remains a later reliability item.

Canonical R1 evidence is tracked in the AutoJs6 worktree at
`docs/dev/yolo-evidence/r1-summary.json`.

## R5 CPU/arm64 RC baseline

- [x] Package the plugin MPL-2.0 text, complete pinned NCNN license/notices, and
  NCNN provenance lock as main APK assets.
- [x] Keep advertised capabilities limited to CPU, `arm64-v8a`, detect, NCNN,
  RGBA_8888, and the registered decoder set.
- [x] Map protocol and capability incompatibility to the matching stable
  open-session error codes; no unsupported-capability fallback is permitted.
- [x] Add focused source tests for the open-session error mapping.
- [x] Run the focused JVM/Android tests and build a current source-bound candidate.
- [x] Verify the candidate APK ABI, native dependencies, license assets, and ELF
  16 KiB alignment in an independently generated packaging report.
- [x] Run candidate native load/inference on an arm64 target. This passed on
  `QV710AF65F` (API 31, arm64-v8a, 4 KiB page size); a separate 16 KiB page-size
  target is still required before claiming `NATIVE_LOAD_16K_DEVICE`.

Canonical R5 evidence is tracked in the AutoJs6 worktree at
`docs/dev/yolo-evidence/r5-summary.json`. It binds the device run to provider
revision `2aa5b100edd5e0f7691cb7edb3dd3b38c194f77d` and Host revision
`c40464957239e1378acd7be92647a4c863ac60e5`.

R5 source and packaging evidence are not production signing, publishing,
upgrade, or rollback evidence. Those remain R6 deliverables.
