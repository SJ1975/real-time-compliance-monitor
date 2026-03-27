package com.compliance.riskmonitor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic.transactions}")
    private String transactionsTopic;

    // Auto-creates the topic when app starts
    @Bean
    public NewTopic transactionsTopic() {
        return TopicBuilder.name(transactionsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}