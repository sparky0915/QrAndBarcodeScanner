# v1.11-lj7

基于 [wewewe718/QrAndBarcodeScanner](https://github.com/wewewe718/QrAndBarcodeScanner) v1.10 的个人修改版
（上游 **Unlicense**，公有领域）。包名 `com.lawrencej.barcodescanner`，可与原版共存。

## 新增 / 改进

**Material 3 / Material You 主题化**
- 升级 Material 3（`Theme.Material3.DayNight`），开关/单选/复选框换成 M3 控件
- **跟随系统动态取色**（Monet，Android 12+）
- **8 套预设主题色板**：蓝 / 青 / 绿 / 黄绿 / 琥珀 / 橙 / 红 / 粉
- **自定义取色**：HSV 三滑块 + Hex 输入框（支持 `#RRGGBB` / `RRGGBB` / 3 位缩写）
- 配色按种子色生成**整套 M3 色阶**（含 surface 系列），标题栏/状态栏/底部导航/内容同一色系分层
- 采用 M3 surface 色调层级：app bar 与底部导航用 `surfaceContainer`，内容与子标签用 `surface`

**HyperOS / MIUI 适配**
- 修复底部导航栏"分层"色块：导航栏透明 + 关闭对比度强制 + 保留上游的逐 View inset 处理
- 扫描页状态栏保持**沉浸式**（相机预览铺到状态栏下），其余页面状态栏与标题栏同色

**缺陷修复**
- 修复 targetSdk 30+ 下**无法唤起第三方浏览器**（补 `<queries>`，去掉 `resolveActivity` 硬门槛）
- 修复**长按图标快捷方式指向旧包名**（`shortcuts.xml` 里写死的 targetPackage/action）
- 修复因缺少 DSN 导致 Sentry `SentryInitProvider` 启动即崩溃（已整体移除 Sentry）

**工程迁移**
- Gradle 7.0.2 → 8.2、AGP 7.0.2 → 8.2.2、compileSdk/targetSdk 31 → 34、Java 8 → 17
- jcenter 停服后改走阿里云镜像；补 `namespace` / `buildConfig` 等 AGP 8 所需配置
- Kotlin 锁定 1.7.22（`kotlin-android-extensions` 在 1.8.0 起为硬错误，锁住可免重写 57 个文件）

## 安装

下载下方 APK 直接安装。首次安装需允许"安装未知来源应用"。
若曾安装同名包（`com.lawrencej.barcodescanner`）会直接覆盖升级。

## 说明

- 本构建**不含**任何崩溃上报/统计，不向第三方发送数据
- 未签名/调试包不适用；本 Release 使用自签名密钥
