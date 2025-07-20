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
        System.out.println("[QB467Plugin] start() called. Thread: " + Thread.currentThread().getName());
        System.out.println("[QB467Plugin] Plugin path: " + getWrapper().getPluginPath());
        System.out.println("[QB467Plugin] Descriptor: id=" + getWrapper().getPluginId() + ", version=" + getWrapper().getDescriptor().getVersion());
        new Exception("[QB467Plugin] Stack trace for start()").printStackTrace(System.out);

        // 注册 PBHServerStartedEvent 监听器
        try {
            Main.getEventBus().register(new Object() {
                @com.google.common.eventbus.Subscribe
                public void onPBHStarted(PBHServerStartedEvent event) {
                    System.out.println("[QB467Plugin] PBHServerStartedEvent received, try get ApplicationContext...");
                    try {
                        ApplicationContext ctx = Main.getApplicationContext();
                        System.out.println("[QB467Plugin] ApplicationContext: " + ctx);
                        if (ctx != null) {
                            String[] beanNames = ctx.getBeanDefinitionNames();
                            System.out.println("[QB467Plugin] Bean count: " + beanNames.length);
                            for (String name : beanNames) {
                                System.out.println("[QB467Plugin] Bean: " + name);
                            }
                        }
                    } catch (Throwable t) {
                        System.out.println("[QB467Plugin] Error accessing ApplicationContext: " + t.getMessage());
                        t.printStackTrace(System.out);
                    }
                }
            });
            System.out.println("[QB467Plugin] PBHServerStartedEvent listener registered.");
        } catch (Throwable t) {
            System.out.println("[QB467Plugin] Failed to register PBHServerStartedEvent listener: " + t.getMessage());
            t.printStackTrace(System.out);
        }
    }

    @Override
    public void stop() {
        System.out.println("[QB467Plugin] stop() called. Thread: " + Thread.currentThread().getName());
        System.out.println("[QB467Plugin] Plugin path: " + getWrapper().getPluginPath());
        new Exception("[QB467Plugin] Stack trace for stop()").printStackTrace(System.out);
    }
}
