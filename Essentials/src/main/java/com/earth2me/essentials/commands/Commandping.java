package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;

import java.util.Collections;
import java.util.List;

public class Commandping extends EssentialsCommand {
    public Commandping() {
        super("ping");
    }

    @Override
    public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
        if (args.length == 0) {
            user.sendTl("pingSelf", user.getBase().getPing());
            return;
        }
        if (!user.isAuthorized("essentials.ping.others")) {
            throw new TranslatableException("noPerm", "essentials.ping.others");
        }
        final User target = getPlayer(server, user, args, 0);
        user.sendTl("pingOther", target.getName(), target.getBase().getPing());
    }

    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length == 0) {
            throw new NotEnoughArgumentsException();
        }
        final User target = getPlayer(server, sender, args, 0);
        sender.sendTl("pingOther", target.getName(), target.getBase().getPing());
    }

    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
        if (args.length == 1) {
            return getPlayers(sender);
        }
        return Collections.emptyList();
    }
}
