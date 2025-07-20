package com.ghostchu.peerbanhelperplugin.detector;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public class QB467Plugin extends Plugin {
    public QB467Plugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        // 留空，自动注册由 @Component 完成
    }
}
