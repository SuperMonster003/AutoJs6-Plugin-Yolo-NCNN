<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-yolo-ncnn-ic-launcher" border="0" width="128" />
  </p>

  <h1>AutoJs6 Plugin: YOLO NCNN</h1>

  <p>AutoJs6 向けのプロセス分離型, 完全オフラインの YOLO 物体検出プラグイン (NCNN 20260526 バックエンド)</p>

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

### 言語 (Languages)

******

現在の README.md は以下の言語に対応しています:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-es.md)
- 日本語 [ja] # 現在
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ar.md)

******

### はじめに

******

AutoJs6 YOLO NCNN プラグインを使うと, AutoJs6 スクリプトが YOLO 物体検出を完全に端末内で実行できます: 画像を渡すと, ラベル, 信頼度, ピクセル座標のバウンディングボックスを持つ検出結果の配列が返ります. 推論は Tencent NCNN 20260526 が独立した `:provider` プロセスで行い, ネットワークアクセスもデータ送信も一切ありません. モデルファイルはユーザーが用意し, プラグイン APK にモデルは同梱されません.

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

上記はホストが本プラグインを発見してバインドするための固定 ID 情報です. モデルはホストから読み取り専用ファイル記述子で渡され, 常に外部リソースのままです.

******

### 機能

******

- オフライン YOLO11 物体検出: AutoJs6 `images` モジュールの画像オブジェクトを入力すると, ラベル + 信頼度 + バウンディングボックスを出力. 全処理が端末内で完結.
- プロセス分離: 推論は独立した `:provider` プロセスで実行され, ネイティブ層の異常が AutoJs6 メインプロセスに波及しません. サービスは `org.autojs.permission.PLUGIN` 権限と署名検査で保護.
- NCNN 20260526 CPU 推論バックエンド: スレッド数は調整可能 (既定 4, 上限 64), `arm64-v8a, armeabi-v7a, x86, x86_64` 端末向け.
- マニフェスト駆動のモデル互換性: `model.json` が入出力とラベルを宣言し, 1 から 256 個のカスタムクラスに対応. 公式 YOLO11 も自前学習モデルも同様に利用可能.
- モデル安全検証: セッションオープン時に 3 ファイルの宣言長と SHA-256 を検証し, 実行時には NCNN グラフの実際の出力形状も検証. 不一致は推測せず拒否.
- 安定したエラー分類: コンポーネント未指定, Provider 利用不可, モデル拒否, 非対応機能などはすべて判定可能なエラーコードを返し, スクリプト側で的確に処理可能.
- 要求のタイムアウトと `detector.close()` による解放に対応. ネイティブ処理は協調的にキャンセルされ, 現在の呼び出しが完了してからリソースを解放する場合があります.
- README と CHANGELOG は簡体字中国語/繁体字中国語 (香港/台湾)/英語/フランス語/スペイン語/日本語/韓国語/ロシア語/アラビア語の 10 言語に対応.

******

### クイックスタート

******

- **インストール** — 本プラグインは現在プライベートなステージング段階にあります (下記のプロジェクト状況節を参照): 互換ホスト AutoJs6 6.8.0 (ビルド 5275) の正式リリース後に, 公開ダウンロードと公式プラグインインデックスへの登録が行われます. それまでは下記のビルド節に従って TEST-SIGNED 候補を自分でビルドし, 同じデバッグ証明書を使う AutoJs6 テスト APK とペアで導入できます. ホストとプラグインは同一証明書での署名が必須です.
- **有効化** — プラグインを入れただけでは YOLO は有効になりません: AutoJs6 ホストが選択, 信頼, 有効化のスイッチを明示的に保持しており (YOLO ルートは既定で無効), ホスト側で本 Provider を有効化して信頼する必要があります. スクリプト側でも `yolo.load` の `options.component` にコンポーネント文字列を明示する必要があり, 暗黙のフォールバックはありません.
- **実行** — `model.json`, `model.ncnn.param`, `model.ncnn.bin` の 3 ファイルを含むモデルディレクトリを用意し (下記のモデル準備節を参照), `yolo.load(modelDir, options)` で検出器を開き, `detector.detect(image, options)` で検出配列を取得, 使い終わったら `detector.close()` で解放します.
- **トラブルシューティング** — `yolo.load` と `detector.detect` が投げる例外は安定したエラー分類を持ちます: `COMPONENT_REQUIRED` (コンポーネント未指定), `PROVIDER_UNAVAILABLE` (ホストが Provider を発見できない, または未信頼), `MODEL_REJECTED` (モデルかマニフェストが検証不合格, 詳細は `MANIFEST_*` などの接頭辞付き), `UNSUPPORTED_CAPABILITY` (CPU/detect 以外の機能を要求), `SESSION_CLOSED`, `DETECT_FAILED` など. 下記の機能境界節と [モデルマニフェスト仕様](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) を照合して調査してください.

