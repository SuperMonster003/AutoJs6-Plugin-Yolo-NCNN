from __future__ import annotations

import json
from pathlib import Path
import sys
import tempfile
import unittest


REPOSITORY = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPOSITORY))

from tools import generate_yolo_ncnn_manifest as generator  # noqa: E402


class GenerateYoloNcnnManifestTest(unittest.TestCase):
    def fixture_manifest(self, name: str) -> dict[str, object]:
        path = REPOSITORY / "fixtures" / name / "model-manifest-v1.json"
        return json.loads(path.read_text(encoding="utf-8"))

    def metadata_text(self, labels: list[str], **overrides: object) -> str:
        values: dict[str, object] = {
            "description": "Ultralytics YOLO11n model trained on a local dataset",
            "stride": 32,
            "task": "detect",
            "head": "Detect",
            "batch": 1,
            "imgsz": [640, 640],
            "names": labels,
            "args": {"batch": 1, "quantize": None},
            "channels": 3,
            "end2end": False,
        }
        values.update(overrides)
        lines = [
            f"description: {values['description']}",
            f"stride: {values['stride']}",
            f"task: {values['task']}",
            f"head: {values['head']}",
            f"batch: {values['batch']}",
            "imgsz:",
        ]
        lines.extend(f"- {item}" for item in values["imgsz"])  # type: ignore[union-attr]
        lines.append("names:")
        names = values["names"]
        if isinstance(names, dict):
            entries = names.items()
        else:
            entries = enumerate(names)  # type: ignore[arg-type]
        lines.extend(
            f"  {index}: {json.dumps(label, ensure_ascii=False)}" for index, label in entries
        )
        lines.append("args:")
        arguments = values["args"]
        self.assertIsInstance(arguments, dict)
        for key, value in arguments.items():  # type: ignore[union-attr]
            if value is None:
                encoded = "null"
            elif value is True:
                encoded = "true"
            elif value is False:
                encoded = "false"
            else:
                encoded = value
            lines.append(f"  {key}: {encoded}")
        lines.extend(
            [
                f"channels: {values['channels']}",
                f"end2end: {str(values['end2end']).lower()}",
            ]
        )
        return "\n".join(lines) + "\n"

    def create_export(self, root: Path, metadata: str) -> None:
        root.mkdir()
        (root / "metadata.yaml").write_text(metadata, encoding="utf-8", newline="\n")
        (root / "model.ncnn.param").write_text(
            "7767517\n"
            "2 2\n"
            "Input in0 0 1 in0\n"
            "Split output 1 1 in0 out0\n",
            encoding="utf-8",
            newline="\n",
        )
        (root / "model.ncnn.bin").write_bytes(b"test-ncnn-weights")

    def generate_fixture(self, fixture: str) -> tuple[dict[str, object], dict[str, object]]:
        expected = self.fixture_manifest(fixture)
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary) / "export"
            self.create_export(root, self.metadata_text(expected["labels"]))  # type: ignore[arg-type]
            receipt = generator.generate(root)
            actual = json.loads((root / "model.json").read_text(encoding="utf-8"))
            self.assertEqual("write", receipt["mode"])
            return expected, actual

    def test_official_yolo11n_manifest_matches_provider_fixture(self) -> None:
        expected, actual = self.generate_fixture("yolo11n")
        self.assertEqual(expected, actual)
        self.assertEqual([1, 84, 8400], actual["output"]["shape"])

    def test_one_class_manifest_matches_provider_fixture(self) -> None:
        expected, actual = self.generate_fixture("yolo11n-synthetic")
        self.assertEqual(expected, actual)
        self.assertEqual([1, 5, 8400], actual["output"]["shape"])

    def test_check_idempotence_drift_and_force_recovery(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary) / "export"
            self.create_export(root, self.metadata_text(["target"]))
            self.assertEqual("write", generator.generate(root)["mode"])
            self.assertEqual("unchanged", generator.generate(root)["mode"])
            self.assertEqual("check", generator.generate(root, check=True)["mode"])
            (root / "model.json").write_text("{}\n", encoding="utf-8")
            with self.assertRaisesRegex(generator.ModelManifestError, "has drifted"):
                generator.generate(root, check=True)
            with self.assertRaisesRegex(generator.ModelManifestError, "without --force"):
                generator.generate(root)
            self.assertEqual("write", generator.generate(root, force=True)["mode"])
            self.assertEqual("check", generator.generate(root, check=True)["mode"])

    def test_rejects_unsupported_metadata_profiles(self) -> None:
        cases = {
            "wrong family": {"description": "Ultralytics YOLO26n model"},
            "wrong task": {"task": "segment"},
            "wrong head": {"head": "Segment"},
            "wrong stride": {"stride": 16},
            "wrong batch": {"batch": 2},
            "wrong image size": {"imgsz": [320, 320]},
            "wrong channels": {"channels": 1},
            "end to end": {"end2end": True},
            "missing args batch": {"args": {"quantize": None}},
            "missing quantize marker": {"args": {"batch": 1}},
            "quantized": {"args": {"batch": 1, "quantize": 16}},
            "nms": {"args": {"batch": 1, "quantize": None, "nms": True}},
        }
        for name, overrides in cases.items():
            with self.subTest(name=name), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary) / "export"
                self.create_export(root, self.metadata_text(["target"], **overrides))
                with self.assertRaises(generator.ModelManifestError):
                    generator.generate(root)

    def test_rejects_invalid_label_contracts(self) -> None:
        cases: dict[str, object] = {
            "empty": [],
            "duplicate": ["same", "same"],
            "blank": [" "],
            "leading whitespace": [" target"],
            "too long": ["界" * 86],
            "index gap": {0: "first", 2: "third"},
        }
        for name, names in cases.items():
            with self.subTest(name=name), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary) / "export"
                self.create_export(root, self.metadata_text(["placeholder"], names=names))
                with self.assertRaises(generator.ModelManifestError):
                    generator.generate(root)

    def test_rejects_invalid_ncnn_graph_contracts(self) -> None:
        cases = {
            "wrong magic": "0\n2 2\nInput in0 0 1 in0\nSplit output 1 1 in0 out0\n",
            "unknown bottom": "7767517\n2 2\nInput in0 0 1 in0\nSplit output 1 1 missing out0\n",
            "wrong input": "7767517\n2 2\nInput input 0 1 images\nSplit output 1 1 images out0\n",
            "wrong output": "7767517\n2 2\nInput in0 0 1 in0\nSplit output 1 1 in0 output0\n",
            "blob count": "7767517\n2 3\nInput in0 0 1 in0\nSplit output 1 1 in0 out0\n",
        }
        for name, param in cases.items():
            with self.subTest(name=name), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary) / "export"
                self.create_export(root, self.metadata_text(["target"]))
                (root / "model.ncnn.param").write_text(param, encoding="utf-8")
                with self.assertRaises(generator.ModelManifestError):
                    generator.generate(root)

    def test_rejects_duplicate_yaml_keys(self) -> None:
        metadata = self.metadata_text(["target"]).replace(
            "task: detect\n", "task: detect\ntask: detect\n"
        )
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary) / "export"
            self.create_export(root, metadata)
            with self.assertRaisesRegex(generator.ModelManifestError, "Duplicate top-level YAML key"):
                generator.generate(root)

    def test_supports_quoted_unicode_labels(self) -> None:
        labels = ["person: adult", "C# tag", "目标", "quote \\\" and apostrophe '"]
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary) / "export"
            metadata = self.metadata_text(labels)
            names_start = metadata.index("names:\n")
            args_start = metadata.index("args:\n")
            # PyYAML safe_dump writes top-level sequences without additional
            # indentation. Exercise that shape as well as indexed mappings.
            metadata = (
                metadata[:names_start]
                + "names:\n"
                + "".join(f"- {json.dumps(label, ensure_ascii=False)}\n" for label in labels)
                + metadata[args_start:]
            )
            self.create_export(root, metadata)
            generator.generate(root)
            manifest = json.loads((root / "model.json").read_text(encoding="utf-8"))
            self.assertEqual(labels, manifest["labels"])


if __name__ == "__main__":
    unittest.main()
