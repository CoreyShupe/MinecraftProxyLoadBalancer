package com.github.coreyshupe.lb.bungee;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.coreyshupe.lb.api.LoadBalancerInstance;
import com.github.coreyshupe.lb.api.config.ConfigProvider;
import com.github.coreyshupe.lb.bungee.listeners.PlayerSwitchServerListener;
import com.github.coreyshupe.lb.bungee.listeners.ProxyReloadHandler;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;

public class BungeeLoadBalancerPlugin extends Plugin {
    private LoadBalancerInstance loadBalancerInstance;

    @Override
    public void onEnable() {
        try {
            this.loadBalancerInstance = new LoadBalancerInstance(
                    new ConfigProvider(new YAMLMapper(), Path.of(getDataFolder().toURI()).resolve("config.yml"))
            );
            getProxy().setReconnectHandler(new BungeeLoadBalanceReconnectHandler(this.loadBalancerInstance));
            getProxy().getPluginManager().registerListener(this, new PlayerSwitchServerListener(this.loadBalancerInstance));
            getProxy().getPluginManager().registerListener(this, new ProxyReloadHandler(this.loadBalancerInstance));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LoadBalancerInstance getLoadBalancerInstance() {
        return loadBalancerInstance;
    }
}
