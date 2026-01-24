package com.tcoded.playerbountiesplus.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.command.admin.PlayerBountiesPlusReloadCmd;
import com.tcoded.playerbountiesplus.command.admin.PlayerBountiesPlusVersionCmd;
import com.tcoded.playerbountiesplus.command.admin.bounty.AdminBountyCmd;

import net.trueog.utilitiesog.UtilitiesOG;

public class PlayerBountiesPlusAdminCmd implements CommandExecutor, TabCompleter {

    private static final ArrayList<String> completions = Lists.newArrayList("reload", "version", "bounty", "help");

    private final PlayerBountiesOG plugin;

    public PlayerBountiesPlusAdminCmd(PlayerBountiesOG plugin) {

        this.plugin = plugin;

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args)
    {

        if (args.length < 1) {

            sendHelpMsg(sender);
            return true;

        }

        final String arg0Lower = args[0].toLowerCase();
        return switch (arg0Lower) {

            case "reload" -> PlayerBountiesPlusReloadCmd.handleCmd(plugin, sender, command, label, args);
            case "version" -> PlayerBountiesPlusVersionCmd.handleCmd(plugin, sender, command, label, args);
            case "bounty" -> AdminBountyCmd.handleCmd(plugin, sender, command, label, args);
            default -> {

                sendHelpMsg(sender);
                yield true;

            }

        };

    }

    private void sendHelpMsg(CommandSender sender) {

        final String help = """
                &f[&bPlayerBountiesPlus&f] &7PlayerBountiesPlus by TechnicallyCoded
                &fAdmin Commands:
                &f/pbp reload &7- Reload the configuration file and the messages.
                &f/pbp version &7- Check the version of the plugin.
                &f/pbp bounty set <player> <amount> &7- Set a player's bounty.
                &f/pbp bounty add <player> <amount> &7- Add to a player's bounty.
                &f/pbp bounty remove <player> <amount> &7- Remove from a player's bounty.
                &f/pbp bounty delete <player> &7- Delete a player's bounty.
                &f/pbp help &7- Get this message.\
                """;

        if (sender instanceof Player player) {

            sender.sendMessage(UtilitiesOG.trueogExpand(help, player));

        } else {

            sender.sendMessage(UtilitiesOG.stripFormatting(help));

        }

    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args)
    {

        if (args.length == 1) {

            return completions.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());

        } else if (args.length > 1) {

            final String subCommand = args[0].toLowerCase();
            return switch (subCommand) {

                case "bounty" -> AdminBountyCmd.onTabComplete(sender, args);
                case "version" -> PlayerBountiesPlusVersionCmd.onTabComplete(sender, args);
                case "reload", "help" -> Collections.emptyList();
                default -> Collections.emptyList();

            };

        }

        return Collections.emptyList();

    }

}