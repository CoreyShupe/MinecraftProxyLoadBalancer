package com.github.coreyshupe.lb.velocity.listeners;

import com.github.coreyshupe.lb.api.LoadBalancerInstance;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;

public record VelocityInitialConnectListener(ProxyServer proxyServer, LoadBalancerInstance instance) {
    @Subscribe
    public EventTask onInitialConnect(PlayerChooseInitialServerEvent event) {
        return EventTask.resumeWhenComplete(this.instance.getServerBalanceCache().getBestServer().whenComplete((bestServer, throwable) -> {
            if (bestServer != null) {
                this.proxyServer.getServer(bestServer).ifPresent(event::setInitialServer);
            }
        }));
    }
}
