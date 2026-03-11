package com.tcoded.playerbountiesplus.command.bounty;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.manager.BountyDataManager;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.trueog.diamondbankog.DiamondBankException.EconomyDisabledException;
import net.trueog.diamondbankog.DiamondBankException.InsufficientFundsException;
import net.trueog.diamondbankog.DiamondBankException.InvalidPlayerException;
import net.trueog.diamondbankog.DiamondBankException.PlayerNotOnlineException;
import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import net.trueog.utilitiesog.UtilitiesOG;

public class BountyAddCmd {

    public static boolean handleCmd(PlayerBountiesOG plugin, CommandSender sender, Command cmd, String cmdName,
            String[] args)
    {

        if (!(sender instanceof Player player)) {

            UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), "&cERROR: Only players can use this command.");
            return true;

        }

        if (!sender.hasPermission("playerbountiesog.command.bounty.add")) {

            final String noPerm = plugin.getLang().getColored("command.no-permission");
            final String noPermDetailed = plugin.getLang().getColored("command.no-permission-detailed")
                    .replace("{no-permission-msg}", noPerm)
                    .replace("{permission}", "playerbountiesog.command.bounty.add");
            UtilitiesOG.trueogMessage(player, noPermDetailed);

            return true;

        }

        if (args.length < 3) {

            UtilitiesOG.trueogMessage(player, plugin.getLang().getColored("command.bounty.add.missing-args"));
            return true;

        }

        final String targetNameArg = args[1];
        final double amount;
        try {

            amount = parseAmount(args[2]);

        } catch (NumberFormatException numberFormatException) {

            UtilitiesOG.trueogMessage(player, plugin.getLang().getColored("command.bounty.add.amount-nan"));
            return true;

        }

        final OfflinePlayer target = Bukkit.getOfflinePlayer(targetNameArg);
        if (target.getUniqueId() == null) {

            UtilitiesOG.trueogMessage(player, plugin.getLang().getColored("command.bounty.add.player-not-found"));
            return true;

        }

        return addBounty(plugin, player, target.getUniqueId(), target.getName(), amount, true);

    }

    public static boolean addBounty(PlayerBountiesOG plugin, Player setter, UUID targetUuid, String targetName,
            double parsedAmount, boolean saveData)
    {

        final String safeTargetName = StringUtils.defaultIfBlank(targetName,
                Bukkit.getOfflinePlayer(targetUuid).getName());
        if (safeTargetName == null || safeTargetName.isBlank()) {

            UtilitiesOG.trueogMessage(setter, plugin.getLang().getColored("command.bounty.add.player-not-found"));
            return true;

        }

        final DiamondBankAPIJava diamondBankAPI = resolveDiamondBankApi(plugin);

        final double minimum = plugin.getConfig().getDouble("bounty-minimum", 1.0);
        if (parsedAmount < minimum) {

            UtilitiesOG.trueogMessage(setter, plugin.getLang().getColored("command.bounty.set.under-minimum")
                    .replace("{minimum}", String.valueOf(minimum)));
            return true;

        }

        final double maximum = plugin.getConfig().getDouble("bounty-maximum", 1000000.0);
        if (parsedAmount > maximum) {

            UtilitiesOG.trueogMessage(setter, plugin.getLang().getColored("command.bounty.set.over-maximum")
                    .replace("{maximum}", String.valueOf(maximum)));
            return true;

        }

        final long addedShards = diamondBankAPI.diamondsToShards((float) parsedAmount);
        if (addedShards <= 0L) {

            UtilitiesOG.trueogMessage(setter, plugin.getLang().getColored("command.bounty.set.internal-invalid-value"));
            return true;

        }

        if (!chargeSetter(plugin, setter, diamondBankAPI, safeTargetName, addedShards)) {

            return true;

        }

        final BountyDataManager bountyDataManager = plugin.getBountyDataManager();
        final long existingShards = diamondBankAPI.diamondsToShards((float) bountyDataManager.getBounty(targetUuid));
        final long totalShards = existingShards + addedShards;
        final double totalBounty = Double.parseDouble(diamondBankAPI.shardsToDiamonds(totalShards));

        bountyDataManager.setBounty(targetUuid, totalBounty);

        final LuckPerms luckPerms = resolveLuckPerms(plugin);
        final String setterDisplay = formatLuckPermsDisplay(luckPerms, setter);
        final String targetDisplay = resolveLuckPermsDisplayName(luckPerms, targetUuid, safeTargetName);
        final String addedDisplay = diamondBankAPI.shardsToDiamonds(addedShards);
        final String totalDisplay = diamondBankAPI.shardsToDiamonds(totalShards);

        UtilitiesOG.trueogMessage(setter, plugin.getLang().getColored("command.bounty.add.success")
                .replace("{amount}", addedDisplay).replace("{target}", targetDisplay).replace("{total}", totalDisplay));

        if (plugin.getConfig().getBoolean("bounty-placed-announce", true)) {

            final String announce = plugin.getLang().getColored("command.bounty.add.announce")
                    .replace("{amount}", addedDisplay).replace("{target}", targetDisplay)
                    .replace("{total}", totalDisplay).replace("{player}", setterDisplay);

            Bukkit.getOnlinePlayers().forEach(
                    player -> UtilitiesOG.trueogMessage(player, PlayerBountiesOG.getPrefix() + "&a" + announce));

        }

        if (saveData) {

            bountyDataManager.saveBountiesAsync();

        }

        return true;

    }

    private static boolean chargeSetter(PlayerBountiesOG plugin, Player setter, DiamondBankAPIJava diamondBankAPI,
            String targetName, long addedShards)
    {

        try {

            diamondBankAPI.consumeFromPlayer(setter.getUniqueId(), addedShards,
                    "Player " + setter.getName() + " contributed " + diamondBankAPI.shardsToDiamonds(addedShards)
                            + " Diamonds toward a bounty on " + targetName + ".",
                    "Plugin: PlayerBounties-OG");

            return true;

        } catch (EconomyDisabledException economyDisabledException) {

            PlayerBountiesOG.disableSelf(
                    "[PlayerBounties-OG] DiamondBank-OG economy disabled: " + economyDisabledException.getMessage());
            return false;

        } catch (InvalidPlayerException invalidPlayerException) {

            plugin.getLogger().warning("Invalid player for consumeFromPlayer: " + setter.getName());
            UtilitiesOG.trueogMessage(setter,
                    "&cERROR: your player account could not be found by DiamondBank-OG. Contact an administrator!");
            return false;

        } catch (InsufficientFundsException insufficientFundsException) {

            UtilitiesOG.trueogMessage(setter, plugin.getLang().getColored("command.bounty.set.not-enough-money"));
            return false;

        }
        // TODO: Remove PlayerNotOnlineException catch block after next DiamondBank-OG
        // update.
        catch (PlayerNotOnlineException temporaryPlayerNotOnlineError) {

            plugin.getLogger().warning("Player " + setter.getName()
                    + " not online when attempting to consumeFromPlayer. This error is expected to be removed in the next DiamondBank-OG update.");
            temporaryPlayerNotOnlineError.printStackTrace();
            return false;

        }

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

        if (provider == null) {

            return null;

        }

        return provider.getProvider();

    }

    private static String resolveLuckPermsDisplayName(LuckPerms luckPerms, UUID playerId, String playerName) {

        final String safeName = StringUtils.defaultIfBlank(playerName, "Unknown Player");
        if (luckPerms == null) {

            return "&f" + safeName;

        }

        final User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) {

            return "&f" + safeName;

        }

        final CachedMetaData meta = user.getCachedData().getMetaData();
        final String prefix = StringUtils.trim(StringUtils.defaultString(meta.getPrefix()).replace('§', '&'));
        final String suffix = StringUtils.trim(StringUtils.defaultString(meta.getSuffix()).replace('§', '&'));
        final boolean metaContainsName = containsVisibleName(prefix, safeName) || containsVisibleName(suffix, safeName);

        final StringBuilder out = new StringBuilder();
        if (!prefix.isBlank()) {

            out.append(prefix).append(' ');

        }

        if (!metaContainsName) {

            out.append(safeName);

        }

        if (!suffix.isBlank()) {

            out.append(' ').append(suffix);

        }

        return sanitizeDisplay(out.toString(), safeName);

    }

    private static String formatLuckPermsDisplay(LuckPerms luckPerms, Player player) {

        return resolveLuckPermsDisplayName(luckPerms, player.getUniqueId(), player.getName());

    }

    private static String sanitizeDisplay(String formatted, String fallbackName) {

        final String cleaned = StringUtils.trimToEmpty(formatted).replaceAll("(?i)(?:\\s*(?:<reset>|[&§]r))+$", "")
                .replaceAll("\\s*>$", "");

        if (cleaned.isBlank()) {

            return fallbackName;

        }

        return cleaned;

    }

    private static boolean containsVisibleName(String text, String playerName) {

        if (text == null || playerName == null || playerName.isBlank()) {

            return false;

        }

        final String normalizedText = StringUtils.lowerCase(UtilitiesOG.stripFormatting(text), Locale.ROOT);
        final String normalizedName = StringUtils.lowerCase(playerName, Locale.ROOT);

        return StringUtils.contains(normalizedText, normalizedName);

    }

    @Nullable
    public static List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length == 2) {

            return sender.getServer().getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> StringUtils.startsWith(StringUtils.lowerCase(name), StringUtils.lowerCase(args[1])))
                    .collect(Collectors.toList());

        } else if (args.length == 3) {

            return Collections.singletonList("<amount>");

        }

        return Collections.emptyList();

    }

}