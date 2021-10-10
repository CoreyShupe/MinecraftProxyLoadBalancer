package com.github.coreyshupe.lb.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record LoadBalancerConfig(
        @JsonProperty("multiProxySupport") boolean multiProxySupport,
        @JsonProperty(value = "supportedServers", required = true) @NotNull String[] supportedServers,
        @JsonProperty(value = "redis") @Nullable RedisConfig redisConfig
) {
}
