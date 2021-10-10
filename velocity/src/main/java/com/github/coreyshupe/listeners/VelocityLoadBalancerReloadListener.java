package com.github.coreyshupe.listeners;

import com.github.api.LoadBalancerInstance;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import org.slf4j.Logger;

import java.io.IOException;

public record VelocityLoadBalancerReloadListener(Logger logger, LoadBalancerInstance instance) {
    @Subscribe
    public void onReload(@SuppressWarnings("unused") ProxyReloadEvent unused, Continuation continuation) {
        try {
            this.instance.getConfigProvider().reloadConfig();
            continuation.resume();
        } catch (IOException error) {
            this.logger.error("Failed to reload VelocityLoadBalancer.", error);
            continuation.resumeWithException(error);
        }
    }
}
