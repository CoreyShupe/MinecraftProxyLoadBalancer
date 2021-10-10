package com.github.coreyshupe.api.cache;

import com.github.coreyshupe.api.config.LoadBalancerConfig;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryServerBalanceCache implements ServerBalanceCache {
    private static record ServerRecord(String id, int currentPlayerCount) {
        public ServerRecord increment() {
            return new ServerRecord(this.id, this.currentPlayerCount + 1);
        }

        public ServerRecord decrement() {
            return new ServerRecord(this.id, this.currentPlayerCount + 1);
        }
    }

    private final ConcurrentHashMap<String, ServerRecord> serverRecordMap;

    public MemoryServerBalanceCache(LoadBalancerConfig config) {
        this.serverRecordMap = new ConcurrentHashMap<>(config.supportedServers().length);
        for (String server : config.supportedServers()) {
            this.serverRecordMap.put(server, new ServerRecord(server, 0));
        }
    }

    @Override
    public CompletableFuture<String> getBestServer() {
        return CompletableFuture.completedFuture(this.serverRecordMap.values()
                .stream()
                .min(Comparator.comparingInt(record -> record.currentPlayerCount))
                .map(record -> record.id)
                .orElse(null));
    }

    @Override
    public void reportConnect(String serverId) {
        this.serverRecordMap.computeIfPresent(serverId, (key, old) -> old.increment());
    }

    @Override
    public void reportDisconnect(String serverId) {
        this.serverRecordMap.computeIfPresent(serverId, (key, old) -> old.decrement());
    }

    @Override
    public void reload(LoadBalancerConfig config) {
        for (String server : config.supportedServers()) {
            this.serverRecordMap.putIfAbsent(server, new ServerRecord(server, 0));
        }
    }

    @Override
    public void shutdown() {
        // do nothing
    }
}
