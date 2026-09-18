<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-yolo-ncnn-ic-launcher" border="0" width="128" />
  </p>

  <h1>AutoJs6 Plugin: YOLO NCNN</h1>

  <p>為 AutoJs6 提供進程隔離, 完全離線的 YOLO 目標檢測能力 (NCNN 20260526 後端)</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/commit/01b9093c55c7c1a78f39246c671e928df244483f"><img alt="Created" src="https://img.shields.io/date/1786442764?color=2e7d32&label=Created"/></a>
    <br>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2023.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2023.3+-EE4677"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 語言 (Languages)

******

目前 README.md 支援以下語言:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hans.md)
- 繁體中文 (香港) [zh-Hant-HK] # 目前
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ar.md)

******

### 簡介

******

AutoJs6 YOLO NCNN 插件讓 AutoJs6 腳本可以完全在本機離線運行 YOLO 目標檢測: 傳入一張圖片, 返回帶標籤, 置信度與像素坐標邊界框的檢測結果數組. 推理由 Tencent NCNN 20260526 在獨立的 `:provider` 進程中完成, 不聯網, 不上傳任何數據; 模型檔案由用戶自備, 插件 APK 不內置任何模型.

```text
application ID: io.github.supermonster003.autojs6.plugin.yolo.ncnn
plugin / engine / variant: yolo-ncnn / yolo / ncnn
provider ID: autojs6-yolo-ncnn
discovery actions: org.autojs.plugin.INFO / org.autojs.plugin.YOLO
runtime process: :provider
protocol version: 1.0
backend / task / decoder: ncnn / detect / ultralytics-detect
supported ABI: arm64-v8a, armeabi-v7a, x86, x86_64
minimum host build: 5275 (AutoJs6 6.8.0+)
```

以上是宿主發現並綁定本插件所依據的固定身份資訊. 模型經由宿主以只讀檔案描述符傳入插件進程, 始終是外部資源.

******

### 功能

******

- 離線 YOLO11 目標檢測: 輸入 AutoJs6 `images` 模組的圖像對象, 輸出標籤 + 置信度 + 邊界框, 全程本機完成.
- 進程隔離: 推理運行在獨立 `:provider` 進程, 原生層異常不影響 AutoJs6 主進程; 服務受 `org.autojs.permission.PLUGIN` 權限與簽名保護.
- NCNN 20260526 CPU 推理後端: 線程數可調 (預設 4, 上限 64), 支援 `arm64-v8a, armeabi-v7a, x86, x86_64` 裝置.
- manifest 驅動的模型兼容: `model.json` 聲明輸入輸出與標籤, 支援 1 到 256 個自訂類別, 官方 YOLO11 與自行訓練的模型同樣適用.
- 模型安全校驗: 打開會話時核驗三個模型檔案的聲明長度與 SHA-256, 運行時核驗 NCNN 圖的實際輸出形狀, 不符即拒絕而非猜測.
- 穩定錯誤類別: 組件缺失, Provider 不可用, 模型被拒, 能力不支援等場景均返回可判定的錯誤碼, 便於腳本針對性處理.
- 請求支援逾時, 使用 `detector.close()` 清理工作階段; 原生任務採用協作取消, 資源可能需等待目前呼叫結束後釋放.
- README 與 CHANGELOG 支援簡體中文/繁體中文 (香港/台灣)/英語/法語/西班牙語/日語/韓語/俄語/阿拉伯語十種語言.

******

### 快速上手

******

