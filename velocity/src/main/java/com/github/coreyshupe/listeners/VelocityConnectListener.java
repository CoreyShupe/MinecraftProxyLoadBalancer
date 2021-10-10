package com.github.coreyshupe.listeners;

import com.github.api.LoadBalancerInstance;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;

public record VelocityConnectListener(LoadBalancerInstance instance) {
    @Subscribe
    public EventTask onServerConnect(ServerConnectedEvent event) {
        return EventTask.async(() -> {
            event.getPreviousServer().ifPresent(server -> this.instance.getServerBalanceCache().reportDisconnect(server.getServerInfo().getName()));
            this.instance.getServerBalanceCache().reportConnect(event.getServer().getServerInfo().getName());
        });
    }

    @Subscribe
    public EventTask onDisconnect(DisconnectEvent event) {
        return EventTask.async(() ->
                event.getPlayer()
                        .getCurrentServer()
                        .ifPresent(connection -> this.instance.getServerBalanceCache().reportDisconnect(connection.getServerInfo().getName()))
        );
    }
}
