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

            sender.sendMessage(plugin.getLang().getColored("command.bounty.set.missing-args"));
            return true;

        }

        if (sender instanceof Player && !sender.hasPermission("playerbountiesplus.command.bounty.set")) {

            final String noPerm = plugin.getLang().getColored("command.no-permission").content();
            final String noPermDetailed = plugin.getLang().getColored("command.no-permission-detailed").content()
                    .replace("{no-permission-msg}", noPerm)
                    .replace("{permission}", "playerbountiesplus.command.bounty.set");
            sender.sendMessage(noPermDetailed);
            return true;

        }

        final String playerNameArg = args[1];
        final double parsedAmount;
        try {

            parsedAmount = Integer.parseInt(args[2]);

        } catch (NumberFormatException error) {

            sender.sendMessage(plugin.getLang().getColored("command.bounty.set.amount-nan"));
            return true;

        }

        final Player target = plugin.getServer().getPlayerExact(playerNameArg);
        if (target == null) {

            sender.sendMessage(plugin.getLang().getColored("command.bounty.set.player-not-found"));
            return true;

        }

        // Check limits
        final double minimum = plugin.getConfig().getDouble("bounty-minimum", 1.0);
        if (parsedAmount < minimum) {

            sender.sendMessage(plugin.getLang().getColored("command.bounty.set.under-minimum").content()
                    .replace("{minimum}", String.valueOf(minimum)));
            return true;

        }

        final double maximum = plugin.getConfig().getDouble("bounty-maximum", 1000000.0);
        if (parsedAmount > maximum) {

            sender.sendMessage(plugin.getLang().getColored("command.bounty.set.over-maximum").content()
                    .replace("{maximum}", String.valueOf(maximum)));
            return true;

        }

        // Apply bounty multiplier
        double amount = parsedAmount * plugin.getConfig().getDouble("bounty-multiplier", 1.0);

        // Trigger bounty set event
        final BountySetEvent event = new BountySetEvent(sender instanceof Player ? (Player) sender : null, target,
                (float) amount);
        plugin.getServer().getPluginManager().callEvent(event);
        amount = event.getAmount();

        // Check if event was cancelled
        if (event.isCancelled()) {

            sender.sendMessage(plugin.getLang().getColored("command.bounty.set.cancelled"));
            return true;

        }

        // Sanity check final amount
        if (amount <= 0) {

            sender.sendMessage(plugin.getLang().getColored("command.bounty.set.internal-invalid-value"));
            return true;

        }

        // Check economy. Only charge if sender is a player.
        if (sender instanceof Player player) {

            final boolean allowed = plugin.getEcoHook().takeEco(player, target, amount, false);

            if (!allowed) {

                sender.sendMessage(plugin.getLang().getColored("command.bounty.set.not-enough-money"));
                return true;

            }

        }

        final UUID playerUUID = target.getUniqueId();

        // Calculate total bounty including previous bounties
        final BountyDataManager bountyDataManager = plugin.getBountyDataManager();
        final int bountyAlreadyPresent = bountyDataManager.getBounty(playerUUID);
        final int chargeAndBountyAmount = (int) amount;
        final int totalBounty = chargeAndBountyAmount + bountyAlreadyPresent;
        bountyDataManager.setBounty(playerUUID, totalBounty);

        // Confirmation
        sender.sendMessage(plugin.getLang().getColored("command.bounty.set.success").content()
                .replace("{bounty}", String.valueOf(chargeAndBountyAmount)).replace("{total}", String.valueOf(totalBounty))
                .replace("{target}", target.getName()).replace("{player}", sender.getName()));

        // Announce
        final String extra;
        if (bountyAlreadyPresent == 0) {

            extra = "";

        } else {

            extra = plugin.getLang().getColored("command.bounty.set.announce-extra").content().replace("{total}",
                    String.valueOf(totalBounty));

        }

        if (plugin.getConfig().getBoolean("bounty-placed-announce", true)) {

            Bukkit.getOnlinePlayers()
                    .forEach((Player player) -> UtilitiesOG.trueogMessage(player,
                            plugin.getLang().getColored("command.bounty.set.announce").content()
                                    .replace("{bounty}", String.valueOf(chargeAndBountyAmount)).replace("{target}", target.getName())
                                    .replace("{player}", sender.getName()).replace("{extra}", extra)));

        }

        bountyDataManager.saveBountiesAsync();

        return true;

    }

    @Nullable
    public static List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length == 2) {

            // Suggest online player names for the username
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());

        } else if (args.length == 3) {

            // Suggest a placeholder for the amount
            return Collections.singletonList("<amount>");

        }

        return Collections.emptyList();

    }

}
