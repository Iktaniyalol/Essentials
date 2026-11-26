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

    public static void onEcoGive(final UUID target, final double amount, final double before, final double after, final String by) {
        record(target, amount, true, before, after, "SOURCE", "eco-admin", "eco-give", by);
    }

    public static void onEcoTake(final UUID target, final double amount, final double before, final double after, final String by) {
        record(target, amount, false, before, after, "SINK", "eco-admin", "eco-take", by);
    }

    public static void onEcoSet(final UUID target, final double delta, final double before, final double after, final String by) {
        if (delta == 0) {
            return;
        }
        record(target, delta > 0 ? delta : -delta, delta > 0, before, after,
                delta > 0 ? "SOURCE" : "SINK", "eco-admin", "eco-set", by);
    }

    public static void onPay(final UUID from, final UUID to, final double amount, final double before, final double after,
                             final String fromName, final String toName) {
        if (amount <= 0) {
            return;
        }
        Object a = api();
        if (a == null) {
            return;
        }
        try {
            String meta = "{\"from\":\"" + fromName + "\",\"to\":\"" + toName + "\"}";
            a.getClass().getMethod("recordTransferStr", String.class, UUID.class, UUID.class, double.class,
                    double.class, double.class, String.class, String.class, String.class, String.class)
              .invoke(a, "Essentials", from, to, amount, before, after, "TRANSFER", "pay", "pay", meta);
        } catch (Throwable ignored) {
        }
    }

    public static void onApiChange(final User user, final double before, final double after, final String op) {
        if (user == null) {
            return;
        }
        double amount = after - before;
        if (amount == 0) {
            return;
        }
        Object a = api();
        if (a == null) {
            return;
        }
        try {
            Boolean wrapped = (Boolean) a.getClass().getMethod("isVaultIngress").invoke(null);
            if (Boolean.TRUE.equals(wrapped)) {
                return;
            }
            UUID uuid = user.getBase().getUniqueId();
            String meta = "{\"op\":\"" + op + "\",\"caller\":\"" + resolveCaller() + "\"}";
            a.getClass().getMethod("recordStr", String.class, UUID.class, double.class, boolean.class,
                    double.class, double.class, String.class, String.class, String.class, String.class)
              .invoke(a, "Essentials", uuid, Math.abs(amount), amount > 0, before, after, "UNKNOWN", "api-economy", op, meta);
        } catch (Throwable ignored) {
        }
    }

    private static String resolveCaller() {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        for (int i = 2; i < st.length; i++) {
            String cls = st[i].getClassName();
            if (cls.startsWith("com.earth2me.essentials")
                    || cls.startsWith("net.ess3")
                    || cls.startsWith("ru.minecraft.javaEconomyStats")
                    || cls.startsWith("java.")
                    || cls.startsWith("jdk.")
                    || cls.startsWith("sun.")
                    || cls.startsWith("org.bukkit")
                    || cls.startsWith("io.papermc")) {
                continue;
            }
            return cls;
        }
        return "unknown";
    }

    private static void record(final UUID target, final double amount, final boolean deposit, final double before,
                               final double after, final String type, final String category, final String reason,
                               final String by) {
        Object a = api();
        if (a == null) {
            return;
        }
        try {
            String meta = "{\"by\":\"" + by + "\"}";
            a.getClass().getMethod("recordStr", String.class, UUID.class, double.class, boolean.class,
                    double.class, double.class, String.class, String.class, String.class, String.class)
              .invoke(a, "Essentials", target, amount, deposit, before, after, type, category, reason, meta);
        } catch (Throwable ignored) {
        }
    }
}
