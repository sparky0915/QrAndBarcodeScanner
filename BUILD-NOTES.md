# 构建说明（Lawrence 自用 fork）

> 上游：https://github.com/wewewe718/QrAndBarcodeScanner （2022-04 停更，Unlicense）
> 本副本：`~/Projects/QrAndBarcodeScanner`，包名 `com.lawrencej.barcodescanner`
> 版本：versionCode 14 / versionName **1.11-lj2**（lj1 因闪退作废）
> 记录时间：2026-10-01

## 构建环境

| 项目 | 值 | 说明 |
|---|---|---|
| JDK | Homebrew `openjdk@17` → `/opt/homebrew/opt/openjdk@17` | 不能用 JDK 21（AGP 8.2 不支持） |
| Android SDK | `~/Library/Android/sdk` | platform 34/36、build-tools 36.0.0、cmdline-tools |
| Gradle | 8.2（wrapper 自动下载） | 仓库把 gradlew/.jar 都 .gitignore 了，本机是从 /tmp/impad 复制过来的 |
| AGP | 8.2.2 | |
| Kotlin | **1.7.22（不能升！见坑 1）** | |

```bash
cd ~/Projects/QrAndBarcodeScanner
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew assembleDebug      # 产物 app/build/outputs/apk/debug/app-debug.apk
```

## 🧪 本机模拟器（自测用，别再让用户替我试）

已建好 AVD `qrtest`（Android 14 / arm64-v8a，镜像 `system-images;android-34;default;arm64-v8a`）。

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
emulator -avd qrtest -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect &   # 后台起
adb wait-for-device
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb logcat -c && adb shell am start -n com.lawrencej.barcodescanner/com.example.barcodescanner.feature.tabs.BottomTabsActivity
sleep 6 && adb logcat -d | grep -A40 "FATAL EXCEPTION"     # 抓崩溃堆栈
adb exec-out screencap -p > /tmp/shot.png                  # 截图看界面
adb shell dumpsys shortcut | grep -i lawrencej             # 查静态快捷方式
```

## ⚠️ 四个必须知道的坑

1. **Kotlin 必须锁 1.7.22**
   `kotlin-android-extensions` 在 **Kotlin 1.8.0 起就是硬错误**（实测 1.8.10 / 1.8.22 都报
   `The Android extensions compiler plugin is no longer supported`，不是 1.9.0 才移除）。
   本项目 **57 个文件**用 `kotlinx.android.synthetic`，锁 1.7.22 就不必重写它们。
   连带后果：androidx 依赖也要待在同年版本（core-ktx 1.9.0 / appcompat 1.6.1）。
   **插件必须用全限定 id 才能带版本号**：`id 'org.jetbrains.kotlin.android.extensions' version '1.7.22'`。
   用短名 `kotlin-android-extensions` + version 会报 "plugin was not found"。

2. **jcenter 已停服**
   `code-scanner` / `zxing-android-embedded:3.4.0` / `simplecropview` / `singledateandtimepicker`
   只在 jcenter 发布过，Google Maven / Maven Central / JitPack 都没有。
   **解法：阿里云镜像** `https://maven.aliyun.com/repository/public`（已在 build.gradle，国内还更快）。

3. **namespace ≠ applicationId**
   改包名只改 `defaultConfig.applicationId`；`namespace` 必须保持 `com.example.barcodescanner`，
   因为 130 个源文件都 `import com.example.barcodescanner.R`（R 类跟 namespace 走）。
   **连带坑：`res/xml/shortcuts.xml` 里 targetPackage / action 是写死的字符串，改包名必须同步改**（坑 4）。

4. **Sentry 会在进程启动时抛异常（lj1 闪退元凶）**
   上游 manifest 里带自己的 Sentry DSN；删掉 DSN 后，Sentry 的 `SentryInitProvider`
   （ContentProvider，比 Application 还早）仍会自动初始化并抛
   `IllegalArgumentException: DSN is required. Use empty string to disable SDK.` → 一点开就闪退。
   **本 fork 已整体移除 `io.sentry:sentry-android`**（不该把崩溃数据发给上游作者），
   `usecase/Logger.kt` 改为写本机 logcat。

## 本副本相对上游的改动

**工程迁移（阶段 0）**
- Gradle 7.0.2→8.2、AGP 7.0.2→8.2.2、compileSdk/targetSdk 31→34、Java 8→17
- 去 jcenter + 阿里云镜像；去掉 `io.sentry.android.gradle` 插件（与 AGP 8 不兼容）与 Sentry SDK
- 去 productFlavors（amazon/aptoide/googlePlay/fDroid）→ 单一包
- `buildFeatures { buildConfig true }`；manifest 去掉 `package=`；gradle.properties 加 JDK17 下 kapt 的 `--add-exports`
- `BarcodeImageScanner.kt`：compileSdk 34 的 `Bitmap.getPixels` 参数带 @NonNull，改用非空局部变量

