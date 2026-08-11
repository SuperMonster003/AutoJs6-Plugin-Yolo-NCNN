# YOLO NCNN Provider R1

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
