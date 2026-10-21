package com.campus.report.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
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
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(readTimeout));

        return builder
                .baseUrl(facilityServiceUrl)
                .requestFactory(factory)
                .build();
    }
}
