# Roadmap — AutoJs6 Plugin: YOLO NCNN

> 当前版本: `0.1.0` (版本号 2) · 协议 1.0 · 要求宿主 ≥ 5275 (AutoJs6 6.8.0) · 私有暂存阶段, 未公开发布
>
> R1–R6 已完成 (2026-08-11 至 2026-08-13): 进程隔离 Provider, NCNN 20260526 CPU/arm64 detect,
> Model Manifest v1, R8/资源收缩接入与离线源码/构建/打包门禁; 逐条证据记录原样保留于文末
> "历史里程碑 (R1–R6)" 章节, 工程叙述全文见 [docs/engineering-notes.md](docs/engineering-notes.md).
>
> R7 本地文档线已完成 (2026-08-27): 十语言 README / CHANGELOG 生成流水线与发布前一致性门禁落地,
> 根文档面向最终用户重写; 公开仓库后的链接与渲染核对仍待发布窗口执行.
>
> R9 两项环境验证已补齐 (2026-09-10): 在既有 API 31 与 API 35 / arm64 / 4 KiB 基线之外,
> 当前干净提交构建已在三星 SM-A566B / API 36 / arm64 / 16 KiB 页设备完成 5 项 instrumentation
> 与真实 YOLO11n 定点推理, 安装字节身份和测试后清理均已核验. 本次解除对应 debug 原生链路的
> 两项环境未验证限制; 不扩展为宿主 Binder/PFD 全链路、收缩 RC 或生产发布 APK 的运行证据.
>
> R10 首个本地能力项已完成 (2026-08-27): Ultralytics YOLO11 NCNN 导出目录到 `model.json` 的
> 离线生成、固定档位校验、测试与转换指南落地; 其余能力候选仍未排期.

## 一、能力现状评估 (结论)

**协议 1.0 能力面在当前冻结范围内已全覆盖, 插件按既定边界"功能完备":**

| 维度 | 协议/合约定义 | 插件实现 | 状态 |
|---|---|---|---|
| 推理任务 | detect (YOLO11, `ultralytics-detect`) | NCNN CPU 推理 + 显式解码器注册表 | ✅ 全覆盖 |
| 模型合约 | Model Manifest v1 (声明长度 / SHA-256 / 输出形状 / 标签 1–256) | 打开时全量校验 + 运行时形状核验 | ✅ 全覆盖 |
| 隔离与安全 | 独立进程, `PLUGIN` 权限, 只读常规文件 PFD, fail-closed | `:provider` 进程 + 单调打开截止时间 + 幂等关闭 | ✅ 全覆盖 |
| 会话模型 | 单会话串行, 零队列, 打开/推理超时上限 | 已实现并有聚焦测试与错误码映射 | ✅ 全覆盖 |
| 不支持面 | Vulkan / 其他 ABI / seg / pose / OBB / 跟踪 / 未知解码器 | 显式拒绝, 无静默回退 | ✅ 按设计拒绝 |

**因此继续精进的空间不在"补功能", 而在四个方向:**

1. **文档与维护 (R7)** — 多语言文档流水线与漂移门禁已落地, 剩余公开后核对及逐版同步;
2. **公开发布工程 (R8)** — 生产签名, 转公开, GitHub Release 与官方索引, 节奏受宿主发布约束;
3. **平台与运行时验证 (R9)** — 16 KiB / API 36 debug 原生链路已验证, 后续补测升级链路并随发布构建复验;
4. **能力扩展候选 (R10)** — 模型 manifest 工具链已落地; GPU / 更多任务 / 动态尺寸 / 有界队列等
   多数需宿主协议联动, 未排期不承诺.

## 二、验证约定 (网络受限环境)

- 本地 Gradle 验证与维护者门禁默认离线执行; `tools/verify-r6-provider-source.ps1` 为非联网门禁,
  文档生成 `.python/generate_markdown.py` 为纯标准库零联网实现.
- 需要联网的条目 (依赖刷新, GitHub 操作) 显式标注 **[需联网]** 并集中在单一时间窗口执行;
  遇 Cloudflare 502/524/529 (尤其 524 源站超时) 仅整窗重试该窗口, 不阻塞其他离线条目.
- GitHub Actions 属云端网络, 不受本地网络约束, 视为离线条目.

---

## R7 — 文档易读性与多语言资源 (文档线, 2026-08-27 启动)

> 背景: 用户反馈原审计式 README 与发行说明晦涩难懂, 难以理解插件功能与用法. 参照
> Kotlin/Lua Runtime 等姊妹插件的 Python 多语言生成方案重建文档流水线; 深度工程内容
> 保留在 `docs/engineering-notes.md` 与本路线图, README / CHANGELOG 面向最终用户.
> 因 R6 门禁锁定 APK 五文件资产白名单, 本仓库采用非 Android 耦合布局: 多语言 CHANGELOG
> 输出至 `.changelog/` 而非 `app/src/main/assets`, 不改变任何 APK 输入.

### 7.1 多语言生成流水线 (已落地 2026-08-27)

- [x] 建立 `.readme/` (common.json + 模板 + 10 语言 JSON) 与 `.changelog/` (模板 + 10 语言 JSON) 资源;
  `.python/generate_markdown.py` 内置重复键拒绝, 键奇偶/类型/列表长度校验, 版本号与
  `version.properties` 对齐断言, 全角标点与翻译占位符检查, 未解析占位符断言与 `--check` 漂移检测;
  全部 22 份输出 (10 README + 10 CHANGELOG + 根目录 2 份 zh-Hans 副本) 生成通过
- [x] 以用户视角重写 README: 简介 (含固定身份信息块) / 功能 / 快速上手 (装—启用—跑—排错,
  含 `COMPONENT_REQUIRED`, `PROVIDER_UNAVAILABLE`, `MODEL_REJECTED` 等稳定错误类别指引) /
  使用示例 (对齐宿主 `sample/yolo/detect.js`) / 模型准备 (Ultralytics NCNN 导出 + Manifest v1 契约) /
  脚本 API (含全部默认值与上限) / 能力边界 / 安全与隔离 / 兼容性 / 项目状态 / 构建 / 发行历史 / 许可
- [x] 以用户可读语言新建 CHANGELOG: `v0.1.0` 按 提示/新增/修复/优化/依赖 分类覆盖 R1–R6
  用户可感知成果, 摒弃工程内审措辞; 私有暂存状态与宿主版本要求置于提示条目
- [x] 生成并核对 zh-Hans / zh-Hant-HK / zh-Hant-TW / en / fr / es / ja / ko / ru / ar 十语言文档;
  根 `README.md` 与 `CHANGELOG.md` 为 zh-Hans 逐字节副本; `--check` 通过, 且连续两次生成的
  全部产物 SHA-256 清单一致 (确定性验证)
- [x] 原审计式 README 全文迁移至 `docs/engineering-notes.md` (证据措辞原样保留, 仅调整相对链接),
  并从新 README 的"延伸阅读"小节与本路线图链接

### 7.2 文档门禁与后续

- [x] 将 `python .\.python\generate_markdown.py --check` 并入
  `tools/verify-r6-provider-source.ps1` 发布前门禁 (2026-08-27): 在签名材料与 Gradle 构建检查前
  快速校验十语言 / 22 生成物, 并在门禁报告中记录 Python 版本、生成器 SHA-256、清单规模与输出;
  可恢复负向测试临时手改 `.readme/README-en.md` 后门禁以退出码 1 报告精确漂移文件且不保留报告,
  还原后 `--check` 再次通过
- [ ] 仓库转公开后核对 README 徽章 (Release / Issues / Created / License) 与全部 GitHub 深链可达,
  并抽查十语言在 GitHub 上的渲染 (重点: 阿拉伯语 RTL 与代码块混排)
- [ ] 每次发布同步更新 `.changelog/lang_*.json` 十语言条目并重新生成; 版本头必须与
  `version.properties` 的 `VERSION_NAME` 对齐 (脚本已强制断言, 流程上禁止手改生成物)

---

## R8 — 公开发布线 (前置: 宿主 AutoJs6 6.8.0 / 5275 正式发布) **[需联网]**

> 在兼容宿主正式发布并通过独立公开评审前, 不改变仓库可见性, 不发布 Release,
> 不提交官方插件索引 (与 R6 冻结的发布边界一致).

- [ ] 使用生产证书完成 `assembleRelease` 签名构建, 与宿主正式 APK 完成双向证书 digest 比对并记录
- [ ] 公开前复查: 确认仓库不含签名材料 (`sign.properties` / `*.jks`), 不含模型与图像负载
  (`fixtures/local/` 保持忽略), 不含私有路径与内部主机信息
- [ ] 转公开仓库并创建首个公开 GitHub Release (附 APK 与 SHA-256), 使 Release 徽章生效
- [ ] 以 R6 已导出的离线索引字符串资源 (插件 ID / 引擎 / 变体 / 最低宿主版本) 提交官方插件索引条目并确认收录
- [ ] 在公开宿主上完成 安装 → 启用/信任 → 运行 `sample/yolo/detect.js` 全链路真机冒烟, 结果回填证据档案

---

## R9 — 平台与运行时验证 (前置: 获得对应硬件/系统环境)

> 以下承接 R6 显式声明的非阻塞未验证项, 逐项解除时记录独立运行证据; 不从静态打包证据
> (如 ELF 对齐) 推断运行时结论. 2026-09-10 的补充结果仅绑定其记录的源码、debug APK、
> 固定模型与设备环境; 历史 R1–R6 和 4 KiB 证据保持原有边界, 发布 APK 仍需独立终验.

- [x] 补充 arm64 / API 35 / 4 KiB 基线 (2026-08-27): 从干净提交 `69e4a62` 离线构建 debug 与
  instrumentation APK, 在 `23046RP50C` (`968e9f18`) 运行 5/5 测试; NCNN 20260526 对固定
  `bus.jpg` 推理得到 1 辆 bus 与 4 个人, 推理耗时 78 ms. 设备端 APK SHA-256 与本地一致,
  instrumentation 结束后进程退出、模型会话目录为 0, 最终两个包均已卸载; 完整边界与哈希见
  [`docs/evidence/r9-arm64-api35-4k-smoke-2026-08-27.json`](docs/evidence/r9-arm64-api35-4k-smoke-2026-08-27.json).
  此项仅刷新普通 arm64 基线; API 36 / 16 KiB 的独立验证见下
- [x] 在优先测试设备复验 arm64 / API 31 / 4 KiB 基线 (2026-08-27): 从干净提交 `e747794`
  离线构建 debug 与 instrumentation APK, 在 `XQ-AT72` (`QV710AF65F`) 运行 5/5 测试;
  NCNN 20260526 对同一固定 `bus.jpg` 推理得到 1 辆 bus 与 4 个人, 推理耗时 92 ms. 设备端
  APK SHA-256 与本地一致, instrumentation 结束后进程退出、模型会话目录为 0, 最终两个包均已
  卸载; 完整边界与哈希见
  [`docs/evidence/r9-arm64-api31-4k-qv710-smoke-2026-08-27.json`](docs/evidence/r9-arm64-api31-4k-qv710-smoke-2026-08-27.json).
  此项将历史 QV710AF65F 基线重新绑定到当时源码; API 36 / 16 KiB 的独立验证见下
- [x] 16 KiB 页大小设备真机原生加载与单次定点推理验证 (2026-09-10): 从干净提交 `dc43e15`
  经 `:app:clean` 离线构建 debug 与 instrumentation APK, 在用户授权的远程三星 `SM-A566B`
  (`a56x`, Android 16 / API 36 / `arm64-v8a`) 完成 5/5 测试. shell 与应用 UID 下
  `getconf PAGE_SIZE` 均为 `16384`; APK 内原生库的全部 3 个 LOAD 段均按 16 KiB 对齐,
  包管理器报告 `pageSizeCompat=0`, 未改动设备全局兼容模式设置. NCNN 20260526 对固定
  `bus.jpg` 推理得到 1 辆 bus 与 4 个人, 单次推理耗时 177 ms. 两个安装 APK 的 SHA-256
  与本地一致; 模型会话目录、模型文件和缓存项均为 0, 测试进程退出且两个包均已卸载.
  `NATIVE_LOAD_16K_DEVICE` 更新为 `PASS_DEBUG_NATIVE_LOAD_AND_FIXED_IMAGE_INFERENCE`;
  源码、APK / 原生库 / fixture 哈希、完整检测输出与边界见
  [`docs/evidence/r9-arm64-api36-16k-samsung-smoke-2026-09-10.json`](docs/evidence/r9-arm64-api36-16k-samsung-smoke-2026-09-10.json)
- [x] API 36 arm64 真机运行验证 (2026-09-10): 同次三星 16 KiB 设备测试覆盖模型 PFD
  校验与物化、JNI/NCNN 加载、原生引擎打开、推理、关闭及清理核验;
  `API36_ARM64_RUNTIME` 更新为 `PASS_DEBUG_NATIVE_LIFECYCLE`, 证据见上.
  此为直接原生引擎 instrumentation, 未经过宿主或 Provider Service 的跨进程 Binder 会话;
  未测试收缩 RC / 最终发布 APK, 177 ms 单次结果不作为性能基准
- [ ] 首次前向修复 (版本号 3) 发布时补测 版本号 2 → 3 升级安装运行链路, 记录 `UPGRADE_RUNTIME` 证据
  (首发版本按产品决策无回滚路径, 恢复仅限验证过的同版本重装)

---

## R10 — 能力候选方向 (未排期; 启动前不承诺, 不虚假声明, 多数需宿主协议联动)

