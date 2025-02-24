package com.example.demo.skm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "skm")
public class SkmConfig {
    private Map<String, String> domains;

    public Map<String, String> getDomains() {
        return domains;
    }

    public void setDomains(Map<String, String> domains) {
        this.domains = domains;
    }
}
