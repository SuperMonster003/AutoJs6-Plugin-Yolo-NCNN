# Runtime evidence

This directory contains sanitized, source-bound runtime receipts that are safe
to keep with the repository. Each receipt states its exact claim boundary. A
debug or instrumentation result is not production-signing, Host Binder/PFD
end-to-end, public-release, or unsupported-platform evidence unless the receipt
explicitly says otherwise.

Local model weights and validation images remain under the ignored
`fixtures/local/` tree. Evidence files record their lengths and SHA-256 digests,
not their payloads.

| Date | Source | Device / Android API / page size | Result | Receipt |
|---|---|---|---|---|
| 2026-08-27 | `69e4a62` | 23046RP50C / 35 / 4 KiB | 5/5 instrumentation, fixed-image inference | [API 35 baseline](r9-arm64-api35-4k-smoke-2026-08-27.json) |
| 2026-08-27 | `e747794` | XQ-AT72 / 31 / 4 KiB | 5/5 instrumentation, fixed-image inference | [API 31 baseline](r9-arm64-api31-4k-qv710-smoke-2026-08-27.json) |
| 2026-09-10 | `dc43e15` | Samsung SM-A566B / 36 / 16 KiB | 5/5 instrumentation, fixed-image inference | [API 36 / 16 KiB validation](r9-arm64-api36-16k-samsung-smoke-2026-09-10.json) |

The Samsung receipt closes the R9 16 KiB and API 36 environment gaps for the
recorded debug native lifecycle. It includes the actual page size, native ELF
LOAD alignment, package-manager `pageSizeCompat` value, installed APK identity,
packaged fixture identity, full inference output, and cleanup. The remote ADB
endpoint is redacted. Empty compatibility system properties are recorded as
unset; this was not a run with global backcompat forcibly disabled. Page-size
and compatibility checks follow the [AOSP page-size guide](https://source.android.com/docs/core/architecture/16kb-page-size/getting-page-size)
and [Android 16 KiB guide](https://developer.android.com/guide/practices/page-sizes#16-kb-backcompat-mode).

To repeat this scope, use the receipt's source revision and frozen local fixture,
run its offline build command, then install the application and test APKs on the
explicitly selected device. Before running its `am instrument -w -r` command,
verify both installed `base.apk` hashes against the local artifacts and record
`getconf PAGE_SIZE`. Retrieve `files/r1-native-result.json` with `run-as` and
check `no_backup/yolo-model-sessions`, `cache`, and target processes before
uninstalling the packages installed for the test. Both packages were absent
before this Samsung run and absent again after cleanup.

Create a new receipt for a new source, artifact, or device run. Historical
receipts retain their original `NOT_RUN` boundaries. Debug native-engine tests
do not exercise the Host/Provider Service Binder route, release shrinking, or
final production artifacts; single-run elapsed times are not benchmarks.
