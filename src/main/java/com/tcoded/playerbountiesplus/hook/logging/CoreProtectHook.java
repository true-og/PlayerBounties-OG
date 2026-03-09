package com.tcoded.playerbountiesplus.hook.logging;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.Plugin;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

public class CoreProtectHook {

    private final CoreProtectAPI coreProtectAPI;

    public CoreProtectHook(Plugin plugin) {

        this.coreProtectAPI = findCoreProtect(plugin);

    }

    public boolean isEnabled() {

        return this.coreProtectAPI != null;

    }

    public boolean logPlacement(String user, BlockState blockState) {

        if (this.coreProtectAPI == null || user == null || blockState == null) {

            return false;

        }

        return this.coreProtectAPI.logPlacement(user, blockState.getLocation(), blockState.getType(),
                blockState.getBlockData());

    }

    public boolean logRemoval(String user, BlockState blockState) {

        if (this.coreProtectAPI == null || user == null || blockState == null) {

            return false;

        }

        return this.coreProtectAPI.logRemoval(user, blockState.getLocation(), blockState.getType(),
                blockState.getBlockData());

    }

    public boolean logContainerTransaction(String user, Location location) {

        if (this.coreProtectAPI == null || user == null || location == null) {

            return false;

        }

        return this.coreProtectAPI.logContainerTransaction(user, location);

    }

    public boolean logInteraction(String user, Location location) {

        if (this.coreProtectAPI == null || user == null || location == null) {

            return false;

        }

        return this.coreProtectAPI.logInteraction(user, location);

    }

    public boolean hasPlaced(String user, Block block, int time, int offset) {

        if (this.coreProtectAPI == null || user == null || block == null) {

            return false;

        }

        return this.coreProtectAPI.hasPlaced(user, block, time, offset);

    }

    private static CoreProtectAPI findCoreProtect(Plugin plugin) {

        if (plugin == null) {

            return null;

        }

        final Plugin coreProtectPlugin = plugin.getServer().getPluginManager().getPlugin("CoreProtect");
        if (!(coreProtectPlugin instanceof CoreProtect coreProtect)) {

            return null;

        }

        final CoreProtectAPI api = coreProtect.getAPI();
        if (api == null || !api.isEnabled() || api.APIVersion() < 11) {

            return null;

        }

        return api;

    }

}
