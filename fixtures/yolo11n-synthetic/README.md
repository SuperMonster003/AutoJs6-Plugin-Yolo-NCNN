# YOLO11n one-class synthetic R4 fixture

This fixture is the second, custom-trained model for the R4 compatibility gate.
It uses the same `ultralytics-detect` decoder as the official YOLO11n fixture,
but has one class (`synthetic-target`) and therefore exports the bounded output
shape `[1, 5, 8400]` at a fixed 640 x 640 input size.

Run `tools/r4_custom_fixture.py` with the pinned R1 Python environment and the
already verified local YOLO11n checkpoint. The generator creates 24 training
images, 6 validation images, and one held-out image by default. Every image is
programmatically generated from a fixed seed; no third-party image asset is
used. Training is CPU-only, uses deterministic settings and no data
augmentation, and may be shortened or extended with `--epochs` without changing
the manifest contract.

Example from the AutoJs6 YOLO worktree and sibling repository:

```powershell
E:\AutoJs6-Worktree-Yolo\build\yolo-r1-export-venv\Scripts\python.exe `
  .\tools\r4_custom_fixture.py `
  --base-model E:\AutoJs6-Worktree-Yolo\build\yolo-r1-export\yolo11n.pt `
  --output-root .\fixtures\local\yolo11n-synthetic `
  --epochs 24
```

The command refuses to overwrite a non-empty output directory. It writes all
model, dataset, training, held-out image, ground-truth, hash-lock, and provenance
bytes below `fixtures/local/yolo11n-synthetic`, which is ignored by Git and must
remain absent from the Provider APK. The generator source, this README, the
public manifest, sanitized provenance/hash lock, and locked numeric expectation
are tracked after a successful local run; model and image bytes are never tracked.

The compatibility gate uses an explicit confidence threshold of `0.03` for this
small synthetic model and requires the single result to overlap the generated
ground truth by at least `0.90` IoU. Confidence calibration is not an R4 quality
or performance gate; the purpose of this fixture is to prove dynamic one-class
manifest and decoder compatibility.

Tracked metadata:

- `model-manifest-v1.json` is the public one-class manifest consumed by the Provider.
- `expected-synthetic-detection.json` locks the generated ground truth and local NCNN reference.
- `fixture.lock.json` records toolchain, training recipe, artifact hashes, and distribution policy.

The base checkpoint and Ultralytics exporter metadata record AGPL-3.0. Fine
tuning and NCNN conversion are not treated as changing that license. Generated
weights and image data are restricted to local validation and are not
distributed. The synthetic images contain no third-party input assets.
