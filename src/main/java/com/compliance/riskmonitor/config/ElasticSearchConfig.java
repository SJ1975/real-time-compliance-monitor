package com.compliance.riskmonitor.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.compliance.riskmonitor.repository"
)
public class ElasticSearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUri;

    @Bean
    public ElasticsearchClient elasticsearchClient() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Handle both "http://localhost:9200" and "http://elasticsearch:9200"
        String uri = elasticsearchUri
                .replace("http://", "")
                .replace("https://", "");
        String host = uri.split(":")[0];
        int port = Integer.parseInt(uri.split(":")[1]);

        String scheme = elasticsearchUri.startsWith("https") ? "https" : "http";

        RestClient restClient = RestClient.builder(
                new HttpHost(host, port, scheme)
        ).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(mapper)
        );

        return new ElasticsearchClient(transport);
    }
}