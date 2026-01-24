package com.tcoded.playerbountiesplus.hook.placeholder;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPIHook extends PlaceholderExpansion implements PlaceholderHook {

    private final PlayerBountiesOG pbpPlugin;

    public PlaceholderAPIHook(PlayerBountiesOG pbpPlugin, Plugin papiPlugin) {

        this.pbpPlugin = pbpPlugin;
        if (!(papiPlugin instanceof PlaceholderAPIPlugin)) {

            throw new IllegalArgumentException("Plugin is not PlaceholderAPI");

        }

    }

    @Override
    public @NotNull String getIdentifier() {

        return pbpPlugin.getName().toLowerCase();

    }

    @Override
    public @NotNull String getAuthor() {

        return pbpPlugin.getPluginMeta().getAuthors().get(0);

    }

    @Override
    public @NotNull String getVersion() {

        return pbpPlugin.getPluginMeta().getVersion();

    }

    @Override
    public boolean persist() {

        return true;

    }

    @Override
    public void enable() {

        this.register();

    }

    @Override
    public void disable() {

        this.unregister();

    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {

        if (params.equals("bounty")) {

            return String.valueOf(pbpPlugin.getBountyDataManager().getBounty(player.getUniqueId()));

        } else if (params.equals("hasbounty")) {

            return String.valueOf(pbpPlugin.getBountyDataManager().hasBounty(player.getUniqueId()));

        }

        return "INVALID-OPTION";

    }

}