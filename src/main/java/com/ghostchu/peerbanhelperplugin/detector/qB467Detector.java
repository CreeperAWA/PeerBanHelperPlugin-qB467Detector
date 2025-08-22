package com.ghostchu.peerbanhelperplugin.detector;

import com.ghostchu.peerbanhelper.bittorrent.peer.Peer;
import com.ghostchu.peerbanhelper.bittorrent.torrent.Torrent;
import com.ghostchu.peerbanhelper.downloader.Downloader;
import com.ghostchu.peerbanhelper.module.AbstractRuleFeatureModule;
import com.ghostchu.peerbanhelper.module.CheckResult;
import com.ghostchu.peerbanhelper.module.PeerAction;
import com.ghostchu.peerbanhelper.text.Lang;
import com.ghostchu.peerbanhelper.text.TranslationComponent;
import com.ghostchu.peerbanhelper.wrapper.StructuredData;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * qB467Detector
 * 
 * 该插件用于检测并封禁恶意使用 qBittorrent/4.6.7 吸血的 Peer。
 * 通过客户端名称或 PeerId 特征识别，并通过 HTTP 探针验证目标地址的吸血特征。
 * 兼容 PBH 插件自动注册机制，无需额外配置即可生效。
 */
public class qB467Detector extends AbstractRuleFeatureModule {
    private static final Logger log = LoggerFactory.getLogger(qB467Detector.class);
    // 空构造方法
    public qB467Detector() {
    }

    /** 目标客户端标识（ClientName） */
    private static final String TARGET_CLIENT = "qBittorrent/4.6.7";
    
    /** 目标 PeerId 前缀特征 */
    private static final String TARGET_PEERID = "-qB4670-";
    
    /** HTTP 探针客户端（1秒超时） */
    private OkHttpClient httpClient;

    /** IP检测缓存，避免24小时内重复检测同一IP */
    private final ConcurrentHashMap<String, Instant> detectionCache = new ConcurrentHashMap<>();
    
    /** 缓存有效期（24小时） */
    private static final Duration CACHE_DURATION = Duration.ofHours(24);

    /**
     * 获取模块名称
     * @return 模块名称 "qB467PeerDetector"
     */
    @Override
    public @NotNull String getName() {
        return "qB467PeerDetector";
    }

    /**
     * 获取配置名称（用于插件自动注册）
     * @return 配置名称 "qb467-detector"
     */
    @Override
    public @NotNull String getConfigName() {
        return "qb467-detector";
    }

    /**
     * 是否支持配置
     * @return false（当前模块无需额外配置）
     */
    @Override
    public boolean isConfigurable() {
        return false;
    }

    /**
     * 模块启用时的回调方法
     * （当前模块无实际启用逻辑）
     */
    @Override
    public void onEnable() {
        // 初始化HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 模块禁用时的回调方法
     * （当前模块无实际禁用逻辑）
     */
    @Override
    public void onDisable() {
        // 清理HTTP客户端资源
        if (this.httpClient != null) {
            this.httpClient.dispatcher().executorService().shutdown();
            this.httpClient.connectionPool().evictAll();
        }
    }

    /**
     * 是否为线程安全模块
     * @return true（模块可在多线程环境中安全使用）
     */
    @Override
    public boolean isThreadSafe() {
        return true;
    }

    /**
     * 核心检测逻辑：判断是否应封禁指定 Peer
     * 
     * @param torrent 当前种子信息
     * @param peer 待检测 Peer
     * @param downloader 下载器实例
     * @return CheckResult 封禁决策结果
     */
    @Override
    public @NotNull CheckResult shouldBanPeer(@NotNull Torrent torrent, @NotNull Peer peer, @NotNull Downloader downloader) {
        // 获取客户端标识和 PeerId
        String clientName = peer.getClientName();
        String peerId = peer.getPeerId();
        
        // 特征匹配检测（客户端名称或 PeerId 符合目标特征）
        if ((TARGET_CLIENT.equals(clientName)) || (peerId != null && peerId.startsWith(TARGET_PEERID))) {
            String ip = peer.getPeerAddress().getIp();
            
            // 检查缓存中是否已有该IP且在24小时内检测过
            Instant lastDetection = detectionCache.get(ip);
            if (lastDetection != null && Duration.between(lastDetection, Instant.now()).toHours() < 24) {
                log.debug("Skipping detection for IP {} as it was checked within the last 24 hours", ip);
                return pass(); // 24小时内已检测过，跳过检测
            }
            
            // 更新缓存中的检测时间
            detectionCache.put(ip, Instant.now());
            
            log.info("Detected qBittorrent/4.6.7 peer, initiating async probe: clientName={}, peerId={}, address={}", 
                clientName, peerId, peer.getPeerAddress());
            String url;
            
            // 处理 IPv6 地址格式（添加方括号包裹）
            if (ip.contains(":") && !ip.startsWith("[")) {
                url = "http://[" + ip + "]:8089/";
            } else {
                url = "http://" + ip + ":8089/";
            }
            
            // 异步探测避免阻塞主线程
            CompletableFuture<Boolean> probeFuture = CompletableFuture.supplyAsync(() -> {
                // 构建 HTTP 探针请求
                Request request = new Request.Builder().url(url).get().build();
                Call call = httpClient.newCall(request);
                try (Response response = call.execute()) {
                    String body = response.body() != null ? response.body().string() : "";
                    int code = response.code();
                    String serverHeader = response.header("Server", "");
                    
                    log.debug("Probe response: address={}, code={}, serverHeader={}, bodyLength={}", 
                        ip, code, serverHeader, body.length());
                    
                    // 吸血特征验证（满足任一条件即判定为恶意Peer）：
                    // 条件一：状态码为 404
                    // 条件二：响应体包含 "File not found"
                    // 条件三：响应头包含 "Python/3.10 aiohttp/3.11.12"
                    if (code == 404 || body.contains("File not found") || serverHeader.contains("Python/3.10 aiohttp/3.11.12")) {
                        log.info("Malicious qBittorrent/4.6.7 peer detected and confirmed: address={}", ip);
                        return true;
                    } else {
                        log.debug("qBittorrent/4.6.7 peer probe completed, but not matching malicious criteria: address={}", ip);
                        return false;
                    }
                } catch (IOException e) {
                    log.debug("qBittorrent/4.6.7 peer probe failed with IOException: address={}, error={}", ip, e.getMessage());
                    return false;
                } catch (RuntimeException e) {
                    log.debug("qBittorrent/4.6.7 peer probe failed with RuntimeException: address={}, error={}", ip, e.getMessage());
                    return false;
                }
            });
            
            // 等待异步探测结果（非阻塞方式）
            boolean isMalicious;
            try {
                // 等待最多2秒，防止长时间阻塞
                isMalicious = probeFuture.get(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.debug("qBittorrent/4.6.7 peer probe timeout or error: address={}, error={}", ip, e.getMessage());
                isMalicious = false;
            }
            
            if (isMalicious) {
                return new CheckResult(
                        getClass(),
                        PeerAction.BAN, // 触发封禁动作
                        0,
                        new TranslationComponent("[Plugin] qB467PeerDetector"), // 模块名称
                        new TranslationComponent("[Plugin] qB467PeerDetector"), // 命中规则
                        StructuredData.create().add("ip", ip).add("reason", "[Plugin] qB467PeerDetector") // 封禁原因
                );
            }
        }
        return pass(); // 未匹配特征时放行
    }
}