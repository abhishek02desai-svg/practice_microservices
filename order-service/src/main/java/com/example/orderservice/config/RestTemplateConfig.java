package com.example.orderservice.config;

// ============================================================================
// RestTemplate configuration  --  COMMENTED OUT (OpenFeign is used instead).
// To switch back to RestTemplate:
//   1. Uncomment this whole file.
//   2. Follow the "RestTemplate" comments in OrderServiceImpl.
//   3. Comment out the Feign parts there (UserClient field/constructor/fetchUser).
// ============================================================================

// import org.springframework.cloud.client.loadbalancer.LoadBalanced;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.client.RestTemplate;
//
// @Configuration
// public class RestTemplateConfig {
//
//     // @LoadBalanced lets us use the Eureka service name (http://USER-SERVICE/...)
//     // instead of a fixed host and port.
//     @Bean
//     @LoadBalanced
//     public RestTemplate restTemplate() {
//         return new RestTemplate();
//     }
// }
