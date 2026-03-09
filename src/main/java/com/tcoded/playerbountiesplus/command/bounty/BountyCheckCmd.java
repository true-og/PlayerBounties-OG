package com.tcoded.playerbountiesplus.command.bounty;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

        final Player target = plugin.getServer().getPlayerExact(playerNameArg);

        if (target == null) {

            final String playerNotFoundMessage = plugin.getLang().getColored("command.bounty.check.player-not-found");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), playerNotFoundMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, playerNotFoundMessage);

            }

            return true;

        }

        final UUID playerUUID = target.getUniqueId();

        final BountyDataManager bountyDataManager = plugin.getBountyDataManager();
        final boolean hasBounty = bountyDataManager.hasBounty(playerUUID);

        if (hasBounty) {

            // Confirmation.
            final double bounty = bountyDataManager.getBounty(playerUUID);
            final String bountyFoundMessage = plugin.getLang().getColored("command.bounty.check.bounty-found")
                    .replace("{target}", target.getName()).replace("{bounty}", Double.toString(bounty));
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), bountyFoundMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, bountyFoundMessage);

            }

        } else {

            final String noBountyFoundMessage = plugin.getLang().getColored("command.bounty.check.no-bounty")
                    .replace("{target}", target.getName());
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), noBountyFoundMessage);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, noBountyFoundMessage);

            }

        }

        return true;

    }

    @Nullable
    public static List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length == 2) {

            // Suggest online player names for the username.
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> StringUtils.startsWith(StringUtils.lowerCase(name), StringUtils.lowerCase(args[1])))
                    .collect(Collectors.toList());

        }

        return Collections.emptyList();

    }

}