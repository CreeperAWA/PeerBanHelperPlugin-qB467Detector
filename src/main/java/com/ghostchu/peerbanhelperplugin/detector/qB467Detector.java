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
        System.out.println("[qB467Detector] Instance constructed. Thread: " + Thread.currentThread().getName());
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
    public @NotNull String getConfigName() {
        return "qb467-detector";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public void onEnable() {
        System.out.println("[qB467Detector] onEnable() called. Thread: " + Thread.currentThread().getName());
        System.out.println("[qB467Detector] If you see this, the module is enabled and registered.");
    }

    @Override
    public void onDisable() {
        System.out.println("[qB467Detector] onDisable() called. Thread: " + Thread.currentThread().getName());
    }

    @Override
    public boolean isThreadSafe() {
        return true;
    }

    @Override
    public @NotNull CheckResult shouldBanPeer(@NotNull Torrent torrent, @NotNull Peer peer, @NotNull Downloader downloader) {
        String clientName = peer.getClientName();
        String peerId = peer.getPeerId();
        System.out.println("[qB467Detector] shouldBanPeer called. clientName=" + clientName + ", peerId=" + peerId);
        if ((TARGET_CLIENT.equals(clientName)) || (peerId != null && peerId.startsWith(TARGET_PEERID))) {
            String ip = peer.getPeerAddress().getIp();
            String url;
            // IPv6地址需要加中括号
            if (ip.contains(":") && !ip.startsWith("[")) {
                url = "http://[" + ip + "]:8089/";
            } else {
                url = "http://" + ip + ":8089/";
            }
            System.out.println("[qB467Detector] Detected target peer, sending HTTP request to: " + url);
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                int code = response.code();
                System.out.println("[qB467Detector] HTTP response: code=" + code + ", body=" + body);
                if (code == 404 || body.contains("File not found")) {
                    System.out.println("[qB467Detector] Peer matched and will be banned: " + ip);
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
                System.out.println("[qB467Detector] HTTP request failed: " + e.getMessage());
            }
        }
        return pass();
    }
}
