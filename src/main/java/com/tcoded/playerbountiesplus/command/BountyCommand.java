package com.tcoded.playerbountiesplus.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.command.bounty.BountyCheckCmd;
import com.tcoded.playerbountiesplus.command.bounty.BountyAddCmd;
import com.tcoded.playerbountiesplus.command.bounty.BountySetCmd;
import com.tcoded.playerbountiesplus.command.bounty.BountyTopCmd;

import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import net.trueog.utilitiesog.UtilitiesOG;

public class BountyCommand implements CommandExecutor, TabCompleter {

    private final PlayerBountiesOG plugin;
    private final DiamondBankAPIJava diamondBankAPI;

    public BountyCommand(PlayerBountiesOG plugin, DiamondBankAPIJava diamondBankAPI) {

        this.plugin = plugin;
        this.diamondBankAPI = diamondBankAPI;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {

        if (!(sender instanceof Player)) {

            UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), "&cERROR: Only players can use this command.");

            return true;

        }

        if (args.length < 1) {

            if (StringUtils.equalsIgnoreCase(cmdName, "bounties")) {

                return BountyTopCmd.handleCmd(plugin, diamondBankAPI, sender, cmd, cmdName, args);

            }

            final String noAction = plugin.getLang().getColored("command.bounty.no-action");
            UtilitiesOG.trueogMessage((Player) sender, noAction);

            return true;

        }

        final String action = StringUtils.lowerCase(args[0]);
        switch (action) {

            case "set" -> {

                return BountySetCmd.handleCmd(plugin, sender, cmd, cmdName, args);

            }

            case "add" -> {

                return BountyAddCmd.handleCmd(plugin, sender, cmd, cmdName, args);

            }

            case "top" -> {

                return BountyTopCmd.handleCmd(plugin, diamondBankAPI, sender, cmd, cmdName, args);

            }

            case "check" -> {

                return BountyCheckCmd.handleCmd(plugin, sender, cmd, cmdName, args);

            }
            default -> {

                // If "/bounty" is run without a valid subcommand, treat it as "/bounty top".
                return BountyTopCmd.handleCmd(plugin, diamondBankAPI, sender, cmd, cmdName, args);

            }

        }

    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args)
    {

        if (args.length == 1) {

            return Lists.newArrayList("set", "add", "top", "check").stream()
                    .filter(action -> StringUtils.startsWith(action, StringUtils.lowerCase(args[0])))
                    .collect(Collectors.toList());

        } else if (args.length > 1) {

            final String subCommand = StringUtils.lowerCase(args[0]);
            return switch (subCommand) {

                case "set" -> BountySetCmd
                        .onTabComplete(sender, args).stream().filter(suggestion -> StringUtils
                                .startsWith(StringUtils.lowerCase(suggestion), StringUtils.lowerCase(args[1])))
                        .collect(Collectors.toList());
                case "add" -> BountyAddCmd
                        .onTabComplete(sender, args).stream().filter(suggestion -> StringUtils
                                .startsWith(StringUtils.lowerCase(suggestion), StringUtils.lowerCase(args[1])))
                        .collect(Collectors.toList());
                case "check" -> BountyCheckCmd
                        .onTabComplete(sender, args).stream().filter(suggestion -> StringUtils
                                .startsWith(StringUtils.lowerCase(suggestion), StringUtils.lowerCase(args[1])))
                        .collect(Collectors.toList());
                case "top" -> Collections.emptyList();
                default -> Collections.emptyList();

            };

        }

        return Collections.emptyList();

    }

}
