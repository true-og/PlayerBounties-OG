package com.tcoded.playerbountiesplus.command.bounty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.gui.MainBountyGui;

import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import net.trueog.utilitiesog.UtilitiesOG;

public class BountyTopCmd {

    public static boolean handleCmd(PlayerBountiesOG plugin, DiamondBankAPIJava diamondBankAPI, CommandSender sender,
            Command cmd, String cmdName, String[] args)
    {

        if (sender instanceof Player player) {

            new MainBountyGui(plugin, player, () -> buildGuiEntries(plugin), diamondBankAPI).open();

            return true;

        }

        sendTextTopList(plugin, diamondBankAPI, sender);

        return true;

    }

    private static List<MainBountyGui.BountyGuiEntry> buildGuiEntries(PlayerBountiesOG plugin) {

        final Set<Map.Entry<UUID, Double>> bountiesSet = plugin.getBountyDataManager().getBounties().entrySet();
        final List<Map.Entry<UUID, Double>> bounties = new ArrayList<>(bountiesSet);

        bounties.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        final List<MainBountyGui.BountyGuiEntry> entries = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : bounties) {

            if (entry.getValue() == null || entry.getValue() <= 0D) {

                continue;

            }

            final OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(entry.getKey());
            String targetName = offlinePlayer.getName();

            if (targetName == null || targetName.isBlank()) {

                targetName = "Unknown Player";

            }

            entries.add(new MainBountyGui.BountyGuiEntry(entry.getKey(), targetName, entry.getValue()));

        }

        return entries;

    }

    private static void sendTextTopList(PlayerBountiesOG plugin, DiamondBankAPIJava diamondBankAPI,
            CommandSender sender)
    {

        final Set<Map.Entry<UUID, Double>> bountiesSet = plugin.getBountyDataManager().getBounties().entrySet();
        final List<Map.Entry<UUID, Double>> bounties = new ArrayList<>(bountiesSet);

        bounties.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        final StringBuilder strb = new StringBuilder();
        strb.append(plugin.getLang().getColored("command.bounty.top.top-10")).append('\n');

        final int bountiesSize = bounties.size();
        final int maxInList = Math.min(10, bountiesSize);
        if (bountiesSize > 0) {

            for (int i = 0; i < maxInList; i++) {

                final Map.Entry<UUID, Double> entry = bounties.get(i);
                final OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(entry.getKey());

                String targetName = offlinePlayer.getName();
                if (targetName == null || targetName.isBlank()) {

                    targetName = "Unknown Player";

                }

                final String formattedBounty = diamondBankAPI
                        .shardsToDiamonds(diamondBankAPI.diamondsToShards(entry.getValue()));

                strb.append("&7 - ");
                strb.append(targetName);
                strb.append(": ");
                strb.append(formattedBounty);
                strb.append('\n');

            }

        } else {

            strb.append(plugin.getLang().getColored("command.bounty.top.no-bounties")).append('\n');

        }

        String message = strb.toString();
        message = message.substring(0, message.length() - 1);

        if (!(sender instanceof Player)) {

            UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), message);

        } else {

            UtilitiesOG.trueogMessage((Player) sender, message);

        }

    }

}