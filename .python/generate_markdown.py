# -*- coding: utf-8 -*-
"""Generate the localized README/CHANGELOG markdown set from the JSON sources.

Source of truth:
    .readme/common.json          -- language-neutral facts (versions, ids, urls)
    .readme/lang_<code>.json     -- localized README strings
    .readme/template_readme.md   -- README skeleton with {{ placeholders }}
    .changelog/lang_<code>.json  -- localized changelog labels and per-version data
    .changelog/template_changelog.md

Outputs (22 artifacts):
    .readme/README-<code>.md     -- one README per language
    README.md                    -- repository root, default language copy
    app/src/main/assets/doc/CHANGELOG-<code>.md
    CHANGELOG.md                 -- repository root, default language copy

Edit the JSON sources, never the generated markdown.

This generator is fully offline by design (standard library only, zero network
calls), matching the sibling-plugin convention of keeping maintainer tooling
clear of Cloudflare 502/524/529 noise on restricted development networks.

Localized release history is bundled under app/src/main/assets/doc. The provider
release gate pins those ten documents together with the license/provenance assets.

Usage:
    python .python/generate_markdown.py            # (re)generate all outputs
    python .python/generate_markdown.py --check    # fail if outputs drifted
"""
from __future__ import annotations

import argparse
import json
import re
import sys
import unicodedata
from pathlib import Path


LANGUAGE_CODES = [
    "zh-Hans",
    "zh-Hant-HK",
    "zh-Hant-TW",
    "en",
    "fr",
    "es",
    "ja",
    "ko",
    "ru",
    "ar",
]
LANGUAGE_CODE_DEFAULT = "zh-Hans"
CHANGELOG_CATEGORIES = ["hint", "feature", "fix", "improvement", "dependency"]
README_LIST_KEYS = ["features", "limits", "security_limits", "further_reading"]
PLACEHOLDER_MARKERS = (
    "TODO_TRANSLATION",
    "TRANSLATION_PENDING",
    "MACHINE_TRANSLATION_PLACEHOLDER",
)
TEMPLATE_PATTERN = re.compile(r"\{\{\s*([A-Za-z0-9_$.-]+)\s*\}\}")


class MarkdownGenerationError(RuntimeError):
    pass


def require(condition: bool, message: str) -> None:
    if not condition:
        raise MarkdownGenerationError(message)


def reject_duplicate_pairs(pairs: list[tuple[str, object]]) -> dict[str, object]:
    result: dict[str, object] = {}
    for key, value in pairs:
        require(key not in result, f"Duplicate JSON key: {key}")
        result[key] = value
    return result


def validate_no_fullwidth_symbols(path: Path, text: str) -> None:
    for line_number, line in enumerate(text.splitlines(), start=1):
        for column_number, char in enumerate(line, start=1):
            category = unicodedata.category(char)
            width = unicodedata.east_asian_width(char)
            if width in {"F", "W"} and category[0] in {"P", "S", "Z"}:
                raise MarkdownGenerationError(
                    f"Fullwidth symbol {char!r} at {path}:{line_number}:{column_number}"
                )


def load_json(path: Path) -> dict[str, object]:
    require(path.is_file() and not path.is_symlink(), f"Missing regular JSON source: {path}")
    text = path.read_text(encoding="utf-8")
    validate_no_fullwidth_symbols(path, text)
    for marker in PLACEHOLDER_MARKERS:
        require(marker.lower() not in text.lower(), f"Translation placeholder in {path}: {marker}")
    try:
        value = json.loads(text, object_pairs_hook=reject_duplicate_pairs)
    except (UnicodeDecodeError, json.JSONDecodeError) as error:
        raise MarkdownGenerationError(f"Invalid UTF-8 JSON: {path}") from error
    require(isinstance(value, dict), f"JSON source must contain an object: {path}")
    return value


def load_template(path: Path) -> str:
    require(path.is_file() and not path.is_symlink(), f"Missing regular template: {path}")
    text = path.read_text(encoding="utf-8")
    validate_no_fullwidth_symbols(path, text)
    for marker in PLACEHOLDER_MARKERS:
        require(marker.lower() not in text.lower(), f"Translation placeholder in {path}: {marker}")
    return text


