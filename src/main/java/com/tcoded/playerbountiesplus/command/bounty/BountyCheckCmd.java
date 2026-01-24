package com.tcoded.playerbountiesplus.command.bounty;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.manager.BountyDataManager;

public class BountyCheckCmd {

    public static boolean handleCmd(PlayerBountiesOG plugin, CommandSender sender, Command cmd, String cmdName,
            String[] args)
    {

        if (args.length < 2) {

            sender.sendMessage(plugin.getLang().getColored("command.bounty.check.no-player-specified"));
            return true;

        }

        final String playerNameArg = args[1];

        final Player target = plugin.getServer().getPlayerExact(playerNameArg);

        if (target == null) {

            sender.sendMessage(plugin.getLang().getColored("command.bounty.check.player-not-found"));
            return true;

        }

        final UUID playerUUID = target.getUniqueId();

        final BountyDataManager bountyDataManager = plugin.getBountyDataManager();
        final boolean hasBounty = bountyDataManager.hasBounty(playerUUID);

        if (hasBounty) {

            // Confirmation
            int bounty = bountyDataManager.getBounty(playerUUID);
            sender.sendMessage(plugin.getLang().getColored("command.bounty.check.bounty-found").content()
                    .replace("{target}", target.getName()).replace("{bounty}", Integer.toString(bounty)));

        } else {

            sender.sendMessage(plugin.getLang().getColored("command.bounty.check.no-bounty").content()
                    .replace("{target}", target.getName()));

        }

        return true;

    }

    @Nullable
    public static List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length == 2) {

            // Suggest online player names for the username
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());

        }

        return Collections.emptyList();

    }

}