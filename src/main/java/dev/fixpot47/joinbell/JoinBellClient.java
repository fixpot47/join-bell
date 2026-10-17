package dev.fixpot47.joinbell;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class JoinBellClient implements ClientModInitializer {
    public static final String MOD_ID = "joinbell";

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "controls")
    );

    private static final long PLAYER_ALERT_COOLDOWN_MS = 30_000L;
    private static final int RING_DURATION_TICKS = 20 * 5;
    private static final int RING_INTERVAL_TICKS = 20;

    private final Set<String> previousKnownPlayers = new HashSet<>();
    private final Map<String, Long> lastAlertTimes = new HashMap<>();

    private ClientPacketListener lastConnection;
    private KeyMapping openMenuKey;
    private int ringTicksRemaining;
    private int ringInterval;

    @Override
    public void onInitializeClient() {
        JoinBellConfig.get();

        this.openMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.joinbell.open_menu",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_J,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                checkJoinMessage(Minecraft.getInstance(), message.getString());
            }
        });

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
                checkJoinMessage(Minecraft.getInstance(), message.getString())
        );
    }

    private void onEndTick(Minecraft client) {
        while (this.openMenuKey.consumeClick()) {
            client.gui.setScreen(new JoinBellScreen(client.gui.screen()));
        }

        tickBell(client);
        detectPlayerListJoins(client);
    }

    private void detectPlayerListJoins(Minecraft client) {
        ClientPacketListener connection = client.getConnection();
        if (connection == null || client.player == null) {
            this.lastConnection = null;
            this.previousKnownPlayers.clear();
            return;
        }

        Set<String> current = new HashSet<>();
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            if (info.getProfile() != null && info.getProfile().name() != null) {
                current.add(info.getProfile().name().toLowerCase(Locale.ROOT));
            }
        }

        if (connection != this.lastConnection) {
            this.lastConnection = connection;
            this.previousKnownPlayers.clear();
            this.previousKnownPlayers.addAll(current);
            return;
        }

        for (String tracked : JoinBellConfig.get().trackedPlayers()) {
            String normalized = tracked.toLowerCase(Locale.ROOT);
            if (current.contains(normalized) && !this.previousKnownPlayers.contains(normalized)) {
                triggerAlert(client, tracked);
            }
        }

        this.previousKnownPlayers.clear();
        this.previousKnownPlayers.addAll(current);
    }

    private void checkJoinMessage(Minecraft client, String message) {
        if (client == null || client.getConnection() == null || message == null || message.isBlank()) {
            return;
        }

        String lower = message.toLowerCase(Locale.ROOT);
        boolean looksLikeJoin = lower.contains(" joined")
                || lower.contains("joined the game")
                || lower.contains("joined the server")
                || lower.contains(" has joined")
                || lower.contains(" connected");

        if (!looksLikeJoin) {
            return;
        }

        for (String tracked : JoinBellConfig.get().trackedPlayers()) {
            if (containsWholeUsername(lower, tracked.toLowerCase(Locale.ROOT))) {
                triggerAlert(client, tracked);
            }
        }
    }

    private void triggerAlert(Minecraft client, String playerName) {
        String key = playerName.toLowerCase(Locale.ROOT);
        long now = System.currentTimeMillis();
        long last = this.lastAlertTimes.getOrDefault(key, 0L);
        if (now - last < PLAYER_ALERT_COOLDOWN_MS) {
            return;
        }

        this.lastAlertTimes.put(key, now);
        this.ringTicksRemaining = Math.max(this.ringTicksRemaining, RING_DURATION_TICKS);
        this.ringInterval = 0;

    }

    private void tickBell(Minecraft client) {
        if (this.ringTicksRemaining <= 0) {
            return;
        }

        if (this.ringInterval <= 0) {
            client.getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.BELL_BLOCK, 1.0F)
            );
            this.ringInterval = RING_INTERVAL_TICKS;
        }

        this.ringInterval--;
        this.ringTicksRemaining--;
    }

    private static boolean containsWholeUsername(String text, String username) {
        int fromIndex = 0;
        while (fromIndex <= text.length() - username.length()) {
            int index = text.indexOf(username, fromIndex);
            if (index < 0) {
                return false;
            }

            int end = index + username.length();
            boolean validLeft = index == 0 || !isUsernameCharacter(text.charAt(index - 1));
            boolean validRight = end == text.length() || !isUsernameCharacter(text.charAt(end));

            if (validLeft && validRight) {
                return true;
            }
            fromIndex = index + 1;
        }
        return false;
    }

    private static boolean isUsernameCharacter(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= '0' && c <= '9')
                || c == '_';
    }
}
