package com.ghostchu.peerbanhelperplugin.detector;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import com.ghostchu.peerbanhelper.event.PBHServerStartedEvent;
import com.ghostchu.peerbanhelper.Main;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class qB467Plugin extends Plugin {
    private ScheduledExecutorService scheduler;

    public qB467Plugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        // 定时输出测试文本
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("[qB467Plugin] Plugin is running at " + System.currentTimeMillis());
        }, 0, 10, TimeUnit.SECONDS);

        // 自动注册功能模块
        Main.getEventBus().register(new Object() {
            @com.google.common.eventbus.Subscribe
            public void onPBHStarted(PBHServerStartedEvent event) {
                try {
                    System.out.println("[qB467Plugin] PBHServerStartedEvent received, try to register qB467Detector...");
                    ApplicationContext ctx = Main.getApplicationContext();
                    Object moduleManager = ctx.getBean("moduleManagerImpl");
                    Object detector = new qB467Detector();
                    System.out.println("[qB467Plugin] Directly new qB467Detector: " + detector);
                    moduleManager.getClass().getMethod("register", detector.getClass().getSuperclass(), String.class)
                        .invoke(moduleManager, detector, "qB467Detector");
                    System.out.println("[qB467Plugin] qB467Detector registered to ModuleManager.");
                } catch (Throwable t) {
                    System.out.println("[qB467Plugin] Register qB467Detector failed: " + t.getMessage());
                    t.printStackTrace(System.out);
                }
            }
        });
    }

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
