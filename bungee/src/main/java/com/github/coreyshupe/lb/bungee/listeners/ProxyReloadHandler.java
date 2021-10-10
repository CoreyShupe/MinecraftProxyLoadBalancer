package com.github.coreyshupe.lb.bungee.listeners;

import com.github.coreyshupe.lb.api.LoadBalancerInstance;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;

public record ProxyReloadHandler(LoadBalancerInstance instance) implements Listener {
    @EventHandler
    public void onProxyReload(@SuppressWarnings("unused") ProxyReloadEvent unused) {
        try {
            instance.getConfigProvider().reloadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
