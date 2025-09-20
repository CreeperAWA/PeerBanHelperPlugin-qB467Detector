package com.ghostchu.peerbanhelperplugin.detector;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import com.ghostchu.peerbanhelper.Main;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

/**
 * qB467 吸血客户端检测插件
 * 
 * 用于将 qB467Detector 模块注册到 PBH 核心系统的插件类。
 * 通过监听服务器启动事件，使用反射机制动态注册检测模块。
 * 遵循 PF4J 插件框架规范，保证与主程序的松耦合。
 */
public class qB467Plugin extends Plugin {
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
     * 直接通过反射注册检测模块，避免对主程序事件类的直接引用。
     */
    @Override
    public void start() {
        // 延迟注册以确保主程序完全启动
        new Thread(() -> {
            try {
                // 等待主程序启动完成
                Thread.sleep(1000);
                
                // 获取 Spring 上下文
                ApplicationContext ctx = Main.getApplicationContext();
                
                // 获取模块管理器实例（通过 Bean 名称）
                Object moduleManager = ctx.getBean("moduleManagerImpl");
                
                // 创建检测器实例
                Object detector = new qB467Detector();
                
                // 获取 FeatureModule 类型
                Class<?> featureModuleClass = Class.forName("com.ghostchu.peerbanhelper.module.FeatureModule");
                
                // 获取模块管理器的注册方法（含访问权限调整）
                Method reg = moduleManager.getClass().getDeclaredMethod(
                    "register",
                    featureModuleClass,
                    String.class
                );
                
                // 允许访问私有方法
                reg.setAccessible(true);
                
                // 执行注册（将检测器实例注册为指定名称的模块）
                reg.invoke(moduleManager, featureModuleClass.cast(detector), "qB467Detector");
            } catch (Throwable t) {
                t.printStackTrace();
                // 注册失败时静默处理（不抛出异常）
            }
        }).start();
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