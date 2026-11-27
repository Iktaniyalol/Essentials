package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.TextInput;
import com.earth2me.essentials.textreader.TextPager;
import org.bukkit.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Commandrules extends EssentialsCommand {
    public Commandrules() {
        super("rules");
    }

    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (sender.isPlayer()) {
            ess.getUser(sender.getPlayer()).setDisplayNick();
        }

        final TextPager pager = new TextPager(new KeywordReplacer(new TextInput(sender, "rules", true, ess), sender, ess));
        pager.showPage(args.length > 0 ? args[0] : null, args.length > 1 ? args[1] : null, commandLabel, sender);
    }

    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        try {
            return new ArrayList<>(new TextInput(sender, "rules", true, ess).getChapters());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
