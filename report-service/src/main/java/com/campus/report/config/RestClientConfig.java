package com.campus.report.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${services.facility-service.url:http://localhost:8082}")
    private String facilityServiceUrl;

    @Value("${services.facility-service.connect-timeout-ms:3000}")
    private int connectTimeout;

    @Value("${services.facility-service.read-timeout-ms:3000}")
    private int readTimeout;

    @Bean
    public RestClient facilityRestClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));

        return builder
                .baseUrl(facilityServiceUrl)
                .requestFactory(factory)
                .build();
    }
}
