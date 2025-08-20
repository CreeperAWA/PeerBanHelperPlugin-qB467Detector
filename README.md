# PeerBanHelper qB467PeerDetectorPlugin

> [!TIP]
> 本项目代码使用 GitHub Copilot 的 Agent 编写

## 简介
本插件用于 PeerBanHelper，自动检测并封禁客户端为 `qBittorrent/4.6.7` 或 PeerID 为 `-qB4670-` 的 Peer。

## 原理
经过观察，这些恶意的 Peer 均有一个使用 `Python/3.10 aiohttp/3.11.12` 编写的 HTTP 服务器监听在 8089 端口（疑似用于集控），如果直接访问响应体为`File not found` 或状态码为 404，该插件利用了这一特征，实现了使用 qBittorrent/4.6.7 吸血的 Peer 的精准检测

## 使用方法
1. 从 Releases 下载 JAR 文件
2. 将 JAR 文件放入 PeerBanHelper 的 `data/plugins` 目录。
3. 重启 PeerBanHelper，插件会自动加载。如果日志中出现 `[Bootstrap/INFO]: [注册] qB467PeerDetector`，说明插件已经开始运行。

## 依赖
- PF4J
- PF4J-Spring
- OkHttp
- PeerBanHelper 主程序（需提供依赖）

## GitHub Actions
本项目已集成 GitHub Actions 自动构建，详见 `.github/workflows/build-plugin.yml`。