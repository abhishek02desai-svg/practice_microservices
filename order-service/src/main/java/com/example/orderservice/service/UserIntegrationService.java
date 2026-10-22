package com.example.orderservice.service;

import com.example.orderservice.client.UserClient;
import com.example.orderservice.dto.UserResponse;
import com.example.orderservice.exception.UserNotFoundException;
import com.example.orderservice.exception.UserServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.util.List;

// ----- RestTemplate imports (COMMENTED OUT) -----
// import org.springframework.web.client.HttpClientErrorException;
// import org.springframework.web.client.RestClientException;
// import org.springframework.web.client.RestTemplate;

// ----- RestClient imports (COMMENTED OUT) -----
// import org.springframework.web.client.RestClient;
// (HttpClientErrorException and RestClientException are in the RestTemplate import block above)

/**
 * All calls from order-service to USER-SERVICE live here, protected by:
 *   - @Retry            : tries again when a call fails
 *   - @CircuitBreaker   : stops calling a service that keeps failing
 *   - fallbackMethod    : gives a safe answer when everything above gave up
 *
 * IMPORTANT: Resilience4j annotations only work when a method is called from ANOTHER bean
 * (that is why these methods are in their own class and not private methods in OrderServiceImpl).
 *
 * Order of protection (outside -> inside): Retry -> CircuitBreaker -> the actual call.
 * The fallbackMethod is placed on @Retry so that all retries happen BEFORE the fallback is used.
 */
@Service
public class UserIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(UserIntegrationService.class);

    // ----- OpenFeign (ACTIVE) -----
    private final UserClient userClient;

    // ----- WebClient (ACTIVE) -----
    private final WebClient userWebClient;

    // ----- RestClient (COMMENTED OUT) -----
    // private final RestClient userRestClient;

    // ----- RestTemplate (COMMENTED OUT) -----
    // private final RestTemplate restTemplate;

    public UserIntegrationService(UserClient userClient,
                                  WebClient userWebClient
                                  // , RestClient userRestClient   // <-- RestClient: uncomment this parameter
    ) {
        this.userClient = userClient;
        this.userWebClient = userWebClient;
        // this.userRestClient = userRestClient;                    // <-- RestClient: uncomment this line
    }

    // =====================================================================
    // 1) Used when CREATING an order (OpenFeign).
    //    Fallback: we must NOT create an order for an unverified user, so we answer 503.
    // =====================================================================
    @Retry(name = "userService", fallbackMethod = "getUserForOrderFallback")
    @CircuitBreaker(name = "userService")
    public UserResponse getUserForOrder(Long userId) {
        log.info("Calling USER-SERVICE with OpenFeign for user {}", userId);
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound ex) {
            throw new UserNotFoundException(userId);
        } catch (FeignException ex) {
            throw new UserServiceUnavailableException("User service is not available right now", ex);
        }
    }

    public UserResponse getUserForOrderFallback(Long userId, Throwable ex) {
        if (ex instanceof UserNotFoundException notFound) {
            throw notFound; // a missing user is a real answer, not a failure
        }
        log.warn("Fallback for getUserForOrder({}): {}", userId, ex.toString());
        throw new UserServiceUnavailableException(
                "User service is unavailable, the order cannot be verified right now. Please try again later.", ex);
    }

    // =====================================================================
    // 2) Used for order DETAILS (WebClient + Mono).
    //    Fallback: return a placeholder user so the order is still shown.
    // =====================================================================
    @Retry(name = "userService", fallbackMethod = "getUserDetailsFallback")
    @CircuitBreaker(name = "userService")
    public UserResponse getUserDetails(Long userId) {
        log.info("Calling USER-SERVICE with WebClient (Mono) for user {}", userId);
        try {
            return userWebClient.get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404,
                            response -> Mono.error(new UserNotFoundException(userId)))
                    .bodyToMono(UserResponse.class)
                    .block();
        } catch (WebClientException ex) {
            throw new UserServiceUnavailableException("User service is not available right now", ex);
        }
    }

    public UserResponse getUserDetailsFallback(Long userId, Throwable ex) {
        if (ex instanceof UserNotFoundException notFound) {
            throw notFound;
        }
        log.warn("Fallback for getUserDetails({}): {}", userId, ex.toString());
        return new UserResponse(userId, "User details unavailable", "N/A");
    }

    // =====================================================================
    // 3) Used to list ALL users (WebClient + Flux).
    //    Fallback: return an empty list.
    // =====================================================================
    @Retry(name = "userService", fallbackMethod = "getAllUsersFallback")
    @CircuitBreaker(name = "userService")
    public List<UserResponse> getAllUsers() {
        log.info("Calling USER-SERVICE with WebClient (Flux) for all users");
        try {
            return userWebClient.get()
                    .uri("/api/users")
                    .retrieve()
                    .bodyToFlux(UserResponse.class)   // Flux = many items
                    .collectList()                    // gather them into a List
                    .block();                         // wait for the result
        } catch (WebClientException ex) {
            throw new UserServiceUnavailableException("User service is not available right now", ex);
        }
    }

    public List<UserResponse> getAllUsersFallback(Throwable ex) {
        log.warn("Fallback for getAllUsers(): {}", ex.toString());
        return List.of();
    }

    // ------------------------------------------------------------------
    // RestClient version (COMMENTED OUT)
    // ------------------------------------------------------------------
    // private UserResponse fetchUserWithRestClient(Long userId) {
    //     try {
    //         return userRestClient.get()
    //                 .uri("/api/users/{id}", userId)
    //                 .retrieve()
    //                 .body(UserResponse.class);
    //     } catch (HttpClientErrorException.NotFound ex) {
    //         throw new UserNotFoundException(userId);
    //     } catch (RestClientException ex) {
    //         throw new UserServiceUnavailableException("User service is not available right now", ex);
    //     }
    // }

    // ------------------------------------------------------------------
    // RestTemplate version (COMMENTED OUT)
    // ------------------------------------------------------------------
    // private UserResponse fetchUserWithRestTemplate(Long userId) {
    //     try {
    //         return restTemplate.getForObject("http://USER-SERVICE/api/users/{id}", UserResponse.class, userId);
    //     } catch (HttpClientErrorException.NotFound ex) {
    //         throw new UserNotFoundException(userId);
    //     } catch (RestClientException ex) {
    //         throw new UserServiceUnavailableException("User service is not available right now", ex);
    //     }
    // }
}
