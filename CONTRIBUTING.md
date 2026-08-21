# Contributing

感谢你改进 Privacy Avatar Test。请先在 Issue 中说明较大的功能变更；小型修复可以直接提交 Pull Request。

## 开发环境

- JDK 17
- Android SDK Platform 36
- Android SDK Build Tools 36.1.0 或兼容版本

```shell
./gradlew --no-daemon clean lintDebug assembleDebug
```

## 安全边界

本项目最重要的约束是测试数据不能离开设备。贡献必须遵守：

- 不得添加 `INTERNET`、`ACCESS_NETWORK_STATE` 或其他联网权限。
- 不得添加遥测、分析、广告、崩溃上传或远程配置 SDK。
- 不得持久化联系人、短信、通话记录或日历内容。
- 不得把真实个人数据写入日志、测试夹具、截图或 Issue。
- 新权限必须与测试目标直接相关，并在 README 和隐私说明中记录。
- 屏幕样本必须保持脱敏，并限制数量。

## Pull Request 检查清单

- [ ] `./gradlew --no-daemon clean lintDebug assembleDebug` 通过。
- [ ] 最终 Manifest 不包含 `android.permission.INTERNET`。
- [ ] 没有提交 `local.properties`、构建目录、密钥或签名文件。
- [ ] 行为变化已更新 README 和 CHANGELOG。
- [ ] 测试数据和截图不包含真实个人信息。

提交贡献即表示你同意按照 Apache License 2.0 授权该贡献。
