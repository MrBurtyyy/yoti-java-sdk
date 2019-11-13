package com.yoti.api.examples.springboot;

import com.yoti.api.spring.YotiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Properties;

@SpringBootApplication
public class YotiSDKSampleApp {

    private static final Logger LOG = LoggerFactory.getLogger(YotiSDKSampleApp.class);

    public static void main(final String[] args) {
        SpringApplication.run(YotiSDKSampleApp.class, args);
    }

}
