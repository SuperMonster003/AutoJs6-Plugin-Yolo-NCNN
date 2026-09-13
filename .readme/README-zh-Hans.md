<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-yolo-ncnn-ic-launcher" border="0" width="128" />
  </p>

  <h1>AutoJs6 Plugin: YOLO NCNN</h1>

  <p>为 AutoJs6 提供进程隔离, 完全离线的 YOLO 目标检测能力 (NCNN 20260526 后端)</p>

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

### 语言 (Languages)

******

当前 README.md 支持以下语言:

- 简体中文 [zh-Hans] # 当前
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/.readme/README-ar.md)

******

### 简介

******

AutoJs6 YOLO NCNN 插件让 AutoJs6 脚本可以完全在本机离线运行 YOLO 目标检测: 传入一张图片, 返回带标签, 置信度与像素坐标边界框的检测结果数组. 推理由 Tencent NCNN 20260526 在独立的 `:provider` 进程中完成, 不联网, 不上传任何数据; 模型文件由用户自备, 插件 APK 不内置任何模型.

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

以上是宿主发现并绑定本插件所依据的固定身份信息. 模型经由宿主以只读文件描述符传入插件进程, 始终是外部资源.

******

### 功能

******

- 离线 YOLO11 目标检测: 输入 AutoJs6 `images` 模块的图像对象, 输出标签 + 置信度 + 边界框, 全程本机完成.
- 进程隔离: 推理运行在独立 `:provider` 进程, 原生层异常不影响 AutoJs6 主进程; 服务受 `org.autojs.permission.PLUGIN` 权限与签名保护.
- NCNN 20260526 CPU 推理后端: 线程数可调 (默认 4, 上限 64), 支持 `arm64-v8a` 设备.
- manifest 驱动的模型兼容: `model.json` 声明输入输出与标签, 支持 1 到 256 个自定义类别, 官方 YOLO11 与自训练模型同样适用.
- 模型安全校验: 打开会话时核验三个模型文件的声明长度与 SHA-256, 运行时核验 NCNN 图的实际输出形状, 不符即拒绝而非猜测.
- 稳定错误类别: 组件缺失, Provider 不可用, 模型被拒, 能力不支持等场景均返回可判定的错误码, 便于脚本针对性处理.
- 请求支持超时, 使用 `detector.close()` 清理会话; 原生任务采用协作取消, 资源可能需等待当前调用结束后释放.
- README 与 CHANGELOG 支持简体中文/繁体中文 (香港/台湾)/英语/法语/西班牙语/日语/韩语/俄语/阿拉伯语十种语言.

******

### 快速上手

******

- **怎么装** — 本插件当前处于私有暂存阶段 (见下方项目状态小节): 兼容宿主 AutoJs6 6.8.0 (版本号 5275) 正式发布后, 才会提供公开下载并提交官方插件索引. 在此之前可按下方构建小节自行构建 TEST-SIGNED 测试包, 并与使用相同调试证书的 AutoJs6 测试包配对安装; 宿主与插件必须同证书签名.
- **怎么启用** — 安装插件不会自动开启 YOLO 能力: AutoJs6 宿主保留显式的选择, 信任与启用开关 (YOLO 路由默认关闭), 需在宿主中启用并信任本 Provider. 脚本侧还需在 `yolo.load` 的 `options.component` 中显式指定组件串, 不存在隐式回退.
- **怎么跑** — 准备一个包含 `model.json`, `model.ncnn.param`, `model.ncnn.bin` 三个文件的模型目录 (见下方模型准备小节), 用 `yolo.load(modelDir, options)` 打开检测器, 用 `detector.detect(image, options)` 得到检测数组, 用完调用 `detector.close()` 释放.
- **出错了看哪里** — `yolo.load` 与 `detector.detect` 抛出的异常带稳定错误类别: `COMPONENT_REQUIRED` (未指定组件), `PROVIDER_UNAVAILABLE` (宿主未找到或未信任 Provider), `MODEL_REJECTED` (模型或 manifest 未通过校验, 详情带 `MANIFEST_*` 等前缀), `UNSUPPORTED_CAPABILITY` (请求了 CPU/arm64/detect 之外的能力), `SESSION_CLOSED`, `DETECT_FAILED` 等; 对照下方能力边界小节与 [模型 manifest 规范](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) 排查.

******

### 使用示例

******

下面是一个可直接运行的最小示例 (另见宿主仓库的 `sample/yolo/detect.js`):

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

每个检测项包含 `classId`, `label`, `confidence` 与 `bounds` (Android `RectF`: `left` / `top` / `right` / `bottom` 字段与 `centerX()` / `centerY()` 方法), 坐标为输入图像的像素坐标. 检测器是单请求串行会话: 推理队列长度为 0, 同一检测器上并发的第二个 `detect` 会直接失败而非排队.

******

### 模型准备

******

模型目录固定包含三个文件, 文件名即角色:

```text
models/yolo11n/
|-- model.json
|-- model.ncnn.param
`-- model.ncnn.bin
```

官方或自训练的 Ultralytics YOLO11 detect 模型可用 `yolo export format=ncnn imgsz=640` 导出, 得到 `model.ncnn.param` 与 `model.ncnn.bin` (参见 [Ultralytics NCNN 导出指南](https://docs.ultralytics.com/integrations/ncnn/)). `model.json` 为 Model Manifest v1 文档: 声明输入 (`in0`, RGB NCHW, 640x640 letterbox), 输出 (`out0`, `ultralytics-detect` 解码器, 形状 `[1, 4 + N, 8400]`, N 为类别数) 与标签列表; 完整示例见 [fixtures/yolo11n/model-manifest-v1.json](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/fixtures/yolo11n/model-manifest-v1.json).

运行 `python tools/generate_yolo_ncnn_manifest.py <导出目录>` 可从 Ultralytics `metadata.yaml` 直接生成 `model.json`. 该纯标准库离线工具会先核验固定的 YOLO11/detect/640/batch/标签档位与 NCNN `in0`/`out0` 图结构; `--check` 可在不写文件的情况下阻断漂移. 精确导出命令, 验证边界与排错说明见 [模型转换指南](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-conversion.md).

manifest 是兼容性契约而非重标签工具: 打开会话时核验三个文件的声明长度与 SHA-256, 运行时还会核验 NCNN 图的实际输出形状, 不符将以 `MODEL_REJECTED` 拒绝 (详情前缀如 `MANIFEST_SHAPE_INVALID`, `MODEL_GRAPH_REJECTED`). 模型保留其来源的许可与使用条件, 转换为 NCNN 不改变许可; 插件不代用户获得任何再分发权利. 详见 [模型 manifest 规范](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md) 与 [模型许可政策](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md).

******

### 脚本 API

******

`yolo.load(modelDir, options)` 打开检测会话并返回 `YoloDetector`. `options.component` 必填 (形如包名/类名的组件串, 本插件为 `io.github.supermonster003.autojs6.plugin.yolo.ncnn/.provider.YoloProviderService`); 可选 `device` (当前仅接受 `"cpu"`), `threads` (默认 4, 上限 64), `decoderId` (默认 `ultralytics-detect`), `timeoutMillis` (模型打开总超时, 默认 120000 ms, 上限 600000 ms).

`detector.detect(image, options)` 对一张图像同步执行检测并返回检测数组. `image` 为 AutoJs6 `images` 模块的图像对象 (如 `images.read` 或截图所得); 可选 `confidence` (置信度阈值, 默认 0.25), `iouThreshold` (NMS IoU 阈值, 默认 0.45), `maxDetections` (默认 100, 上限 400), `timeoutMillis` (默认 30000 ms).

`detector.close()` 释放会话与原生资源, 可重复调用; 脚本结束时 AutoJs6 也会代为关闭, 但建议用 `try...finally` 显式释放. 会话关闭后再调用 `detect` 会返回 `SESSION_CLOSED`.

******

### 能力边界

******

为保证行为可预期, 超出以下范围的请求会被明确拒绝, 不做静默回退:

- 仅 CPU 推理: Vulkan/GPU 不支持, `options.device` 仅接受 `"cpu"`.
- 仅 `arm64-v8a` ABI: 其他 ABI 设备无法加载本插件的原生库.
- 仅目标检测 (detect) 任务: 分割, 姿态, OBB, 分类与目标跟踪均不支持.
- 仅注册 `ultralytics-detect` 解码器: 未知 `decoderId` 直接拒绝而非回退到其他解码器.
- 输入按 640x640 letterbox 预处理 (manifest v1 固定档位), 像素格式 RGBA_8888.
- 单会话串行推理: 排队上限为 0, 同一会话并发的第二个 `detect` 会失败.
- 模型打开仅接受常规文件的只读描述符 (不接受管道或 socket), 三个文件均须可读.
- 安装本插件不会自动启用 YOLO: 启用, 信任与选择状态始终由 AutoJs6 宿主掌握.

******

### 安全与隔离

******

插件按 fail-closed 原则设计, 以下机制始终生效:

- 推理在独立 `:provider` 进程执行, 与 AutoJs6 主进程隔离; 服务受 `org.autojs.permission.PLUGIN` 权限与签名保护.
- 模型经由宿主以只读 `ParcelFileDescriptor` 传入; 插件不自行读取存储, 也不发起任何网络请求.
- 打开会话前核验模型声明长度, EOF 与 SHA-256; 整个打开过程共用一个单调截止时间, 超时的会话不会被发布.
- NCNN 运行时无法加载或初始化时直接失败 (fail closed), 不降级运行.
- 畸形请求与活动请求相互隔离; 当可恢复请求标识时, 发布确切的失败终态而非悬挂.
- 回调方死亡与陈旧会话会被检测并清理, 原生资源延迟释放且关闭操作幂等.

******

### 兼容性

******

需要 AutoJs6 版本号不低于 5275 (即 6.8.0 及以上) 且与插件同证书签名; Android 24+ (Android 7.0), targetSdk 36; 设备须为 `arm64-v8a`. 插件协议版本 1.0; 当前 Provider 版本 0.1.1 (版本号 2).

******

### 项目状态

******

本仓库当前为私有证据暂存库: 兼容宿主 AutoJs6 6.8.0 (5275) 尚未正式发布, 本插件也尚未公开发布或收录进官方插件索引; 仓库公开前, 上方 GitHub 徽章可能无法显示. `assembleRelease` 在缺少 `sign.properties` 时产出未签名 APK, 仅作为源码/构建证据, 不可视为可发布产物. 首个发布版本为 0.1.1 (版本号 2, 无版本号 1 前身); 缺陷通过前向修复版本号 3 解决, 不做版本回滚. 生产签名, 真机终验与发布状态以外部 R6 证据档案为准, 详见 [工程记录](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md).

******

### 构建

******

推荐 JDK 21+; Android SDK 需提供 platforms 24 与 36, 以及 NDK 29.0.14206865 与 CMake 3.22.1 (编译 NCNN JNI 需要). 常用命令:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRc
.\gradlew.bat :app:assembleRelease
```