******

### 使用例

******

すぐ実行できる最小例です (ホストリポジトリの `sample/yolo/detect.js` も参照):

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

各検出項目は `classId`, `label`, `confidence`, `bounds` (Android `RectF`: `left` / `top` / `right` / `bottom` フィールドと `centerX()` / `centerY()` メソッド) を持ち, 座標は入力画像のピクセル座標です. 検出器は単一リクエストの直列セッションで, 推論キュー長は 0 です. 同じ検出器への並行した 2 つ目の `detect` は待機せず即座に失敗します.

******

### モデル準備

******

モデルディレクトリは常に 3 ファイル構成で, ファイル名が役割を決めます:

```text
models/yolo11n/
|-- model.json
|-- model.ncnn.param
`-- model.ncnn.bin
```

公式または自前学習の Ultralytics YOLO11 detect モデルは `yolo export format=ncnn imgsz=640` でエクスポートでき, `model.ncnn.param` と `model.ncnn.bin` が得られます ([Ultralytics NCNN エクスポートガイド](https://docs.ultralytics.com/integrations/ncnn/) 参照). `model.json` は Model Manifest v1 文書で, 入力 (`in0`, RGB NCHW, 640x640 letterbox), 出力 (`out0`, `ultralytics-detect` デコーダ, 形状 `[1, 4 + N, 8400]`, N はクラス数), ラベル一覧を宣言します. 完全な例は [fixtures/yolo11n/model-manifest-v1.json](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/fixtures/yolo11n/model-manifest-v1.json) にあります.

`python tools/generate_yolo_ncnn_manifest.py <export-directory>` を実行すると, Ultralytics の `metadata.yaml` から `model.json` を直接生成できます. 標準ライブラリだけで動くオフラインツールが固定 YOLO11/detect/640/batch/ラベルプロファイルと NCNN の `in0`/`out0` グラフ構造を検証してから書き込みます. `--check` は書き込まずにドリフトを拒否します. 正確なエクスポート手順, 検証境界, トラブルシュートは[モデル変換ガイド](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-conversion.md)を参照してください.

マニフェストは互換性の契約であり, ラベル貼り替えの道具ではありません: セッションオープン時に 3 ファイルの宣言長と SHA-256 を検証し, 実行時には NCNN グラフの実際の出力形状も検証します. 不一致は `MODEL_REJECTED` で拒否されます (詳細接頭辞は `MANIFEST_SHAPE_INVALID`, `MODEL_GRAPH_REJECTED` など). モデルは出所のライセンスと利用条件を保持し, NCNN への変換でそれは変わりません. プラグインが再配布権を与えることもありません. 詳細は [モデルマニフェスト仕様](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) と [モデルライセンスポリシー](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md) を参照.

******

### スクリプト API

******

`yolo.load(modelDir, options)` は検出セッションを開いて `YoloDetector` を返します. `options.component` は必須です (パッケージ名/クラス名形式のコンポーネント文字列, 本プラグインでは `io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService`). 任意項目: `device` (現在は `"cpu"` のみ), `threads` (既定 4, 上限 64), `decoderId` (既定 `ultralytics-detect`), `timeoutMillis` (モデルオープン全体のタイムアウト, 既定 120000 ms, 上限 600000 ms).

`detector.detect(image, options)` は 1 枚の画像に対して同期的に検出を実行し, 検出配列を返します. `image` は AutoJs6 `images` モジュールの画像オブジェクトです (`images.read` やスクリーンショットなど). 任意項目: `confidence` (信頼度しきい値, 既定 0.25), `iouThreshold` (NMS の IoU しきい値, 既定 0.45), `maxDetections` (既定 100, 上限 400), `timeoutMillis` (既定 30000 ms).

`detector.close()` はセッションとネイティブリソースを解放し, 何度呼んでも安全です. スクリプト終了時には AutoJs6 も代わりに閉じますが, `try...finally` での明示的な解放を推奨します. クローズ後の `detect` 呼び出しは `SESSION_CLOSED` を返します.

******

### 機能境界

******

予測可能な動作を保つため, 以下の範囲外のリクエストは静かなフォールバックではなく明示的に拒否されます:

- CPU 推論のみ: Vulkan/GPU は非対応で, `options.device` は `"cpu"` のみ受け付けます.
- 対応 ABI: `arm64-v8a, armeabi-v7a, x86, x86_64`. universal APK に各 ABI のネイティブライブラリを含みます.
- 物体検出 (detect) タスクのみ: セグメンテーション, ポーズ, OBB, 分類, トラッキングはいずれも非対応.
- 登録済みデコーダは `ultralytics-detect` のみ: 未知の `decoderId` は他のデコーダに切り替えず拒否します.
- 入力は 640x640 letterbox で前処理され (manifest v1 の固定プロファイル), ピクセル形式は RGBA_8888.
- セッションごとの単一リクエスト直列推論: キュー上限は 0 で, 同一セッションへの並行した 2 つ目の `detect` は失敗します.
- モデルオープンは通常ファイルの読み取り専用記述子のみ受け付けます (パイプや socket は不可). 3 ファイルすべてが可読である必要があります.
- 本プラグインの導入だけでは YOLO は有効になりません: 有効化, 信頼, 選択の状態は常に AutoJs6 ホストが保持します.

******

### セキュリティと分離

******

プラグインは fail-closed 原則で設計されており, 以下の仕組みが常に有効です:

- 推論は独立した `:provider` プロセスで実行され, AutoJs6 メインプロセスから分離されます. サービスは `org.autojs.permission.PLUGIN` 権限と署名検査で保護.
- モデルはホストから読み取り専用 `ParcelFileDescriptor` として渡されます. プラグインは自らストレージを読まず, ネットワークリクエストも発行しません.
- セッションオープン前に宣言長, EOF, SHA-256 を検証します. オープン全体が単調な締切を 1 つ共有し, 期限切れセッションは決して公開されません.
- NCNN ランタイムを読み込みまたは初期化できない場合は, 縮退運転せずにそのまま失敗します (fail closed).
- 不正な入力はアクティブなリクエストから隔離されます. リクエスト ID を回復できる場合は, 宙づりにせず正確な失敗終端を公開します.
- コールバック側の死亡と古いセッションは検出されて清掃されます. ネイティブリソースは遅延解放され, クローズ操作は冪等です.

******

### 互換性

******

AutoJs6 のバージョンコード 5275 以上 (つまり 6.8.0 以降) で, プラグインと同一証明書で署名されている必要があります. Android 24+ (Android 7.0), targetSdk 37. 端末は `arm64-v8a, armeabi-v7a, x86, x86_64` 必須. プラグインプロトコルバージョン 1.0, 現在の Provider バージョン 0.1.3 (バージョンコード 36).

******

### プロジェクト状況

******

本リポジトリは現在プライベートなステージングアーカイブです: 互換ホスト AutoJs6 6.8.0 (5275) は未リリースで, 本プラグインも未公開かつ公式プラグインインデックス未収載です. リポジトリ公開までは上部の GitHub バッジが表示されない場合があります. `sign.properties` がない場合の `assembleRelease` は未署名 APK を生成し, これはソース/ビルドの証跡にすぎず, 公開可能な成果物ではありません. 初回リリースは 0.1.3 (バージョンコード 36, バージョンコード 1 の前身は存在しません). 欠陥はロールバックではなくバージョンコード 3 の前方修正で対処します. 本番署名, 実機での最終検証, 公開状態は外部の R6 証跡アーカイブが確定します. 詳細は [エンジニアリングノート](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md) を参照.

******

### ビルド

******

JDK 21+ 推奨. Android SDK には platforms 24 と 37, さらに NDK 29.0.14206865 と CMake 3.22.1 が必要です (NCNN JNI のコンパイルに使用). 主なコマンド:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRc
.\gradlew.bat :app:assembleRelease
```