- **怎麼裝** — 本倉庫已公開, 但尚未發佈公開 Release, 也尚未收錄進官方插件索引 (見下方項目狀態小節); 公開下載與索引條目待生產簽名 APK 與真機終驗完成後提供, 宿主 AutoJs6 6.8.0 (版本號 5275) 的正式發佈不再是硬性前置條件. 在此之前可按下方構建小節自行構建 TEST-SIGNED 測試包, 並與使用相同調試證書的 AutoJs6 測試包配對安裝; 宿主與插件必須同證書簽名, 且宿主版本號不低於 5275.
- **怎麼啟用** — 啟用, 信任與選擇狀態始終由 AutoJs6 宿主掌握: 通過宿主插件中心安裝且由 AutoJs6 官方證書簽名的插件包, 安裝完成後會由插件中心自動寫入啟用記錄並完成啟用探測; TEST-SIGNED 測試包, adb 側載, 覆蓋安裝與其他簽名仍需在插件中心手動授權並啟用. 宿主執行時只讀取已儲存的啟用記錄, 無記錄即視為關閉 (YOLO 路由預設關閉). 腳本側還需在 `yolo.load` 的 `options.component` 中顯式指定組件串, 不存在隱式回退.
- **怎麼跑** — 準備一個包含 `model.json`, `model.ncnn.param`, `model.ncnn.bin` 三個檔案的模型目錄 (見下方模型準備小節), 用 `yolo.load(modelDir, options)` 打開檢測器, 用 `detector.detect(image, options)` 得到檢測數組, 用完調用 `detector.close()` 釋放.
- **出錯了看哪裏** — `yolo.load` 與 `detector.detect` 拋出的異常帶穩定錯誤類別: `COMPONENT_REQUIRED` (未指定組件), `PROVIDER_UNAVAILABLE` (宿主未找到或未信任 Provider), `MODEL_REJECTED` (模型或 manifest 未通過校驗, 詳情帶 `MANIFEST_*` 等前綴), `UNSUPPORTED_CAPABILITY` (請求了 CPU/detect 之外的能力), `SESSION_CLOSED`, `DETECT_FAILED` 等; 對照下方能力邊界小節與 [模型 manifest 規範](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) 排查.

******

### 使用示例

******

下面是一個可直接運行的最小示例 (另見宿主倉庫的 `sample/yolo/detect.js`):

```javascript
"use strict";

const providerComponent = "io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService";
const modelDirectory = files.path("./models/yolo11n");

let detector = null;
let image = null;

try {
    detector = yolo.load(modelDirectory, {
        component: providerComponent,
        device: "cpu",
        threads: 4,
        decoderId: "ultralytics-detect",
        timeoutMillis: 120000,
    });
    image = images.read(files.path("./bus.jpg"), true);

    const detections = detector.detect(image, {
        confidence: 0.25,
        iouThreshold: 0.45,
        maxDetections: 100,
        timeoutMillis: 30000,
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

每個檢測項包含 `classId`, `label`, `confidence` 與 `bounds` (Android `RectF`: `left` / `top` / `right` / `bottom` 欄位與 `centerX()` / `centerY()` 方法), 坐標為輸入圖像的像素坐標. 檢測器是單請求串行會話: 推理隊列長度為 0, 同一檢測器上並發的第二個 `detect` 會直接失敗而非排隊.

******

### 模型準備

******

模型目錄固定包含三個檔案, 檔案名即角色:

```text
models/yolo11n/
|-- model.json
|-- model.ncnn.param
`-- model.ncnn.bin
```

