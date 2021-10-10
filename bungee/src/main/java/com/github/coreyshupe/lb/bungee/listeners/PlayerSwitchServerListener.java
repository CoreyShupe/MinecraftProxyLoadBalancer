package com.github.coreyshupe.lb.bungee.listeners;

import com.github.coreyshupe.lb.api.LoadBalancerInstance;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public record PlayerSwitchServerListener(LoadBalancerInstance instance) implements Listener {
    @EventHandler
    public void onServerSwitch(ServerConnectedEvent event) {
        instance.getServerBalanceCache().reportConnect(event.getServer().getInfo().getName());
    }

    @EventHandler
    public void onDisconnect(ServerDisconnectEvent event) {
        instance.getServerBalanceCache().reportDisconnect(event.getTarget().getName());
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent event) {
        if (event.getFrom() != null) {
            instance.getServerBalanceCache().reportDisconnect(event.getFrom().getName());
        }
    }
}
