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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * qB467Detector
 * 
 * 该插件用于检测并封禁恶意使用 qBittorrent/4.6.7 吸血的 Peer。
 * 通过客户端名称或 PeerId 特征识别，并通过 HTTP 探针验证目标地址的吸血特征。
 * 兼容 PBH 插件自动注册机制，无需额外配置即可生效。
 */
public class qB467Detector extends AbstractRuleFeatureModule {
    // 空构造方法
    public qB467Detector() {
    }

    /** 目标客户端标识（ClientName） */
    private static final String TARGET_CLIENT = "qBittorrent/4.6.7";
    
    /** 目标 PeerId 前缀特征 */
    private static final String TARGET_PEERID = "-qB4670-";
    
    /** HTTP 探针客户端（2秒超时） */
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .build();

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
    }

    /**
     * 模块禁用时的回调方法
     * （当前模块无实际禁用逻辑）
     */
    @Override
    public void onDisable() {
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
            String url;
            
            // 处理 IPv6 地址格式（添加方括号包裹）
            if (ip.contains(":") && !ip.startsWith("[")) {
                url = "http://[" + ip + "]:8089/";
            } else {
                url = "http://" + ip + ":8089/";
            }
            
            // 构建 HTTP 探针请求
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                int code = response.code();
                
                // 吸血特征验证：404 状态码或包含 "File not found" 内容
                if (code == 404 || body.contains("File not found")) {
                    return new CheckResult(
                            getClass(),
                            PeerAction.BAN, // 触发封禁动作
                            0,
                            new TranslationComponent("[Plugin] qB467PeerDetector"), // 模块名称
                            new TranslationComponent("[Plugin] qB467PeerDetector"), // 命中规则
                            StructuredData.create().add("ip", ip).add("reason", "[Plugin] qB467PeerDetector") // 封禁原因
                    );
                }
            } catch (IOException | RuntimeException e) {
                // 探针失败（超时/连接拒绝等）时不封禁
            }
        }
        return pass(); // 未匹配特征时放行
    }
}