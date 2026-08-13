# AutoJs6 Plugin: YOLO NCNN

Independent, process-isolated YOLO provider for AutoJs6.

The Gradle wrapper, build logic, and version-selection structure were reused
from the clean local sibling `AutoJs6-Plugin-DEX-Compiler@98037a3e`; provider
identity and runtime code are YOLO-specific.

## Current deployment state

R1 through R4 established the process-isolated provider, Host Binder/PFD route,
Rhino Preview API, and a manifest-driven YOLO11 detect compatibility set. The
repository contains the pinned protocol AARs, NCNN 20260526 CPU/arm64 inputs, JNI
implementation, and one explicit `ultralytics-detect` decoder. Local-only model,
training-data, and test-image bytes remain ignored and are not shipped in the
provider APK.

Synchronous model-open accepts regular-file PFDs only. One monotonic session-open
deadline covers descriptor duplication/materialization, exact length, EOF, SHA-256,
and native engine construction; a session is never published after expiry. Native
model construction currently checks the deadline before and after the call but is
not cooperatively abortable, so R1 makes no hard-timeout claim.

The current release-candidate scope is CPU-only `arm64-v8a` detection. Vulkan,
other ABIs, segmentation, pose, OBB, tracking, and unknown decoders are explicit
unsupported capabilities; they do not silently fall back. Installing this APK
alone does not enable the default-off YOLO route in AutoJs6.

`assembleRc` creates an installable, non-debuggable, arm64-only test candidate.
As of R6 it inherits the release minifier and resource shrinker, while retaining
the standard Android debug signing config. Its version name ends in
`-rc-test-signed` and its BuildConfig channel is `RC_TEST_SIGNED`. This artifact
is TEST-SIGNED, not a production release. It is intended to pair with an AutoJs6
test APK using the same local debug keystore; every device gate must still compare
the two actual certificate digests. The historical R5 receipt remains bound to
its earlier, explicitly unshrunk candidate and is not rewritten by this change.

`assembleRelease` is shrinker-admitted but remains unsigned when
`sign.properties` is absent. An unsigned local APK is source/build/package
evidence only and must not be described as publishable.

`tools/verify-r6-provider-source.ps1` defaults to a clean-source build: it
captures Git state, runs `:app:clean` before the focused tests and two APK
assemblies, confirms Git state is unchanged, and records exact test XML and
artifact hashes. A process lock prevents concurrent gate runs from racing the
clean/build boundary. The APK check uses an exact four-file asset allowlist and
identifies the compiled raw plugin instruction by content rather than a generated
resource filename. `-SkipBuild` is only an existing-artifact diagnostic. It sets
`buildIdentityProven=false` and makes no test, build, minifier, resource-shrinker,
or package-pass claim, even when `-RequireClean` is also requested.

The APK source set includes the plugin MPL-2.0 text, the complete pinned NCNN
license/notices, the third-party notice index, and the NCNN provenance lock under
`app/src/main/assets`.
Models remain external and retain the license and usage conditions of their own
source; converting a model to NCNN does not change those conditions.

ELF 16 KiB alignment is packaging evidence only. Production signing, release
shrinker admission, upgrade/rollback coverage, and real native loading on a 16 KiB page-size target
remain separate release evidence and are not implied by the source files or an
ordinary arm64 device smoke.

This sibling Git repository is independent and has no remote. Canonical receipts
are tracked in the AutoJs6 worktree; local commits and test APKs do not imply that
anything was pushed or published.

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

See [ROADMAP.md](ROADMAP.md) for the source/build/package/native evidence boundary.
The model distribution rules are in
[docs/model-license-policy.md](docs/model-license-policy.md), and the current
release-note draft is [docs/release-notes/0.1.0.md](docs/release-notes/0.1.0.md).
