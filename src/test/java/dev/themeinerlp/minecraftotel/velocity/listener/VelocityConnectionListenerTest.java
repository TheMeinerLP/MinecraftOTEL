package dev.themeinerlp.minecraftotel.velocity.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import dev.themeinerlp.minecraftotel.api.metrics.StandardMetrics;
import dev.themeinerlp.minecraftotel.velocity.support.MutableClock;
import dev.themeinerlp.minecraftotel.velocity.support.RecordingTelemetryCollector;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

class VelocityConnectionListenerTest {
    private static final AttributeKey<String> RESULT = AttributeKey.stringKey("result");
    private static final AttributeKey<String> REASON = AttributeKey.stringKey("reason");
    private static final AttributeKey<String> SERVER = AttributeKey.stringKey("server");
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-30T12:00:00Z"));
    private final RecordingTelemetryCollector collector = new RecordingTelemetryCollector();
    private final VelocityConnectionListener listener = new VelocityConnectionListener(collector, clock);

    @Test
    void deniedLoginCountsAsDenied() {
        LoginEvent event = new LoginEvent(player());
        event.setResult(ResultedEvent.ComponentResult.denied(Component.text("banned")));

        listener.onLogin(event);

        assertEquals(
                1L,
                collector.counterTotal(StandardMetrics.PROXY_LOGINS, Attributes.of(RESULT, "denied")),
                "a login denied by any plugin must count as denied"
        );
    }

    @Test
    void allowedLoginCountsAsAllowed() {
        listener.onLogin(new LoginEvent(player()));

        assertEquals(
                1L,
                collector.counterTotal(StandardMetrics.PROXY_LOGINS, Attributes.of(RESULT, "allowed")),
                "a login without denial must count as allowed"
        );
    }

    @Test
    void disconnectReasonIsTheLowerCaseLoginStatus() {
        listener.onDisconnect(new DisconnectEvent(player(), DisconnectEvent.LoginStatus.CANCELLED_BY_USER));

        assertEquals(
                1L,
                collector.counterTotal(StandardMetrics.PROXY_DISCONNECTS, Attributes.of(REASON, "cancelled_by_user")),
                "the disconnect reason must be the lower-case login status"
        );
    }

    @Test
    void sessionDurationIsRecordedInSecondsFromLoginToDisconnect() {
        listener.onPostLogin(new PostLoginEvent(player()));
        clock.advance(Duration.ofSeconds(90));

        listener.onDisconnect(new DisconnectEvent(player(), DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN));

        assertEquals(
                List.of(90.0),
                durations(),
                "a player who disconnects 90 seconds after login must produce a 90 second session"
        );
    }

    @Test
    void trackedSessionIsRemovedOnDisconnect() {
        listener.onPostLogin(new PostLoginEvent(player()));
        clock.advance(Duration.ofSeconds(90));
        listener.onDisconnect(new DisconnectEvent(player(), DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN));

        listener.onDisconnect(new DisconnectEvent(player(), DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN));

        assertEquals(
                List.of(90.0),
                durations(),
                "a second disconnect of the same player must not reuse the removed session"
        );
    }

    @Test
    void disconnectWithoutTrackedSessionRecordsNoDuration() {
        listener.onDisconnect(new DisconnectEvent(player(), DisconnectEvent.LoginStatus.CONFLICTING_LOGIN));

        assertEquals(
                List.of(),
                durations(),
                "a disconnect for a player whose login never completed must not record a duration"
        );
    }

    @Test
    void successfulBackendConnectCountsSuccessForCurrentServer() {
        Player player = player();
        ServerConnection lobby = connection("lobby");
        when(player.getCurrentServer()).thenReturn(Optional.of(lobby));

        listener.onServerPostConnect(new ServerPostConnectEvent(player, registeredServer("lobby")));

        assertEquals(
                1L,
                collector.counterTotal(
                        StandardMetrics.PROXY_SERVER_CONNECTS,
                        Attributes.of(SERVER, "lobby", RESULT, "success")
                ),
                "a connect that completes must count as success for the backend the player is on"
        );
    }

