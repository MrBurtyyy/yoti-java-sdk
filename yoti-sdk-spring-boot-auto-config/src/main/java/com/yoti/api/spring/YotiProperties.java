package com.yoti.api.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

/**
 * Allows properties for Yoti configuration to be supplied via spring properties (e.g. YAML or properties file).
 */
@ConfigurationProperties(prefix = "com.yoti")
public class YotiProperties {

    private String baseUrl;

    private String yotiApiUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getYotiApiUrl() {
        return yotiApiUrl;
    }

    public void setYotiApiUrl(String yotiApiUrl) {
        this.yotiApiUrl = yotiApiUrl;
    }

    @PostConstruct
    public void init() {
        if (yotiApiUrl != null) {
            System.setProperty("yoti.api.url", yotiApiUrl);
        }
    }
}