def render_template(text: str, values: dict[str, object]) -> str:
    def replace(match: re.Match[str]) -> str:
        key = match.group(1).strip()
        require(key in values, f"Missing template value: {key}")
        return str(values[key])

    return TEMPLATE_PATTERN.sub(replace, text)


def render_dynamic(value: object, values: dict[str, object]) -> object:
    if isinstance(value, dict):
        return {key: render_dynamic(item, values) for key, item in value.items()}
    if isinstance(value, list):
        return [render_dynamic(item, values) for item in value]
    if isinstance(value, str):
        return render_template(value, values)
    return value


def bullet_list(value: object) -> str:
    require(
        isinstance(value, list) and all(isinstance(item, str) for item in value),
        "Bullet list source must be a list of strings",
    )
    return "\n".join(f"- {item}" for item in value)


def markdown_link(label: str, url: str) -> str:
    return f"[{label}]({url})"


def read_properties(path: Path) -> dict[str, str]:
    properties: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        require(key.strip() not in properties, f"Duplicate property in {path}: {key.strip()}")
        properties[key.strip()] = value.strip()
    return properties


def version_label(root: Path) -> str:
    version_name = read_properties(root / "version.properties").get("VERSION_NAME")
    require(bool(version_name), "VERSION_NAME is missing from version.properties")
    assert version_name is not None
    return version_name if version_name.startswith("v") else f"v{version_name}"


def validate_key_parity(items: dict[str, dict[str, object]], kind: str) -> None:
    base_keys = set(items[LANGUAGE_CODE_DEFAULT])
    for code, item in items.items():
        keys = set(item)
        require(
            keys == base_keys,
            f"{kind} key mismatch for {code}: "
            f"missing={sorted(base_keys - keys)}, extra={sorted(keys - base_keys)}",
        )


def validate_collection_shapes(items: dict[str, dict[str, object]], kind: str) -> None:
    base_item = items[LANGUAGE_CODE_DEFAULT]
    for code, item in items.items():
        for key, base_value in base_item.items():
            value = item[key]
            require(
                type(value) is type(base_value),
                f"{kind} type mismatch for {code}.{key}: "
                f"expected={type(base_value).__name__}, got={type(value).__name__}",
            )
            if isinstance(base_value, list):
                require(
                    len(value) == len(base_value),
                    f"{kind} list length mismatch for {code}.{key}: "
                    f"expected={len(base_value)}, got={len(value)}",
                )


def validate_changelog_shapes(changelogs: dict[str, dict[str, object]]) -> None:
    base_data = changelogs[LANGUAGE_CODE_DEFAULT]["$data"]
    require(isinstance(base_data, dict), "Default changelog $data must be an object")
    base_versions = list(base_data)
    allowed_keys = {"released_date", *CHANGELOG_CATEGORIES}
    for code, changelog in changelogs.items():
        data = changelog["$data"]
        require(isinstance(data, dict), f"Changelog $data must be an object: {code}")
        require(list(data) == base_versions, f"Changelog version mismatch for {code}")
        for release_version, base_release_value in base_data.items():
            release_value = data[release_version]
            require(
                isinstance(base_release_value, dict) and isinstance(release_value, dict),
                f"Invalid changelog release object: {code}.{release_version}",
            )
            require(
                set(release_value) <= allowed_keys,
                f"Unknown changelog fields for {code}.{release_version}: "
                f"{sorted(set(release_value) - allowed_keys)}",
            )
            require(
                set(release_value) == set(base_release_value),
                f"Changelog shape mismatch for {code}.{release_version}",
            )
            for key, base_value in base_release_value.items():
                value = release_value[key]
                require(
                    type(value) is type(base_value),
                    f"Changelog type mismatch for {code}.{release_version}.{key}",
                )
                if isinstance(base_value, list):
                    require(
                        len(value) == len(base_value),
                        f"Changelog list length mismatch for {code}.{release_version}.{key}",
                    )


