package com.ghostchu.peerbanhelperplugin.detector;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public class QB467Plugin extends Plugin {
    public QB467Plugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println("[QB467Plugin] start() called. If you see this, plugin entry is active.");
    }
}
