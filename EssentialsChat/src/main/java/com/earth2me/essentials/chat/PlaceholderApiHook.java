package com.earth2me.essentials.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public final class PlaceholderApiHook {
    private static boolean present = false;

    private PlaceholderApiHook() {
    }

    public static void init(final boolean present) {
        PlaceholderApiHook.present = present;
    }

    public static String apply(final Player player, final String text) {
        if (!present) {
            return text;
        }
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
