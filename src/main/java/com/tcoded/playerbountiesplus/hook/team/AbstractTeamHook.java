package com.tcoded.playerbountiesplus.hook.team;

import java.util.List;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

public abstract class AbstractTeamHook implements TeamHook {

    protected PlayerBountiesOG plugin;
    protected JavaPlugin teamPlugin;

    public AbstractTeamHook(PlayerBountiesOG plugin, Plugin teamPlugin) {

        this.plugin = plugin;
        this.teamPlugin = (JavaPlugin) teamPlugin;

    }

    @Override
    public String getPluginName() {

        return this.teamPlugin.getName();

    }

    @Override
    public String getAuthor() {

        final List<String> authors = this.teamPlugin.getPluginMeta().getAuthors();

        return authors.isEmpty() ? "N/A" : authors.get(0);

    }

    @Override
    public String[] getAuthors() {

        final List<String> authors = this.teamPlugin.getPluginMeta().getAuthors();
        if (authors.isEmpty()) {

            return new String[0];

        }

        return authors.toArray(new String[0]);

    }

}