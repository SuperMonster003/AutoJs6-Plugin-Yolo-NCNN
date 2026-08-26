<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="{{ repo_url }}/blob/{{ default_branch }}/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-yolo-ncnn-ic-launcher" border="0" width="128" />
  </p>

  <h1>{{ text_title }}</h1>

  <p>{{ text_plugin_synopsis }}</p>

  <p>
    <a href="{{ repo_url }}/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/{{ repo_slug }}?label=Release"/></a>
    <a href="{{ repo_url }}/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/{{ repo_slug }}?color=A24232&label=Issues"/></a>
    <a href="{{ repo_url }}/commit/{{ created_commit }}"><img alt="Created" src="https://img.shields.io/date/{{ created_timestamp }}?color=2e7d32&label=Created"/></a>
    <br>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2023.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2023.3+-EE4677"/></a>
    <a href="{{ license_url }}"><img alt="GitHub License" src="https://img.shields.io/github/license/{{ repo_slug }}?color=534BAE&label=License"/></a>
  </p>
</div>

******

### {{ h3_languages }}

******

{{ p_languages }}:

{{ placeholder_ul_languages_all_supported }}

******

### {{ h3_introduction }}

******

{{ p_introduction }}

```text
application ID: {{ application_id }}
plugin / engine / variant: {{ plugin_id }} / {{ plugin_engine }} / {{ plugin_variant }}
provider ID: {{ provider_id }}
discovery actions: org.autojs.plugin.INFO / org.autojs.plugin.YOLO
runtime process: {{ runtime_process }}
protocol version: {{ protocol_version }}
backend / task / decoder: {{ plugin_variant }} / detect / {{ decoder_id }}
supported ABI: {{ supported_abi }}
minimum host build: {{ required_host_build }} (AutoJs6 {{ required_host_version }}+)
```

{{ p_identity_note }}

******

### {{ h3_features }}

******

{{ placeholder_features }}

******

### {{ h3_quick_start }}

******

- **{{ quick_start_install_title }}** — {{ quick_start_install }}
- **{{ quick_start_enable_title }}** — {{ quick_start_enable }}
- **{{ quick_start_run_title }}** — {{ quick_start_run }}
- **{{ quick_start_debug_title }}** — {{ quick_start_debug }}

******

### {{ h3_usage }}

******

{{ p_usage_intro }}:

```javascript
"use strict";

const providerComponent = "{{ provider_component }}";
const modelDirectory = files.path("./models/yolo11n");

let detector = null;
let image = null;

try {
    detector = yolo.load(modelDirectory, {
        component: providerComponent,
        device: "cpu",
        threads: {{ default_cpu_threads }},
        decoderId: "{{ decoder_id }}",
        timeoutMillis: {{ default_open_timeout_millis }},
    });
    image = images.read(files.path("./bus.jpg"), true);

    const detections = detector.detect(image, {
        confidence: {{ default_confidence }},
        iouThreshold: {{ default_iou }},
        maxDetections: {{ default_max_detections }},
        timeoutMillis: {{ default_detect_timeout_millis }},
    });

    detections.forEach((detection) => {
        const bounds = detection.bounds;
        console.log(
            detection.label + " " + detection.confidence.toFixed(4)
            + " [" + bounds.left + ", " + bounds.top + ", " + bounds.right + ", " + bounds.bottom + "]",
        );
    });
} finally {
    if (image !== null) {
        images.recycle(image);
    }
    if (detector !== null) {
        detector.close();
    }
}
```

{{ p_usage_note }}

******

### {{ h3_models }}

******

{{ p_models_intro }}:

```text
models/yolo11n/
|-- {{ model_manifest_file }}
|-- {{ model_param_file }}
`-- {{ model_bin_file }}
```

{{ p_models_export }}

{{ p_models_note }}

******

### {{ h3_script_api }}

******

{{ p_api_load }}

{{ p_api_detect }}

{{ p_api_close }}

******

### {{ h3_limits }}

******

{{ p_limits_intro }}:

{{ placeholder_limits }}

******

### {{ h3_security }}

******

{{ p_security_intro }}:

{{ placeholder_security_limits }}

******

### {{ h3_compatibility }}

******

{{ p_compatibility }}

******

### {{ h3_project_status }}

******

{{ p_project_status }}

******

### {{ h3_build }}

******

{{ p_build_intro }}:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRc
.\gradlew.bat :app:assembleRelease
```

{{ p_build_variants }}

{{ p_build_gate }}

******

### {{ h3_release_history }}

******

{{ placeholder_latest_release_history }}

##### {{ h5_more_release_history }}

* {{ placeholder_read_more_in_changelog_md }}

******

### {{ h3_license }}

******

{{ p_license }}

******

### {{ h3_further_reading }}

******

{{ placeholder_further_reading }}

******

### {{ h3_resource_layout }}

******

```text
.readme/common.json
.readme/lang_*.json
.readme/template_readme.md
.changelog/lang_*.json
.changelog/template_changelog.md
.python/generate_markdown.py
```

{{ p_resource_layout }}:

```powershell
python .\.python\generate_markdown.py
python .\.python\generate_markdown.py --check
```

******

### {{ h3_links }}

******

- {{ text_link_autojs6 }}: {{ autojs6_url }}
- {{ text_link_ncnn }}: {{ ncnn_url }}
- {{ text_link_ultralytics_export }}: {{ ultralytics_ncnn_url }}
- {{ text_link_third_party }}: {{ third_party_notices_url }}
- {{ text_link_license }}: {{ license_url }}
