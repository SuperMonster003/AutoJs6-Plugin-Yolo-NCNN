******

### 發行歷史

******

# v0.1.2

###### 2026/09/13

* `修復` 版本日期保持統一的英文格式
* `修復` 增加標準 Wake 啟用入口, 根據已安裝 APK 回報原生 ABI, 並補齊所有語言的描述
* `優化` 發佈下載檔案產生前校驗 APK 版本, 簽署與完整變體集合

# v0.1.1

###### 2026/09/13

* `優化` 建置階段校驗 64 位原生程式庫的 16 KB 頁面大小對齊, 檢查 manifest 契約並輸出 JSON 報告

# v0.1.0

###### 2026/08/13

* `提示` 首個版本 (版本號 2, 無版本號 1 前身); 需 AutoJs6 版本號不低於 5275 (6.8.0+) 且與插件同證書簽名
* `提示` 目前處於私有暫存階段: 待兼容宿主正式發佈後再公開發佈並提交官方插件索引; 能力範圍為 CPU / arm64-v8a / 目標檢測
* `新增` 新增離線 `tools/generate_yolo_ncnn_manifest.py`: 從 Ultralytics YOLO11 NCNN 元數據生成 `model.json`, 核驗固定元數據/標籤/圖檔位並輸出產物哈希
* `新增` 進程隔離的 YOLO 目標檢測 Provider 成型: 獨立 `:provider` 進程提供 `org.autojs.plugin.YOLO` 推理服務與 `org.autojs.plugin.INFO` 發現服務, 均受 `org.autojs.permission.PLUGIN` 權限保護
* `新增` 內置 NCNN 20260526 CPU 推理後端與 `ultralytics-detect` 解碼器, 支援 YOLO11 detect 模型與 manifest 聲明的自訂類別數 (1 到 256)
* `新增` 落地 Model Manifest v1 模型契約: 打開會話時核驗聲明長度與 SHA-256, 運行時核驗輸出形狀, 不符即以穩定錯誤碼拒絕
* `新增` 模型由宿主以只讀檔案描述符傳入, 整個打開過程共用單調截止時間; 插件 APK 不內置任何模型, 不聯網
* `新增` 會話生命週期防護: 單會話串行推理 (零排隊), 回調方死亡檢測, 冪等關閉與原生資源延遲釋放
* `修復` 啟動時清理陳舊模型會話, 避免宿主異常退出後殘留的原生資源佔用
* `優化` release 與 TEST-SIGNED RC 構建接入 R8 與資源收縮, 並完成 ELF 16 KiB 對齊打包
* `優化` APK 完整打包 MPL-2.0, Kotlin Apache-2.0 與 NCNN 許可及來源鎖定檔案, 附第三方聲明索引
* `優化` 新增離線源碼/構建/打包門禁 `tools/verify-r6-provider-source.ps1`: 阻斷 22 個 README/CHANGELOG 生成物漂移, 乾淨源碼起跑, 記錄測試與產物哈希, 按五檔案資產白名單核驗 APK
* `依賴` 固定 NCNN 20260526 (BSD-3-Clause, 附來源與哈希鎖定), Kotlin 2.2.21 與 YOLO 協議 AAR 1.0 (從凍結的 AutoJs6 源碼修訂交接)
