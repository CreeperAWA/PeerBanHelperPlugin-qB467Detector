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
                    Object detector = null;
                    try {
                        detector = ctx.getBean("qB467Detector");
                        System.out.println("[qB467Plugin] Got qB467Detector from main Spring context: " + detector);
                    } catch (Exception e) {
                        System.out.println("[qB467Plugin] Main Spring context getBean('qB467Detector') failed: " + e.getMessage());
                        // 尝试插件内自建Spring上下文
                        try {
                            org.springframework.context.annotation.AnnotationConfigApplicationContext pluginCtx = new org.springframework.context.annotation.AnnotationConfigApplicationContext();
                            pluginCtx.scan("com.ghostchu.peerbanhelperplugin.detector");
                            pluginCtx.refresh();
                            detector = pluginCtx.getBean("qB467Detector");
                            System.out.println("[qB467Plugin] Got qB467Detector from plugin Spring context: " + detector);
                        } catch (Exception ex) {
                            System.out.println("[qB467Plugin] Plugin Spring context getBean('qB467Detector') failed: " + ex.getMessage());
                        }
                    }
                    System.out.println("[qB467Plugin] moduleManagerImpl=" + moduleManager + ", detector=" + detector);
                    if (detector != null) {
                        moduleManager.getClass().getMethod("register", detector.getClass().getSuperclass(), String.class)
                            .invoke(moduleManager, detector, "qB467Detector");
                        System.out.println("[qB467Plugin] qB467Detector registered to ModuleManager.");
                    } else {
                        System.out.println("[qB467Plugin] qB467Detector is null, registration skipped.");
                    }
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
