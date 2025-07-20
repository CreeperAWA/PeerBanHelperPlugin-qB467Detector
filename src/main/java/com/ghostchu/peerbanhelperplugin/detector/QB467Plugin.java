package com.ghostchu.peerbanhelperplugin.detector;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

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
    }

    @Override
    public void stop() {
        System.out.println("[QB467Plugin] stop() called. Thread: " + Thread.currentThread().getName());
        System.out.println("[QB467Plugin] Plugin path: " + getWrapper().getPluginPath());
        new Exception("[QB467Plugin] Stack trace for stop()").printStackTrace(System.out);
    }
}
