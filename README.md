# Privacy Avatar Test / 隐私替身测试

一个最小化、完全离线的 Android 测试应用，用于观察 OPPO/ColorOS“隐私替身”是否向普通应用返回空白数据。

> This is a small, offline Android utility for observing whether OPPO/ColorOS Privacy Avatar returns empty data to a regular app. The user interface is currently Chinese.

本项目与 OPPO 或 ColorOS 没有隶属、赞助或背书关系。相关商标归其权利人所有。

## 能测试什么

应用分别查询以下 Android 数据源：

| 测试项 | Android 权限 | 屏幕输出 |
| --- | --- | --- |
| 联系人 | `READ_CONTACTS` | 记录数、最多三条脱敏姓名和号码 |
| 通话记录 | `READ_CALL_LOG` | 记录数、最多三条脱敏号码和日期 |
| 短信 | `READ_SMS` | 记录数、最多三条脱敏号码、日期和正文字数 |
| 日历 | `READ_CALENDAR` | 记录数、最多三条脱敏标题和日期 |

短信及通话记录属于 Android 受严格限制的权限，部分系统可能不允许普通侧载应用授权。这不影响使用“联系人”验证核心行为。

## 隐私与安全设计

- Manifest **不声明 `INTERNET`**，应用无法直接建立网络连接。
- 只申请四项只读权限，不申请写入权限。
- 不写文件、数据库、日志或剪贴板。
- 不缓存或持久化任何查询结果。
- 屏幕仅显示记录数和最多三条脱敏样本。
- 禁止应用备份和设备迁移数据。
- Gradle Wrapper 使用官方发布的 SHA-256 校验值。

完整说明见 [隐私设计](docs/PRIVACY.md)。

## 正确测试方法

1. 安装应用，暂时关闭该应用的“隐私替身”。
2. 在应用内单独申请“联系人”权限并刷新；手机存在联系人时，结果应大于零。
3. 进入 ColorOS 设置，为“隐私替身测试”开启联系人保护。
4. 返回应用并刷新；如果保护生效，联系人查询应返回零条。
5. 测试完成后撤销权限并卸载应用。

零条结果本身不能证明隐私替身生效：设备本来没有数据、权限被额外限制或系统提供器异常也可能返回零。请务必进行开启前后的对照测试。详见 [测试指南](docs/TESTING.md)。

## 构建

环境要求：

- JDK 17
- Android SDK Platform 36
- Android SDK Build Tools 36.1.0 或兼容版本

```shell
./gradlew --no-daemon clean lintDebug assembleDebug
```

APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

这是测试工具，不包含正式发行签名。不要把调试密钥或任何签名凭据提交到仓库。

## 验证 APK 权限

可以使用 Android SDK 中的 `aapt2` 检查最终 APK。输出中不应出现 `android.permission.INTERNET`：

```shell
aapt2 dump permissions app/build/outputs/apk/debug/app-debug.apk
```

## 贡献与安全

提交更改前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。安全问题请遵循 [SECURITY.md](SECURITY.md)，不要在公开 Issue 中粘贴真实联系人、短信、通话记录或日志。

## 许可证

Copyright 2026 blank

本项目依据 [Apache License 2.0](LICENSE) 开源。
