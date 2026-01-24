package com.tcoded.playerbountiesplus.command.bounty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

public class BountyTopCmd {

    public static boolean handleCmd(PlayerBountiesOG plugin, CommandSender sender, Command cmd, String cmdName,
            String[] args)
    {

        final Set<Map.Entry<UUID, Integer>> bountiesSet = plugin.getBountyDataManager().getBounties().entrySet();
        final List<Map.Entry<UUID, Integer>> bounties = new ArrayList<>(bountiesSet);

        bounties.sort((a, b) -> b.getValue() - a.getValue());

        final StringBuilder strb = new StringBuilder();
        strb.append(plugin.getLang().getColored("command.bounty.top.top-10") + "\n");

        final int bountiesSize = bounties.size();
        final int maxInList = Math.min(10, bountiesSize);

        if (bountiesSize > 0) {

            for (int i = 0; i < maxInList; i++) {

                strb.append("&7");
                strb.append(" - ");
                final Map.Entry<UUID, Integer> entry = bounties.get(i);
                strb.append(plugin.getServer().getOfflinePlayer(entry.getKey()).getName());
                strb.append(": ");
                strb.append(entry.getValue());
                strb.append('\n');

            }

        } else {

            strb.append(plugin.getLang().getColored("command.bounty.top.no-bounties") + "\n");

        }

        String message = strb.toString();
        message = message.substring(0, message.length() - 1); // remove last \n
        sender.sendMessage(message);

        return true;

    }

}