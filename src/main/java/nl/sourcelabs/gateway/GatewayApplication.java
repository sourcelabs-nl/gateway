package nl.sourcelabs.gateway;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerResilience4JFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.DispatcherHandler;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
@RestController
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder, ApiKeyResolver apiKeyResolver, ApiKeyRateLimiter apiKeyRateLimiter) {
        return builder.routes()
                .route(p -> p
                        .path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri("http://httpbin.org:80"))
                .route(p -> p
                        .host("*.hystrix.com")
                        .filters(f -> f.circuitBreaker(config -> {
                                    config
                                            .setName("mycmd")
                                            .setFallbackUri("forward:/fallback");
                                }
                        ))
                        .uri("http://httpbin.org:80"))
                .route(p -> p
                        .path("/limited")
//                        .filters(f ->
//                                f.requestRateLimiter(r -> {
//                                    r.setKeyResolver(apiKeyResolver);
//                                    r.setRateLimiter(apiKeyRateLimiter);
//                                    r.setStatusCode(HttpStatus.I_AM_A_TEAPOT);
//                                } )
//                        )
                        .uri("http://httpbin.org:80/anything?route=limited")
                )
                .build();
    }


    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback");
    }


//    @Bean
//    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
//        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
//                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build())
//                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//                .build());
//    }

//    @Bean
//    public Resilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory(CircuitBreakerRegistry circuitBreakerRegistry) {
//        Resilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory = new Resilience4JCircuitBreakerFactory();
//        reactiveResilience4JCircuitBreakerFactory.configureCircuitBreakerRegistry(circuitBreakerRegistry);
//        return reactiveResilience4JCircuitBreakerFactory;
//    }

//    @Bean
//    public CircuitBreakerRegistry getCircuitBreakerRegistry() {
//
//        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
//                .failureRateThreshold(50)
//                .waitDurationInOpenState(Duration.ofMillis(1000))
//                .permittedNumberOfCallsInHalfOpenState(2)
//                .slidingWindowSize(2)
//                .recordExceptions(IOException.class, TimeoutException.class)
//                //.ignoreExceptions(BusinessException.class, OtherBusinessException.class)
//                .build();
//
//        // Create a CircuitBreakerRegistry with a custom global configuration
//        return CircuitBreakerRegistry.of(circuitBreakerConfig);
//    }

//    @Bean
//    public SpringCloudCircuitBreakerFilterFactory resilience4JCircuitBreakerFactory(ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory,
//            ObjectProvider<DispatcherHandler> dispatcherHandlers) {
//        return new SpringCloudCircuitBreakerResilience4JFilterFactory(reactiveCircuitBreakerFactory, dispatcherHandlers);
//    }
//
//    @Bean
//    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
//        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
//                .circuitBreakerConfig(CircuitBreakerConfig.custom().minimumNumberOfCalls(5).failureRateThreshold(20).build())
//                .build());
//    }

}
