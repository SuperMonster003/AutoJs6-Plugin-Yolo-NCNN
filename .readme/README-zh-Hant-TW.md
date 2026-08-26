<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-yolo-ncnn-ic-launcher" border="0" width="128" />
  </p>

  <h1>AutoJs6 Plugin: YOLO NCNN</h1>

  <p>為 AutoJs6 提供程序隔離, 完全離線的 YOLO 物件偵測能力 (NCNN 20260526 後端)</p>

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
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-HK.md)
- 繁體中文 (台灣) [zh-Hant-TW] # 目前
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

AutoJs6 YOLO NCNN 插件讓 AutoJs6 指令碼可以完全在本機離線執行 YOLO 物件偵測: 傳入一張圖片, 回傳帶標籤, 置信度與像素座標邊界框的偵測結果陣列. 推論由 Tencent NCNN 20260526 在獨立的 `:provider` 程序中完成, 不連網, 不上傳任何資料; 模型檔案由使用者自備, 插件 APK 不內建任何模型.

```text
application ID: io.github.supermonster003.autojs6.plugin.yolo.ncnn
plugin / engine / variant: yolo-ncnn / yolo / ncnn
provider ID: autojs6-yolo-ncnn
discovery actions: org.autojs.plugin.INFO / org.autojs.plugin.YOLO
runtime process: :provider
protocol version: 1.0
backend / task / decoder: ncnn / detect / ultralytics-detect
supported ABI: arm64-v8a
minimum host build: 5275 (AutoJs6 6.8.0+)
```

以上是宿主發現並繫結本插件所依據的固定身分資訊. 模型經由宿主以唯讀檔案描述符傳入插件程序, 始終是外部資源.

******

### 功能

******

- 離線 YOLO11 物件偵測: 輸入 AutoJs6 `images` 模組的影像物件, 輸出標籤 + 置信度 + 邊界框, 全程本機完成.
- 程序隔離: 推論執行在獨立 `:provider` 程序, 原生層異常不影響 AutoJs6 主程序; 服務受 `org.autojs.permission.PLUGIN` 權限與簽章保護.
- NCNN 20260526 CPU 推論後端: 執行緒數可調 (預設 4, 上限 64), 支援 `arm64-v8a` 裝置.
- manifest 驅動的模型相容: `model.json` 宣告輸入輸出與標籤, 支援 1 到 256 個自訂類別, 官方 YOLO11 與自行訓練的模型同樣適用.
- 模型安全驗證: 開啟工作階段時核驗三個模型檔案的宣告長度與 SHA-256, 執行期核驗 NCNN 圖的實際輸出形狀, 不符即拒絕而非猜測.
- 穩定錯誤類別: 元件缺失, Provider 不可用, 模型被拒, 能力不支援等情境均回傳可判定的錯誤碼, 便於指令碼針對性處理.
- 逾時與生命週期可控: 模型開啟與單次偵測均有逾時上限; `detector.close()` 與指令碼停止可立即釋放工作階段與原生資源.
- README 與 CHANGELOG 支援簡體中文/繁體中文 (香港/台灣)/英語/法語/西班牙語/日語/韓語/俄語/阿拉伯語十種語言.

******

### 快速上手

******

- **怎麼裝** — 本插件目前處於私有暫存階段 (見下方專案狀態小節): 相容宿主 AutoJs6 6.8.0 (版本號 5275) 正式發布後, 才會提供公開下載並提交官方插件索引. 在此之前可按下方建置小節自行建置 TEST-SIGNED 測試包, 並與使用相同偵錯憑證的 AutoJs6 測試包配對安裝; 宿主與插件必須以同一憑證簽章.
- **怎麼啟用** — 安裝插件不會自動開啟 YOLO 能力: AutoJs6 宿主保留顯式的選擇, 信任與啟用開關 (YOLO 路由預設關閉), 需在宿主中啟用並信任本 Provider. 指令碼側還需在 `yolo.load` 的 `options.component` 中顯式指定元件字串, 不存在隱式回退.
- **怎麼跑** — 準備一個包含 `model.json`, `model.ncnn.param`, `model.ncnn.bin` 三個檔案的模型目錄 (見下方模型準備小節), 用 `yolo.load(modelDir, options)` 開啟偵測器, 用 `detector.detect(image, options)` 取得偵測陣列, 用完呼叫 `detector.close()` 釋放.
- **出錯了看哪裡** — `yolo.load` 與 `detector.detect` 擲出的例外帶穩定錯誤類別: `COMPONENT_REQUIRED` (未指定元件), `PROVIDER_UNAVAILABLE` (宿主未找到或未信任 Provider), `MODEL_REJECTED` (模型或 manifest 未通過驗證, 詳情帶 `MANIFEST_*` 等前綴), `UNSUPPORTED_CAPABILITY` (請求了 CPU/arm64/detect 之外的能力), `SESSION_CLOSED`, `DETECT_FAILED` 等; 對照下方能力邊界小節與 [模型 manifest 規範](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) 排查.

