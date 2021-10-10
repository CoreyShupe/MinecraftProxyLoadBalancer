package com.github.coreyshupe;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.github.coreyshupe.api.LoadBalancerInstance;
import com.github.coreyshupe.api.config.ConfigProvider;
import com.github.coreyshupe.listeners.VelocityConnectListener;
import com.github.coreyshupe.listeners.VelocityInitialConnectListener;
import com.github.coreyshupe.listeners.VelocityLoadBalancerReloadListener;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "velocity-load-balancer",
        name = "VelocityLoadBalancer",
        authors = {"Corey Shupe (FiXed)"},
        url = "https://github.com/CoreyShupe/VelocityLoadBalancer",
        description = "Simple load balancer plugin for velocity networks.",
        version = "0.0.1"
)
public class VelocityLoadBalancerPlugin {
    private final ProxyServer proxyServer;
    private final LoadBalancerInstance loadBalancerInstance;
    private final Logger logger;

    @Inject
    public VelocityLoadBalancerPlugin(ProxyServer proxyServer, Logger logger, @DataDirectory Path directory) throws IOException {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.loadBalancerInstance = new LoadBalancerInstance(new ConfigProvider(new TomlMapper(), directory.resolve("config.toml")));
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        this.proxyServer.getEventManager().register(this, new VelocityConnectListener(this.loadBalancerInstance));
        this.proxyServer.getEventManager().register(this, new VelocityInitialConnectListener(this.proxyServer, this.loadBalancerInstance));
        this.proxyServer.getEventManager().register(this, new VelocityLoadBalancerReloadListener(this.logger, this.loadBalancerInstance));
    }
}
