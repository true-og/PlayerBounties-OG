package com.tcoded.playerbountiesplus.command.admin.bounty;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.manager.BountyDataManager;

public class AdminBountySetCmd {

    private static final String PERMISSION = "playerbountiesplus.command.admin.bounty.set";

    public static boolean handleCmd(PlayerBountiesOG plugin, CommandSender sender, Command cmd, String label,
            String[] args)
    {

        if (!sender.hasPermission(PERMISSION)) {

            final String noPerm = plugin.getLang().getColored("command.no-permission").content();
            final String noPermDetailed = plugin.getLang().getColored("command.no-permission-detailed").content()
                    .replace("{no-permission-msg}", noPerm).replace("{permission}", PERMISSION);
            sender.sendMessage(noPermDetailed);
            return true;

        }

        if (args.length < 4) {

            sender.sendMessage(plugin.getLang().getColored("command.admin.bounty.set.missing-args"));
            return true;

        }

        final String playerName = args[2];
        final int amount;
        try {

            amount = Integer.parseInt(args[3]);

        } catch (NumberFormatException ex) {

            sender.sendMessage(plugin.getLang().getColored("command.admin.bounty.set.amount-nan"));
            return true;

        }

        final OfflinePlayer target = plugin.getServer().getOfflinePlayer(playerName);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {

            sender.sendMessage(plugin.getLang().getColored("command.admin.bounty.set.player-not-found"));
            return true;

        }

        final UUID uuid = target.getUniqueId();
        final BountyDataManager m = plugin.getBountyDataManager();
        m.setBounty(uuid, amount);
        m.saveBountiesAsync();

        sender.sendMessage(plugin.getLang().getColored("command.admin.bounty.set.success").content()
                .replace("{target}", target.getName()).replace("{bounty}", String.valueOf(amount)));
        return true;

    }

    @Nullable
    public static List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args)
    {

        if (args.length == 3) {

            // Suggest all online player names for the username
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());

        } else if (args.length == 4) {

            // Suggest a placeholder for the amount
            return Collections.singletonList("<amount>");

        }

        return Collections.emptyList();

    }

}