******

### 使用範例

******

下面是一個可直接執行的最小範例 (另見宿主儲存庫的 `sample/yolo/detect.js`):

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

每個偵測項包含 `classId`, `label`, `confidence` 與 `bounds` (Android `RectF`: `left` / `top` / `right` / `bottom` 欄位與 `centerX()` / `centerY()` 方法), 座標為輸入影像的像素座標. 偵測器是單請求序列工作階段: 推論佇列長度為 0, 同一偵測器上並行的第二個 `detect` 會直接失敗而非排隊.

******

### 模型準備

******

模型目錄固定包含三個檔案, 檔名即角色:

```text
models/yolo11n/
|-- model.json
|-- model.ncnn.param
`-- model.ncnn.bin
```

官方或自行訓練的 Ultralytics YOLO11 detect 模型可用 `yolo export format=ncnn imgsz=640` 匯出, 得到 `model.ncnn.param` 與 `model.ncnn.bin` (參見 [Ultralytics NCNN 匯出指南](https://docs.ultralytics.com/integrations/ncnn/)). `model.json` 為 Model Manifest v1 文件: 宣告輸入 (`in0`, RGB NCHW, 640x640 letterbox), 輸出 (`out0`, `ultralytics-detect` 解碼器, 形狀 `[1, 4 + N, 8400]`, N 為類別數) 與標籤清單; 完整範例見 [fixtures/yolo11n/model-manifest-v1.json](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/fixtures/yolo11n/model-manifest-v1.json).

執行 `python tools/generate_yolo_ncnn_manifest.py <匯出目錄>` 可從 Ultralytics `metadata.yaml` 直接生成 `model.json`. 此純標準函式庫離線工具會先核驗固定的 YOLO11/detect/640/batch/標籤檔位與 NCNN `in0`/`out0` 圖結構; `--check` 可在不寫入檔案的情況下阻斷漂移. 精確匯出命令, 驗證邊界與疑難排解見 [模型轉換指南](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-conversion.md).

manifest 是相容性契約而非重貼標籤的工具: 開啟工作階段時核驗三個檔案的宣告長度與 SHA-256, 執行期還會核驗 NCNN 圖的實際輸出形狀, 不符將以 `MODEL_REJECTED` 拒絕 (詳情前綴如 `MANIFEST_SHAPE_INVALID`, `MODEL_GRAPH_REJECTED`). 模型保留其來源的授權與使用條件, 轉換為 NCNN 不改變授權; 插件不代使用者取得任何再散布權利. 詳見 [模型 manifest 規範](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) 與 [模型授權政策](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md).

******

### 指令碼 API

******

`yolo.load(modelDir, options)` 開啟偵測工作階段並回傳 `YoloDetector`. `options.component` 必填 (形如套件名/類別名的元件字串, 本插件為 `io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService`); 可選 `device` (目前僅接受 `"cpu"`), `threads` (預設 4, 上限 64), `decoderId` (預設 `ultralytics-detect`), `timeoutMillis` (模型開啟總逾時, 預設 120000 ms, 上限 600000 ms).

`detector.detect(image, options)` 對一張影像同步執行偵測並回傳偵測陣列. `image` 為 AutoJs6 `images` 模組的影像物件 (如 `images.read` 或螢幕截圖所得); 可選 `confidence` (置信度閾值, 預設 0.25), `iouThreshold` (NMS IoU 閾值, 預設 0.45), `maxDetections` (預設 100, 上限 400), `timeoutMillis` (預設 30000 ms).

`detector.close()` 釋放工作階段與原生資源, 可重複呼叫; 指令碼結束時 AutoJs6 也會代為關閉, 但建議用 `try...finally` 顯式釋放. 工作階段關閉後再呼叫 `detect` 會回傳 `SESSION_CLOSED`.

******

### 能力邊界

******

為保證行為可預期, 超出以下範圍的請求會被明確拒絕, 不做靜默回退:

- 僅 CPU 推論: Vulkan/GPU 不支援, `options.device` 僅接受 `"cpu"`.
- 僅 `arm64-v8a` ABI: 其他 ABI 裝置無法載入本插件的原生程式庫.
- 僅物件偵測 (detect) 任務: 分割, 姿態, OBB, 分類與物件追蹤均不支援.
- 僅註冊 `ultralytics-detect` 解碼器: 未知 `decoderId` 直接拒絕而非回退到其他解碼器.
- 輸入按 640x640 letterbox 前處理 (manifest v1 固定規格), 像素格式 RGBA_8888.
- 單工作階段序列推論: 排隊上限為 0, 同一工作階段並行的第二個 `detect` 會失敗.
- 模型開啟僅接受一般檔案的唯讀描述符 (不接受管線或 socket), 三個檔案均須可讀.
- 安裝本插件不會自動啟用 YOLO: 啟用, 信任與選擇狀態始終由 AutoJs6 宿主掌握.

******

### 安全與隔離

******

插件按 fail-closed 原則設計, 以下機制始終生效:

- 推論在獨立 `:provider` 程序執行, 與 AutoJs6 主程序隔離; 服務受 `org.autojs.permission.PLUGIN` 權限與簽章保護.
- 模型經由宿主以唯讀 `ParcelFileDescriptor` 傳入; 插件不自行讀取儲存空間, 也不發起任何網路請求.
- 開啟工作階段前核驗模型宣告長度, EOF 與 SHA-256; 整個開啟過程共用一個單調截止時間, 逾時的工作階段不會被發布.
- NCNN 執行期無法載入或初始化時直接失敗 (fail closed), 不降級執行.
- 畸形請求與進行中的請求相互隔離; 當可恢復請求識別時, 發布確切的失敗終態而非無回應.
- 回呼方死亡與過期工作階段會被偵測並清理, 原生資源延遲釋放且關閉操作冪等.

******

### 相容性

******

需要 AutoJs6 版本號不低於 5275 (即 6.8.0 及以上) 且與插件以同一憑證簽章; Android 24+ (Android 7.0), targetSdk 36; 裝置須為 `arm64-v8a`. 插件協定版本 1.0; 目前 Provider 版本 0.1.0 (版本號 2).

******

### 專案狀態

******

本儲存庫目前為私有證據暫存庫: 相容宿主 AutoJs6 6.8.0 (5275) 尚未正式發布, 本插件也尚未公開發布或收錄進官方插件索引; 儲存庫公開前, 上方 GitHub 徽章可能無法顯示. `assembleRelease` 在缺少 `sign.properties` 時產出未簽章 APK, 僅作為原始碼/建置證據, 不可視為可發布產物. 首個發布版本為 0.1.0 (版本號 2, 無版本號 1 前身); 缺陷透過前向修復版本號 3 解決, 不做版本回滾. 生產簽章, 真機終驗與發布狀態以外部 R6 證據封存為準, 詳見 [工程紀錄](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md).

******

### 建置

******

推薦 JDK 21+; Android SDK 需提供 platforms 24 與 36, 以及 NDK 29.0.14206865 與 CMake 3.22.1 (編譯 NCNN JNI 需要). 常用指令:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRc
.\gradlew.bat :app:assembleRelease
```

