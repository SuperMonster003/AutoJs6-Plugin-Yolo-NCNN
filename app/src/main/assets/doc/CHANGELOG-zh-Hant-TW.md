******

### 發行歷史

******

# v0.1.1

###### 2026/09/13

* `修復` 版本日期保持統一的英文格式
* `優化` 建置階段校驗 64 位原生函式庫的 16 KB 頁面大小對齊, 檢查 manifest 契約並輸出 JSON 報告
* `優化` 發行下載檔案產生前驗證 APK 版本, 簽章與完整變體集合

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