`assembleRc` はインストール可能な universal の TEST-SIGNED 候補を生成します: release の R8 とリソース縮小を継承し, 標準デバッグ署名を使い, バージョン名は `-rc-test-signed` で終わります. 同一証明書の AutoJs6 テスト APK とペアにして実機検証する用途です. `assembleRelease` はリリースビルドで, 署名素材がなければ未署名のままです.

メンテナゲート `tools/verify-r6-provider-source.ps1` はまず `--check` で 10 言語の README/CHANGELOG 生成物 22 件をすべて検証し, ドリフトがあれば直ちに失敗します. その後は既定でクリーンなソースから開始します: `:app:clean` の後に焦点テストと 2 種類の APK 組み立てを実行し, テスト XML と成果物ハッシュを記録し, 5 ファイルのアセット許可リストで APK を検証します. ローカル検証もドキュメント生成も既定でオフライン実行 (ネットワーク呼び出しゼロ) とし, 開発ネットワークの Cloudflare 502/524/529 ノイズを避けます.

Gradle ビルド前に同じゲートが `tools/generate_yolo_ncnn_manifest.py` の標準ライブラリのみで動く 8 テストも実行し, ソースハッシュと結果を記録します. モデルツールの回帰はオフラインのリリース事前検証で遮断されます.

