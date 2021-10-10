package com.github.api.cache;

import com.github.api.config.LoadBalancerConfig;
import com.github.api.config.RedisConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class RedisServerBalanceCache implements ServerBalanceCache {
    private static record ServerRecord(String id, int currentPlayerCount) {
    }

    private final static String SERVER_ID_PREFIX = "load-balancer-server:%s";
    private final AtomicBoolean redisLock = new AtomicBoolean(false);
    private final List<Runnable> actionableQueue;
    private String[] servers;
    private JedisPool jedisPool;

    public RedisServerBalanceCache(LoadBalancerConfig config) {
        this.actionableQueue = new CopyOnWriteArrayList<>();
        if (config.redisConfig() == null) {
            throw new IllegalStateException("Failed to initialize multi proxy support without an external cache option.");
        }
        this.servers = config.supportedServers();
        createJedisPool(config.redisConfig(), true);
    }

    private void createJedisPool(RedisConfig config, boolean clear) {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), config.hostName(), config.port(), config.username(), config.password());
        if (clear) {
            this.jedisPool.getResource().del(this.jedisPool.getResource().keys(SERVER_ID_PREFIX.formatted('*')).toArray(new String[0]));
        }
        for (String server : this.servers) {
            this.jedisPool.getResource().setnx(key(server), "1");
        }
    }

    private String key(String serverId) {
        return SERVER_ID_PREFIX.formatted(serverId);
    }

    @Override
    public CompletableFuture<String> getBestServer() {
        if (this.redisLock.get()) {
            CompletableFuture<String> future = new CompletableFuture<>();
            this.actionableQueue.add(() -> getBestServer().whenComplete((product, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                } else if (product != null) {
                    future.complete(product);
                } else {
                    future.completeExceptionally(new IllegalStateException("Failed to locate best server."));
                }
            }));
            return future;
        }
        String[] keys = new String[this.servers.length];
        for (int i = 0; i < this.servers.length; i++) {
            keys[i] = key(this.servers[i]);
        }
        List<String> keyResults = this.jedisPool.getResource().mget(keys);
        List<ServerRecord> records = new ArrayList<>(keyResults.size());
        for (int i = 0; i < keyResults.size() && i < this.servers.length; i++) {
            String keyResult = keyResults.get(i);
            if (keyResult != null) {
                records.add(new ServerRecord(this.servers[i], Integer.parseInt(keyResult)));
            }
        }
        return records.stream()
                .min(Comparator.comparingInt(record -> record.currentPlayerCount))
                .map(record -> CompletableFuture.completedFuture(record.id))
                .orElseGet(() -> CompletableFuture.failedFuture(new IllegalStateException("Found no valid servers.")));
    }

    @Override
    public void reportConnect(String serverId) {
        if (this.redisLock.get()) {
            this.actionableQueue.add(() -> reportConnect(serverId));
            return;
        }
        this.jedisPool.getResource().incr(this.key(serverId));
    }

    @Override
    public void reportDisconnect(String serverId) {
        if (this.redisLock.get()) {
            this.actionableQueue.add(() -> reportDisconnect(serverId));
            return;
        }
        this.jedisPool.getResource().decr(this.key(serverId));
    }

    @Override
    public void reload(LoadBalancerConfig config) {
        this.redisLock.set(true);
        this.servers = config.supportedServers();
        if (config.redisConfig() == null) {
            this.redisLock.set(false);
            this.actionableQueue.forEach(Runnable::run);
            this.actionableQueue.clear();
            throw new IllegalStateException("Failed to initialize multi proxy support without an external cache option.");
        }
        this.jedisPool.close();
        createJedisPool(config.redisConfig(), false);
        this.redisLock.set(false);
        this.actionableQueue.forEach(Runnable::run);
        this.actionableQueue.clear();
    }

    @Override
    public void shutdown() {
        this.jedisPool.close();
    }
}
