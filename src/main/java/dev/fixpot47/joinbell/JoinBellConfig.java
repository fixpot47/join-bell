package dev.fixpot47.joinbell;

import net.uku3lig.ukulib.config.ConfigManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class JoinBellConfig implements Serializable {
    private static final ConfigManager<JoinBellConfig> MANAGER =
            ConfigManager.createDefault(JoinBellConfig.class, "joinbell");

    private List<String> trackedPlayers = new ArrayList<>();

    public JoinBellConfig() {
    }

    public static JoinBellConfig get() {
        JoinBellConfig config = MANAGER.getConfig();
        if (config.trackedPlayers == null) {
            config.trackedPlayers = new ArrayList<>();
        }
        return config;
    }

    public static void save() {
        MANAGER.saveConfig();
    }

    public List<String> trackedPlayers() {
        return trackedPlayers;
    }

    public boolean addPlayer(String rawName) {
        String name = sanitize(rawName);
        if (name.isEmpty()) {
            return false;
        }

        boolean alreadyExists = trackedPlayers.stream().anyMatch(existing -> existing.equalsIgnoreCase(name));
        if (alreadyExists) {
            return false;
        }

        trackedPlayers.add(name);
        save();
        return true;
    }

    public void removePlayer(String name) {
        trackedPlayers.removeIf(existing -> existing.equalsIgnoreCase(name));
        save();
    }

    public boolean isTracked(String name) {
        return name != null && trackedPlayers.stream().anyMatch(existing -> existing.equalsIgnoreCase(name));
    }

    private static String sanitize(String rawName) {
        if (rawName == null) {
            return "";
        }

        String name = rawName.strip();
        if (name.length() < 3 || name.length() > 16) {
            return "";
        }

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean ok = (c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '_';
            if (!ok) {
                return "";
            }
        }
        return name;
    }
}
