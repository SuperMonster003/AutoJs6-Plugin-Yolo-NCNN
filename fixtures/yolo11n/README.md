# YOLO11n R1 local-only fixture

The selected R1 target is one external YOLO11n detect model using the
`ultralytics-detect` decoder. Its export identity, graph and artifact hashes are
frozen in `fixture.lock.json`.

Model, exporter metadata, and test-image bytes live only below the ignored
`fixtures/local/yolo11n` directory. They are passed through PFD for local
validation and are not included in Git or the provider APK. The small protocol
manifest is intended for the initial commit because it defines the decoder
contract, not model weights. The `tracked` fields in `fixture.lock.json` express
repository policy: `false` artifacts must remain ignored and outside the Provider
APK.