    @Test
    void connectWithoutCurrentServerRecordsNothing() {
        listener.onServerPostConnect(new ServerPostConnectEvent(player(), registeredServer("lobby")));

        assertEquals(
                List.of(),
                collector.samples(StandardMetrics.PROXY_SERVER_CONNECTS),
                "a connect event without a current server must not count"
        );
    }

    @Test
    void kickDuringBackendConnectCountsFailedConnectAndNoKick() {
        listener.onKickedFromServer(kicked("survival", true));

        assertEquals(
                1L,
                collector.counterTotal(
                        StandardMetrics.PROXY_SERVER_CONNECTS,
                        Attributes.of(SERVER, "survival", RESULT, "failed")
                ),
                "a player refused while connecting must count as a failed connect"
        );
        assertEquals(
                List.of(),
                collector.samples(StandardMetrics.PROXY_SERVER_KICKS),
                "a refused connect must not also count as a kick"
        );
    }

    @Test
    void kickFromConnectedBackendCountsKickForThatBackend() {
        listener.onKickedFromServer(kicked("survival", false));

        assertEquals(
                1L,
                collector.counterTotal(StandardMetrics.PROXY_SERVER_KICKS, Attributes.of(SERVER, "survival")),
                "a player kicked from a connected backend must count as a kick for that backend"
        );
    }

    @Test
    void serverListPingCountsPing() {
        InboundConnection connection = mock(InboundConnection.class);

        listener.onProxyPing(new ProxyPingEvent(connection, mock(ServerPing.class)));

        assertEquals(
                1L,
                collector.counterTotal(StandardMetrics.PROXY_PINGS, Attributes.empty()),
                "every server list ping must count once"
        );
    }

    @Test
    void connectionMetricsNeverCarryPlayerIdentifyingAttributeKeys() {
        Player player = player();
        ServerConnection lobby = connection("lobby");
        when(player.getCurrentServer()).thenReturn(Optional.of(lobby));
        listener.onLogin(new LoginEvent(player));
        listener.onPostLogin(new PostLoginEvent(player));
        listener.onServerPostConnect(new ServerPostConnectEvent(player, registeredServer("lobby")));
        listener.onKickedFromServer(kicked("survival", false));
        listener.onProxyPing(new ProxyPingEvent(mock(InboundConnection.class), mock(ServerPing.class)));
        listener.onDisconnect(new DisconnectEvent(player, DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN));

        Set<String> keys = collector.all().stream()
                .flatMap(sample -> sample.attributes().asMap().keySet().stream())
                .map(AttributeKey::getKey)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(
                Set.of("result", "reason", "server").containsAll(keys),
                "connection metrics may only use result, reason and server attributes but used " + keys
        );
    }

    private List<Double> durations() {
        return collector.samples(StandardMetrics.PROXY_SESSION_DURATION).stream()
                .map(RecordingTelemetryCollector.Sample::value)
                .toList();
    }

    private static Player player() {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(PLAYER_ID);
        return player;
    }

    private static ServerConnection connection(String name) {
        ServerConnection connection = mock(ServerConnection.class);
        when(connection.getServerInfo()).thenReturn(serverInfo(name));
        return connection;
    }

    private static RegisteredServer registeredServer(String name) {
        RegisteredServer server = mock(RegisteredServer.class);
        when(server.getServerInfo()).thenReturn(serverInfo(name));
        return server;
    }

    private static KickedFromServerEvent kicked(String name, boolean duringConnect) {
        return new KickedFromServerEvent(
                player(),
                registeredServer(name),
                Component.text("kicked"),
                duringConnect,
                KickedFromServerEvent.Notify.create(Component.text("kicked"))
        );
    }

    private static ServerInfo serverInfo(String name) {
        return new ServerInfo(name, new InetSocketAddress("127.0.0.1", 25565));
    }
}
