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

public class AdminBountyRemoveCmd {

    private static final String PERMISSION = "playerbountiesplus.command.admin.bounty.remove";

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

        if (args.length < 4) {

            final String missingArguments = plugin.getLang().getColored("command.admin.bounty.remove.missing-args");

            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), missingArguments);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, missingArguments);

            }

            return true;

        }

        final String playerName = args[2];
        final int amount;
        try {

            amount = Integer.parseInt(args[3]);

        } catch (NumberFormatException numberFormatException) {

            final String notNumberMessage = plugin.getLang().getColored("command.admin.bounty.remove.amount-nan");

            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), notNumberMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, notNumberMessage);

            }

            return true;

        }

        final OfflinePlayer target = plugin.getServer().getOfflinePlayer(playerName);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {

            final String playerNotFound = plugin.getLang().getColored("command.admin.bounty.remove.player-not-found");

            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), playerNotFound);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, playerNotFound);

            }

            return true;

        }

        final UUID uuid = target.getUniqueId();
        final BountyDataManager m = plugin.getBountyDataManager();
        final double current = m.getBounty(uuid);
        double total = current - amount;
        if (total <= 0) {

            m.removeBounty(uuid);
            total = 0;

        } else {

            m.setBounty(uuid, total);

        }

        m.saveBountiesAsync();

        final String bountyRemovedMessage = plugin.getLang().getColored("command.admin.bounty.remove.success")
                .replace("{target}", target.getName()).replace("{amount}", String.valueOf(amount))
                .replace("{total}", String.valueOf(total));
        if (!(sender instanceof Player)) {

            UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), bountyRemovedMessage);

        } else {

            UtilitiesOG.trueogMessage((Player) sender, bountyRemovedMessage);

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

        } else if (args.length == 4) {

            // Suggest a placeholder for the amount.
            return Collections.singletonList("<amount>");

        }

        return Collections.emptyList();

    }

}