**② HyperOS/MIUI 底部栏分层（阶段 1）**
- ❌ 一开始我改成"根布局统一 insets"，**这是错的** —— 上游本来就有逐 View 的 inset 处理
  （`extension/WindowsInsets.kt` 的 `applySystemWindowInsets()`，用在各 Activity 根部、
  app_bar_layout、bottom_navigation_view），叠加会挤坏布局。已回退。
- ✅ 真正原因在"颜色"：① 主题 `android:navigationBarColor` 被设成不透明色 → 改成透明；
  ② MIUI/HyperOS 默认开启"导航栏对比度强制"，给手势条垫半透明底衬 →
  `BaseActivity` 里 `window.isNavigationBarContrastEnforced = false`（API 29+）。
  上游的 `LAYOUT_STABLE | LAYOUT_HIDE_NAVIGATION` 布局逻辑保持不变。

**③ 无法唤起第三方浏览器（阶段 2）**
- 根因：`BarcodeActivity.startActivityIfExists()` 用 `intent.resolveActivity(packageManager)` 预判，
  targetSdk 30+ 未声明 `<queries>` 时该调用恒返回 null → 直接弹"没有可用应用"（全项目无 WebView）
- 改动：manifest 加 `<queries>`（http/https BROWSABLE + LAUNCHER）；
  `startActivityIfExists` 改为直接 `startActivity` + 捕获 `ActivityNotFoundException`

**④ 长按图标快捷方式指向老 App（2026-10-01 用户反馈）**
- 根因：`res/xml/shortcuts.xml` 写死 `android:targetPackage="com.example.barcodescanner"` +
  `android:action="com.example.barcodescanner.SCAN_FROM_CAMERA"` 等 → 改用新 applicationId 后，
  长按弹出的快捷方式去指向老包（本机装着原版就跳到原版）
- 改动：targetPackage 与 4 条 action 前缀全改为 `com.lawrencej.barcodescanner`
  （action 必须与代码里 `"${BuildConfig.APPLICATION_ID}.CREATE_BARCODE"` / `.HISTORY` 一致才能切页签）
- 验证：`adb shell dumpsys shortcut | grep lawrencej` → 4 条全部指向新包 ✓

## 阶段 3（已完成）：Material 3 + Material You 主题色

配套：material 1.5.0 → **1.12.0**（实测 Kotlin 1.7.22 能编译，元数据没冲突）。

**M3 主题**：`BaseAppTheme` 父类 `Theme.AppCompat.Light.NoActionBar`
→ `Theme.Material3.DayNight.NoActionBar`（app 自带 values-night，深色模式照旧可用）。
注意：**移除了主题里的 `colorPrimary` 覆盖**（原为白色），因为 M3 用 colorPrimary 做强调色；
app 自己的 `@color/color_primary`（资源名）保持白色不受影响。

**主题色**（`Settings.THEME_COLOR`，默认 `blue` = 原来的蓝）：
- `dynamic` = 跟随系统 Monet 取色 → `DynamicColors.applyToActivityIfAvailable()`
- 8 个预设色板：blue/teal/green/lime/amber/orange/red/pink
- 每个色板 = 一个 theme overlay（`ThemeOverlay.Lj.*`，在 `values/styles.xml`），
  提供 colorPrimary/onPrimary/container/onContainer/secondary/secondaryContainer/tertiary；
  **色值由脚本按 HSL 色调生成**（非官方 HCT，观感接近即可）；深色另有一套 `values-night/colors.xml`
- 注入点：`BaseActivity.applyThemeColor()`（必须在 `super.onCreate()` 之前）
- **改色后必须重建界面**：theme overlay 只在 Activity 创建时生效，所以
  `ChooseThemeActivity.selectThemeColor()` 是重启任务（先起 BottomTabs 清栈、再叠主题页）

**强调色接线（4 处，不用碰 168 个布局）**：
- `res/color/color_bottom_navigation_item.xml` → `?attr/colorPrimary`
- `fragment_barcode_history.xml` 的 tabSelectedTextColor / tabIndicatorColor → `?attr/colorPrimary`
- `styles.xml` 里 3 处写死的 `@color/blue`（分组标题 / colorControlActivated / backgroundTint）→ `?attr/colorPrimary`
- M3 底部导航的**药丸指示器**用 `colorSecondaryContainer` → 已一并写进各色板（否则会是 M3 默认紫）

**控件现代化**：
- `SwitchCompat` → `MaterialSwitch`（M3 开关）；2 个 `<CheckBox>` → `MaterialCheckBox`
- 其余开关/单选随 M3 主题自动变样式

