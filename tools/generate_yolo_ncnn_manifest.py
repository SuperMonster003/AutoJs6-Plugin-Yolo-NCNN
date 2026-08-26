#!/usr/bin/env python3
"""Generate AutoJs6 Model Manifest v1 from an Ultralytics YOLO11 NCNN export.

The command is deliberately offline and standard-library-only. It accepts the
normal Ultralytics NCNN export directory containing metadata.yaml,
model.ncnn.param, and model.ncnn.bin, validates the frozen Provider profile,
and writes a deterministic model.json next to those files.

This is a compatibility preflight, not an inference test. The Provider remains
authoritative and validates the NCNN runtime output shape when opening a model.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import sys
from typing import Any


METADATA_NAME = "metadata.yaml"
PARAM_NAME = "model.ncnn.param"
BIN_NAME = "model.ncnn.bin"
MANIFEST_NAME = "model.json"
METADATA_MAX_BYTES = 1024 * 1024
PARAM_MAX_BYTES = 16 * 1024 * 1024
MANIFEST_MAX_BYTES = 256 * 1024
NCNN_PARAM_MAGIC = "7767517"
INPUT_NAME = "in0"
OUTPUT_NAME = "out0"
INPUT_SIZE = 640
OUTPUT_COLUMNS = 8400
MAX_LABELS = 256
MAX_LABEL_BYTES = 256
BOX_FIELDS = 4


class ModelManifestError(ValueError):
    """An expected, user-actionable export or manifest validation failure."""


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ModelManifestError(message)


def _read_text(path: Path, label: str, maximum_bytes: int) -> str:
    require(path.exists(), f"Missing {label}: {path}")
    require(not path.is_symlink(), f"{label} must not be a symbolic link: {path}")
    require(path.is_file(), f"{label} is not a regular file: {path}")
    size = path.stat().st_size
    require(1 <= size <= maximum_bytes, f"{label} length must be within 1..{maximum_bytes} bytes")
    try:
        text = path.read_text(encoding="utf-8", errors="strict")
    except UnicodeDecodeError as error:
        raise ModelManifestError(f"{label} must be strict UTF-8: {path}") from error
    require("\x00" not in text, f"{label} contains a NUL character: {path}")
    return text


def _require_binary(path: Path) -> None:
    require(path.exists(), f"Missing NCNN bin: {path}")
    require(not path.is_symlink(), f"NCNN bin must not be a symbolic link: {path}")
    require(path.is_file(), f"NCNN bin is not a regular file: {path}")
    require(path.stat().st_size > 0, f"NCNN bin is empty: {path}")


def _strip_yaml_comment(value: str) -> str:
    quote: str | None = None
    escaped = False
    depth = 0
    index = 0
    while index < len(value):
        char = value[index]
        if quote == '"':
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == quote:
                quote = None
        elif quote == "'":
            if char == "'" and index + 1 < len(value) and value[index + 1] == "'":
                index += 1
            elif char == quote:
                quote = None
        elif char in {'"', "'"}:
            quote = char
        elif char in "[{":
            depth += 1
        elif char in "]}":
            depth -= 1
            require(depth >= 0, "Unbalanced YAML flow collection")
        elif char == "#" and depth == 0 and (index == 0 or value[index - 1].isspace()):
            return value[:index].rstrip()
        index += 1
    require(quote is None and depth == 0, "Unterminated YAML scalar or flow collection")
    return value.rstrip()


def _split_flow(value: str) -> list[str]:
    items: list[str] = []
    quote: str | None = None
    escaped = False
    depth = 0
    start = 0
    index = 0
    while index < len(value):
        char = value[index]
        if quote == '"':
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == quote:
                quote = None
        elif quote == "'":
            if char == "'" and index + 1 < len(value) and value[index + 1] == "'":
                index += 1
            elif char == quote:
                quote = None
        elif char in {'"', "'"}:
            quote = char
        elif char in "[{":
            depth += 1
        elif char in "]}":
            depth -= 1
            require(depth >= 0, "Unbalanced YAML flow collection")
        elif char == "," and depth == 0:
            item = value[start:index].strip()
            require(bool(item), "Empty YAML flow item")
            items.append(item)
            start = index + 1
        index += 1
    require(quote is None and depth == 0, "Unterminated YAML flow collection")
    tail = value[start:].strip()
    if tail:
        items.append(tail)
    elif value.strip():
        raise ModelManifestError("Trailing comma in YAML flow collection")
    return items


def _find_mapping_colon(value: str) -> int:
    quote: str | None = None
    escaped = False
    depth = 0
    index = 0
    while index < len(value):
        char = value[index]
        if quote == '"':
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == quote:
                quote = None
        elif quote == "'":
            if char == "'" and index + 1 < len(value) and value[index + 1] == "'":
                index += 1
            elif char == quote:
                quote = None
        elif char in {'"', "'"}:
            quote = char
        elif char in "[{":
            depth += 1
        elif char in "]}":
            depth -= 1
        elif char == ":" and depth == 0:
            return index
        index += 1
    return -1


def _parse_yaml_scalar(raw: str) -> Any:
    value = _strip_yaml_comment(raw.strip())
    require(bool(value), "Empty YAML scalar")
    if value.startswith('"'):
        try:
            parsed = json.loads(value)
        except json.JSONDecodeError as error:
            raise ModelManifestError(f"Invalid double-quoted YAML scalar: {value}") from error
        require(isinstance(parsed, str), "Double-quoted YAML scalar must be a string")
        return parsed
    if value.startswith("'"):
        require(value.endswith("'") and len(value) >= 2, "Invalid single-quoted YAML scalar")
        return value[1:-1].replace("''", "'")
    if value.startswith("["):
        require(value.endswith("]"), "Invalid YAML flow sequence")
        body = value[1:-1].strip()
        return [] if not body else [_parse_yaml_scalar(item) for item in _split_flow(body)]
    if value.startswith("{"):
        require(value.endswith("}"), "Invalid YAML flow mapping")
        body = value[1:-1].strip()
        result: dict[Any, Any] = {}
        for item in ([] if not body else _split_flow(body)):
            colon = _find_mapping_colon(item)
            require(colon > 0, f"Invalid YAML flow mapping item: {item}")
            key = _parse_yaml_scalar(item[:colon])
            require(key not in result, f"Duplicate YAML mapping key: {key!r}")
            result[key] = _parse_yaml_scalar(item[colon + 1 :])
        return result
    lower = value.lower()
    if lower in {"null", "~"}:
        return None
    if lower in {"true", "false"}:
        return lower == "true"
    if re.fullmatch(r"[-+]?(?:0|[1-9][0-9]*)", value):
        return int(value)
    if re.fullmatch(r"[-+]?(?:[0-9]+\.[0-9]*|[0-9]*\.[0-9]+)(?:e[-+]?[0-9]+)?", value, re.I):
        return float(value)
    require(value[0] not in "&*!>|%@`", f"Unsupported YAML scalar construct: {value}")
    return value


def _parse_yaml_block(records: list[tuple[int, int, str]], key: str) -> Any:
    require(bool(records), f"YAML key {key!r} has no value")
    indentation = records[0][1]
    require(
        all(record[1] == indentation for record in records),
        f"YAML key {key!r} uses unsupported nested indentation",
    )
    contents = [record[2] for record in records]
    is_sequence = contents[0].startswith("-")
    require(
        all(content.startswith("-") == is_sequence for content in contents),
        f"YAML key {key!r} mixes sequence and mapping entries",
    )
    if is_sequence:
        # PyYAML's safe dumper emits block sequence entries directly below a
        # mapping key without additional indentation ("imgsz:\n- 640").
        values = []
        for content in contents:
            require(content == "-" or content.startswith("- "), f"Invalid YAML sequence item: {content}")
            values.append(_parse_yaml_scalar(content[1:].strip()))
        return values

    require(indentation > 0, f"YAML mapping below {key!r} must be indented")
    result: dict[Any, Any] = {}
    for line_number, _, content in records:
        colon = _find_mapping_colon(content)
        require(colon > 0, f"Invalid YAML mapping at line {line_number}: {content}")
        raw_key = content[:colon].strip()
        raw_value = content[colon + 1 :].strip()
        parsed_key = _parse_yaml_scalar(raw_key)
        require(parsed_key not in result, f"Duplicate YAML key {parsed_key!r} below {key!r}")
        require(bool(raw_value), f"Nested YAML structures are unsupported below {key!r}")
        result[parsed_key] = _parse_yaml_scalar(raw_value)
    return result


def parse_ultralytics_metadata(text: str) -> dict[str, Any]:
    """Parse the conservative subset emitted by Ultralytics YAML.save.

    The exporter uses PyYAML safe_dump with block mappings and sequences. This
    parser intentionally implements only that bounded shape, which keeps the
    standalone conversion command dependency-free and fail-closed.
    """

    stripped = text.lstrip()
    if stripped.startswith("{"):
        try:
            value = json.loads(
                text,
                object_pairs_hook=lambda pairs: _reject_duplicate_json_pairs(pairs),
            )
        except json.JSONDecodeError as error:
            raise ModelManifestError("metadata.yaml is not valid JSON-compatible YAML") from error
        require(isinstance(value, dict), "metadata.yaml root must be a mapping")
        return value

    records: list[tuple[int, int, str]] = []
    for line_number, raw_line in enumerate(text.splitlines(), start=1):
        require("\t" not in raw_line, f"Tabs are not supported in metadata.yaml at line {line_number}")
        content = raw_line.lstrip(" ")
        if not content or content.startswith("#") or content in {"---", "..."}:
            continue
        records.append((line_number, len(raw_line) - len(content), content.rstrip()))

    result: dict[str, Any] = {}
    relevant_blocks = {"imgsz", "names", "args"}
    index = 0
    while index < len(records):
        line_number, indentation, content = records[index]
        require(indentation == 0, f"Unexpected YAML indentation at line {line_number}")
        match = re.fullmatch(r"([A-Za-z_][A-Za-z0-9_-]*):(.*)", content)
        require(match is not None, f"Invalid top-level YAML mapping at line {line_number}: {content}")
        key = match.group(1)
        require(key not in result, f"Duplicate top-level YAML key: {key}")
        raw_value = match.group(2).strip()
        next_index = index + 1
        while next_index < len(records):
            candidate = records[next_index]
            if candidate[1] > 0 or (
                key in relevant_blocks and candidate[1] == 0 and candidate[2].startswith("-")
            ):
                next_index += 1
                continue
            break
        block = records[index + 1 : next_index]
        if raw_value:
            require(not block, f"YAML key {key!r} has both inline and block values")
            result[key] = _parse_yaml_scalar(raw_value)
        elif key in relevant_blocks:
            result[key] = _parse_yaml_block(block, key)
        else:
            result[key] = None
        index = next_index
    return result


def _reject_duplicate_json_pairs(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    value: dict[str, Any] = {}
    for key, item in pairs:
        require(key not in value, f"Duplicate JSON-compatible YAML key: {key}")
        value[key] = item
    return value


def _require_int(value: Any, name: str, expected: int) -> None:
    require(type(value) is int and value == expected, f"metadata {name} must be integer {expected}")


def _metadata_labels(metadata: dict[str, Any]) -> list[str]:
    names = metadata.get("names")
    if isinstance(names, list):
        labels = names
    elif isinstance(names, dict):
        indexed: dict[int, Any] = {}
        for raw_index, label in names.items():
            require(not isinstance(raw_index, bool), "metadata names keys must be class indices")
            if type(raw_index) is int:
                index = raw_index
            elif isinstance(raw_index, str) and re.fullmatch(r"0|[1-9][0-9]*", raw_index):
                index = int(raw_index)
            else:
                raise ModelManifestError(f"metadata names key is not a non-negative class index: {raw_index!r}")
            require(index not in indexed, f"metadata names contains duplicate class index {index}")
            indexed[index] = label
        require(
            sorted(indexed) == list(range(len(indexed))),
            "metadata names class indices must be contiguous from 0",
        )
        labels = [indexed[index] for index in range(len(indexed))]
    else:
        raise ModelManifestError("metadata names must be a class-name mapping or list")

    require(1 <= len(labels) <= MAX_LABELS, f"metadata names must contain 1..{MAX_LABELS} labels")
    checked: list[str] = []
    for index, label in enumerate(labels):
        require(isinstance(label, str), f"metadata label {index} must be a string")
        require(bool(label) and not label.isspace(), f"metadata label {index} must not be blank")
        require(label == label.strip(), f"metadata label {index} has leading or trailing whitespace")
        require(
            all(ord(character) >= 0x20 and character != "\x7f" for character in label),
            f"metadata label {index} contains a control character",
        )
        require(
            len(label.encode("utf-8")) <= MAX_LABEL_BYTES,
            f"metadata label {index} exceeds {MAX_LABEL_BYTES} UTF-8 bytes",
        )
        checked.append(label)
    require(len(set(checked)) == len(checked), "metadata labels must be unique")
    return checked


def validate_metadata(metadata: dict[str, Any]) -> list[str]:
    description = metadata.get("description")
    require(isinstance(description, str), "metadata description must be a string")
    require(
        re.search(r"\bYOLO11[A-Za-z0-9._-]*\b", description, re.I) is not None,
        "metadata description must identify a YOLO11 model",
    )
    require(metadata.get("task") == "detect", "metadata task must be detect")
    require(metadata.get("head") == "Detect", "metadata head must be Detect")
    _require_int(metadata.get("stride"), "stride", 32)
    _require_int(metadata.get("batch"), "batch", 1)
    _require_int(metadata.get("channels"), "channels", 3)
    require(metadata.get("imgsz") == [INPUT_SIZE, INPUT_SIZE], "metadata imgsz must be [640, 640]")
    require(metadata.get("end2end") is False, "metadata end2end must be false")

    arguments = metadata.get("args")
    require(isinstance(arguments, dict), "metadata args must be a mapping")
    _require_int(arguments.get("batch"), "args.batch", 1)
    for flag in ("dynamic", "nms", "half", "int8"):
        if flag in arguments:
            require(arguments[flag] is False, f"metadata args.{flag} must be false")
    require(
        "quantize" in arguments and arguments["quantize"] is None,
        "metadata args.quantize must be present and null",
    )
    return _metadata_labels(metadata)


def validate_ncnn_param(text: str) -> dict[str, int]:
    lines = [line.strip() for line in text.splitlines() if line.strip()]
    require(len(lines) >= 3, "NCNN param must contain a header and at least one layer")
    require(lines[0] == NCNN_PARAM_MAGIC, f"NCNN param magic must be {NCNN_PARAM_MAGIC}")
    counts = lines[1].split()
    require(len(counts) == 2, "NCNN param count line must contain layer and blob counts")
    try:
        declared_layers, declared_blobs = (int(item) for item in counts)
    except ValueError as error:
        raise ModelManifestError("NCNN param layer/blob counts must be integers") from error
    require(declared_layers > 0 and declared_blobs > 0, "NCNN param layer/blob counts must be positive")
    layer_lines = lines[2:]
    require(
        len(layer_lines) == declared_layers,
        f"NCNN param declares {declared_layers} layers but contains {len(layer_lines)}",
    )

    produced: set[str] = set()
    consumed: set[str] = set()
    input_layers = 0
    for index, line in enumerate(layer_lines):
        tokens = line.split()
        require(len(tokens) >= 4, f"NCNN layer {index} is truncated")
        layer_type = tokens[0]
        try:
            bottom_count = int(tokens[2])
            top_count = int(tokens[3])
        except ValueError as error:
            raise ModelManifestError(f"NCNN layer {index} has non-integer edge counts") from error
        require(bottom_count >= 0 and top_count > 0, f"NCNN layer {index} has invalid edge counts")
        required_tokens = 4 + bottom_count + top_count
        require(len(tokens) >= required_tokens, f"NCNN layer {index} omits one or more blob names")
        bottoms = tokens[4 : 4 + bottom_count]
        tops = tokens[4 + bottom_count : required_tokens]
        require(len(set(tops)) == len(tops), f"NCNN layer {index} repeats an output blob")
        for bottom in bottoms:
            require(bottom in produced, f"NCNN layer {index} consumes unknown blob {bottom!r}")
            consumed.add(bottom)
        for top in tops:
            require(top not in produced, f"NCNN blob {top!r} has multiple producers")
            produced.add(top)
        if layer_type == "Input":
            input_layers += 1
            require(index == 0, "NCNN Input must be the first layer")
            require(bottom_count == 0 and tops == [INPUT_NAME], f"NCNN Input must expose only {INPUT_NAME}")

    require(input_layers == 1, "NCNN graph must contain exactly one Input layer")
    require(len(produced) == declared_blobs, f"NCNN param declares {declared_blobs} blobs but produces {len(produced)}")
    graph_outputs = produced - consumed
    require(graph_outputs == {OUTPUT_NAME}, f"NCNN graph outputs must be exactly [{OUTPUT_NAME}]")
    return {"layers": declared_layers, "blobs": declared_blobs}


def build_manifest(labels: list[str]) -> dict[str, Any]:
    return {
        "schemaVersion": 1,
        "engine": "yolo",
        "backend": "ncnn",
        "family": "ultralytics",
        "variant": "yolo11",
        "task": "detect",
        "input": {
            "name": INPUT_NAME,
            "width": INPUT_SIZE,
            "height": INPUT_SIZE,
            "color": "RGB",
            "layout": "NCHW",
            "letterbox": True,
            "scale": 1.0 / 255.0,
            "paddingValue": 114,
        },
        "output": {
            "name": OUTPUT_NAME,
            "decoder": "ultralytics-detect",
            "shape": [1, BOX_FIELDS + len(labels), OUTPUT_COLUMNS],
            "coordinates": "cxcywh",
            "objectness": False,
        },
        "labels": labels,
    }


def _sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _artifact_record(path: Path) -> dict[str, Any]:
    return {"path": str(path), "bytes": path.stat().st_size, "sha256": _sha256(path)}


def _write_atomic(path: Path, content: bytes) -> None:
    temporary = path.with_name(f".{path.name}.tmp-{os.getpid()}")
    require(
        not temporary.exists() and not temporary.is_symlink(),
        f"Temporary manifest path already exists: {temporary}",
    )
    try:
        with temporary.open("xb") as stream:
            stream.write(content)
            stream.flush()
            os.fsync(stream.fileno())
        os.replace(temporary, path)
    finally:
        temporary.unlink(missing_ok=True)


def _content_matches(path: Path, content: bytes) -> bool:
    return path.stat().st_size == len(content) and path.read_bytes() == content


def generate(export_directory: Path, *, check: bool = False, force: bool = False) -> dict[str, Any]:
    require(not (check and force), "--check and --force are mutually exclusive")
    requested = export_directory.expanduser()
    require(requested.exists(), f"NCNN export directory does not exist: {requested}")
    require(not requested.is_symlink(), f"NCNN export directory must not be a symbolic link: {requested}")
    root = requested.resolve()
    require(root.is_dir(), f"NCNN export path is not a directory: {root}")

    metadata_path = root / METADATA_NAME
    param_path = root / PARAM_NAME
    bin_path = root / BIN_NAME
    output_path = root / MANIFEST_NAME
    metadata_text = _read_text(metadata_path, "Ultralytics metadata", METADATA_MAX_BYTES)
    param_text = _read_text(param_path, "NCNN param", PARAM_MAX_BYTES)
    _require_binary(bin_path)

    metadata = parse_ultralytics_metadata(metadata_text)
    labels = validate_metadata(metadata)
    graph = validate_ncnn_param(param_text)
    manifest = build_manifest(labels)
    content = (json.dumps(manifest, ensure_ascii=False, indent=2) + "\n").encode("utf-8")
    require(len(content) <= MANIFEST_MAX_BYTES, "Generated manifest exceeds the Provider byte limit")
    require(not output_path.is_symlink(), f"Manifest output must not be a symbolic link: {output_path}")

    mode: str
    if check:
        require(output_path.exists(), f"Generated manifest is missing: {output_path}")
        require(output_path.is_file(), f"Generated manifest is not a regular file: {output_path}")
        require(_content_matches(output_path, content), f"Generated manifest has drifted: {output_path}")
        mode = "check"
    elif output_path.exists():
        require(output_path.is_file(), f"Manifest output is not a regular file: {output_path}")
        if _content_matches(output_path, content):
            mode = "unchanged"
        else:
            require(force, f"Refusing to overwrite differing manifest without --force: {output_path}")
            _write_atomic(output_path, content)
            mode = "write"
    else:
        _write_atomic(output_path, content)
        mode = "write"

    return {
        "schemaVersion": 1,
        "mode": mode,
        "labels": len(labels),
        "outputShape": manifest["output"]["shape"],
        "graph": graph,
        "metadata": _artifact_record(metadata_path),
        "artifacts": {
            "manifest": _artifact_record(output_path),
            "ncnn-param": _artifact_record(param_path),
            "ncnn-bin": _artifact_record(bin_path),
        },
    }


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate AutoJs6 model.json from an Ultralytics YOLO11 NCNN export directory."
    )
    parser.add_argument(
        "export_directory",
        type=Path,
        help="Directory containing metadata.yaml, model.ncnn.param, and model.ncnn.bin.",
    )
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--check", action="store_true", help="Fail unless model.json is present and current.")
    mode.add_argument("--force", action="store_true", help="Atomically replace a differing model.json.")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    try:
        result = generate(args.export_directory, check=args.check, force=args.force)
    except (ModelManifestError, OSError) as error:
        print(f"MODEL_MANIFEST_ERROR {error}", file=sys.stderr)
        return 1
    print("MODEL_MANIFEST_OK " + json.dumps(result, ensure_ascii=False, separators=(",", ":")))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
