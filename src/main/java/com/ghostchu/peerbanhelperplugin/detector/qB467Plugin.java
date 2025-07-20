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
        Main.getEventBus().register(new Object() {
            @com.google.common.eventbus.Subscribe
            public void onPBHStarted(PBHServerStartedEvent event) {
                try {
                    ApplicationContext ctx = Main.getApplicationContext();
                    Object moduleManager = ctx.getBean("moduleManagerImpl");
                    Object detector = new qB467Detector();
                    Class<?> featureModuleClass = Class.forName("com.ghostchu.peerbanhelper.module.FeatureModule");
                    java.lang.reflect.Method reg = moduleManager.getClass().getDeclaredMethod(
                        "register",
                        featureModuleClass,
                        String.class
                    );
                    reg.setAccessible(true);
                    reg.invoke(moduleManager, featureModuleClass.cast(detector), "qB467Detector");
                } catch (Throwable t) {
                    // 注册失败静默
                }
            }
        });
    }

    @Override
    public void stop() {
    }
}
