# Convert an Ultralytics YOLO11 detect export

This guide turns a standard Ultralytics YOLO11 NCNN export into the three-file
model directory consumed by AutoJs6 and this Provider. The conversion helper is
fully offline, uses only the Python standard library, and does not copy, upload,
or redistribute model weights.

The Provider currently supports one frozen model profile. This guide does not
apply to YOLO26 or another YOLO family, segmentation, pose, OBB,
classification, tracking, dynamic input sizes, NMS/end-to-end export, batches
larger than one, or quantized exports.

## 1. Export the model

Start from an official or custom-trained YOLO11 detect checkpoint. In the
Python environment where Ultralytics and its NCNN exporter are installed, run:

```powershell
yolo export model=.\best.pt format=ncnn imgsz=640 batch=1 device=cpu half=false int8=false
```

The exact command syntax can vary with the Ultralytics release. The relevant
requirements are a non-quantized, non-end-to-end YOLO11 detect graph with batch
1 and a fixed `640 x 640` input. See the official
[Ultralytics NCNN integration guide](https://docs.ultralytics.com/integrations/ncnn/)
and [NCNN exporter source](https://github.com/ultralytics/ultralytics/blob/main/ultralytics/utils/export/ncnn.py).

The resulting export directory must contain these files:

```text
best_ncnn_model/
|-- metadata.yaml
|-- model.ncnn.param
`-- model.ncnn.bin
```

Do not rename `metadata.yaml`, `model.ncnn.param`, or `model.ncnn.bin` before
running the conversion helper.

## 2. Generate `model.json`

From this repository, pass the export directory to the helper:

```powershell
python .\tools\generate_yolo_ncnn_manifest.py .\path\to\best_ncnn_model
```

On success it writes `model.json` next to the NCNN files and prints one
`MODEL_MANIFEST_OK` JSON receipt. The receipt records the label count, expected
output shape, NCNN layer/blob counts, and the byte length and SHA-256 digest of
each artifact.

The completed directory is:

```text
best_ncnn_model/
|-- model.json
|-- model.ncnn.param
`-- model.ncnn.bin
```

`metadata.yaml` may remain alongside those files for provenance, but the Host
passes only the three files above to the Provider.

The command is idempotent. If an existing `model.json` already has the expected
bytes, the receipt reports `"mode":"unchanged"`. It refuses to overwrite a
different file unless replacement is explicitly requested:

```powershell
python .\tools\generate_yolo_ncnn_manifest.py .\path\to\best_ncnn_model --force
```

Use check mode in a model packaging workflow to reject missing or manually
edited output without writing anything:

```powershell
python .\tools\generate_yolo_ncnn_manifest.py .\path\to\best_ncnn_model --check
```

## What the helper validates

The metadata check requires all of the following:

- the exporter description identifies a YOLO11 model;
- `task: detect`, `head: Detect`, stride 32, RGB channels 3, batch 1;
- fixed `imgsz: [640, 640]` and `end2end: false`;
- no dynamic, NMS, half, INT8, or other quantized export flag;
- 1 through 256 contiguous class names, with no blanks, duplicates, control
  characters, surrounding whitespace, or label longer than 256 UTF-8 bytes.

The NCNN param check parses the graph declaration rather than looking for a
substring. It verifies the file magic, declared layer/blob counts, every
producer/consumer reference, exactly one first `Input` exposing `in0`, and
exactly one unconsumed graph output named `out0`. The NCNN bin must be a
non-empty regular file. Symlinked inputs and output are rejected.

For `N` validated labels, the generated manifest declares
`[1, 4 + N, 8400]`, centered RGB letterbox preprocessing at `640 x 640`, and
the `ultralytics-detect` decoder. Every other field is emitted from fixed
Provider profile constants rather than guessed from filenames.

## Validation boundary

Static metadata and graph-structure checks cannot prove the runtime tensor
shape or numerical correctness of arbitrary weights. The Provider remains the
authority: it loads the NCNN graph in the isolated process and rejects a graph
whose actual float output is not `[4 + N, 8400]`. Run at least one known-image
inference on the target device before treating a custom model as validated.

The repository's standard-library test suite checks that the helper generates
manifests semantically identical to both frozen Provider fixtures:

| Fixture | Labels | Manifest output shape | Real NCNN graph |
| --- | ---: | --- | ---: |
| Official YOLO11n | 80 | `[1, 84, 8400]` | 275 layers / 327 blobs |
| Custom one-class YOLO11n | 1 | `[1, 5, 8400]` | 274 layers / 326 blobs |

Run the tests with:

```powershell
python -m unittest discover -s .\tools\tests -p "test_generate_yolo_ncnn_manifest.py"
```

## Troubleshooting

All expected failures use the prefix `MODEL_MANIFEST_ERROR` and exit with code
1. Common causes are an unsupported task/family, export size other than 640,
end-to-end or quantized export flags, non-contiguous class indices, renamed
`in0`/`out0` nodes, a malformed NCNN param, or an existing differing
`model.json` without `--force`.

The generated manifest is a compatibility declaration, not a way to relabel an
incompatible graph. Changing the `labels` array by hand will not change the
model output and will be rejected when its row count differs.

Model and dataset rights remain the model supplier's responsibility. NCNN
conversion and manifest generation do not change the checkpoint's license or
grant redistribution rights. See the repository's
[model and validation-asset policy](model-license-policy.md).