`assembleRc` 產出可安裝的 arm64-only TEST-SIGNED 候選包: 繼承 release 的 R8 與資源縮減, 使用標準偵錯簽章, 版本名以 `-rc-test-signed` 結尾, 用於與同憑證 AutoJs6 測試包配對真機驗證. `assembleRelease` 為發布建置, 無簽章材料時保持未簽章.

維護者門禁 `tools/verify-r6-provider-source.ps1` 首先用 `--check` 核驗十語言 README/CHANGELOG 的全部 22 個生成物, 任一漂移立即失敗. 隨後預設從乾淨原始碼起跑: 執行 `:app:clean` 後執行聚焦測試與兩種 APK 組裝, 記錄測試 XML 與產物雜湊, 並按五檔案資產白名單核驗 APK 內容. 本地驗證與文件生成均預設離線執行 (零連網), 以避開開發網路中的 Cloudflare 502/524/529 波動.

任何 Gradle 建置前, 同一門禁還會執行 `tools/generate_yolo_ncnn_manifest.py` 的 8 項純標準函式庫離線測試並記錄原始碼雜湊與回執, 使模型工具鏈迴歸直接阻斷發佈預檢.

******

### 發行歷史

******

# v0.1.0

###### 2026/08/13

* `提示` 首個版本 (版本號 2, 無版本號 1 前身); 需 AutoJs6 版本號不低於 5275 (6.8.0+) 且與插件以同一憑證簽章
* `提示` 目前處於私有暫存階段: 待相容宿主正式發布後再公開發布並提交官方插件索引; 能力範圍為 CPU / arm64-v8a / 物件偵測
* `新增` 新增離線 `tools/generate_yolo_ncnn_manifest.py`: 從 Ultralytics YOLO11 NCNN 中繼資料生成 `model.json`, 核驗固定中繼資料/標籤/圖檔位並輸出產物雜湊
* `新增` 程序隔離的 YOLO 物件偵測 Provider 成型: 獨立 `:provider` 程序提供 `org.autojs.plugin.YOLO` 推論服務與 `org.autojs.plugin.INFO` 發現服務, 均受 `org.autojs.permission.PLUGIN` 權限保護
* `新增` 內建 NCNN 20260526 CPU 推論後端與 `ultralytics-detect` 解碼器, 支援 YOLO11 detect 模型與 manifest 宣告的自訂類別數 (1 到 256)
* `新增` 落地 Model Manifest v1 模型契約: 開啟工作階段時核驗宣告長度與 SHA-256, 執行期核驗輸出形狀, 不符即以穩定錯誤碼拒絕
* `新增` 模型由宿主以唯讀檔案描述符傳入, 整個開啟過程共用單調截止時間; 插件 APK 不內建任何模型, 不連網
* `新增` 工作階段生命週期防護: 單工作階段序列推論 (零排隊), 回呼方死亡偵測, 冪等關閉與原生資源延遲釋放
* `修復` 啟動時清理過期模型工作階段, 避免宿主異常結束後殘留的原生資源佔用
* `優化` release 與 TEST-SIGNED RC 建置接入 R8 與資源縮減, 並完成 ELF 16 KiB 對齊打包
* `優化` APK 完整打包 MPL-2.0, Kotlin Apache-2.0 與 NCNN 授權及來源鎖定檔案, 附第三方聲明索引
* `優化` 新增離線原始碼/建置/打包門禁 `tools/verify-r6-provider-source.ps1`: 阻斷 22 個 README/CHANGELOG 生成物漂移, 乾淨原始碼起跑, 記錄測試與產物雜湊, 按五檔案資產白名單核驗 APK
* `相依` 固定 NCNN 20260526 (BSD-3-Clause, 附來源與雜湊鎖定), Kotlin 2.2.21 與 YOLO 協定 AAR 1.0 (從凍結的 AutoJs6 原始碼修訂交接)