- [ ] 可协作中止的原生模型构造: 让模型打开具备硬超时保证 (R1 明确遗留的可靠性项;
  2026-08-27 产品决策为首发非阻塞, 当前继续显式保留限制且不作硬超时声明);
  完成判据: NCNN 图构造覆盖协作中止点, 新增超时注入测试证明截止后构造可被中断且资源可回收
- [ ] Vulkan/GPU 设备档: `options.device="gpu"` 会话档位与能力协商 (需协议能力位与宿主联动);
  完成判据: 逐设备验证矩阵, GPU 不可用时按 `UNSUPPORTED_CAPABILITY` 显式拒绝而非静默回退 CPU
- [ ] 更多任务解码器: segmentation / pose / OBB 的解码器注册与 Model Manifest profile 扩展
  (输出形状族与解码语义); 每项配定点 fixture, 期望结果与未知档位拒绝路径测试
- [ ] 动态输入尺寸档: 640 之外的方形/矩形 letterbox 档位 (manifest 驱动);
  完成判据: 预处理与像素坐标反变换在新尺寸下有精确测试
- [ ] 有界推理队列: 每会话可配置排队上限 (当前固定 0), 保持超时 / 取消 / 回调死亡语义不变;
  完成判据: 队列满拒绝, 排队请求超时与会话关闭清空队列均有测试
- [x] 模型工具链 (2026-08-27): `tools/generate_yolo_ncnn_manifest.py` 以纯标准库离线解析
  Ultralytics `metadata.yaml`, fail-closed 核验 YOLO11 / detect / 640 / batch 1 / 非量化档位、
  1–256 标签边界及 NCNN `in0` / `out0` 拓扑后确定性生成 `model.json`, 支持幂等、`--check`
  与原子 `--force`; `docs/model-conversion.md` 覆盖导出、转换、验证边界与排错. 8 项测试已并入
  发布前门禁; 官方 80 类真实导出生成 `[1,84,8400]` (275 层 / 327 Blob), 单类自训练真实导出
  生成 `[1,5,8400]` (274 层 / 326 Blob), 两者 JSON 语义均与已冻结 Provider fixture 完全一致

---

## 持续任务 (不绑定里程碑, 每次触发即执行)

- [ ] 文档改动只编辑 `.readme/` 与 `.changelog/` JSON 源, 改后运行生成器并跑 `--check`; 生成物不手改
- [ ] 协议 AAR 刷新一律三 AAR 同源同刷: 从同一 AutoJs6 提交重建 `common-plugin-api` /
  `protocol-wire-api` / `yolo-api`, 同步更新 `libs/protocol-aars.lock.json`, `libs/README.md` 与
  `tools/verify-r6-provider-source.ps1` 钉住值, 并重跑两仓聚焦协议/Provider 门禁后才可变更协议范围
- [ ] 本地验证默认离线; **[需联网]** 条目集中单一时间窗口执行, 遇 Cloudflare 502/524/529 仅重试该窗口

---

## 历史里程碑 (R1–R6, 已完成)

> 以下为已完成里程碑的证据记录, 自原路线图原样保留 (英文措辞不作改写);
> 其中的证据边界声明持续有效. 下文 16 KiB / API 36 的 `NOT_RUN` 为当时的历史状态,
> 后续 debug 原生运行验证以 R9 的 2026-09-10 独立证据为准, 不改写历史产物的验证结论.

## R1-SOURCE

- [x] Create an independent sibling Git repository with one `:app` module.
- [x] Freeze application, plugin, engine, variant, provider, action, and process identities.
- [x] Add `org.autojs.plugin.INFO` and `org.autojs.plugin.YOLO` services guarded by
  `org.autojs.permission.PLUGIN`; both support actionless explicit-component binding.
- [x] Add provider/session ownership, callback-death, bounded serial control
  admission, single-in-flight with zero inference queue, deferred native cleanup,
  and idempotent close.
- [x] Add bounded model descriptor materialization with declared length, EOF,
  producer error, SHA-256, regular-file, and one whole-open monotonic deadline.
- [x] Keep malformed ingress isolated from any active request; when a valid
  request ID and sequence can be recovered, publish its exact failure terminal.
- [x] Fail closed when the pinned NCNN runtime cannot be loaded or initialized.
- [x] Freeze the AutoJs6 R0 source revision and stage three protocol AARs with hashes.
- [x] Freeze NCNN version/source/hash/license and stage its build inputs.
- [x] Freeze the YOLO11n NCNN fixture, manifest, test image, hashes, and license.

## R1-BUILD

