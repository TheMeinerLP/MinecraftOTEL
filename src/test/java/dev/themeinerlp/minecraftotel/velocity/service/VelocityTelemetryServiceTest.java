package dev.themeinerlp.minecraftotel.velocity.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import java.nio.file.Path;
import java.time.Duration;
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
                "test"
        );

        assertDoesNotThrow(service::start, "start must not fail when the plugin instance owns the task");
        verify(scheduler).buildTask(eq(plugin), any(Runnable.class));
        verify(builder).schedule();
    }
}
