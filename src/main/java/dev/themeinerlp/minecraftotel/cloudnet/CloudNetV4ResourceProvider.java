package dev.themeinerlp.minecraftotel.cloudnet;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.service.ServiceConfiguration;
import eu.cloudnetservice.driver.service.ServiceId;
import eu.cloudnetservice.wrapper.configuration.WrapperConfiguration;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

import java.util.Optional;

public class CloudNetV4ResourceProvider implements ResourceProvider {
    private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
    private static final AttributeKey<String> SERVICE_INSTANCE_ID = AttributeKey.stringKey("service.instance.id");

    private static final AttributeKey<String> CLOUDNET_TASK = AttributeKey.stringKey("cloudnet.task.name");
    private static final AttributeKey<String> CLOUDNET_SERVICE = AttributeKey.stringKey("cloudnet.service.name");

    @Override
    public Resource createResource(ConfigProperties config) {
        String configured = config.getString("otel.service.name");
        if (configured != null && !configured.isBlank()) {
            return Resource.empty();
        }

        WrapperConfiguration wrapperConfiguration = InjectionLayer.ext().instance(WrapperConfiguration.class);
        if (wrapperConfiguration == null) {
            return Resource.empty();
        }

        String serviceName = Optional.of(wrapperConfiguration.serviceConfiguration())
                .map(ServiceConfiguration::serviceId)
                .map(ServiceId::name)
                .orElse("unknown");
        String taskName = Optional.of(wrapperConfiguration.serviceConfiguration())
                .map(ServiceConfiguration::serviceId)
                .map(ServiceId::taskName)
                .orElse("unknown");
        ServiceId info = wrapperConfiguration.serviceConfiguration().serviceId();

        Attributes attrs = Attributes.builder()
                .put(SERVICE_NAME, serviceName)
                .put(SERVICE_INSTANCE_ID, info.uniqueId().toString())
                .put(CLOUDNET_TASK, taskName)
                .put(CLOUDNET_SERVICE, serviceName)
                .build();

        return Resource.create(attrs);
    }
}