******

### リリース履歴

******

# v0.1.3

###### 2026/09/15

* `改善` compileSdk と targetSdk を 37 (Android 17) に引き上げ, プラグインの動作は新しいターゲットの影響を受けない

# v0.1.2

###### 2026/09/13

* `修正` バージョン日付は英語の統一形式で表示されます
* `修正` 標準 Wake 有効化エントリを追加し, インストール済み APK のネイティブ ABI と完全な翻訳済み説明を表示
* `改善` ダウンロード用ファイルの作成前に, リリース APK のバージョン, 署名, バリアントの完全性を検証
* `改善` ネイティブ ABI のパッケージ化とプラグインメタデータを arm64-v8a, armeabi-v7a, x86, x86_64 に拡張し, ユニバーサル APK と ABI 別 APK を同期

# v0.1.1

###### 2026/09/13

* `改善` 64 ビットのネイティブライブラリの 16 KB ページアラインメントをビルド時に検証, manifest 契約の検査と JSON レポートに対応

##### さらに詳しい履歴はこちら

* [CHANGELOG-ja.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/assets/doc/CHANGELOG-ja.md)

******

### ライセンス

******

プラグインのソースコードは [MPL-2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE) で公開されています. APK には NCNN の完全なライセンスと通知 (BSD-3-Clause と上流のサードパーティ通知), Kotlin ランタイムの Apache-2.0 全文, サードパーティ通知索引が同梱されます. 詳細は [THIRD_PARTY_NOTICES](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md). モデルは外部リソースであり, 出所のライセンスと利用条件を保持します. プラグインはモデルを同梱せず, 再配布権も付与しません.

******

### 参考資料

******

- [モデルマニフェスト仕様 (Model Manifest v1)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)
- [モデルマニフェスト JSON Schema](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/schemas/model-manifest-v1.schema.json)
- [モデルと検証アセットのライセンスポリシー](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md)
- [0.1.0 リリースノート (エンジニアリング表現)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/release-notes/0.1.0.md)
- [エンジニアリングノート (旧監査スタイル README の全文)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md)
- [プロジェクトロードマップ (過去のマイルストーンと今後の計画)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/ROADMAP.md)

******

### リソース構成

******

```text
.readme/common.json
.readme/lang_*.json
.readme/template_readme.md
.changelog/lang_*.json
.changelog/template_changelog.md
.python/generate_markdown.py
```

README と CHANGELOG は `.python/generate_markdown.py` が上記の JSON ソースからオフラインで生成します (標準ライブラリのみ, ネットワークゼロ). ドキュメントの変更は生成済み Markdown ではなく JSON ソースを編集し, 以下のコマンドで再生成してください. `--check` は生成物とソースの一致を検証します:

```powershell
python .\.python\generate_markdown.py
python .\.python\generate_markdown.py --check
```

******

### 関連リンク

******

- AutoJs6 プロジェクトホーム: https://github.com/SuperMonster003/AutoJs6
- Tencent NCNN: https://github.com/Tencent/ncnn
- Ultralytics NCNN エクスポートガイド: https://docs.ultralytics.com/integrations/ncnn/
- サードパーティ通知: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md
- ライセンス: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/16kb.md)
