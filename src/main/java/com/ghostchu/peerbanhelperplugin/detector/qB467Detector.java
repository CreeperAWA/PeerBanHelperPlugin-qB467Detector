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

public class qB467Detector extends AbstractRuleFeatureModule {
    public qB467Detector() {
    }
    private static final String TARGET_CLIENT = "qBittorrent/4.6.7";
    private static final String TARGET_PEERID = "-qB4670-";
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .build();

    @Override
    public @NotNull String getName() {
        return "qB467 Peer Detector";
    }

    @Override
/**
 * qB467Detector
 * 
 * 检测 qBittorrent/4.6.7 客户端或 PeerId 特征的 Peer，并通过 HTTP 校验后自动封禁。
 * 兼容 PBH 插件自动注册机制。
 */
    public @NotNull String getConfigName() {
        return "qb467-detector";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean isThreadSafe() {
        return true;
    }

    @Override
    public @NotNull CheckResult shouldBanPeer(@NotNull Torrent torrent, @NotNull Peer peer, @NotNull Downloader downloader) {
        String clientName = peer.getClientName();
        String peerId = peer.getPeerId();
        if ((TARGET_CLIENT.equals(clientName)) || (peerId != null && peerId.startsWith(TARGET_PEERID))) {
            String ip = peer.getPeerAddress().getIp();
            String url;
            // IPv6地址需要加中括号
            if (ip.contains(":") && !ip.startsWith("[")) {
                url = "http://[" + ip + "]:8089/";
            } else {
                url = "http://" + ip + ":8089/";
            }
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                int code = response.code();
                if (code == 404 || body.contains("File not found")) {
                    return new CheckResult(
                            getClass(),
                            PeerAction.BAN,
                            0,
                            new TranslationComponent("qB467 吸血特征检测插件"),
                            new TranslationComponent(Lang.MODULE_BTN_BAN, "qB467"),
                            StructuredData.create().add("ip", ip).add("reason", "qB467 吸血特征检测插件'")
                    );
                }
            } catch (IOException | RuntimeException e) {
            }
        }
        return pass();
    }
}
