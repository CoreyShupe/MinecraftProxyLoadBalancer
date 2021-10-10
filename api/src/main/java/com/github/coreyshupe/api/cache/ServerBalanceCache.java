package com.github.coreyshupe.api.cache;

import com.github.coreyshupe.api.config.LoadBalancerConfig;

import java.util.concurrent.CompletableFuture;

public interface ServerBalanceCache {
    CompletableFuture<String> getBestServer();

    void reportConnect(String serverId);

    void reportDisconnect(String serverId);

    void reload(LoadBalancerConfig config);

    void shutdown();
}
