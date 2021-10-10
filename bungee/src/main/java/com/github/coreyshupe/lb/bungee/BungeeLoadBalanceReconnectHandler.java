package com.github.coreyshupe.lb.bungee;

import com.github.coreyshupe.lb.api.LoadBalancerInstance;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.ExecutionException;

public record BungeeLoadBalanceReconnectHandler(LoadBalancerInstance instance) implements ReconnectHandler {

    @Override
    public ServerInfo getServer(ProxiedPlayer proxiedPlayer) {
        try {
            String server = instance.getServerBalanceCache().getBestServer().get();
            return ProxyServer.getInstance().getServerInfo(server);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setServer(ProxiedPlayer proxiedPlayer) {
    }

    @Override
    public void save() {
    }

    @Override
    public void close() {
    }
}
