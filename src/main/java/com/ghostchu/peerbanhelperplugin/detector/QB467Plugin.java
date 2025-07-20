package com.ghostchu.peerbanhelperplugin.detector;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import com.ghostchu.peerbanhelper.event.PBHServerStartedEvent;
import com.ghostchu.peerbanhelper.Main;
import org.springframework.context.ApplicationContext;

public class QB467Plugin extends Plugin {
    public QB467Plugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        // 注册 PBHServerStartedEvent 监听器，实现插件自动注册为功能模块
        Main.getEventBus().register(new Object() {
            @com.google.common.eventbus.Subscribe
            public void onPBHStarted(PBHServerStartedEvent event) {
                try {
                    ApplicationContext ctx = Main.getApplicationContext();
                    Object moduleManager = ctx.getBean("moduleManagerImpl");
                    Object detector = ctx.getBean("QB467Detector");
                    // 反射调用 register(FeatureModule, String)
                    moduleManager.getClass().getMethod("register", detector.getClass().getSuperclass(), String.class)
                        .invoke(moduleManager, detector, "QB467Detector");
                } catch (Throwable t) {
                    // 静默失败
                }
            }
        });
    }

    @Override
    public void stop() {
        // 无需处理
    }
}
