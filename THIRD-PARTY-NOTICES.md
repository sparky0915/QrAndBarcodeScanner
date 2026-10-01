# Third-party notices

本仓库包含三类来源，各自的许可如下。 / This repository contains code from three sources,
licensed as follows.

## 1. 本 fork 的改动 / Modifications in this fork

Copyright (c) 2026 sparky0915 — **BSD 3-Clause License**（见 [LICENSE](LICENSE)）。

即：Material 3 / Material You 主题化、HyperOS 适配、各项缺陷修复、工程迁移等改动。

## 2. 上游原版 / Upstream

`wewewe718/QrAndBarcodeScanner` —— **The Unlicense（公有领域）**。
上游代码已献出为公有领域，**无任何条件**（不要求署名、无 copyleft），因此本 fork 可以
将自己的改动以 BSD-3-Clause 发布；下载者对上溯代码仍享有公有领域的一切自由。

```
This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <https://unlicense.org>
```

## 3. 第三方依赖 / Bundled libraries

以下库随 APK 一同分发，各自保留其原许可（**均为宽松许可，无 GPL/LGPL/AGPL**）：

| 依赖 | 许可 |
|---|---|
| androidx.* （core / appcompat / fragment / lifecycle / room / recyclerview / constraintlayout / paging …） | Apache-2.0 |
| com.google.android.material:material | Apache-2.0 |
| io.reactivex.rxjava2:rxjava / rxandroid、com.jakewharton.rxbinding2:* | Apache-2.0 |
| org.jetbrains.kotlin:kotlin-stdlib*、kotlinx-coroutines | Apache-2.0 |
| org.freemarker:freemarker（经 ez-vcard 间接引入） | Apache-2.0 |
| com.google.zxing:core、com.journeyapps:zxing-android-embedded | Apache-2.0 |
| com.fasterxml.jackson.core:jackson-core、com.google.guava:listenablefuture | Apache-2.0 |
| com.github.florent37:singledateandtimepicker、dev.turingcomplete:kotlin-onetimepassword | Apache-2.0 |
| com.budiyev.android:code-scanner、com.github.mangstadt:vinnie | MIT |
| com.isseiaoki:simplecropview、org.jsoup:jsoup | MIT |
| com.googlecode.ez-vcard:ez-vcard | BSD (FreeBSD) |
| org.reactivestreams:reactive-streams | CC0 |

> Apache-2.0 要求分发时随附许可文本与声明，各库的完整许可文本见其官方仓库。
