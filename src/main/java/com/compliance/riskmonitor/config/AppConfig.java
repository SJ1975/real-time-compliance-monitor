package com.compliance.riskmonitor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppConfig {

    private Kafka kafka = new Kafka();
    private Compliance compliance = new Compliance();

    @Getter @Setter
    public static class Kafka {
        private Topic topic = new Topic();

        @Getter @Setter
        public static class Topic {
            private String transactions;
        }
    }

    @Getter @Setter
    public static class Compliance {
        private Rules rules = new Rules();
        private RiskScore riskScore = new RiskScore();

        @Getter @Setter
        public static class Rules {
            private BigDecimal highAmountThreshold;
            private int velocityMaxTransactions;
            private int velocityWindowMinutes;
            private List<String> suspiciousLocations;
        }

        @Getter @Setter
        public static class RiskScore {
            private int highAmountWeight;
            private int velocityWeight;
            private int suspiciousLocationWeight;
        }
    }
}