**选色界面**：`ChooseThemeActivity` 追加「主题色」区块 —— 「跟随系统（Material You）」+ 8 个色点
（排列方式见阶段 4：已从"两行"改为"单排等分居中"），色点用 `contentDescription` 标注便于自动化测试。

## 阶段 4（2026-10-01 第二轮，用户反馈后）：配色一致性 + 自定义取色

用户反馈三点，全部修复：

**① 标题栏不跟主题色（Material You 下仍是纯白）**
- 根因：`ToolbarStyle` 写死 `@color/toolbar_background_color`（白），而内容区用的是 M3 surface
- 修复：→ **`?attr/colorSurfaceContainer`**（M3 的 top app bar 容器色角色，material 1.12 提供）

**② 预设色只覆盖了 colorPrimary 等角色、没覆盖 surface 系列**
- 症状：选预设蓝时，强调色是蓝的，但浅色背景仍是 M3 默认的淡紫 → 观感割裂
- 修复：**三条路径统一改用 Material 的 DynamicColors**
  - 跟随系统 → `applyToActivityIfAvailable(activity)`（Monet 取壁纸色）
  - 预设色 / 自定义色 → `DynamicColorsOptions.Builder().setContentBasedSource(seed)`
    （**content-based dynamic color**：按种子色在本地用 HCT 算出整套 M3 色阶，含 surface 系列）
  - 因此 `res/values/styles.xml` 里原先手写的 `ThemeOverlay.Lj.*` 覆盖层**已删除**（单一通路，避免两条路打架）
  - 各预设的"种子色"定义在 `Settings.themeColorSeedRes()`；色点显示的是种子色，
    落到控件上的 primary 是同色系的色调调整值 —— 这是 M3 的正常表现

**③ 8 个色点靠左/两行 → 单排居中**
- 做法：单行 `LinearLayout` + 每个色点 `layout_weight = 1f` 等分 → 永远铺满且居中，窄屏也不裁切

**④ 新增"自定义颜色"（用户要求）**
- ⚠️ **Android / Material 没有公开的系统取色控件**（`ColorPickerDialog` 是内部隐藏类），
  所以用原生 `com.google.android.material.slider.Slider` 拼了个 HSV 取色器：
  `dialog_color_picker.xml` + `ChooseThemeActivity.showColorPickerDialog()`
  （预览块 + `#RRGGBB` + 色相/饱和度/明度三滑块 + 取消/确定）
- 存 `THEME_COLOR_CUSTOM`（ARGB int）+ `THEME_COLOR = "custom"`，同样走 content-based DynamicColors

### 🐛 本轮踩的坑

**M3 Slider 设了 `stepSize` 后取值必须是整数倍**，否则抛
`IllegalStateException: Value(198.35295) must be equal to valueFrom(0.0) plus a multiple of stepSize(1.0)`
—— 从 `Color.colorToHSV()` 取出的色相带小数，直接赋给 `stepSize=1` 的 Slider 会**当场崩溃**。
取色器这类连续滑动场景**不要设 stepSize**。

## 阶段 5（2026-10-01 第三轮，定稿）：状态栏沉浸 + Hex 输入框

**① 状态栏与标题栏同色（更沉浸）**
- 做法：`BaseActivity.applyStatusBarColor()` 里把 `window.statusBarColor` 设为
  `?attr/colorSurfaceContainer`（与 `ToolbarStyle` 的背景同一个色角色）→ 两块连成一体
- ⚠️ **取色必须从"已 inflate 的视图"取，不能从 `window.decorView` 取**：
  AppCompat 给视图挂的是带 DynamicColors 覆盖层的 ContextThemeWrapper，而 `window.decorView`
  持有另一个 theme 实例、**不含覆盖层** —— 用它取 `colorSurfaceContainer` 会拿到 M3 **基线色**。
  实测对比：`#F3EDF7`（基线淡紫，错） vs `#E7F1E3`（按绿色种子推导，对）。
  因此改为在 `onPostCreate()`（视图已 inflate）里 `findViewById(android.R.id.content)` 取色 ✓

**② 取色器加 Hex 输入框**
- `dialog_color_picker.xml` 里原来的只读色值文本 → `TextInputLayout`(OutlinedBox) + `TextInputEditText`
- 与滑块**双向联动**：拖滑块更新色号；直接填 `#RRGGBB` / `RRGGBB`（也接受 3 位缩写）会反推滑块与预览
- 用 `syncing` 标志防回环；**从输入框取色时不回写输入框** —— HSV 往返取整会把用户填的色号改掉
  （如填 `#00C853` 经 HSVToColor 往返可能变成 `#00C852`）

### 本轮踩的坑（新增）

**从 view 取主题属性 ≠ 从 window.decorView 取**：见 ① —— 这是"改了主题色但状态栏仍是旧色/基线色"的根因。

## 阶段 6（2026-10-01 第四轮）：用 M3 surface 层级消除白色断层

