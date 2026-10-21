package com.campus.assignment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${services.report-service.url:http://localhost:8081}")
    private String reportServiceUrl;

    @Value("${services.report-service.connect-timeout-ms:3000}")
    private int connectTimeout;

    @Value("${services.report-service.read-timeout-ms:3000}")
    private int readTimeout;

    @Bean
    public RestClient reportRestClient(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(readTimeout));

        return builder
                .baseUrl(reportServiceUrl)
                .requestFactory(factory)
                .build();
    }
}