官方或自行訓練的 Ultralytics YOLO11 detect 模型可用 `yolo export format=ncnn imgsz=640` 導出, 得到 `model.ncnn.param` 與 `model.ncnn.bin` (參見 [Ultralytics NCNN 導出指南](https://docs.ultralytics.com/integrations/ncnn/)). `model.json` 為 Model Manifest v1 文件: 聲明輸入 (`in0`, RGB NCHW, 640x640 letterbox), 輸出 (`out0`, `ultralytics-detect` 解碼器, 形狀 `[1, 4 + N, 8400]`, N 為類別數) 與標籤列表; 完整示例見 [fixtures/yolo11n/model-manifest-v1.json](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/fixtures/yolo11n/model-manifest-v1.json).

運行 `python tools/generate_yolo_ncnn_manifest.py <導出目錄>` 可從 Ultralytics `metadata.yaml` 直接生成 `model.json`. 該純標準庫離線工具會先核驗固定的 YOLO11/detect/640/batch/標籤檔位與 NCNN `in0`/`out0` 圖結構; `--check` 可在不寫檔案的情況下阻斷漂移. 精確導出命令, 驗證邊界與排錯說明見 [模型轉換指南](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-conversion.md).

manifest 是兼容性契約而非重標籤工具: 打開會話時核驗三個檔案的聲明長度與 SHA-256, 運行時還會核驗 NCNN 圖的實際輸出形狀, 不符將以 `MODEL_REJECTED` 拒絕 (詳情前綴如 `MANIFEST_SHAPE_INVALID`, `MODEL_GRAPH_REJECTED`). 模型保留其來源的許可與使用條件, 轉換為 NCNN 不改變許可; 插件不代用戶獲得任何再分發權利. 詳見 [模型 manifest 規範](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) 與 [模型許可政策](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md).

******

### 腳本 API

******

`yolo.load(modelDir, options)` 打開檢測會話並返回 `YoloDetector`. `options.component` 必填 (形如包名/類名的組件串, 本插件為 `io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService`); 可選 `device` (目前僅接受 `"cpu"`), `threads` (預設 4, 上限 64), `decoderId` (預設 `ultralytics-detect`), `timeoutMillis` (模型打開總超時, 預設 120000 ms, 上限 600000 ms).

`detector.detect(image, options)` 對一張圖像同步執行檢測並返回檢測數組. `image` 為 AutoJs6 `images` 模組的圖像對象 (如 `images.read` 或截圖所得); 可選 `confidence` (置信度閾值, 預設 0.25), `iouThreshold` (NMS IoU 閾值, 預設 0.45), `maxDetections` (預設 100, 上限 400), `timeoutMillis` (預設 30000 ms).

`detector.close()` 釋放會話與原生資源, 可重複調用; 腳本結束時 AutoJs6 也會代為關閉, 但建議用 `try...finally` 顯式釋放. 會話關閉後再調用 `detect` 會返回 `SESSION_CLOSED`.

******

### 能力邊界

******

為保證行為可預期, 超出以下範圍的請求會被明確拒絕, 不做靜默回退:

- 僅 CPU 推理: Vulkan/GPU 不支援, `options.device` 僅接受 `"cpu"`.
- 支援的 ABI: `arm64-v8a, armeabi-v7a, x86, x86_64`; universal APK 包含各架構對應的原生庫.
- 僅目標檢測 (detect) 任務: 分割, 姿態, OBB, 分類與目標跟蹤均不支援.
- 僅註冊 `ultralytics-detect` 解碼器: 未知 `decoderId` 直接拒絕而非回退到其他解碼器.
- 輸入按 640x640 letterbox 預處理 (manifest v1 固定檔位), 像素格式 RGBA_8888.
- 單會話串行推理: 排隊上限為 0, 同一會話並發的第二個 `detect` 會失敗.
- 模型打開僅接受常規檔案的只讀描述符 (不接受管道或 socket), 三個檔案均須可讀.
- 啟用, 信任與選擇狀態始終由 AutoJs6 宿主掌握: 僅由官方證書簽名並經插件中心安裝的包會被自動寫入啟用記錄, 其餘安裝方式需手動啟用, 執行時無記錄即視為關閉.

******

### 安全與隔離

******

插件按 fail-closed 原則設計, 以下機制始終生效:

- 推理在獨立 `:provider` 進程執行, 與 AutoJs6 主進程隔離; 服務受 `org.autojs.permission.PLUGIN` 權限與簽名保護.
- 模型經由宿主以只讀 `ParcelFileDescriptor` 傳入; 插件不自行讀取儲存, 也不發起任何網絡請求.
- 打開會話前核驗模型聲明長度, EOF 與 SHA-256; 整個打開過程共用一個單調截止時間, 超時的會話不會被發佈.
- NCNN 運行時無法加載或初始化時直接失敗 (fail closed), 不降級運行.
- 畸形請求與活動請求相互隔離; 當可恢復請求標識時, 發佈確切的失敗終態而非懸掛.
- 回調方死亡與陳舊會話會被檢測並清理, 原生資源延遲釋放且關閉操作冪等.

******

### 兼容性

******

需要 AutoJs6 版本號不低於 5275 (即 6.8.0 及以上) 且與插件同證書簽名; Android 24+ (Android 7.0), targetSdk 37; 裝置須為 `arm64-v8a, armeabi-v7a, x86, x86_64`. 插件協議版本 1.0; 目前 Provider 版本 0.1.3 (版本號 37).

******

### 項目狀態

******

本倉庫已公開, 但本插件尚未發佈公開 Release, 也尚未收錄進官方插件索引; 兼容宿主 AutoJs6 6.8.0 (5275) 尚未正式發佈, 但宿主正式發佈不再是本插件公開發佈的硬性前置條件, 公開 Release 與官方索引條目以生產簽名 APK 與真機終驗為前置. `assembleRelease` 在缺少 `sign.properties` 時產出未簽名 APK, 僅作為源碼/構建證據, 不可視為可發佈產物. 當前版本為 0.1.3 (版本號 37); 缺陷通過前向修復的新版本號解決, 不做版本回滾. 生產簽名, 真機終驗與發佈狀態以外部 R6 證據檔案為準, 詳見 [工程記錄](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md).

******

### 構建

******

推薦 JDK 21+; Android SDK 需提供 platforms 24 與 37, 以及 NDK 29.0.14206865 與 CMake 3.22.1 (編譯 NCNN JNI 需要). 常用命令:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRc
.\gradlew.bat :app:assembleRelease
```

`assembleRc` 產出可安裝的 universal TEST-SIGNED 候選包: 繼承 release 的 R8 與資源收縮, 使用標準調試簽名, 版本名以 `-rc-test-signed` 結尾, 用於與同證書 AutoJs6 測試包配對真機驗證. `assembleRelease` 為發佈構建, 無簽名材料時保持未簽名.

維護者門禁 `tools/verify-r6-provider-source.ps1` 首先用 `--check` 核驗十語言 README/CHANGELOG 的全部 22 個生成物, 任一漂移立即失敗. 隨後預設從乾淨源碼起跑: 執行 `:app:clean` 後運行聚焦測試與兩種 APK 組裝, 記錄測試 XML 與產物哈希, 並按五檔案資產白名單核驗 APK 內容. 本地驗證與文檔生成均預設離線執行 (零聯網), 以避開開發網絡中的 Cloudflare 502/524/529 波動.

任何 Gradle 構建前, 同一門禁還會運行 `tools/generate_yolo_ncnn_manifest.py` 的 8 項純標準庫離線測試並記錄源碼哈希與回執, 使模型工具鏈回歸直接阻斷發佈預檢.

******

### 發行歷史

******

# v0.1.3

###### 2026/09/15

* `優化` 將 compileSdk 與 targetSdk 提升到 37 (Android 17), 插件行為不受新目標版本影響

# v0.1.2

###### 2026/09/13

* `修復` 版本日期保持統一的英文格式
* `修復` 增加標準 Wake 啟用入口, 根據已安裝 APK 回報原生 ABI, 並補齊所有語言的描述
* `優化` 發佈下載檔案產生前校驗 APK 版本, 簽署與完整變體集合
* `優化` 擴展原生 ABI 打包與插件中繼資料至 arm64-v8a, armeabi-v7a, x86 和 x86_64, 同步通用 APK 與各 ABI 獨立 APK

# v0.1.1

###### 2026/09/13

* `優化` 建置階段校驗 64 位原生程式庫的 16 KB 頁面大小對齊, 檢查 manifest 契約並輸出 JSON 報告

##### 更多發行歷史可參閱

* [CHANGELOG-zh-Hant-HK.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hant-HK.md)

******

### 許可

******

插件源碼以 [MPL-2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE) 發佈. APK 內完整打包 NCNN 許可與聲明 (BSD-3-Clause 及其上游第三方聲明), Kotlin 運行時 Apache-2.0 全文與第三方聲明索引, 詳見 [THIRD_PARTY_NOTICES](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md). 模型是外部資源: 保留其來源的許可與使用條件, 插件不內置模型, 也不授予任何再分發權利.

******

### 延伸閱讀

******

- [模型 manifest 規範 (Model Manifest v1)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)
- [模型 manifest JSON Schema](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/schemas/model-manifest-v1.schema.json)
- [模型與驗證資產許可政策](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md)
- [0.1.0 發行說明 (工程口徑)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/release-notes/0.1.0.md)
- [工程記錄 (原審計式 README 全文)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md)
- [項目路線圖 (含歷史里程碑與後續計劃)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/ROADMAP.md)

******

### 資源結構

******

```text
.readme/common.json
.readme/lang_*.json
.readme/template_readme.md
.changelog/lang_*.json
.changelog/template_changelog.md
.python/generate_markdown.py
```

README 與 CHANGELOG 由 `.python/generate_markdown.py` 從上述 JSON 源離線生成 (僅標準庫, 零聯網). 修改文檔請編輯 JSON 源檔案而非生成的 Markdown, 然後運行以下命令重新生成; `--check` 用於校驗生成物與源一致:

```powershell
python .\.python\generate_markdown.py
python .\.python\generate_markdown.py --check
```

******

### 相關鏈接

******

- AutoJs6 項目主頁: https://github.com/SuperMonster003/AutoJs6
- Tencent NCNN: https://github.com/Tencent/ncnn
- Ultralytics NCNN 導出指南: https://docs.ultralytics.com/integrations/ncnn/
- 第三方組件聲明: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md
- 許可證: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/16kb.md)