用户反馈：① 历史页「全部/喜爱」子标签是白底 → 与标题栏之间出现颜色断层；② 底部 4 个标签是白底；
诉求是"同一色系但有色差、自然过渡、统一"。

**M3 的官方答案就是 surface 色调层级（tonal elevation）** —— 不是靠阴影，而是靠同一色相下不同色调的
surface 角色来分层。按规范对号入座：

| 组件 | M3 色角色 |
|---|---|
| 顶部 app bar / 状态栏 | `colorSurfaceContainer` |
| **底部导航栏**（官方规定用它）| `colorSurfaceContainer` |
| **Tabs（子标签）** | `colorSurface` |
| 内容主体 | `colorSurface` |

改动只有 **3 处**（资源已集中，无需碰 168 个布局）：
- `fragment_barcode_history.xml`  `@color/tab_layout_background_color` → `?attr/colorSurface`
- `activity_bottom_tabs.xml`      `@color/bottom_navigation_background_color` → `?attr/colorSurfaceContainer`
- `values/styles.xml`             `android:colorBackground` `@color/color_background` → `?attr/colorSurface`

实测像素（绿色种子）：
```
状态栏 / 标题栏  #E7F1E3   ← surfaceContainer
子标签条 / 内容区 #F3FCEE   ← surface
底部 4 个标签    #E7F1E3   ← surfaceContainer
```
即"上下同档呼应、中间基准"，同一色系两个色调档，白色断层消失 ✓

## 阶段 7（2026-10-01 第五轮）：修扫描页沉浸式状态栏

**用户反馈**：阶段 5 的状态栏改动把**扫描页**也涂成了实色 —— 而扫描页原本是沉浸式的
（相机预览一直铺到状态栏下），现在被"单独拿出来"变成一条色带，属于我引入的 bug。

**修法**（关注点分离：相机页自己管状态栏，与它已有的 `LIGHT_STATUS_BAR` 逻辑放在一起）：
- `BaseActivity`：`applyStatusBarColor()` 改为公开 + 新增 `applyTransparentStatusBar()`
- `ScanBarcodeFromCameraFragment`：**onResume → 透明**、**onDestroyView → 还原主题色**
  （`BottomTabsActivity.showFragment()` 用的是 `replace()`，切页签会走 onDestroyView ✓）

### ⚠️ 时序坑（关键）

**透明状态栏不能写在 `onViewCreated`** —— `BaseActivity` 是在 `onPostCreate` 里设主题色的，
而 **`onPostCreate` 晚于 `onViewCreated`**，写在 onViewCreated 会被覆盖回主题色。
必须放在 **`onResume`**（一定晚于 onPostCreate）。

实测（1080×1920，状态栏横带的通道标准差）：
```
① 启动即扫描页  #BFBFBF  标准差 14.0  → 相机内容铺到状态栏下 ✓ 沉浸
② 切到设置页    #E7F1E3  标准差 ~0    → 纯主题色 ✓ 与标题栏同色
③ 切回扫描页    #BFBFBF  标准差 14.0  → 又变回沉浸 ✓
```

## 尚未做（可选）

- 深色模式下未逐一核对 168 个布局的对比度






## 阶段 8（2026-10-01）：Release 构建（R8）修复

发布 release 时才暴露的问题（debug 不跑 R8，所以一直没发现）：

**AGP 8 起，R8 的"缺失类"（missing classes）从警告升级为硬错误**，报
`Execution failed for task ':app:minifyReleaseWithR8'`。缺失的类全部来自
`org.freemarker:freemarker`（经由 `ez-vcard` → `vinnie` 间接引入），它引用了
`java.beans.*` / `java.rmi.*` / `javax.swing.*` / `org.jaxen.*` / `org.python.core.*` /
`com.sun.org.apache.xml.internal.*` 等 Android 上不存在的 JDK 与可选库类。

**修法**：把 AGP 自动生成的 `app/build/outputs/mapping/release/missing_rules.txt`
（37 条 `-dontwarn`）追加进 `app/proguard-rules.pro`。

**验证**：release 产物从 11M 降到 **5.0M**，装到 Android 14 模拟器切换 4 个页签 + 打开主题页，
`FATAL EXCEPTION` 计数 0 ✓

## 发布流程（GitHub）

```bash
# 1) 生成密钥（密码自己保管，别进仓库）
keytool -genkeypair -v -keystore ~/keystores/barcodescanner.jks \
  -alias barcodescanner -keyalg RSA -keysize 4096 -validity 10000
# 2) 写 ~/.gradle/gradle.properties 的 BCS_* 四项
# 3) 构建
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew assembleRelease        # → app/build/outputs/apk/release/app-release.apk
# 4) 校验签名
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs app/build/outputs/apk/release/*.apk
```