def load_languages(
    root: Path,
) -> tuple[dict[str, dict[str, object]], dict[str, dict[str, object]]]:
    readme_dir = root / ".readme"
    changelog_dir = root / ".changelog"
    common = load_json(readme_dir / "common.json")
    raw_languages = {
        code: load_json(readme_dir / f"lang_{code}.json") for code in LANGUAGE_CODES
    }
    raw_changelogs = {
        code: load_json(changelog_dir / f"lang_{code}.json") for code in LANGUAGE_CODES
    }
    validate_key_parity(raw_languages, "README")
    validate_key_parity(raw_changelogs, "changelog")
    validate_collection_shapes(raw_languages, "README")
    validate_collection_shapes(raw_changelogs, "changelog")
    validate_changelog_shapes(raw_changelogs)

    current_version = version_label(root)
    base_data = raw_changelogs[LANGUAGE_CODE_DEFAULT]["$data"]
    assert isinstance(base_data, dict)
    require(
        list(base_data)[:1] == [current_version],
        f"Latest changelog version must be {current_version!r}",
    )

    languages: dict[str, dict[str, object]] = {}
    changelogs: dict[str, dict[str, object]] = {}
    for code in LANGUAGE_CODES:
        merged_language = {**common, **raw_languages[code]}
        merged_language["version_name"] = current_version.removeprefix("v")
        rendered_language = render_dynamic(merged_language, merged_language)
        assert isinstance(rendered_language, dict)
        languages[code] = rendered_language

        raw_changelog = raw_changelogs[code]
        changelog_values = {
            key: value for key, value in raw_changelog.items() if key != "$data"
        }
        rendered_values = render_dynamic(changelog_values, changelog_values)
        assert isinstance(rendered_values, dict)
        rendered_data = render_dynamic(
            raw_changelog["$data"],
            {**common, **rendered_values},
        )
        assert isinstance(rendered_data, dict)
        changelogs[code] = {"values": rendered_values, "data": rendered_data}
    return languages, changelogs


def format_changelog_items(changelog: dict[str, object], limit: int | None = None) -> str:
    values = changelog["values"]
    data = changelog["data"]
    assert isinstance(values, dict) and isinstance(data, dict)
    chunks: list[str] = []
    for index, (release_version, item_value) in enumerate(data.items()):
        if limit is not None and index >= limit:
            break
        assert isinstance(item_value, dict)
        lines = [f"# {release_version}", "", f"###### {item_value['released_date']}", ""]
        for category in CHANGELOG_CATEGORIES:
            entries = item_value.get(category, [])
            assert isinstance(entries, list)
            for entry in entries:
                lines.append(f"* `{values[f'changelog_label_{category}']}` {entry}")
        chunks.append("\n".join(lines).rstrip())
    return "\n\n".join(chunks).rstrip() + "\n"


def build_language_list(
    target_code: str,
    languages: dict[str, dict[str, object]],
) -> str:
    target = languages[target_code]
    repo_url = str(target["repo_url"])
    default_branch = str(target["default_branch"])
    lines: list[str] = []
    for code in LANGUAGE_CODES:
        content = languages[code]
        label = f"{content['$name']} [{code}]"
        if code == target_code:
            lines.append(f"- {label} # {content['text_current_lowercase']}")
        else:
            lines.append(
                f"- {markdown_link(label, f'{repo_url}/blob/{default_branch}/.readme/README-{code}.md')}"
            )
    return "\n".join(lines)


def build_readme_values(
    root: Path,
    code: str,
    languages: dict[str, dict[str, object]],
    changelogs: dict[str, dict[str, object]],
) -> dict[str, object]:
    content = dict(languages[code])
    repo_url = str(content["repo_url"])
    default_branch = str(content["default_branch"])
    content["version_name"] = version_label(root).removeprefix("v")
    content["placeholder_ul_languages_all_supported"] = build_language_list(code, languages)
    for key in README_LIST_KEYS:
        content[f"placeholder_{key}"] = bullet_list(content[key])
    content["placeholder_latest_release_history"] = format_changelog_items(
        changelogs[code], limit=3
    ).rstrip()
    content["placeholder_read_more_in_changelog_md"] = markdown_link(
        f"CHANGELOG-{code}.md",
        f"{repo_url}/blob/{default_branch}/app/src/main/assets/doc/CHANGELOG-{code}.md",
    )
    return content


