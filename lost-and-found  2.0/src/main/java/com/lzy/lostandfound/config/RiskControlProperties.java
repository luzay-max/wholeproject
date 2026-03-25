package com.lzy.lostandfound.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "risk-control")
public class RiskControlProperties {
    private boolean enabled = true;
    private boolean sensitiveEnabled = true;
    private boolean rateLimitEnabled = true;
    private int publishPerMinute = 6;
    private int commentPerMinute = 20;
    private int reportPerMinute = 10;
    private List<String> sensitiveWords = new ArrayList<>(List.of(
            "赌博",
            "诈骗",
            "色情",
            "毒品",
            "约炮",
            "裸聊"
    ));
}

