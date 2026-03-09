package com.tcoded.playerbountiesplus.command.admin;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.util.LangUtil;

import net.trueog.utilitiesog.UtilitiesOG;

public class PlayerBountiesPlusVersionCmd {

    private static final String VERSION_PERMISSION = "playerbountiesog.command.admin.version";

    public static boolean handleCmd(PlayerBountiesOG plugin, CommandSender sender, Command cmd, String cmdName,
            String[] args)
    {

        final LangUtil lang = plugin.getLang();
        final String version;
        if (sender.hasPermission(VERSION_PERMISSION)) {

            version = "v" + plugin.getPluginMeta().getVersion();

        } else {

            version = "\n" + lang.getColored("command.admin.version.no-permission");

        }

        if (sender instanceof Player player) {

            UtilitiesOG.trueogMessage(player, PlayerBountiesOG.getPrefix()
                    + "&6 Authors: &aTechnicallyCoded, NotAlexNoyle &6Version: &e" + version);

        } else {

            UtilitiesOG.logToConsole(PlayerBountiesOG.getPrefix(),
                    "Authors: TechnicallyCoded, NotAlexNoyle Version: " + version);

        }

        return true;

    }

    public static List<String> onTabComplete(CommandSender sender, String[] args) {

        // No sub-commands for version, return empty list.
        return Collections.emptyList();

    }

}