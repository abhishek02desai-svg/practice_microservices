package com.example.orderservice.config;

// import org.springframework.cloud.client.loadbalancer.LoadBalanced;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.client.RestClient;
//
// @Configuration
// public class RestClientConfig {
//
//     @Bean
//     @LoadBalanced
//     public RestClient.Builder loadBalancedRestClientBuilder() {
//         return RestClient.builder();
//     }
//
//     @Bean
//     public RestClient userRestClient(RestClient.Builder builder) {
//         return builder.baseUrl("http://USER-SERVICE").build();
//     }
// }