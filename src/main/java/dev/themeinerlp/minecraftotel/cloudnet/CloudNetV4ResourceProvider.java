package dev.themeinerlp.minecraftotel.cloudnet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class CloudNetV4ResourceProvider implements ResourceProvider {
    private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
    private static final AttributeKey<String> SERVICE_INSTANCE_ID = AttributeKey.stringKey("service.instance.id");

    private static final AttributeKey<String> CLOUDNET_TASK = AttributeKey.stringKey("cloudnet.task.name");
    private static final AttributeKey<String> CLOUDNET_SERVICE = AttributeKey.stringKey("cloudnet.service.name");

    private static final String WRAPPER_DIR = ".wrapper";
    private static final String WRAPPER_JSON = "wrapper.json";
    private static final Gson GSON = new Gson();

    @Override
    public Resource createResource(ConfigProperties config) {
        CloudNetServiceId serviceId = loadWrapperServiceId();
        if (serviceId == null) {
            return null;
        }

        String serviceName = resolveServiceName(serviceId);
        String taskName = Optional.ofNullable(serviceId.taskName()).orElse("unknown");

        Attributes attrs = Attributes.builder()
                .put(SERVICE_NAME, serviceName)
                .put(SERVICE_INSTANCE_ID, Optional.ofNullable(serviceId.uniqueId()).orElse("unknown"))
                .put(CLOUDNET_TASK, taskName)
                .put(CLOUDNET_SERVICE, serviceName)
                .build();

        return Resource.create(attrs);
    }

    private static CloudNetServiceId loadWrapperServiceId() {
        Path wrapperDir = Paths.get(WRAPPER_DIR);
        if (!Files.isDirectory(wrapperDir)) {
            return null;
        }

        Path wrapperJson = wrapperDir.resolve(WRAPPER_JSON);
        if (!Files.isRegularFile(wrapperJson)) {
            return null;
        }

        String json;
        try {
            json = Files.readString(wrapperJson, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            return null;
        }

        CloudNetWrapperData wrapperData;
        try {
            wrapperData = GSON.fromJson(json, CloudNetWrapperData.class);
        } catch (RuntimeException ignored) {
            return null;
        }

        CloudNetServiceId serviceId = Optional.ofNullable(wrapperData)
                .map(CloudNetWrapperData::serviceConfiguration)
                .map(CloudNetServiceConfiguration::serviceId)
                .orElse(null);

        if (serviceId == null || serviceId.isEmpty()) {
            return null;
        }

        return serviceId;
    }

    private static String resolveServiceName(CloudNetServiceId serviceId) {
        if (serviceId == null) {
            return "unknown";
        }

        if (serviceId.name() != null && !serviceId.name().isBlank()) {
            return serviceId.name();
        }

        if (serviceId.taskName() != null && serviceId.taskServiceId() != null) {
            String splitter = Optional.ofNullable(serviceId.nameSplitter()).orElse("-");
            return serviceId.taskName() + splitter + serviceId.taskServiceId();
        }

        return Optional.ofNullable(serviceId.taskName()).orElse("unknown");
    }
}
