package com.github.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RedisConfig(
        @JsonProperty(value = "hostName", required = true) @NotNull String hostName,
        @JsonProperty(value = "port", required = true) int port,
        @JsonProperty(value = "username") @Nullable String username,
        @JsonProperty(value = "password") @Nullable String password
) {
}