`assembleRc` 产出可安装的 arm64-only TEST-SIGNED 候选包: 继承 release 的 R8 与资源收缩, 使用标准调试签名, 版本名以 `-rc-test-signed` 结尾, 用于与同证书 AutoJs6 测试包配对真机验证. `assembleRelease` 为发布构建, 无签名材料时保持未签名.

维护者门禁 `tools/verify-r6-provider-source.ps1` 首先用 `--check` 校验十语言 README/CHANGELOG 的全部 22 个生成物, 任一漂移立即失败. 随后默认从干净源码起跑: 执行 `:app:clean` 后运行聚焦测试与两种 APK 组装, 记录测试 XML 与产物哈希, 并按五文件资产白名单核验 APK 内容. 本地验证与文档生成均默认离线执行 (零联网), 以避开开发网络中的 Cloudflare 502/524/529 波动.

任何 Gradle 构建前, 同一门禁还会运行 `tools/generate_yolo_ncnn_manifest.py` 的 8 项纯标准库离线测试并记录源码哈希与回执, 使模型工具链回归直接阻断发布预检.

******

### 发行历史

******

# v0.1.1

###### 2026/09/13

* `优化` 构建阶段校验 64 位原生库的 16 KB 页大小对齐, 检查 manifest 契约并输出 JSON 报告
* `优化` 发布下载文件生成前校验 APK 版本, 签名与完整变体集合

# v0.1.0

###### 2026/08/13

