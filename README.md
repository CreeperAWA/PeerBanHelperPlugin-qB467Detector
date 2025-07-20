# PeerBanHelper qB467Detector 插件

> [!TIP]
> 本项目完全使用 GitHub Copilot 的 Agent 编写（所有文件，包括你现在看到的 README.md）

## 简介
本插件用于 PeerBanHelper，自动检测并封禁客户端为 `qBittorrent/4.6.7` 或 PeerID 为 `-qB4670-` 的 Peer。

## 使用方法
1. 从 Releases 下载 JAR 文件
2. 将 JAR 文件放入 PeerBanHelper 的 `data/plugins` 目录。
3. 重启 PeerBanHelper，插件会自动加载。

## 开发说明
- 插件基于 PF4J 插件系统，入口类为 `com.ghostchu.peerbanhelperplugin.detector.QB467Plugin`。
- 检测逻辑见 `QB467Detector.java`。

## 依赖
- PF4J
- PF4J-Spring
- OkHttp
- PeerBanHelper 主程序（需提供依赖）

## GitHub Actions
本项目已集成 GitHub Actions 自动构建，详见 `.github/workflows/build-plugin.yml`。
