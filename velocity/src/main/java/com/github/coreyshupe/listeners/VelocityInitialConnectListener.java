package com.github.coreyshupe.listeners;

import com.github.coreyshupe.api.LoadBalancerInstance;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;

public record VelocityInitialConnectListener(ProxyServer proxyServer, LoadBalancerInstance instance) {
    @Subscribe
    public void onInitialConnect(PlayerChooseInitialServerEvent event, Continuation continuation) {
        this.instance.getServerBalanceCache().getBestServer().whenComplete((bestServer, throwable) -> {
            if (throwable != null) {
                continuation.resumeWithException(throwable);
            } else if (bestServer != null) {
                this.proxyServer.getServer(bestServer).ifPresent(event::setInitialServer);
                continuation.resume();
            }
        });
    }
}
