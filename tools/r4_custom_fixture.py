#!/usr/bin/env python3
"""Build the local-only one-class YOLO11n R4 compatibility fixture.

This script is intentionally offline once its Python environment and the base
checkpoint exist. It generates all training/test images itself, fine-tunes on
CPU, exports NCNN at 640x640, and records the inputs and outputs needed for an
auditable device gate.
"""

from __future__ import annotations

import argparse
import hashlib
import importlib.metadata
import json
import os
from pathlib import Path
import platform
import random
import shutil
import sys
from datetime import datetime, timezone
from typing import Any, Iterable

from PIL import Image, ImageDraw


SCRIPT_VERSION = 1
CLASS_NAME = "synthetic-target"
EXPORT_IMAGE_SIZE = 640
DEFAULT_BASE_SHA256 = "0ebbc80d4a7680d14987a577cd21342b65ecfd94632bd9a8da63ae6417644ee1"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate, train, and export the local-only YOLO11n R4 synthetic fixture."
    )
    parser.add_argument("--base-model", type=Path, required=True, help="Local YOLO11n .pt checkpoint.")
    parser.add_argument(
        "--output-root",
        type=Path,
        required=True,
        help="Empty directory below fixtures/local; no existing content is overwritten.",
    )
    parser.add_argument(
        "--base-sha256",
        default=DEFAULT_BASE_SHA256,
        help="Expected SHA-256 of --base-model.",
    )
    parser.add_argument("--seed", type=int, default=20260811)
    parser.add_argument("--train-count", type=int, default=24)
    parser.add_argument("--val-count", type=int, default=6)
    parser.add_argument("--epochs", type=int, default=24)
    parser.add_argument("--train-imgsz", type=int, default=320)
    parser.add_argument("--batch", type=int, default=8)
    parser.add_argument("--threads", type=int, default=6)
    return parser.parse_args()


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def file_record(path: Path, root: Path) -> dict[str, Any]:
    return {
        "path": path.relative_to(root).as_posix(),
        "length": path.stat().st_size,
        "sha256": sha256(path),
    }


def write_json(path: Path, value: Any) -> None:
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def package_version(name: str) -> str:
    try:
        return importlib.metadata.version(name)
    except importlib.metadata.PackageNotFoundError:
        return "not-installed"


def require_file(path: Path, label: str) -> Path:
    resolved = path.expanduser().resolve(strict=True)
    if not resolved.is_file():
        raise ValueError(f"{label} is not a regular file: {resolved}")
    return resolved


def prepare_output_root(requested: Path) -> tuple[Path, Path]:
    repository_root = Path(__file__).resolve().parent.parent
    local_fixture_root = (repository_root / "fixtures" / "local").resolve()
    output_root = requested.expanduser().resolve()
    try:
        output_root.relative_to(local_fixture_root)
    except ValueError as error:
        raise ValueError(f"--output-root must be below {local_fixture_root}") from error
    if output_root == local_fixture_root:
        raise ValueError("--output-root must name a fixture directory below fixtures/local")
    if output_root.exists() and any(output_root.iterdir()):
        raise ValueError(f"Refusing to overwrite non-empty output directory: {output_root}")
    output_root.mkdir(parents=True, exist_ok=True)
    return repository_root, output_root


def validate_args(args: argparse.Namespace) -> None:
    total = args.train_count + args.val_count
    if args.train_count < 1 or args.val_count < 1 or total < 20 or total > 40:
        raise ValueError("train-count + val-count must be between 20 and 40, with both splits non-empty")
    if args.epochs < 1 or args.epochs > 200:
        raise ValueError("epochs must be between 1 and 200")
    if args.train_imgsz < 160 or args.train_imgsz > 640 or args.train_imgsz % 32 != 0:
        raise ValueError("train-imgsz must be a multiple of 32 between 160 and 640")
    if args.batch < 1 or args.batch > 64:
        raise ValueError("batch must be between 1 and 64")
    if args.threads < 1 or args.threads > 64:
        raise ValueError("threads must be between 1 and 64")
    expected_hash = args.base_sha256.lower()
    if len(expected_hash) != 64 or any(character not in "0123456789abcdef" for character in expected_hash):
        raise ValueError("base-sha256 must contain exactly 64 hexadecimal characters")


def synthetic_bounds(seed: int, index: int, width: int, height: int) -> tuple[int, int, int, int]:
    rng = random.Random(seed + index * 1_000_003)
    box_width = rng.randint(150, 270)
    box_height = rng.randint(110, 230)
    left = rng.randint(36, width - box_width - 36)
    top = rng.randint(36, height - box_height - 36)
    return left, top, left + box_width, top + box_height


