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

public class AdminBountySetCmd {

    private static final String PERMISSION = "playerbountiesog.command.admin.bounty.set";

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

            final String missingArguments = plugin.getLang().getColored("command.admin.bounty.set.missing-args");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), missingArguments);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, missingArguments);

            }

            return true;

        }

        final String playerName = args[2];
        final double amount;
        try {

            amount = parseAmount(args[3]);

        } catch (NumberFormatException numberFormatException) {

            final String notNumberMessage = plugin.getLang().getColored("command.admin.bounty.set.amount-nan");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), notNumberMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, notNumberMessage);

            }

            return true;

        }

        final OfflinePlayer target = plugin.getServer().getOfflinePlayer(playerName);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {

            final String playerNotFound = plugin.getLang().getColored("command.admin.bounty.set.player-not-found");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), playerNotFound);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, playerNotFound);

            }

            return true;

        }

        final UUID uuid = target.getUniqueId();
        final BountyDataManager dataManager = plugin.getBountyDataManager();

        dataManager.setBounty(uuid, amount);
        dataManager.saveBountiesAsync();

        final String bountySetMessage = plugin.getLang().getColored("command.admin.bounty.set.success")
                .replace("{target}", target.getName()).replace("{bounty}", String.valueOf(amount));

        if (!(sender instanceof Player)) {

            UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), bountySetMessage);

        } else {

            UtilitiesOG.trueogMessage((Player) sender, bountySetMessage);

        }

        return true;

    }

    private static double parseAmount(String amountInput) {

        if (!amountInput.matches("^\\d+(?:\\.\\d)?$")) {

            throw new NumberFormatException("Invalid amount format");

        }

        return Double.parseDouble(amountInput);

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