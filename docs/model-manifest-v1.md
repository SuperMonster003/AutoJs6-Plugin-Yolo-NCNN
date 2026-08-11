# AutoJs6 YOLO NCNN model manifest v1

The model manifest is the compatibility contract between a model directory and
the isolated NCNN provider. The provider reads the manifest contents; it never
infers a YOLO family or decoder from an artifact file name.

The machine-readable schema is
[`schemas/model-manifest-v1.schema.json`](../schemas/model-manifest-v1.schema.json).
JSON Schema validates the document structure. The provider additionally enforces
the cross-field invariants described below.

## Supported profile

Manifest v1 intentionally exposes one finite profile:

| Field | Required value |
| --- | --- |
| Engine / backend | `yolo` / `ncnn` |
| Family / variant / task | `ultralytics` / `yolo11` / `detect` |
| Input | `in0`, RGB NCHW, `640 x 640`, centered letterbox, scale `1/255`, padding `114` |
| Output | `out0`, `ultralytics-detect`, `cxcywh`, no separate objectness |
| Device | CPU, selected by the session rather than the manifest |
| Labels | 1 through 256 unique, non-blank UTF-8 strings; at most 256 UTF-8 bytes each |

For `N` labels, `output.shape` must be exactly `[1, 4 + N, 8400]`. The graph
loaded by NCNN must produce a float32, unpacked tensor with the corresponding
runtime shape `[4 + N, 8400]`. A manifest cannot be used to relabel an
incompatible graph: the provider validates the runtime output before decoding.

Unknown keys are rejected. The whole manifest is limited to 256 KiB, must be
strict UTF-8 JSON without NUL or trailing content, and must contain exactly the
keys defined by the schema.

## Artifact directory

AutoJs6 supplies these three regular files through read-only file descriptors:

```text
model.json
model.ncnn.param
model.ncnn.bin
```

The names select artifact roles only. Model identity and compatibility come
from `model.json`, not from directory or weight file names. AutoJs6 and the
provider verify declared lengths and SHA-256 digests before NCNN loads a graph.

## Decoder negotiation

The session `decoderId` must equal `output.decoder`. Version 1 currently
registers only `ultralytics-detect`; an unknown ID is rejected rather than
falling back to another decoder. Provider capabilities are the authoritative
list of decoder IDs available at runtime.

## Rejection categories

Model-open failures use the protocol `MODEL_REJECTED` category with a stable
detail prefix. Current prefixes include `MANIFEST_SCHEMA_UNSUPPORTED`,
`MANIFEST_PROFILE_UNSUPPORTED`, `MANIFEST_NODE_UNSUPPORTED`,
`MANIFEST_DECODER_UNSUPPORTED`, `MANIFEST_SHAPE_INVALID`,
`MANIFEST_LABELS_INVALID`, and `MODEL_GRAPH_REJECTED`.

This contract describes compatibility, not model licensing. Model and dataset
rights remain the responsibility of the model supplier.

