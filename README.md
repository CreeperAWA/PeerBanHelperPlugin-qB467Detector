# qB467PeerDetectorPlugin

> [!TIP]
> 本项目代码由 GitHub Copilot Agent 辅助编写
>
> 该插件仅在 PeerBanHelper v9.0.1 版本经过测试，低于 v9.0.0-beta2 的版本可能无法正常工作。

## 简介

本插件专为 **PeerBanHelper** 设计，用于自动识别并封禁使用 `qBittorrent/4.6.7` 客户端或 PeerID 为 `-qB4670-` 的恶意 Peer 节点。

经分析发现，使用 qBittorrent 4.6.7 客户端的这些恶意节点通常运行一个基于 `Python/3.10 aiohttp/3.11.12` 构建的 HTTP 服务，监听在 **8089 端口**（疑似用于集中控制）。该服务在直接访问时返回如下特征：
- 响应头 `Server`: `Python/3.10 aiohttp/3.11.12`
- 响应体内容: `File not found`
- HTTP 状态码: `404`

本插件利用上述特征，实现对"吸血"行为的 qBittorrent 4.6.7 客户端的精准识别与封禁。

## 检测逻辑

当一个 Peer 连接进入时，插件将按以下流程判断是否应予以封禁：

1. 若其 `ClientName` 为 `qBittorrent/4.6.7` 或 `PeerID` 为 `-qB4670-`，则触发进一步探测；
2. 向该 Peer 的 IP 地址发起 HTTP GET 请求至 **端口 8089**（超时 1 秒）；
3. 若满足以下任意一项条件，则判定为恶意节点并执行封禁：
   - HTTP 响应状态码为 `404`
   - 响应体包含 `File not found`
   - 响应头 `Server` 包含 `Python/3.10 aiohttp/3.11.12`

> ✅ 只需满足任一条件即可触发封禁机制，提高检测覆盖率与可靠性。

### 检测流程图

```mermaid
flowchart TD
    classDef condition fill:#ffe4b5,stroke:#333,stroke-width:1px;
    classDef action fill:#e6f7ff,stroke:#333,stroke-width:1px;
    classDef ban fill:#ffcccc,stroke:#333,stroke-width:2px;
    classDef allow fill:#e6ffe6,stroke:#333,stroke-width:1px;
    classDef subgraphHeader fill:#f0f8ff,stroke:#4682b4,stroke-width:2px;

    A[Peer连接]:::action --> B{检查客户端标识}
    B -->|匹配 qBittorrent/4.6.7| C[执行HTTP探测]:::action
    B -->|匹配 -qB4670-| C
    B -->|不匹配| D[放行Peer<br>不是有该特征的吸血客户端]:::allow

    subgraph HTTP探测流程 ["🔍 HTTP恶意特征检测流程"]
        direction TB
        C --> E[构造HTTP请求]:::action
        E --> F[发送GET请求到IP:8089<br>（超时1秒）]:::action
        F --> G{检查响应}
        G --> H{状态码是否为 404？}
        H -->|是| M[封禁Peer<br>有恶意特征]:::ban
        H -->|否| I{响应体是否包含<br>File not found？}
        I -->|是| M
        I -->|否| J{Server头是否包含<br>Python/3.10 aiohttp/3.11.12？}
        J -->|是| M
        J -->|否| N[放行Peer<br>无恶意特征]:::allow
    end

    M --> Z[PeerBanHelper 执行封禁]:::action
    Z --> END[结束]
    N --> END
    D --> END

    class B,G,H,I,J condition
    class C,E,F,M,N,Z,END action
    class M ban
    class D,N allow
```

## 使用方法

1. 从 [Releases 页面](https://github.com/your-repo/releases) 下载最新 `.jar` 插件文件；
2. 将其复制到 PeerBanHelper 的 `data/plugins/` 目录下；
3. 重启 PeerBanHelper 服务；
4. 查看日志，若出现以下提示，表示插件已成功加载并运行：
   ```
   [Bootstrap/INFO]: [注册] qB467PeerDetector
   ```

## 依赖项

本插件依赖以下库（部分由 PeerBanHelper 主程序提供）：
- PF4J（插件框架）
- PF4J-Spring（Spring 集成支持）
- OkHttp（HTTP 请求客户端）
- SLF4J（日志门面）
- PeerBanHelper 主程序（运行环境依赖）

## 构建要求

- JDK 21 或更高版本
- Maven 3.10.1 或更高版本

## CI/CD 构建

项目已集成 GitHub Actions 自动化构建流程，详情请参阅：
`.github/workflows/build-plugin.yml`