package com.ghostchu.peerbanhelperplugin.detector;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import com.ghostchu.peerbanhelper.event.PBHServerStartedEvent;
import com.ghostchu.peerbanhelper.Main;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * qB467 吸血客户端检测插件
 * 
 * 用于将 qB467Detector 模块注册到 PBH 核心系统的插件类。
 * 通过监听服务器启动事件，使用反射机制动态注册检测模块。
 * 遵循 PF4J 插件框架规范，保证与主程序的松耦合。
 */
public class qB467Plugin extends Plugin {
    /** 线程池（当前未实际使用，保留用于未来扩展） */
    private ScheduledExecutorService scheduler;

    /**
     * 插件构造方法
     * 
     * @param wrapper 插件包装器
     */
    public qB467Plugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    /**
     * 插件启动方法
     * 
     * 监听 PBH 服务器启动事件，在事件触发时通过反射注册检测模块。
     * 使用事件总线实现与主程序的解耦，确保插件化兼容性。
     */
    @Override
    public void start() {
        Main.getEventBus().register(new Object() {
            /**
             * 服务器启动事件处理方法
             * 
             * 通过反射将 qB467Detector 注册到模块管理器中：
             * 1. 获取 Spring 应用上下文
             * 2. 获取模块管理器实例
             * 3. 创建检测器实例
             * 4. 通过反射调用注册方法
             * 
             * @param event 服务器启动事件
             */
            @com.google.common.eventbus.Subscribe
            public void onPBHStarted(PBHServerStartedEvent event) {
                try {
                    // 获取 Spring 上下文
                    ApplicationContext ctx = Main.getApplicationContext();
                    
                    // 获取模块管理器实例（通过 Bean 名称）
                    Object moduleManager = ctx.getBean("moduleManagerImpl");
                    
                    // 创建检测器实例
                    Object detector = new qB467Detector();
                    
                    // 获取 FeatureModule 类型
                    Class<?> featureModuleClass = Class.forName("com.ghostchu.peerbanhelper.module.FeatureModule");
                    
                    // 获取模块管理器的注册方法（含访问权限调整）
                    java.lang.reflect.Method reg = moduleManager.getClass().getDeclaredMethod(
                        "register",
                        featureModuleClass,
                        String.class
                    );
                    
                    // 允许访问私有方法
                    reg.setAccessible(true);
                    
                    // 执行注册（将检测器实例注册为指定名称的模块）
                    reg.invoke(moduleManager, featureModuleClass.cast(detector), "qB467Detector");
                } catch (Throwable t) {
                    // 注册失败时静默处理（不抛出异常）
                }
            }
        });
    }

    /**
     * 插件停止方法
     * 
     * 当前未实现具体清理逻辑（保留方法用于框架规范完整性）
     */
    @Override
    public void stop() {
    }
}