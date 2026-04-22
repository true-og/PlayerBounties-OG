package com.tcoded.playerbountiesplus.command.bounty;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.manager.BountyDataManager;

import net.trueog.utilitiesog.UtilitiesOG;

public class BountyCheckCmd {

    public static boolean handleCmd(PlayerBountiesOG plugin, CommandSender sender, Command cmd, String cmdName,
            String[] args)
    {

        if (args.length < 2) {

            final String noPlayerSpecifiedMessage = plugin.getLang()
                    .getColored("command.bounty.check.no-player-specified");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), noPlayerSpecifiedMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, noPlayerSpecifiedMessage);

            }

            return true;

        }

        final String playerNameArg = args[1];

        final UUID targetUuid;
        final String targetName;

        final Player onlineTarget = plugin.getServer().getPlayerExact(playerNameArg);
        if (onlineTarget != null) {

            targetUuid = onlineTarget.getUniqueId();
            targetName = onlineTarget.getName();

        } else {

            final OfflinePlayer offlineTarget = resolveOfflineTarget(playerNameArg);
            if (offlineTarget == null) {

                final String playerNotFoundMessage = plugin.getLang()
                        .getColored("command.bounty.check.player-not-found");
                if (!(sender instanceof Player)) {

                    UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), playerNotFoundMessage);

                } else {

                    UtilitiesOG.trueogMessage((Player) sender, playerNotFoundMessage);

                }

                return true;

            }

            targetUuid = offlineTarget.getUniqueId();
            targetName = StringUtils.defaultIfBlank(offlineTarget.getName(), playerNameArg);

        }

        final BountyDataManager bountyDataManager = plugin.getBountyDataManager();
        final boolean hasBounty = bountyDataManager.hasBounty(targetUuid);

        if (hasBounty) {

            final double bounty = bountyDataManager.getBounty(targetUuid);
            final String formattedBounty = plugin.getBountyHeadFormatter().formatDiamonds(bounty) + " Diamonds";
            final String bountyFoundMessage = plugin.getLang().getColored("command.bounty.check.bounty-found")
                    .replace("{target}", targetName).replace("{bounty}", formattedBounty);
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), bountyFoundMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, bountyFoundMessage);

            }

        } else {

            final String noBountyFoundMessage = plugin.getLang().getColored("command.bounty.check.no-bounty")
                    .replace("{target}", targetName);
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), noBountyFoundMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, noBountyFoundMessage);

            }

        }

        return true;

    }

    private static OfflinePlayer resolveOfflineTarget(String name) {

        for (OfflinePlayer candidate : Bukkit.getOfflinePlayers()) {

            if (StringUtils.equalsIgnoreCase(candidate.getName(), name)) {

                return candidate;

            }

        }

        @SuppressWarnings("deprecation")
        final OfflinePlayer byName = Bukkit.getOfflinePlayerIfCached(name);

        return byName;

    }

    @Nullable
    public static List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length != 2) {

            return Collections.emptyList();

        }

        final String prefix = StringUtils.lowerCase(args[1]);
        final PlayerBountiesOG plugin = PlayerBountiesOG.getInstance();
        final BountyDataManager bountyDataManager = plugin == null ? null : plugin.getBountyDataManager();

        final Stream<String> onlineNames = sender.getServer().getOnlinePlayers().stream().map(Player::getName);
        final Stream<String> offlineBountyNames = bountyDataManager == null ? Stream.empty()
                : bountyDataManager.getBounties().keySet().stream().map(Bukkit::getOfflinePlayer)
                        .map(OfflinePlayer::getName).filter(name -> name != null && !name.isBlank());

        return Stream.concat(onlineNames, offlineBountyNames).distinct()
                .filter(name -> StringUtils.startsWith(StringUtils.lowerCase(name), prefix))
                .collect(Collectors.toList());

    }

}