# QR & Barcode Scanner

[简体中文](https://github.com/sparky0915/QrAndBarcodeScanner/blob/master/README.zh-CN.md)

An ad-free Android QR / barcode scanner — a personal fork with Material 3 / Material You theming.

[![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)](http://unlicense.org/)
[![Latest release](https://img.shields.io/github/v/release/sparky0915/QrAndBarcodeScanner)](https://github.com/sparky0915/QrAndBarcodeScanner/releases/latest)

> **This is a personal fork** of [wewewe718/QrAndBarcodeScanner][upstream], based on code released
> under the **Unlicense** (public domain). See [BUILD-NOTES.md](BUILD-NOTES.md) for the full change
> log and build pitfalls. For the original app, please use the [upstream repository][upstream] —
> the builds in this repository are personal.

## What's different from upstream

**Material 3 / Material You theming**
- System dynamic color (Monet, Android 12+)
- 8 built-in color schemes: blue / teal / green / lime / amber / orange / red / pink
- Custom color picker: HSV sliders plus a hex input field, two-way bound
- A full M3 tonal palette is generated from the seed color, so the app bar, status bar,
  bottom navigation and content all share one color family with natural tonal steps

**HyperOS / MIUI fixes**
- Fixed the banded color block between the bottom navigation bar and the content
  (transparent navigation bar + disabled contrast enforcement)
- The Scan tab stays immersive — the camera preview extends behind the status bar

**Bug fixes**
- Fixed launching third-party browsers on targetSdk 30+ (added `<queries>`, removed the
  hard `resolveActivity` gate)
- Fixed launcher shortcuts pointing at the old package name
- Removed the upstream author's Sentry reporting: **this build sends no data anywhere**

**Toolchain upgrade**: Gradle 8.2 / AGP 8.2.2 / compileSdk 34 / JDK 17
(Kotlin is pinned to 1.7.22 — see BUILD-NOTES for why)

## Screenshots

<img src="docs/screenshots/01_scan.png" width="180"/> <img src="docs/screenshots/02_create.png" width="180"/> <img src="docs/screenshots/03_history.png" width="180"/> <img src="docs/screenshots/04_settings.png" width="180"/> <img src="docs/screenshots/05_theme_color.png" width="180"/> <img src="docs/screenshots/06_color_picker.png" width="180"/>

> The camera preview in the screenshots is the Android emulator's synthetic test scene.

## Download

See the **[Releases](../../releases)** page of this repository.

- Android 7.0+ (minSdk 21)
- Package name `com.lawrencej.barcodescanner` — different from the upstream app, so **both can be
  installed side by side**

## Build

```bash
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=/path/to/android-sdk

./gradlew assembleDebug      # debug build
./gradlew assembleRelease    # release build (configure signing yourself, see BUILD-NOTES.md)
```

> ⚠️ Kotlin must stay at **1.7.22**: `kotlin-android-extensions` is a hard error from Kotlin 1.8.0,
> and 57 files in this project still use `kotlinx.android.synthetic`.
> See [BUILD-NOTES.md](BUILD-NOTES.md) for details.

## Supported barcode formats

| Read | Create |
|---|---|
| [AZTEC][aztec] | [AZTEC][aztec] |
| [CODABAR][codabar] | [CODABAR][codabar] |
| [CODE-39][code_39] | [CODE-39][code_39] |
| [CODE-93][code_93] | [CODE-93][code_93] |
| [CODE-128][code_128] | [CODE-128][code_128] |
| [DATA MATRIX][data_matrix] | [DATA MATRIX][data_matrix] |
| [EAN-8][ean_8] | [EAN-8][ean_8] |
| [EAN-13][ean_13] | [EAN-13][ean_13] |
| [ITF][itf] | [ITF][itf] |
| [PDF417][pdf417] | [PDF417][pdf417] |
| [QR CODE][qr_code] | [QR CODE][qr_code] |
| [RSS 14][rss] | |
| [RSS EXPANDED][rss] | |
| [UPC-A][upc_a] | [UPC-A][upc_a] |
| [UPC-E][upc_e] | [UPC-E][upc_e] |
| [UPC-EAN EXTENSION][upc_ean] | |

## License & credits

- Based on [wewewe718/QrAndBarcodeScanner][upstream], released under the **Unlicense** (public
  domain): free to copy, modify, publish, distribute, commercially or otherwise
  (see [LICENSE](LICENSE))
- Scanning is powered by [ZXing][zxing]
- Upstream translation work happens on [Transifex][transifex] (this fork does not take part)

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
[pdf417]: https://en.wikipedia.org/wiki/PDF417
[qr_code]: https://en.wikipedia.org/wiki/QR_code
[rss]: https://en.wikipedia.org/wiki/GS1_DataBar
[upc_a]: https://en.wikipedia.org/wiki/Universal_Product_Code
[upc_e]: https://en.wikipedia.org/wiki/Universal_Product_Code#UPC-E
[upc_ean]: https://en.wikipedia.org/wiki/Universal_Product_Code#EAN-13
