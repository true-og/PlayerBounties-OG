package com.tcoded.playerbountiesplus.hook.team;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

public interface TeamHook {

    static AbstractTeamHook findTeamHook(PlayerBountiesOG plugin) {

        final PluginManager pluginManager = plugin.getServer().getPluginManager();

        // RoinujNosde's SimpleClans: TrueOG Network branch.
        // https://git.trueog.net/trueog/Unions-OG
        // License: GPLv3
        // (https://git.trueog.net/trueog/Unions-OG/raw/branch/master/LICENSE)
        final Plugin simpleClansPlugin = pluginManager.getPlugin("SimpleClans");
        if (simpleClansPlugin != null && simpleClansPlugin.isEnabled()) {

            return new SimpleClansHook(plugin, simpleClansPlugin);

        }

        // Unions-OG is a soft fork of SimpleClans that preserves the same API
        // and main class but exposes a different plugin name to Bukkit.
        final Plugin unionsPlugin = pluginManager.getPlugin("Unions-OG");
        if (unionsPlugin != null && unionsPlugin.isEnabled()) {

            return new SimpleClansHook(plugin, unionsPlugin);

        }

        return null;

    }

    String getPluginName();

    String getAuthor();

    String[] getAuthors();

    boolean isFriendly(Player player1, Player player2);

}
