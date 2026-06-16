package com.tcoded.playerbountiesplus.util;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

public final class BountyWorldUtil {

    private static final String BOUNTY_WORLD_WHITELIST_KEY = "bounty-world-whitelist";
    private static final List<String> DEFAULT_BOUNTY_WORLD_WHITELIST = List.of("world", "world_nether",
            "world_the_end");

    private BountyWorldUtil() {

    }

    public static boolean isBountyWorldAllowed(PlayerBountiesOG plugin, Player player) {

        return player != null && isBountyWorldAllowed(plugin, player.getWorld());

    }

    public static boolean isBountyWorldAllowed(PlayerBountiesOG plugin, World world) {

        return world != null && isBountyWorldAllowed(plugin, world.getName());

    }

    public static boolean isBountyWorldAllowed(PlayerBountiesOG plugin, String worldName) {

        final List<String> worldWhitelist = getBountyWorldWhitelist(plugin);
        final boolean hasConfiguredWorld = worldWhitelist.stream().anyMatch(world -> !StringUtils.isBlank(world));

        return !hasConfiguredWorld || worldWhitelist.stream()
                .anyMatch(world -> StringUtils.equalsIgnoreCase(StringUtils.trim(world), worldName));

    }

    public static String formatBountyWorldWhitelist(PlayerBountiesOG plugin) {

        final List<String> worldWhitelist = getBountyWorldWhitelist(plugin).stream().map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());

        return worldWhitelist.isEmpty() ? "every world" : StringUtils.join(worldWhitelist, ", ");

    }

    private static List<String> getBountyWorldWhitelist(PlayerBountiesOG plugin) {

        if (plugin.getConfig().contains(BOUNTY_WORLD_WHITELIST_KEY)) {

            return plugin.getConfig().getStringList(BOUNTY_WORLD_WHITELIST_KEY);

        }

        return DEFAULT_BOUNTY_WORLD_WHITELIST;

    }

}
