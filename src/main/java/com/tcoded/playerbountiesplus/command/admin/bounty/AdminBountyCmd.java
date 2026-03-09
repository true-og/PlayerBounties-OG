package com.tcoded.playerbountiesplus.command.admin.bounty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

import net.trueog.utilitiesog.UtilitiesOG;

public class AdminBountyCmd {

    public static boolean handleCmd(PlayerBountiesOG plugin, CommandSender sender, Command cmd, String label,
            String[] args)
    {

        if (args.length < 2) {

            final String noAction = plugin.getLang().getColored("command.bounty.no-action");
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), noAction);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, noAction);

            }

            return true;

        }

        final String action = args[1].toLowerCase();
        return switch (action) {

            case "set" -> AdminBountySetCmd.handleCmd(plugin, sender, cmd, label, args);
            case "add" -> AdminBountyAddCmd.handleCmd(plugin, sender, cmd, label, args);
            case "remove" -> AdminBountyRemoveCmd.handleCmd(plugin, sender, cmd, label, args);
            case "delete" -> AdminBountyDeleteCmd.handleCmd(plugin, sender, cmd, label, args);
            case "get" -> AdminBountyGetCmd.handleCmd(plugin, sender, cmd, label, args);
            default -> {

                final String invalidAction = plugin.getLang().getColored("command.bounty.invalid-action");
                if (!(sender instanceof Player)) {

                    UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), invalidAction);

                } else {

                    UtilitiesOG.trueogMessage((Player) sender, invalidAction);

                }

                yield true;

            }

        };

    }

    public static List<String> onTabComplete(CommandSender sender, String[] args) {

        List<String> suggestions = Collections.emptyList();

        if (args.length == 2) {

            suggestions = Arrays.asList("set", "add", "remove", "delete", "get");

        } else if (args.length > 2) {

            final String sub = args[1].toLowerCase();
            switch (sub) {

                case "add" -> suggestions = AdminBountyAddCmd.onTabComplete(sender, null, null, args);
                case "remove" -> suggestions = AdminBountyRemoveCmd.onTabComplete(sender, null, null, args);
                case "delete" -> suggestions = AdminBountyDeleteCmd.onTabComplete(sender, null, null, args);
                case "get" -> suggestions = AdminBountyGetCmd.onTabComplete(sender, null, null, args);
                case "set" -> suggestions = AdminBountySetCmd.onTabComplete(sender, null, null, args);

            }

        }

        final String input = args[args.length - 1].toLowerCase();
        return suggestions.stream().filter(opt -> opt.toLowerCase().startsWith(input)).collect(Collectors.toList());

    }

}