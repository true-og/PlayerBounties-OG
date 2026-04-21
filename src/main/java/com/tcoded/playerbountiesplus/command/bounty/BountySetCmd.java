package com.tcoded.playerbountiesplus.command.bounty;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.event.BountySetEvent;
import com.tcoded.playerbountiesplus.manager.BountyDataManager;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.trueog.diamondbankog.api.DiamondBankAPIJava;
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

        if (sender instanceof Player && !sender.hasPermission("playerbountiesog.command.bounty.set")) {

            final String noPerm = plugin.getLang().getColored("command.no-permission");
            final String noPermDetailed = plugin.getLang().getColored("command.no-permission-detailed")
                    .replace("{no-permission-msg}", noPerm)
                    .replace("{permission}", "playerbountiesog.command.bounty.set");
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

            parsedAmount = parseAmount(args[2]);

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
        final DiamondBankAPIJava diamondBankAPI = resolveDiamondBankApi(plugin);
        final long newBountyShards = diamondBankAPI.diamondsToShards(amount);
        final long existingBountyShards = diamondBankAPI
                .diamondsToShards((float) bountyDataManager.getBounty(playerUUID));
        final long totalBountyShards = newBountyShards + existingBountyShards;
        final double totalBounty = Double.parseDouble(diamondBankAPI.shardsToDiamonds(totalBountyShards));
        bountyDataManager.setBounty(playerUUID, totalBounty);

        // Announcement.
        final String extra;
        if (existingBountyShards == 0L) {

            extra = "";

        } else {

            extra = plugin.getLang().getColored("command.bounty.set.announce-extra").replace("{total}",
                    diamondBankAPI.shardsToDiamonds(totalBountyShards));

        }

        if (plugin.getConfig().getBoolean("bounty-placed-announce", true)) {

            final LuckPerms luckPerms = resolveLuckPerms(plugin);
            final String setterDisplay = sender instanceof Player player ? formatLuckPermsDisplay(luckPerms, player)
                    : sender.getName();
            final String targetDisplay = formatLuckPermsDisplay(luckPerms, target);
            final String bountyDisplay = diamondBankAPI.shardsToDiamonds(newBountyShards);

            Bukkit.getOnlinePlayers()
                    .forEach((Player player) -> UtilitiesOG.trueogMessage(player,
                            PlayerBountiesOG.getPrefix() + "&a"
                                    + plugin.getLang().getColored("command.bounty.set.announce")
                                            .replace("{bounty}", bountyDisplay).replace("{target}", targetDisplay)
                                            .replace("{player}", setterDisplay).replace("{extra}", extra)));

        }

        bountyDataManager.saveBountiesAsync();

        return true;

    }

    private static double parseAmount(String amountInput) {

        if (!amountInput.matches("^\\d+(?:\\.\\d)?$")) {

            throw new NumberFormatException("Invalid amount format");

        }

        return Double.parseDouble(amountInput);

    }

    private static DiamondBankAPIJava resolveDiamondBankApi(PlayerBountiesOG plugin) {

        final RegisteredServiceProvider<DiamondBankAPIJava> provider = plugin.getServer().getServicesManager()
                .getRegistration(DiamondBankAPIJava.class);

        if (provider == null) {

            throw new IllegalStateException("DiamondBank-OG API is null");

        }

        return provider.getProvider();

    }

    private static LuckPerms resolveLuckPerms(PlayerBountiesOG plugin) {

        final RegisteredServiceProvider<LuckPerms> provider = plugin.getServer().getServicesManager()
                .getRegistration(LuckPerms.class);

        return provider == null ? null : provider.getProvider();

    }

    private static String formatLuckPermsDisplay(LuckPerms luckPerms, Player player) {

        final String prefix = stripLeadingReset(stripTrailingReset(getLuckPermsPrefixLegacy(luckPerms, player)));
        final String leadingColorCodes = extractLeadingColorCodes(prefix);

        if (prefix.isBlank()) {

            return player.getName().replaceAll("\\s*>$", "");

        }

        if (leadingColorCodes.isBlank()) {

            return (prefix + " " + player.getName()).replaceAll("\\s*>$", "");

        }

        return (prefix + " " + leadingColorCodes + player.getName()).replaceAll("\\s*>$", "");

    }

    private static String getLuckPermsPrefixLegacy(LuckPerms luckPerms, Player player) {

        if (luckPerms == null) {

            return "";

        }

        final User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {

            return "";

        }

        final CachedMetaData meta = user.getCachedData().getMetaData();
        final String prefix = meta.getPrefix();

        if (prefix == null || prefix.isBlank()) {

            return "";

        }

        return StringUtils.trim(prefix).replace('§', '&');

    }

    private static String stripTrailingReset(String input) {

        if (input == null || input.isBlank()) {

            return "";

        }

        return StringUtils.trim(input.replaceAll("(?i)(?:\\s*(?:<reset>|[&§]r))+$", ""));

    }

    private static String stripLeadingReset(String input) {

        if (input == null || input.isBlank()) {

            return "";

        }

        return StringUtils.trim(input.replaceFirst("(?i)^(?:\\s*(?:<reset>|[&§]r))+", ""));

    }

    private static String extractLeadingColorCodes(String input) {

        if (input == null || input.length() < 2) {

            return "";

        }

        final StringBuilder out = new StringBuilder();
        for (int i = 0; i < input.length() - 1; i++) {

            final char current = input.charAt(i);
            if (current != '&' && current != '§') {

                break;

            }

            out.append(current).append(input.charAt(i + 1));
            i++;

        }

        return out.toString();

    }

    @Nullable
    public static List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length == 2) {

            // Suggest online player names for the username.
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> StringUtils.startsWith(StringUtils.lowerCase(name), StringUtils.lowerCase(args[1])))
                    .collect(Collectors.toList());

        } else if (args.length == 3) {

            // Suggest a placeholder for the amount.
            return Collections.singletonList("<amount>");

        }

        return Collections.emptyList();

    }

}