- [x] Compile AIDL/Kotlin and package an arm64-v8a debug APK.
- [x] Compile and link the JNI/NCNN shared library.

## R1-NATIVE

- [x] Check the whole-open deadline before and after native model construction and
  reject a session that expires during construction.
- [x] On an explicitly authorized, non-protected arm64 Android target, run one
  real fixed-image inference and record the exact APK/model/environment identity.

R1 does not claim Host/provider Binder-PFD end-to-end or production readiness.
Native model construction is not yet cooperatively abortable, so no hard-timeout
runtime guarantee is claimed; that hardening remains a later reliability item.

Canonical R1 evidence is tracked in the AutoJs6 Host repository at
`docs/dev/yolo-evidence/r1-summary.json`.

## R5 CPU/arm64 RC baseline

- [x] Package the plugin MPL-2.0 text, complete Apache-2.0 text for the Kotlin
  runtime, complete pinned NCNN license/notices, and NCNN provenance lock as
  main APK assets.
- [x] Keep advertised capabilities limited to CPU, `arm64-v8a`, detect, NCNN,
  RGBA_8888, and the registered decoder set.
- [x] Map protocol and capability incompatibility to the matching stable
  open-session error codes; no unsupported-capability fallback is permitted.
- [x] Add focused source tests for the open-session error mapping.
- [x] Run the focused JVM/Android tests and build a current source-bound candidate.
- [x] Verify the candidate APK ABI, native dependencies, license assets, and ELF
  16 KiB alignment in an independently generated packaging report.
- [x] Run candidate native load/inference on an arm64 target. This passed on
  `QV710AF65F` (API 31, arm64-v8a, 4 KiB page size); a separate 16 KiB page-size
  target is still required before claiming `NATIVE_LOAD_16K_DEVICE`.

Canonical R5 evidence is tracked in the AutoJs6 Host repository at
`docs/dev/yolo-evidence/r5-summary.json`. It binds the device run to provider
revision `2aa5b100edd5e0f7691cb7edb3dd3b38c194f77d` and Host revision
`c40464957239e1378acd7be92647a4c863ac60e5`.

R5 source and packaging evidence are not production signing or publishing
evidence. Provider `0.1.0` version code `2` is the first release by explicit
product decision; no version code `1` predecessor is produced or retained, so
upgrade runtime and version rollback are `NOT_RUN_BY_PRODUCT_DECISION`, not
deferred R6 deliverables.

## R6 source and local artifact preflight

- [x] Export the fixed plugin ID, engine, variant, and minimum Host version as
  Android string resources for offline official-index generation.
- [x] Keep the two service manifest declarations and runtime capability at
  minimum Host version code `5275`.
- [x] Resolve the flat-AAR Parcelize dependency explicitly and admit both the
  unsigned release build and TEST-SIGNED RC through R8 and resource shrinking.
- [x] Add the third-party notice index, model/validation-asset license policy,
  and a release-note draft limited to the actual API/ABI/backend/model scope.
- [x] Add a non-connected Provider source/build/package preflight that rejects
  production signing material, model/image payloads, missing notices, non-
  allowlisted APK assets, and stale or absent shrinker outputs. Its full mode
  starts and ends at the same clean Git revision, begins with `:app:clean`, and
  records exact test XML and APK/mapping/resource-shrinker hashes.

R6 source preflight produces an unsigned release APK and a debug-key
TEST-SIGNED RC. Neither is a publishable production artifact. Production
certificate, signed-build, final-artifact device, index, and publication status
are external evidence facts and must be read from the exact R6 evidence archive;
they are never inferred from this source roadmap.
`-SkipBuild` is explicitly downgraded to a source/static and existing-artifact
diagnostic; it cannot establish build identity or any build/package pass.

First-release recovery does not invent a predecessor or claim rollback. Disable
the Provider and withdraw its index entry; an archived byte-identical version
code `2` APK may be reinstalled only as same-version recovery after package,
component, production signer, and SHA-256 verification. Defects are shipped as a
forward-fix version code `3`.

The current GitHub repository is private and is used only to stage source and
evidence. Before a compatible AutoJs6 Host is publicly released, do not change
repository visibility, publish a Release, or submit the Provider to the official
plugin index. A private Draft Release may be created only after its exact APK and
sanitized evidence assets have passed the refreshed R6 gates.

Runtime loading on a 16 KiB page-size target and API 36 arm64 runtime validation
remain explicit non-blocking limitations:
`NATIVE_LOAD_16K_DEVICE=NOT_RUN_NO_16K_DEVICE` and
`API36_ARM64_RUNTIME=NOT_RUN_NO_AVAILABLE_ENVIRONMENT`.
