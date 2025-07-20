package com.ghostchu.peerbanhelperplugin.detector;

import com.ghostchu.peerbanhelper.module.ModuleManager;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;

public class QB467Plugin extends Plugin {
    public QB467Plugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        // 通过 PF4J/Spring 集成方式获取 ApplicationContext
        // 这里假设主程序已将 ApplicationContext 注入到 PF4J 插件上下文
        ApplicationContext ctx = org.pf4j.spring.SpringExtensionFactory.getApplicationContext();
        ModuleManager moduleManager = ctx.getBean(ModuleManager.class);
        moduleManager.register(QB467Detector.class);
    }
}
