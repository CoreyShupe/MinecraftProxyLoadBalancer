package com.github.coreyshupe.api.config;

import java.util.function.Consumer;

@FunctionalInterface
public interface ReloadHook extends Consumer<LoadBalancerConfig> {
}
