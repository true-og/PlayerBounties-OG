package com.tcoded.playerbountiesplus.hook.team;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

public interface TeamHook {

    static AbstractTeamHook findTeamHook(PlayerBountiesOG plugin) {

        final PluginManager pluginManager = plugin.getServer().getPluginManager();

        // loving11ish - ClansLite -
        // https://www.spigotmc.org/resources/clanslite-1-19-4-support.97163/
        final Plugin clansLitePlugin = pluginManager.getPlugin("ClansLite");
        if (clansLitePlugin != null && clansLitePlugin.isEnabled()) {

            return new ClansLiteHook(plugin, clansLitePlugin);

        }

        // RoinujNosde - SimpleClans -
        // https://www.spigotmc.org/resources/simpleclans.71242/
        final Plugin simpleClansPlugin = pluginManager.getPlugin("SimpleClans");
        if (simpleClansPlugin != null && simpleClansPlugin.isEnabled()) {

            return new SimpleClansHook(plugin, simpleClansPlugin);

        }

        // LlmDl - Towny - https://www.spigotmc.org/resources/towny-advanced.72694/
        Plugin townyPlugin = pluginManager.getPlugin("Towny");
        if (townyPlugin != null && townyPlugin.isEnabled()) {

            return new TownyHook(plugin, townyPlugin);

        }

        // CortezRomeo - ClansPlus - https://github.com/CortezRomeo/ClansPlus
        Plugin clansPlusPlugin = pluginManager.getPlugin("ClansPlus");
        if (clansPlusPlugin != null && clansPlusPlugin.isEnabled()) {

            return new ClansPlusHook(plugin, clansPlusPlugin);

        }

        return null;

    }

    String getPluginName();

    String getAuthor();

    String[] getAuthors();

    boolean isFriendly(Player player1, Player player2);

}