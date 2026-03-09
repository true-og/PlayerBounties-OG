package com.tcoded.playerbountiesplus.command.bounty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.gui.MainBountyGui;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
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

            final String rankName = resolveLuckPermsRankName(plugin.getLuckPerms(), entry.getKey());

            entries.add(new MainBountyGui.BountyGuiEntry(entry.getKey(), targetName, rankName, entry.getValue()));

        }

        return entries;

    }

    private static String resolveLuckPermsRankName(LuckPerms luckPerms, UUID playerId) {

        if (luckPerms == null) {

            return "&7Unranked";

        }

        final User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) {

            return "&7Unranked";

        }

        final CachedMetaData meta = user.getCachedData().getMetaData();
        final String prefix = meta.getPrefix();
        if (prefix != null && !prefix.isBlank()) {

            return StringUtils.trim(prefix).replace('§', '&');

        }

        final String primaryGroup = user.getPrimaryGroup();
        if (primaryGroup == null || primaryGroup.isBlank()) {

            return "&7Unranked";

        }

        return "&7" + primaryGroup;

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
        message = StringUtils.substring(message, 0, message.length() - 1);

        if (!(sender instanceof Player)) {

            UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), message);

        } else {

            UtilitiesOG.trueogMessage((Player) sender, message);

        }

    }

}