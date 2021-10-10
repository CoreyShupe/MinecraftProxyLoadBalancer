package com.github.coreyshupe.lb.api.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConfigProvider {
    private final @NotNull ObjectMapper objectMapper;
    private final @NotNull Path configPath;
    private final @NotNull List<ReloadHook> reloadHooks;
    private @Nullable LoadBalancerConfig memoizedConfig;

    public ConfigProvider(@NotNull ObjectMapper objectMapper, @NotNull Path configPath) {
        this.objectMapper = objectMapper
                .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
                .configure(JsonParser.Feature.IGNORE_UNDEFINED, true)
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                .configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        this.configPath = configPath;
        this.reloadHooks = new CopyOnWriteArrayList<>();
        this.memoizedConfig = null;
    }

    public void saveDefaultConfig(LoadBalancerConfig defaultConfiguration) throws IOException {
        if (!Files.exists(configPath)) {
            this.writeConfig(defaultConfiguration);
        }
    }

    private LoadBalancerConfig loadConfig0(boolean reload) throws IOException {
        if (!reload && memoizedConfig != null) {
            return memoizedConfig;
        }
        try (InputStream inputStream = Files.newInputStream(configPath)) {
            return this.memoizedConfig = this.objectMapper.readValue(inputStream, LoadBalancerConfig.class);
        }
    }

    public LoadBalancerConfig loadConfig() throws IOException {
        return loadConfig0(false);
    }

    public void reloadConfig() throws IOException {
        loadConfig0(true);
        this.reloadHooks.forEach(consumer -> consumer.accept(this.memoizedConfig));
    }

    public void addReloadHook(ReloadHook configConsumer) {
        this.reloadHooks.add(configConsumer);
    }

    public void writeConfig(LoadBalancerConfig config) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(this.configPath)) {
            this.objectMapper.writeValue(outputStream, config);
        }
    }
}