def build_artifacts(root: Path) -> dict[Path, str]:
    root = root.resolve()
    require(LANGUAGE_CODE_DEFAULT in LANGUAGE_CODES, "Default language is not supported")
    require(len(LANGUAGE_CODES) == 10 and len(set(LANGUAGE_CODES)) == 10, "Language inventory drift")
    languages, changelogs = load_languages(root)
    readme_template = load_template(root / ".readme/template_readme.md")
    changelog_template = load_template(root / ".changelog/template_changelog.md")

    artifacts: dict[Path, str] = {}
    for code in LANGUAGE_CODES:
        readme_output = render_template(
            readme_template,
            build_readme_values(root, code, languages, changelogs),
        ).rstrip() + "\n"
        require(
            not TEMPLATE_PATTERN.search(readme_output),
            f"Unresolved README placeholder for {code}",
        )
        artifacts[Path(f".readme/README-{code}.md")] = readme_output
        if code == LANGUAGE_CODE_DEFAULT:
            artifacts[Path("README.md")] = readme_output

        changelog_values = dict(languages[code])
        changelog_values["placeholder_release_history"] = format_changelog_items(
            changelogs[code]
        ).rstrip()
        changelog_output = render_template(changelog_template, changelog_values).rstrip() + "\n"
        require(
            not TEMPLATE_PATTERN.search(changelog_output),
            f"Unresolved changelog placeholder for {code}",
        )
        artifacts[Path(f"app/src/main/assets/doc/CHANGELOG-{code}.md")] = changelog_output
        if code == LANGUAGE_CODE_DEFAULT:
            artifacts[Path("CHANGELOG.md")] = changelog_output

    require(len(artifacts) == 22, f"Generated artifact inventory drift: {len(artifacts)}")
    return artifacts


def generated_inventory(root: Path) -> set[Path]:
    inventory = {
        path.relative_to(root)
        for path in (root / ".readme").glob("README-*.md")
        if path.is_file()
    }
    inventory.update(
        path.relative_to(root)
        for path in (root / "app/src/main/assets/doc").glob("CHANGELOG-*.md")
        if path.is_file()
    )
    for name in ("README.md", "CHANGELOG.md"):
        if (root / name).is_file():
            inventory.add(Path(name))
    return inventory


def write_or_check(root: Path, check: bool) -> int:
    root = root.resolve()
    artifacts = build_artifacts(root)
    expected_inventory = set(artifacts)
    actual_inventory = generated_inventory(root)
    if check:
        require(
            actual_inventory == expected_inventory,
            f"Generated output inventory drift: expected={sorted(map(str, expected_inventory))}, "
            f"actual={sorted(map(str, actual_inventory))}",
        )
    for relative, expected in artifacts.items():
        destination = root / relative
        if check:
            require(
                destination.is_file()
                and not destination.is_symlink()
                and destination.read_text(encoding="utf-8") == expected,
                f"Generated content drift: {relative}",
            )
        else:
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_text(expected, encoding="utf-8", newline="\n")
            print(f"Generated {relative.as_posix()}")
    return len(artifacts)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Generate the 10-language README and changelog set")
    parser.add_argument("--check", action="store_true", help="Fail if generated Markdown has drifted")
    parser.add_argument(
        "--root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help=argparse.SUPPRESS,
    )
    args = parser.parse_args(argv)
    try:
        artifacts = write_or_check(args.root, args.check)
    except (MarkdownGenerationError, OSError) as error:
        print(f"MARKDOWN_ERROR {error}", file=sys.stderr)
        return 1
    mode = "check" if args.check else "write"
    print(f"MARKDOWN_OK languages={len(LANGUAGE_CODES)} artifacts={artifacts} mode={mode}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