* `提示` 首个版本 (版本号 2, 无版本号 1 前身); 需 AutoJs6 版本号不低于 5275 (6.8.0+) 且与插件同证书签名
* `提示` 当前处于私有暂存阶段: 待兼容宿主正式发布后再公开发布并提交官方插件索引; 能力范围为 CPU / arm64-v8a / 目标检测
* `新增` 新增离线 `tools/generate_yolo_ncnn_manifest.py`: 从 Ultralytics YOLO11 NCNN 元数据生成 `model.json`, 核验固定元数据/标签/图档位并输出产物哈希
* `新增` 进程隔离的 YOLO 目标检测 Provider 成型: 独立 `:provider` 进程提供 `org.autojs.plugin.YOLO` 推理服务与 `org.autojs.plugin.INFO` 发现服务, 均受 `org.autojs.permission.PLUGIN` 权限保护
* `新增` 内置 NCNN 20260526 CPU 推理后端与 `ultralytics-detect` 解码器, 支持 YOLO11 detect 模型与 manifest 声明的自定义类别数 (1 到 256)
* `新增` 落地 Model Manifest v1 模型契约: 打开会话时核验声明长度与 SHA-256, 运行时核验输出形状, 不符即以稳定错误码拒绝
* `新增` 模型由宿主以只读文件描述符传入, 整个打开过程共用单调截止时间; 插件 APK 不内置任何模型, 不联网
* `新增` 会话生命周期防护: 单会话串行推理 (零排队), 回调方死亡检测, 幂等关闭与原生资源延迟释放
* `修复` 启动时清理陈旧模型会话, 避免宿主异常退出后残留的原生资源占用
* `优化` release 与 TEST-SIGNED RC 构建接入 R8 与资源收缩, 并完成 ELF 16 KiB 对齐打包
* `优化` APK 完整打包 MPL-2.0, Kotlin Apache-2.0 与 NCNN 许可及来源锁定文件, 附第三方声明索引
* `优化` 新增离线源码/构建/打包门禁 `tools/verify-r6-provider-source.ps1`: 阻断 22 个 README/CHANGELOG 生成物漂移, 干净源码起跑, 记录测试与产物哈希, 按五文件资产白名单核验 APK
* `依赖` 固定 NCNN 20260526 (BSD-3-Clause, 附来源与哈希锁定), Kotlin 2.2.21 与 YOLO 协议 AAR 1.0 (从冻结的 AutoJs6 源码修订交接)

##### 更多发行历史可参阅

* [CHANGELOG-zh-Hans.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hans.md)

******

### 许可

******

插件源码以 [MPL-2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE) 发布. APK 内完整打包 NCNN 许可与声明 (BSD-3-Clause 及其上游第三方声明), Kotlin 运行时 Apache-2.0 全文与第三方声明索引, 详见 [THIRD_PARTY_NOTICES](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md). 模型是外部资源: 保留其来源的许可与使用条件, 插件不内置模型, 也不授予任何再分发权利.

******

### 延伸阅读

******

- [模型 manifest 规范 (Model Manifest v1)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-manifest-v1.md)
- [模型 manifest JSON Schema](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/schemas/model-manifest-v1.schema.json)
- [模型与验证资产许可政策](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/model-license-policy.md)
- [0.1.0 发行说明 (工程口径)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/release-notes/0.1.0.md)
- [工程记录 (原审计式 README 全文)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/engineering-notes.md)
- [项目路线图 (含历史里程碑与后续计划)](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/ROADMAP.md)

******

### 资源结构

******

```text
.readme/common.json
.readme/lang_*.json
.readme/template_readme.md
.changelog/lang_*.json
.changelog/template_changelog.md
.python/generate_markdown.py
```

README 与 CHANGELOG 由 `.python/generate_markdown.py` 从上述 JSON 源离线生成 (仅标准库, 零联网). 修改文档请编辑 JSON 源文件而非生成的 Markdown, 然后运行以下命令重新生成; `--check` 用于校验生成物与源一致:

```powershell
python .\.python\generate_markdown.py
python .\.python\generate_markdown.py --check
```

******

### 相关链接

******

- AutoJs6 项目主页: https://github.com/SuperMonster003/AutoJs6
- Tencent NCNN: https://github.com/Tencent/ncnn
- Ultralytics NCNN 导出指南: https://docs.ultralytics.com/integrations/ncnn/
- 第三方组件声明: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/THIRD_PARTY_NOTICES.md
- 许可证: https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/LICENSE


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Yolo-NCNN/blob/master/docs/16kb.md)
