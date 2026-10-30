package dev.themeinerlp.minecraftotel.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import dev.themeinerlp.minecraftotel.api.collector.TelemetryCollector;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import io.opentelemetry.api.common.Attributes;
import java.time.Clock;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Counts Velocity connection events and records player session durations.
 *
 * @since 1.7.0
 */
public final class VelocityConnectionListener {
    private static final String ALLOWED = "allowed";
    private static final String DENIED = "denied";
    private static final String SUCCESS = "success";
    private static final String FAILED = "failed";

    private final TelemetryCollector collector;
    private final Clock clock;
    private final Map<UUID, Long> sessionStarts = new ConcurrentHashMap<>();

    /**
     * Creates a listener that reports connection events to the collector.
     *
     * @param collector collector that receives the metrics
     * @param clock clock used for session durations
     * @since 1.7.0
     */
    public VelocityConnectionListener(TelemetryCollector collector, Clock clock) {
        this.collector = collector;
        this.clock = clock;
    }

    /**
     * Counts a login as allowed or denied after all other plugins decided.
     *
     * @param event login event
     * @since 1.7.0
     */
    @Subscribe(order = PostOrder.LAST)
    public void onLogin(LoginEvent event) {
        String result = event.getResult().isAllowed() ? ALLOWED : DENIED;
        collector.recordLongCounter(
                StandardMetrics.PROXY_LOGINS,
                1L,
                StandardMetrics.UNIT_COUNT,
                Attributes.of(StandardMetrics.RESULT_KEY, result)
        );
    }

    /**
     * Starts a session for a player who completed login.
     *
     * @param event post-login event
     * @since 1.7.0
     */
    @Subscribe(order = PostOrder.LAST)
    public void onPostLogin(PostLoginEvent event) {
        sessionStarts.put(event.getPlayer().getUniqueId(), clock.millis());
    }

    /**
     * Counts a disconnect and records the session duration when one was tracked.
     *
     * @param event disconnect event
     * @since 1.7.0
     */
    @Subscribe(order = PostOrder.LAST)
    public void onDisconnect(DisconnectEvent event) {
        String reason = event.getLoginStatus().name().toLowerCase(Locale.ROOT);
        collector.recordLongCounter(
                StandardMetrics.PROXY_DISCONNECTS,
                1L,
                StandardMetrics.UNIT_COUNT,
                Attributes.of(StandardMetrics.REASON_KEY, reason)
        );

        Long start = sessionStarts.remove(event.getPlayer().getUniqueId());
        if (start != null) {
            double seconds = (clock.millis() - start) / 1000.0;
            collector.recordDoubleHistogram(
                    StandardMetrics.PROXY_SESSION_DURATION,
                    seconds,
                    StandardMetrics.UNIT_SECONDS,
                    Attributes.empty()
            );
        }
    }

    /**
     * Counts a successful connect to the backend the player is on.
     *
     * @param event post-connect event
     * @since 1.7.0
     */
    @Subscribe(order = PostOrder.LAST)
    public void onServerPostConnect(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        player.getCurrentServer().ifPresent(connection -> collector.recordLongCounter(
                StandardMetrics.PROXY_SERVER_CONNECTS,
                1L,
                StandardMetrics.UNIT_COUNT,
                Attributes.of(
                        StandardMetrics.SERVER_KEY, connection.getServerInfo().getName(),
                        StandardMetrics.RESULT_KEY, SUCCESS
                )
        ));
    }

    /**
     * Counts a failed connect or a kick from a backend.
     *
     * @param event kick event
     * @since 1.7.0
     */
    @Subscribe(order = PostOrder.LAST)
    public void onKickedFromServer(KickedFromServerEvent event) {
        String server = event.getServer().getServerInfo().getName();
        if (event.kickedDuringServerConnect()) {
            collector.recordLongCounter(
                    StandardMetrics.PROXY_SERVER_CONNECTS,
                    1L,
                    StandardMetrics.UNIT_COUNT,
                    Attributes.of(StandardMetrics.SERVER_KEY, server, StandardMetrics.RESULT_KEY, FAILED)
            );
            return;
        }
        collector.recordLongCounter(
                StandardMetrics.PROXY_SERVER_KICKS,
                1L,
                StandardMetrics.UNIT_COUNT,
                Attributes.of(StandardMetrics.SERVER_KEY, server)
        );
    }

    /**
     * Counts a server list ping.
     *
     * @param event ping event
     * @since 1.7.0
     */
    @Subscribe(order = PostOrder.LAST)
    public void onProxyPing(ProxyPingEvent event) {
        collector.recordLongCounter(
                StandardMetrics.PROXY_PINGS,
                1L,
                StandardMetrics.UNIT_COUNT,
                Attributes.empty()
        );
    }
}
