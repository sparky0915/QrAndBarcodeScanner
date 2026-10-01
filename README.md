## Overview

> ### ⚠️ 这是个人修改版（fork） / This is a personal fork
>
> fork 自 [wewewe718/QrAndBarcodeScanner][upstream]（**Unlicense，公有领域**）。
> 相对上游的主要改动：
> - **Material 3 / Material You 主题化** —— 跟随系统动态取色 + 8 套预设色板 + 自定义取色（HSV + Hex 输入）
> - **HyperOS / MIUI 适配** —— 底部栏与状态栏分层修复、扫描页沉浸式状态栏
> - 修复无法唤起第三方浏览器（targetSdk 30+ 的 `<queries>` / `resolveActivity` 门槛）
> - 修复长按图标的快捷方式指向旧包名
> - 移除上游作者的 Sentry 上报（不在 fork 里向第三方发送崩溃数据）
>
> 完整改动与踩坑记录见 [BUILD-NOTES.md](BUILD-NOTES.md)。
> **上游原版**请前往 [原仓库][upstream]；本仓库 Release 仅为个人自用构建。
>
[![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)](http://unlicense.org/)
[![](https://img.shields.io/github/v/release/wewewe718/QrAndBarcodeScanner)](https://github.com/wewewe718/QrAndBarcodeScanner/releases/latest)

QR & Barcode Scanner is an ad-free, open-source scanner app. It uses the [ZXing][zxing] scanning library.

## Download

本 fork 的构建见本仓库 [Releases](../../releases)（Android 7.0+，包名 `com.lawrencej.barcodescanner`，可与原版共存）。

**上游原版**的下载渠道（Google Play / AppGallery / F-Droid / GitHub）请见 [原仓库][upstream] —— 那些是上游作者
发布的官方构建，与本仓库无关。

## Screenshots

<img src="https://github.com/wewewe718/QrAndBarcodeScanner/blob/develop/images/screenshots/en/1_scan.png" width="180" height="320"/> <img src="https://github.com/wewewe718/QrAndBarcodeScanner/blob/develop/images/screenshots/en/2_scan_from_file.png" width="180" height="320"/> <img src="https://github.com/wewewe718/QrAndBarcodeScanner/blob/develop/images/screenshots/en/3_result.png" width="180" height="320"/> <img src="https://github.com/wewewe718/QrAndBarcodeScanner/blob/develop/images/screenshots/en/4_result_dark_theme.png" width="180" height="320"/> <img src="https://github.com/wewewe718/QrAndBarcodeScanner/blob/develop/images/screenshots/en/5_create.png" width="180" height="320"/> <img src="https://github.com/wewewe718/QrAndBarcodeScanner/blob/develop/images/screenshots/en/6_history.png" width="180" height="320"/> <img src="https://github.com/wewewe718/QrAndBarcodeScanner/blob/develop/images/screenshots/en/7_settings.png" width="180" height="320"/>

## Contributing

You can contribute by adding a translation on [Transifex][transifex], reporting a bug or asking for a feature.

## Supported Barcode Formats

### Read

The app can read the following barcode formats:
* [AZTEC][aztec]
* [CODABAR][codabar]
* [CODE-39][code_39]
* [CODE-93][code_93]
* [CODE-128][code_128]
* [DATA MATRIX][data_matrix]
* [EAN-8][ean_8]
* [EAN-13][ean_13]
* [ITF][itf]
* [PDF417][pdf417]
* [QR CODE][qr_code]
* [RSS 14][rss]
* [RSS EXPANDED][rss]
* [UPC-A][upc_a]
* [UPC-E][upc_e]
* [UPC-EAN EXTENSION][upc_ean]

### Create

The app can create the following barcode formats:
* [AZTEC][aztec]
* [CODABAR][codabar]
* [CODE 39][code_39]
* [CODE 93][code_93]
* [CODE 128][code_128]
* [DATA MATRIX][data_matrix]
* [EAN-8][ean_8]
* [EAN-13][ean_13]
* [ITF][itf]
* [PDF417][pdf417]
* [QR CODE][qr_code]
* [UPC-A][upc_a]
* [UPC-E][upc_e]

[upstream]: https://github.com/wewewe718/QrAndBarcodeScanner
[zxing]: https://github.com/zxing/zxing
[transifex]: https://www.transifex.com/a-302/qr-barcode-scanner/
[aztec]: https://en.wikipedia.org/wiki/Aztec_Code
[codabar]: https://en.wikipedia.org/wiki/Codabar
[code_39]: https://en.wikipedia.org/wiki/Code_39
[code_93]: https://en.wikipedia.org/wiki/Code_93
[code_128]: https://en.wikipedia.org/wiki/Code_128
[data_matrix]: https://en.wikipedia.org/wiki/Data_Matrix
[ean_8]: https://en.wikipedia.org/wiki/EAN-8
[ean_13]: https://en.wikipedia.org/wiki/International_Article_Number
[itf]: https://en.wikipedia.org/wiki/Interleaved_2_of_5
[maxicode]: https://en.wikipedia.org/wiki/MaxiCode
[pdf417]: https://en.wikipedia.org/wiki/PDF417
[qr_code]: https://en.wikipedia.org/wiki/QR_code
[rss]: https://en.wikipedia.org/wiki/GS1_DataBar
[upc_a]: https://en.wikipedia.org/wiki/Universal_Product_Code
[upc_e]: https://en.wikipedia.org/wiki/Universal_Product_Code#UPC-E
[upc_ean]: https://en.wikipedia.org/wiki/Universal_Product_Code#EAN-13
[rs]: https://developer.android.com/guide/topics/renderscript/compute