##### 更多發行歷史可參閱

* [CHANGELOG-zh-Hant-TW.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)

******

### 授權

******

插件原始碼以 [MPL-2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE) 發布. APK 內完整打包 NCNN 授權與聲明 (BSD-3-Clause 及其上游第三方聲明), Kotlin 執行期 Apache-2.0 全文與第三方聲明索引, 詳見 [THIRD_PARTY_NOTICES](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md). 模型是外部資源: 保留其來源的授權與使用條件, 插件不內建模型, 也不授予任何再散布權利.

******

### 延伸閱讀

******

- [模型 manifest 規範 (Model Manifest v1)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)
- [模型 manifest JSON Schema](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/schemas/model-manifest-v1.schema.json)
- [模型與驗證資產授權政策](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md)
- [0.1.0 發行說明 (工程口徑)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/release-notes/0.1.0.md)
- [工程紀錄 (原審計式 README 全文)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md)
- [專案路線圖 (含歷史里程碑與後續計畫)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/ROADMAP.md)

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

README 與 CHANGELOG 由 `.python/generate_markdown.py` 從上述 JSON 來源離線生成 (僅標準程式庫, 零連網). 修改文件請編輯 JSON 來源檔案而非生成的 Markdown, 然後執行以下指令重新生成; `--check` 用於驗證生成物與來源一致:

```powershell
python .\.python\generate_markdown.py
python .\.python\generate_markdown.py --check
```

******

### 相關連結

******

- AutoJs6 專案首頁: https://github.com/SuperMonster003/AutoJs6
- Tencent NCNN: https://github.com/Tencent/ncnn
- Ultralytics NCNN 匯出指南: https://docs.ultralytics.com/integrations/ncnn/
- 第三方元件聲明: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md
- 授權條款: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE
