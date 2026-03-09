package com.tcoded.playerbountiesplus.hook.team;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

public interface TeamHook {

    static AbstractTeamHook findTeamHook(PlayerBountiesOG plugin) {

        final PluginManager pluginManager = plugin.getServer().getPluginManager();

        // RoinujNosde's SimpleClans: TrueOG Network branch.
        // https://github.com/true-og/SimpleClans
        // License: GPLv3
        // (https://raw.githubusercontent.com/true-og/SimpleClans/refs/heads/master/LICENSE)
        final Plugin simpleClansPlugin = pluginManager.getPlugin("SimpleClans");
        if (simpleClansPlugin != null && simpleClansPlugin.isEnabled()) {

            return new SimpleClansHook(plugin, simpleClansPlugin);

        }

        return null;

    }

    String getPluginName();

    String getAuthor();

    String[] getAuthors();

    boolean isFriendly(Player player1, Player player2);

}