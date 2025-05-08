package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.AdventureUtil;
import com.earth2me.essentials.utils.DescParseTickFormat;
import com.google.common.collect.Lists;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class Commandptime extends EssentialsLoopCommand {
    private static final List<String> getAliases = Arrays.asList("get", "list", "show", "display");

    public Commandptime() {
        super("ptime");
    }

    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length == 0 || getAliases.contains(args[0].toLowerCase())) {
            // /ptime get player || /ptime get *
            if (args.length > 1) {
                if ("*".equals(args[1]) || "**".equals(args[1])) {
                    sender.sendTl("pTimePlayers");
                }
                loopOnlinePlayersConsumer(server, sender, false, true, args[1], player -> getUserTime(sender, player));
                throw new NoChargeException();
            }

            //ptime get
            if (sender.isPlayer() && args.length == 1) {
                getUserTime(sender, sender.getUser());
                throw new NoChargeException();
            }

            // Default to showing the player times of all online users for console when no arguments are provided
            Iterable<User> onlineUsers = ess.getOnlineUsers();
            sender.sendTl("pTimePlayers");
            onlineUsers.forEach(player -> getUserTime(sender, player));
        }

        if (args.length > 1 && !sender.isAuthorized("essentials.ptime.others") && !args[1].equalsIgnoreCase(sender.getSelfSelector())) {
            sender.sendTl("pTimeOthersPermission");
            throw new NoChargeException();
        }

        String time = args[0];
        final boolean fixed = time.startsWith("@");
        if (fixed) {
            time = time.substring(1);
        }

        final Long ticks;
        if (DescParseTickFormat.meansReset(time)) {
            ticks = null;
        } else {
            try {
                ticks = DescParseTickFormat.parse(time);
            } catch (final NumberFormatException e) {
                throw new NotEnoughArgumentsException(e);
            }
        }

        final StringJoiner joiner = new StringJoiner(", ");
        loopOnlinePlayersConsumer(server, sender, false, true, args.length > 1 ? args[1] : sender.getSelfSelector(), player -> {
            setUserTime(player, ticks, !fixed);
            joiner.add(player.getName());
        });

        if (ticks == null) {
            sender.sendTl("pTimeReset", joiner.toString());
            throw new NoChargeException();
        }

        final String formattedTime = DescParseTickFormat.format(ticks);
        sender.sendTl(fixed ? "pTimeSetFixed" : "pTimeSet", AdventureUtil.parsed(formattedTime), joiner.toString());
    }

    public void getUserTime(final CommandSource sender, final IUser user) {
        if (user == null) {
            return;
        }

        if (user.getBase().getPlayerTimeOffset() == 0) {
            sender.sendTl("pTimeNormal", user.getName());
            return;
        }

        final String time = DescParseTickFormat.format(user.getBase().getPlayerTime());
        sender.sendTl(user.getBase().isPlayerTimeRelative() ? "pTimeCurrent" : "pTimeCurrentFixed", user.getName(), AdventureUtil.parsed(time));
    }

    private void setUserTime(final User user, final Long ticks, final Boolean relative) {
        if (ticks == null) {
            user.getBase().resetPlayerTime();
        } else {
            final World world = user.getWorld();
            long time = user.getBase().getPlayerTime();
            time -= time % 24000;
            time += 24000 + ticks;
            if (relative) {
                time -= world.getTime();
            }
            user.getBase().setPlayerTime(time, relative);
        }
    }

    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
        final User user = ess.getUser(sender.getPlayer());

        if (args.length == 1) {
            return Lists.newArrayList("get", "reset", "sunrise", "day", "morning", "noon", "afternoon", "sunset", "night", "midnight");
        } else if (args.length == 2 && (getAliases.contains(args[0]) || user == null || user.isAuthorized("essentials.ptime.others"))) {
            return getPlayers(server, sender);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected void updatePlayer(final Server server, final CommandSource sender, final User user, final String[] args) {
    }
}
