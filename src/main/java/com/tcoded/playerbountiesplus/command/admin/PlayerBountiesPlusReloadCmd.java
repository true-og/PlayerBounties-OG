package com.tcoded.playerbountiesplus.command.admin;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

import net.trueog.utilitiesog.UtilitiesOG;

public class PlayerBountiesPlusReloadCmd implements TabCompleter {

    private static final String RELOAD_PERMISSION = "playerbountiesog.command.admin.reload";

    public static boolean handleCmd(PlayerBountiesOG plugin, CommandSender sender, Command cmd, String cmdName,
            String[] args)
    {

        if (!sender.hasPermission(RELOAD_PERMISSION)) {

            final String noPerm = plugin.getLang().getColored("command.no-permission");
            final String noPermDetailed = plugin.getLang().getColored("command.no-permission-detailed")
                    .replace("{no-permission-msg}", noPerm).replace("{permission}", RELOAD_PERMISSION);
            if (!(sender instanceof Player)) {

                UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), noPermDetailed);

            } else {

                UtilitiesOG.trueogMessage((Player) sender, noPermDetailed);

            }

            return true;

        }

        plugin.reloadConfig();
        plugin.reloadLang();

        final String reloadMessage = plugin.getLang().getColored("command.admin.reload.reloaded");

        if (!(sender instanceof Player)) {

            UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(), reloadMessage);

        } else {

            UtilitiesOG.trueogMessage((Player) sender, reloadMessage);

        }

        return true;

    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args)
    {

        // No additional arguments for reload command.
        return Collections.emptyList();

    }

}