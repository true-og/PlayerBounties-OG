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

import net.trueog.utilitiesog.UtilitiesOG;

public class AdminBountyDeleteCmd {

    private static final String PERMISSION = "playerbountiesog.command.admin.bounty.delete";

    public static boolean handleCmd(PlayerBountiesOG plugin, CommandSender sender, Command cmd, String label,
            String[] args)
    {

        if (!sender.hasPermission(PERMISSION)) {

            final String noPerm = plugin.getLang().getColored("command.no-permission");
            final String noPermDetailed = plugin.getLang().getColored("command.no-permission-detailed")
                    .replace("{no-permission-msg}", noPerm).replace("{permission}", PERMISSION);
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), noPermDetailed);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, noPermDetailed);

            }

            return true;

        }

        if (args.length < 3) {

            final String missingArguments = plugin.getLang().getColored("command.admin.bounty.delete.missing-args");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), missingArguments);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, missingArguments);

            }

            return true;

        }

        final String playerName = args[2];
        final OfflinePlayer target = plugin.getServer().getOfflinePlayer(playerName);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {

            final String playerNotFound = plugin.getLang().getColored("command.admin.bounty.delete.player-not-found");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), playerNotFound);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, playerNotFound);

            }

            return true;

        }

        final UUID uuid = target.getUniqueId();
        final BountyDataManager bountyData = plugin.getBountyDataManager();

        bountyData.removeBounty(uuid);
        bountyData.saveBountiesAsync();

        final String bountyDeletedMessage = plugin.getLang().getColored("command.admin.bounty.delete.success")
                .replace("{target}", target.getName());
        if (!(sender instanceof Player)) {

            UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), bountyDeletedMessage);

        } else {

            UtilitiesOG.trueogMessage((Player) sender, bountyDeletedMessage);

        }

        return true;

    }

    @Nullable
    public static List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args)
    {

        if (args.length == 3) {

            // Suggest all online player names for the username.
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());

        }

        return Collections.emptyList();

    }

}