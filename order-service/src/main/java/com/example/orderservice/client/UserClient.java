package com.example.orderservice.client;

import com.example.orderservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// "USER-SERVICE" is the name registered in Eureka. Feign finds it and load-balances automatically.
@FeignClient(name = "USER-SERVICE", path = "/api/users")
public interface UserClient {

    @GetMapping("/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
}
