******

### 发行历史

******

# v0.1.3

###### 2026/09/15

* `优化` 将 compileSdk 与 targetSdk 提升到 37 (Android 17), 插件行为不受新目标版本影响

# v0.1.2

###### 2026/09/13

* `修复` 版本日期保持统一的英文格式
* `修复` 增加标准 Wake 激活入口, 根据已安装 APK 报告原生 ABI, 并补齐所有语言的描述
* `优化` 发布下载文件生成前校验 APK 版本, 签名与完整变体集合
* `优化` 扩展原生 ABI 打包与插件元数据至 arm64-v8a, armeabi-v7a, x86 和 x86_64, 同步通用 APK 与各 ABI 独立 APK

# v0.1.1

###### 2026/09/13

* `优化` 构建阶段校验 64 位原生库的 16 KB 页大小对齐, 检查 manifest 契约并输出 JSON 报告

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
