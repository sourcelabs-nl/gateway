package nl.sourcelabs.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Primary
@Component
public class ApiKeyRateLimiter implements RateLimiter {

    Logger logger = LoggerFactory.getLogger(ApiKeyRateLimiter.class);
    AtomicInteger counter = new AtomicInteger();

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        logger.info("TEST" + counter.get());
        if (counter.addAndGet(1) > 5) {
            return Mono.just(new Response(false, new HttpHeaders().toSingleValueMap()));
        }
        return Mono.just(new Response(true, new HttpHeaders().toSingleValueMap()));
    }

    @Override
    public Map getConfig() {
        return null;
    }

    @Override
    public Class getConfigClass() {
        return null;
    }

    @Override
    public Object newConfig() {
        return null;
    }
}
