package com.github.api;

import com.github.api.cache.MemoryServerBalanceCache;
import com.github.api.cache.RedisServerBalanceCache;
import com.github.api.cache.ServerBalanceCache;
import com.github.api.config.ConfigProvider;
import com.github.api.config.LoadBalancerConfig;
import com.github.api.config.RedisConfig;

import java.io.IOException;

public class LoadBalancerInstance {
    private final static LoadBalancerConfig DEFAULT_BALANCER_CONFIG = new LoadBalancerConfig(
            false,
            new String[]{},
            new RedisConfig("127.0.0.1", 6379, "admin", "password")
    );

    private final ConfigProvider configProvider;
    private ServerBalanceCache serverBalanceCache;

    public LoadBalancerInstance(ConfigProvider configProvider) throws IOException {
        this.configProvider = configProvider;
        this.configProvider.saveDefaultConfig(DEFAULT_BALANCER_CONFIG);
        LoadBalancerConfig config = this.configProvider.loadConfig();
        if (config.multiProxySupport()) {
            this.serverBalanceCache = new RedisServerBalanceCache(config);
        } else {
            this.serverBalanceCache = new MemoryServerBalanceCache(config);
        }
        this.configProvider.addReloadHook((newConfig) -> {
            if (newConfig.multiProxySupport() && !(this.serverBalanceCache instanceof RedisServerBalanceCache)) {
                this.serverBalanceCache.shutdown();
                this.serverBalanceCache = new RedisServerBalanceCache(newConfig);
            } else if (!newConfig.multiProxySupport() && !(this.serverBalanceCache instanceof MemoryServerBalanceCache)) {
                this.serverBalanceCache.shutdown();
                this.serverBalanceCache = new MemoryServerBalanceCache(newConfig);
            } else {
                this.serverBalanceCache.reload(newConfig);
            }
        });
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public ServerBalanceCache getServerBalanceCache() {
        return serverBalanceCache;
    }
}
