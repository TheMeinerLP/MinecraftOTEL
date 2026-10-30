package dev.themeinerlp.minecraftotel.velocity.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import dev.themeinerlp.minecraftotel.velocity.listener.VelocityConnectionListener;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.helpers.NOPLogger;

class VelocityTelemetryServiceTest {
    @TempDir
    Path dataDirectory;

    @Test
    void startSchedulesSamplingUnderThePluginInstance() {
        Object plugin = new Object();
        Scheduler scheduler = mock(Scheduler.class);
        Scheduler.TaskBuilder builder = mock(Scheduler.TaskBuilder.class);
        ScheduledTask task = mock(ScheduledTask.class);
        ProxyServer proxyServer = mock(ProxyServer.class);
        when(proxyServer.getScheduler()).thenReturn(scheduler);
        EventManager eventManager = mock(EventManager.class);
        when(proxyServer.getEventManager()).thenReturn(eventManager);

        // Velocity rejects any owner that is not a registered plugin instance.
        when(scheduler.buildTask(any(), any(Runnable.class)))
                .thenThrow(new IllegalArgumentException("plugin is not registered"));
        when(scheduler.buildTask(eq(plugin), any(Runnable.class))).thenReturn(builder);
        when(builder.repeat(any(Duration.class))).thenReturn(builder);
        when(builder.schedule()).thenReturn(task);

        VelocityTelemetryService service = new VelocityTelemetryService(
                plugin,
                proxyServer,
                NOPLogger.NOP_LOGGER,
                dataDirectory,
                "test",
                fixedClock()
        );

        assertDoesNotThrow(service::start, "start must not fail when the plugin instance owns the task");
        verify(scheduler).buildTask(eq(plugin), any(Runnable.class));
        verify(builder).schedule();
    }

    @Test
    void startRegistersConnectionListenerUnderThePluginInstance() {
        Object plugin = new Object();
        ProxyServer proxyServer = proxyServerWithScheduler(plugin);
        EventManager eventManager = proxyServer.getEventManager();
        VelocityTelemetryService service = newService(plugin, proxyServer);

        service.start();

        verify(eventManager).register(eq(plugin), any(VelocityConnectionListener.class));
    }

    @Test
    void stopUnregistersConnectionListener() {
        Object plugin = new Object();
        ProxyServer proxyServer = proxyServerWithScheduler(plugin);
        EventManager eventManager = proxyServer.getEventManager();
        VelocityTelemetryService service = newService(plugin, proxyServer);
        service.start();

        service.stop();

        verify(eventManager).unregisterListener(eq(plugin), any(VelocityConnectionListener.class));
    }

    private ProxyServer proxyServerWithScheduler(Object plugin) {
        Scheduler scheduler = mock(Scheduler.class);
        Scheduler.TaskBuilder builder = mock(Scheduler.TaskBuilder.class);
        when(builder.repeat(any(Duration.class))).thenReturn(builder);
        when(builder.schedule()).thenReturn(mock(ScheduledTask.class));
        when(scheduler.buildTask(eq(plugin), any(Runnable.class))).thenReturn(builder);
        ProxyServer proxyServer = mock(ProxyServer.class);
        when(proxyServer.getScheduler()).thenReturn(scheduler);
        when(proxyServer.getEventManager()).thenReturn(mock(EventManager.class));
        return proxyServer;
    }

    private VelocityTelemetryService newService(Object plugin, ProxyServer proxyServer) {
        return new VelocityTelemetryService(
                plugin,
                proxyServer,
                NOPLogger.NOP_LOGGER,
                dataDirectory,
                "test",
                fixedClock()
        );
    }

    private static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC);
    }
}
