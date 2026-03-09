package com.tcoded.playerbountiesplus.command.bounty;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.event.BountySetEvent;
import com.tcoded.playerbountiesplus.manager.BountyDataManager;

import net.trueog.utilitiesog.UtilitiesOG;

public class BountySetCmd {

    public static boolean handleCmd(PlayerBountiesOG plugin, CommandSender sender, Command cmd, String cmdName,
            String[] args)
    {

        if (args.length < 3) {

            final String missingArgumentsMessage = plugin.getLang().getColored("command.bounty.set.missing-args");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), missingArgumentsMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, missingArgumentsMessage);

            }

            return true;

        }

        if (sender instanceof Player && !sender.hasPermission("playerbountiesplus.command.bounty.set")) {

            final String noPerm = plugin.getLang().getColored("command.no-permission");
            final String noPermDetailed = plugin.getLang().getColored("command.no-permission-detailed")
                    .replace("{no-permission-msg}", noPerm)
                    .replace("{permission}", "playerbountiesplus.command.bounty.set");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), noPermDetailed);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, noPermDetailed);

            }

            return true;

        }

        final String playerNameArg = args[1];
        final double parsedAmount;
        try {

            parsedAmount = Integer.parseInt(args[2]);

        } catch (NumberFormatException numberFormatException) {

            final String notNumberMessage = plugin.getLang().getColored("command.bounty.set.amount-nan");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), notNumberMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, notNumberMessage);

            }

            return true;

        }

        final Player target = plugin.getServer().getPlayerExact(playerNameArg);
        if (target == null) {

            final String playerNotFoundMessage = plugin.getLang().getColored("command.bounty.set.player-not-found");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), playerNotFoundMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, playerNotFoundMessage);

            }

            return true;

        }

        // Check limits.
        final double minimum = plugin.getConfig().getDouble("bounty-minimum", 1.0);
        if (parsedAmount < minimum) {

            final String underMinimumMessage = plugin.getLang().getColored("command.bounty.set.under-minimum")
                    .replace("{minimum}", String.valueOf(minimum));
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), underMinimumMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, underMinimumMessage);

            }

            return true;

        }

        final double maximum = plugin.getConfig().getDouble("bounty-maximum", 1000000.0);
        if (parsedAmount > maximum) {

            final String overMaximumMessage = plugin.getLang().getColored("command.bounty.set.over-maximum")
                    .replace("{maximum}", String.valueOf(maximum));
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), overMaximumMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, overMaximumMessage);

            }

            return true;

        }

        // Apply bounty multiplier.
        double amount = parsedAmount * plugin.getConfig().getDouble("bounty-multiplier", 1.0);

        // Trigger bounty set event.
        final BountySetEvent event = new BountySetEvent(sender instanceof Player ? (Player) sender : null, target,
                (float) amount);
        plugin.getServer().getPluginManager().callEvent(event);
        amount = event.getAmount();

        // Check if event was cancelled.
        if (event.isCancelled()) {

            final String cancelledBounty = plugin.getLang().getColored("command.bounty.set.cancelled");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), cancelledBounty);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, cancelledBounty);

            }

            return true;

        }

        // Sanity check final amount.
        if (amount <= 0) {

            final String invalidValueMessage = plugin.getLang().getColored("command.bounty.set.internal-invalid-value");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), invalidValueMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, invalidValueMessage);

            }

            return true;

        }

        // Check economy. Only charge if sender is a player.
        if (sender instanceof Player player) {

            final boolean allowed = plugin.getEcoHook().takeEco(player, target, amount);

            if (!allowed) {

                final String notEnoughDiamondsMessage = plugin.getLang()
                        .getColored("command.bounty.set.not-enough-money");
                if (!(sender instanceof Player)) {

                    UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), notEnoughDiamondsMessage);

                } else {

                    UtilitiesOG.trueogMessage((Player) sender, notEnoughDiamondsMessage);

                }

                return true;

            }

        }

        final UUID playerUUID = target.getUniqueId();

        // Calculate total bounty including previous bounties.
        final BountyDataManager bountyDataManager = plugin.getBountyDataManager();
        final double bountyAlreadyPresent = bountyDataManager.getBounty(playerUUID);
        final double chargeAndBountyAmount = (int) amount;
        final double totalBounty = chargeAndBountyAmount + bountyAlreadyPresent;
        bountyDataManager.setBounty(playerUUID, totalBounty);

        // Confirmation.
        final String bountySetSuccessMessage = plugin.getLang().getColored("command.bounty.set.success")
                .replace("{bounty}", String.valueOf(chargeAndBountyAmount))
                .replace("{total}", String.valueOf(totalBounty)).replace("{target}", target.getName())
                .replace("{player}", sender.getName());
        if (!(sender instanceof Player)) {

            UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), bountySetSuccessMessage);

        } else {

            UtilitiesOG.trueogMessage((Player) sender, bountySetSuccessMessage);

        }

        // Announcement.
        final String extra;
        if (bountyAlreadyPresent == 0) {

            extra = "";

        } else {

            extra = plugin.getLang().getColored("command.bounty.set.announce-extra").replace("{total}",
                    String.valueOf(totalBounty));

        }

        if (plugin.getConfig().getBoolean("bounty-placed-announce", true)) {

            Bukkit.getOnlinePlayers()
                    .forEach((Player player) -> UtilitiesOG.trueogMessage(player,
                            plugin.getLang().getColored("command.bounty.set.announce")
                                    .replace("{bounty}", String.valueOf(chargeAndBountyAmount))
                                    .replace("{target}", target.getName()).replace("{player}", sender.getName())
                                    .replace("{extra}", extra)));

        }

        bountyDataManager.saveBountiesAsync();

        return true;

    }

    @Nullable
    public static List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length == 2) {

            // Suggest online player names for the username.
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());

        } else if (args.length == 3) {

            // Suggest a placeholder for the amount.
            return Collections.singletonList("<amount>");

        }

        return Collections.emptyList();

    }

}