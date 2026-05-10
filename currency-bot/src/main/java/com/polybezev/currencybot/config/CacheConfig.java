package com.polybezev.currencybot.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("currency",
                Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build());
        manager.registerCustomCache("currencyList",
                Caffeine.newBuilder().expireAfterWrite(4, TimeUnit.HOURS).build());
        manager.registerCustomCache("crypto",
                Caffeine.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES).build());
        return manager;
    }
}
