package com.ghostchu.peerbanhelperplugin.detector;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.ghostchu.peerbanhelperplugin.detector")
public class qB467SpringConfig {
    public qB467SpringConfig() {
        System.out.println("[qB467SpringConfig] Spring config loaded, detector package will be scanned.");
    }
}
