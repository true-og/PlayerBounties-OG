package com.tcoded.playerbountiesplus.command.admin;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

public class PlayerBountiesPlusReloadCmd implements TabCompleter {

    private static final String RELOAD_PERMISSION = "playerbountiesplus.command.playerbountiesplus.reload";

    public static boolean handleCmd(PlayerBountiesOG plugin, CommandSender sender, Command cmd, String cmdName,
            String[] args)
    {

        if (!sender.hasPermission(RELOAD_PERMISSION)) {

            final String noPerm = plugin.getLang().getColored("command.no-permission").content();
            final String noPermDetailed = plugin.getLang().getColored("command.no-permission-detailed").content()
                    .replace("{no-permission-msg}", noPerm).replace("{permission}", RELOAD_PERMISSION);
            sender.sendMessage(noPermDetailed);
            return true;

        }

        plugin.reloadConfig();
        plugin.reloadLang();

        sender.sendMessage(plugin.getLang().getColored("command.admin.reload.reloaded"));

        return true;

    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args)
    {

        // No additional arguments for reload command
        return Collections.emptyList();

    }

}