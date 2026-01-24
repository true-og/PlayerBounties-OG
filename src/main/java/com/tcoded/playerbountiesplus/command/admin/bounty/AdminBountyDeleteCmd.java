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

public class AdminBountyDeleteCmd {

    private static final String PERMISSION = "playerbountiesplus.command.admin.bounty.delete";

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

        if (args.length < 3) {

            sender.sendMessage(plugin.getLang().getColored("command.admin.bounty.delete.missing-args"));
            return true;

        }

        final String playerName = args[2];
        final OfflinePlayer target = plugin.getServer().getOfflinePlayer(playerName);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {

            sender.sendMessage(plugin.getLang().getColored("command.admin.bounty.delete.player-not-found"));
            return true;

        }

        final UUID uuid = target.getUniqueId();
        final BountyDataManager m = plugin.getBountyDataManager();
        m.removeBounty(uuid);
        m.saveBountiesAsync();

        sender.sendMessage(plugin.getLang().getColored("command.admin.bounty.delete.success").content()
                .replace("{target}", target.getName()));
        return true;

    }

    @Nullable
    public static List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args)
    {

        if (args.length == 3) {

            // Suggest all online player names for the username
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());

        }

        return Collections.emptyList();

    }

}