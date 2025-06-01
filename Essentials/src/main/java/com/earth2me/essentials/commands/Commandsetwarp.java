package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.api.IWarps;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import net.ess3.api.TranslatableException;
import net.essentialsx.api.v2.events.WarpModifyEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;

import java.util.Collection;

public class Commandsetwarp extends EssentialsCommand {
    public Commandsetwarp() {
        super("setwarp");
    }

    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        if (args.length == 0) {
            throw new NotEnoughArgumentsException();
        }

        if (NumberUtil.isInt(args[0]) || args[0].isEmpty()) {
            throw new TranslatableException("invalidWarpName");
        }

        checkWarpLimit(user, args[0]);

        final IWarps warps = ess.getWarps();
        Location warpLoc = null;

        try {
            warpLoc = warps.getWarp(args[0]);
        } catch (final WarpNotFoundException ignored) {
        }
        if (warpLoc == null) {
            final WarpModifyEvent event = new WarpModifyEvent(user, args[0], null, user.getLocation(), WarpModifyEvent.WarpModifyCause.CREATE);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            warps.setWarp(user, args[0], user.getLocation());
        } else if (user.isAuthorized("essentials.warp.overwrite." + StringUtil.safeString(args[0]))) {
            final WarpModifyEvent event = new WarpModifyEvent(user, args[0], warpLoc, user.getLocation(), WarpModifyEvent.WarpModifyCause.UPDATE);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            warps.setWarp(user, args[0], user.getLocation());
        } else {
            throw new TranslatableException("warpOverwrite");
        }
        user.sendTl("warpSet", args[0]);
    }

    private void checkWarpLimit(final User user, final String name) throws Exception {
        if (!user.isAuthorized("essentials.setwarp.unlimited")) {
            final int limit = ess.getSettings().getWarpLimit(user);
            final Collection<String> warps = ess.getWarps().getList();
            int count = 0;
            for (String warp : warps) {
                if (ess.getWarps().getLastOwner(warp).equals(user.getUUID())) {
                    count++;
                }
            }

            if (count >= limit) {
                if (warps.contains(name) && ess.getWarps().getLastOwner(name).equals(user.getUUID())) {
                    return;
                }
                throw new TranslatableException("maxWarps", ess.getSettings().getWarpLimit(user));
            }
        }
    }

}