def render_synthetic_image(path: Path, seed: int, index: int) -> tuple[int, int, int, int]:
    width = EXPORT_IMAGE_SIZE
    height = EXPORT_IMAGE_SIZE
    rng = random.Random(seed ^ (index * 2_000_033))
    background = (rng.randint(12, 28), rng.randint(22, 38), rng.randint(34, 52))
    image = Image.new("RGB", (width, height), background)
    draw = ImageDraw.Draw(image)

    for offset in range(0, height, 32):
        shade = 28 + ((offset // 32 + index) % 4) * 5
        draw.line((0, offset, width, offset), fill=(shade, shade + 5, shade + 11), width=1)
    for _ in range(5):
        x = rng.randint(12, width - 72)
        y = rng.randint(12, height - 72)
        size = rng.randint(18, 54)
        color = (rng.randint(32, 74), rng.randint(52, 92), rng.randint(72, 112))
        draw.ellipse((x, y, x + size, y + size), outline=color, width=3)

    bounds = synthetic_bounds(seed, index, width, height)
    left, top, right, bottom = bounds
    target_fill = (238, 48 + index % 20, 44)
    draw.rectangle(bounds, fill=target_fill, outline=(255, 244, 196), width=6)
    draw.line((left + 12, top + 12, right - 12, bottom - 12), fill=(255, 224, 128), width=5)
    draw.line((right - 12, top + 12, left + 12, bottom - 12), fill=(255, 224, 128), width=5)

    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, format="PNG", optimize=False, compress_level=9)
    return bounds


def yolo_label(bounds: tuple[int, int, int, int]) -> str:
    left, top, right, bottom = bounds
    width = EXPORT_IMAGE_SIZE
    height = EXPORT_IMAGE_SIZE
    center_x = ((left + right) / 2.0) / width
    center_y = ((top + bottom) / 2.0) / height
    box_width = (right - left) / width
    box_height = (bottom - top) / height
    return f"0 {center_x:.9f} {center_y:.9f} {box_width:.9f} {box_height:.9f}\n"


def generate_dataset(output_root: Path, seed: int, train_count: int, val_count: int) -> dict[str, Any]:
    dataset_root = output_root / "dataset"
    index = 0
    split_counts = {"train": train_count, "val": val_count}
    for split, count in split_counts.items():
        for split_index in range(count):
            stem = f"synthetic-{split}-{split_index:03d}"
            image_path = dataset_root / "images" / split / f"{stem}.png"
            label_path = dataset_root / "labels" / split / f"{stem}.txt"
            bounds = render_synthetic_image(image_path, seed, index)
            label_path.parent.mkdir(parents=True, exist_ok=True)
            label_path.write_text(yolo_label(bounds), encoding="ascii")
            index += 1

    dataset_yaml = dataset_root / "dataset.yaml"
    yaml_root = dataset_root.resolve().as_posix().replace("'", "''")
    dataset_yaml.write_text(
        f"path: '{yaml_root}'\n"
        "train: images/train\n"
        "val: images/val\n"
        "names:\n"
        f"  0: {CLASS_NAME}\n",
        encoding="utf-8",
    )

    held_out_image = output_root / "heldout" / "synthetic-heldout.png"
    held_out_bounds = render_synthetic_image(held_out_image, seed, 10_000)
    ground_truth = {
        "schemaVersion": 1,
        "image": held_out_image.relative_to(output_root).as_posix(),
        "imageWidth": EXPORT_IMAGE_SIZE,
        "imageHeight": EXPORT_IMAGE_SIZE,
        "detections": [
            {
                "classId": 0,
                "label": CLASS_NAME,
                "bounds": {
                    "left": held_out_bounds[0],
                    "top": held_out_bounds[1],
                    "right": held_out_bounds[2],
                    "bottom": held_out_bounds[3],
                },
            }
        ],
        "suggestedAcceptance": {"minimumConfidence": 0.03, "minimumIoU": 0.90},
    }
    ground_truth_path = output_root / "ground-truth.json"
    write_json(ground_truth_path, ground_truth)

    generated_files = sorted(
        [path for path in dataset_root.rglob("*") if path.is_file()] + [held_out_image, ground_truth_path],
        key=lambda path: path.relative_to(output_root).as_posix(),
    )
    dataset_index = {
        "schemaVersion": 1,
        "generatorSeed": seed,
        "classNames": [CLASS_NAME],
        "trainImages": train_count,
        "validationImages": val_count,
        "heldOutImages": 1,
        "files": [file_record(path, output_root) for path in generated_files],
    }
    write_json(output_root / "dataset-index.json", dataset_index)
    return {
        "datasetYaml": dataset_yaml,
        "heldOutImage": held_out_image,
        "groundTruth": ground_truth_path,
    }


def write_model_manifest(path: Path) -> None:
    manifest = {
        "schemaVersion": 1,
        "engine": "yolo",
        "backend": "ncnn",
        "family": "ultralytics",
        "variant": "yolo11",
        "task": "detect",
        "input": {
            "name": "in0",
            "width": EXPORT_IMAGE_SIZE,
            "height": EXPORT_IMAGE_SIZE,
            "color": "RGB",
            "layout": "NCHW",
            "letterbox": True,
            "scale": 1.0 / 255.0,
            "paddingValue": 114,
        },
        "output": {
            "name": "out0",
            "decoder": "ultralytics-detect",
            "shape": [1, 5, 8400],
            "coordinates": "cxcywh",
            "objectness": False,
        },
        "labels": [CLASS_NAME],
    }
    write_json(path, manifest)


def locate_single(root: Path, name: str) -> Path:
    matches = [path for path in root.rglob(name) if path.is_file()]
    if len(matches) != 1:
        raise RuntimeError(f"Expected exactly one {name} below {root}, found {len(matches)}")
    return matches[0]


def validate_export_metadata(metadata_path: Path) -> dict[str, Any]:
    import yaml

    metadata = yaml.safe_load(metadata_path.read_text(encoding="utf-8"))
    if not isinstance(metadata, dict):
        raise RuntimeError("NCNN metadata root is not a mapping")
    if metadata.get("task") != "detect" or bool(metadata.get("end2end", False)):
        raise RuntimeError("NCNN export is not a non-end-to-end detect model")
    names = metadata.get("names")
    if isinstance(names, dict):
        exported_names = [names[key] for key in sorted(names, key=lambda item: int(item))]
    elif isinstance(names, list):
        exported_names = names
    else:
        raise RuntimeError("NCNN export metadata has no bounded class-name mapping")
    if exported_names != [CLASS_NAME]:
        raise RuntimeError(f"Unexpected NCNN export class names: {exported_names}")
    return metadata


def train_and_export(
    args: argparse.Namespace,
    base_model: Path,
    output_root: Path,
    dataset_yaml: Path,
) -> dict[str, Path]:
    os.environ.setdefault("OMP_NUM_THREADS", str(args.threads))
    os.environ.setdefault("MKL_NUM_THREADS", str(args.threads))
    os.environ.setdefault("YOLO_OFFLINE", "true")
    os.environ.setdefault("WANDB_MODE", "disabled")

    import torch
    from ultralytics import YOLO

    torch.set_num_threads(args.threads)
    torch.manual_seed(args.seed)

    training_args = {
        "data": str(dataset_yaml),
        "epochs": args.epochs,
        "imgsz": args.train_imgsz,
        "batch": args.batch,
        "device": "cpu",
        "workers": 0,
        "seed": args.seed,
        "deterministic": True,
        "amp": False,
        "cache": False,
        "optimizer": "SGD",
        "project": str(output_root / "runs"),
        "name": "train",
        "exist_ok": False,
        "plots": False,
        "save": True,
        "save_period": -1,
        "val": True,
        "freeze": 10,
        "mosaic": 0.0,
        "mixup": 0.0,
        "copy_paste": 0.0,
        "degrees": 0.0,
        "translate": 0.0,
        "scale": 0.0,
        "shear": 0.0,
        "perspective": 0.0,
        "flipud": 0.0,
        "fliplr": 0.0,
        "hsv_h": 0.0,
        "hsv_s": 0.0,
        "hsv_v": 0.0,
        "close_mosaic": 0,
        "verbose": False,
    }
    recipe_path = output_root / "training-recipe.json"
    write_json(
        recipe_path,
        {
            "schemaVersion": 1,
            "scriptVersion": SCRIPT_VERSION,
            "baseModelSha256": sha256(base_model),
            "arguments": training_args,
        },
    )

    model = YOLO(str(base_model))
    model.train(**training_args)
    best_path = require_file(Path(model.trainer.best), "trained best checkpoint")
    canonical_best = output_root / "model.best.pt"
    shutil.copy2(best_path, canonical_best)

    exported = YOLO(str(canonical_best)).export(
        format="ncnn",
        imgsz=EXPORT_IMAGE_SIZE,
        half=False,
        int8=False,
        device="cpu",
        batch=1,
    )
    export_root = Path(exported).resolve()
    if not export_root.exists():
        raise RuntimeError(f"Ultralytics returned a missing NCNN export path: {export_root}")
    search_root = export_root if export_root.is_dir() else export_root.parent
    exported_param = locate_single(search_root, "model.ncnn.param")
    exported_bin = locate_single(search_root, "model.ncnn.bin")
    exported_metadata = locate_single(search_root, "metadata.yaml")
    validate_export_metadata(exported_metadata)

    canonical_param = output_root / "model.ncnn.param"
    canonical_bin = output_root / "model.ncnn.bin"
    canonical_metadata = output_root / "metadata.yaml"
    shutil.copy2(exported_param, canonical_param)
    shutil.copy2(exported_bin, canonical_bin)
    shutil.copy2(exported_metadata, canonical_metadata)

    param_text = canonical_param.read_text(encoding="utf-8")
    if "Input                    in0" not in param_text or " out0 " not in f" {param_text} ":
        raise RuntimeError("NCNN graph does not expose the required in0/out0 nodes")

    manifest_path = output_root / "model-manifest-v1.json"
    write_model_manifest(manifest_path)
    return {
        "bestCheckpoint": canonical_best,
        "param": canonical_param,
        "bin": canonical_bin,
        "metadata": canonical_metadata,
        "manifest": manifest_path,
        "recipe": recipe_path,
    }


def build_lock(output_root: Path, artifacts: Iterable[Path]) -> Path:
    ordered = sorted(artifacts, key=lambda path: path.relative_to(output_root).as_posix())
    value = {
        "schemaVersion": 1,
        "state": "local-only",
        "family": "yolo11n-synthetic",
        "task": "detect",
        "backend": "ncnn",
        "decoderId": "ultralytics-detect",
        "classCount": 1,
        "outputShape": [1, 5, 8400],
        "distributionPolicy": "local validation only; model, dataset, and image bytes are excluded from Git and the provider APK",
        "artifacts": [file_record(path, output_root) for path in ordered],
    }
    path = output_root / "fixture.lock.json"
    write_json(path, value)
    return path


def main() -> int:
    args = parse_args()
    validate_args(args)
    repository_root, output_root = prepare_output_root(args.output_root)
    base_model = require_file(args.base_model, "base model")
    actual_base_hash = sha256(base_model)
    if actual_base_hash != args.base_sha256.lower():
        raise ValueError(
            f"Base model SHA-256 mismatch: expected {args.base_sha256.lower()}, found {actual_base_hash}"
        )

    dataset = generate_dataset(output_root, args.seed, args.train_count, args.val_count)
    exported = train_and_export(args, base_model, output_root, dataset["datasetYaml"])

    lock_inputs = [
        output_root / "dataset-index.json",
        dataset["groundTruth"],
        dataset["heldOutImage"],
        *exported.values(),
    ]
    fixture_lock = build_lock(output_root, lock_inputs)
    provenance = {
        "schemaVersion": 1,
        "state": "local-only",
        "generatedAtUtc": datetime.now(timezone.utc).isoformat(),
        "generator": {
            "path": Path(__file__).resolve().relative_to(repository_root).as_posix(),
            "version": SCRIPT_VERSION,
            "sha256": sha256(Path(__file__).resolve()),
        },
        "host": {
            "python": platform.python_version(),
            "implementation": platform.python_implementation(),
            "platform": platform.platform(),
            "cpuOnly": True,
        },
        "toolchain": {
            name: package_version(name)
            for name in ("ultralytics", "torch", "torchvision", "ncnn", "numpy", "Pillow", "PyYAML")
        },
        "baseModel": {
            "path": str(base_model),
            "length": base_model.stat().st_size,
            "sha256": actual_base_hash,
            "expectedSource": "https://github.com/ultralytics/assets/releases/download/v8.4.0/yolo11n.pt",
        },
        "dataset": {
            "kind": "programmatically generated; no third-party image assets",
            "seed": args.seed,
            "trainImages": args.train_count,
            "validationImages": args.val_count,
            "heldOutImages": 1,
            "index": file_record(output_root / "dataset-index.json", output_root),
        },
        "licenseBoundary": {
            "baseModelAndExporter": "Ultralytics metadata records AGPL-3.0; conversion and fine-tuning do not assert a license change",
            "syntheticDataset": "project-generated with no third-party image input; kept local-only",
            "distributionPolicy": "all generated model/data/image bytes remain below ignored fixtures/local and are not distributed",
        },
        "fixtureLock": file_record(fixture_lock, output_root),
    }
    provenance_path = output_root / "provenance.json"
    write_json(provenance_path, provenance)

    print(
        json.dumps(
            {
                "status": "PASS",
                "outputRoot": str(output_root),
                "fixtureLock": str(fixture_lock),
                "provenance": str(provenance_path),
                "modelParamSha256": sha256(exported["param"]),
                "modelBinSha256": sha256(exported["bin"]),
            },
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:
        print(f"R4 custom fixture failed: {error}", file=sys.stderr)
        raise
