# Model and Validation-Asset License Policy

## Release boundary

The Provider release contains the NCNN runtime and the YOLO protocol/decoder
implementation. It contains no model weights, training data, exporter runtime,
or validation image. AutoJs6 opens user-selected files and supplies bounded,
read-only file descriptors to the isolated Provider process.

The release preflight must reject an APK containing any known fixture artifact
or model/image payload. Public model manifests and sanitized fixture receipts
may remain in this source repository because they describe compatibility and
provenance; they are not Android `main` assets.

## License responsibility

A model keeps the license and use restrictions of its source after export or
conversion. In particular, converting an Ultralytics checkpoint to NCNN does
not change the checkpoint's license. The user or distributor must establish
that they may use and redistribute each model, its labels, training data, and
associated images.

The Provider does not infer a license from a filename, model family, decoder,
or file format. It also does not grant redistribution rights for external
models.

## Repository fixtures

The official YOLO11n and generated one-class fixtures are for local validation
only. Their weight, exporter-metadata, training-data, and image bytes live below
the ignored `fixtures/local/` tree and must remain absent from Git and release
APKs.

Tracked fixture files are limited to public protocol manifests, expected-result
metadata, sanitized provenance/hash locks, documentation, and the deterministic
fixture generator. The lock files are the authority for each fixture's source,
hashes, recorded license, and distribution policy.

## Release check

Before publishing, run the R6 Provider source preflight and the Host-side
publishable-artifact verifier. A successful source/build/package check does not
authorize model redistribution and does not constitute production signing or
publication evidence.
