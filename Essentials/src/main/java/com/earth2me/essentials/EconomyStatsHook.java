package com.earth2me.essentials;

import net.ess3.api.IUser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.UUID;

public final class EconomyStatsHook {
    private static volatile Object api;

    private EconomyStatsHook() {
    }

    private static Object api() {
        Object a = api;
        if (a != null) return a;
        synchronized (EconomyStatsHook.class) {
            if (api != null) return api;
            try {
                Plugin jes = Bukkit.getPluginManager().getPlugin("JavaEconomyStats");
                if (jes != null) {
                    Class<?> c = jes.getClass().getClassLoader().loadClass("ru.minecraft.javaEconomyStats.api.PublicApi");
                    api = c.getMethod("get").invoke(null);
                }
            } catch (Throwable t) {
            }
            return api;
        }
    }

    public static void onCommandCost(final IUser user, final String command, final BigDecimal cost) {
        if (cost == null || cost.signum() <= 0 || user == null) {
            return;
        }
        Object a = api();
        if (a == null) {
            return;
        }
        try {
            double amount = cost.doubleValue();
            double after = user.getMoney().doubleValue();
            double before = after + amount;
            UUID uuid = user.getUUID();
            String cmd = command != null && command.startsWith("/") ? command.substring(1) : command;
            String meta = "{\"command\":\"" + cmd + "\"}";
            a.getClass().getMethod("recordStr", String.class, UUID.class, double.class, boolean.class,
                    double.class, double.class, String.class, String.class, String.class, String.class)
              .invoke(a, "Essentials", uuid, amount, false, before, after, "SINK", "command", cmd, meta);
        } catch (Throwable ignored) {
        }
    }
}
