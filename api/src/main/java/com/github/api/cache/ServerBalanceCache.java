package com.github.api.cache;

import com.github.api.config.LoadBalancerConfig;

import java.util.concurrent.CompletableFuture;

public interface ServerBalanceCache {
    CompletableFuture<String> getBestServer();

    void reportConnect(String serverId);

    void reportDisconnect(String serverId);

    void reload(LoadBalancerConfig config);

    void shutdown();
}
