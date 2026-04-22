package com.tcoded.playerbountiesplus.util;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;

import com.destroystokyo.paper.profile.PlayerProfile;

import net.kyori.adventure.text.TextComponent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import net.trueog.utilitiesog.UtilitiesOG;

public final class BountyHeadFormatter {

    // LuckPerms data for offline players is not in memory, so we load it async
    // and cache the resolved display string. Entries expire so rank changes get
    // picked up without requiring a server restart.
    private static final long DISPLAY_CACHE_TTL_MILLIS = 5L * 60L * 1000L;

    private final DiamondBankAPIJava diamondBankAPI;
    private final LuckPerms luckPerms;
    private final Map<UUID, CachedDisplay> offlineDisplayCache = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> offlineLoadsInFlight = new ConcurrentHashMap<>();

    public BountyHeadFormatter(DiamondBankAPIJava diamondBankAPI, LuckPerms luckPerms) {

        this.diamondBankAPI = diamondBankAPI;
        this.luckPerms = luckPerms;

    }

    public void applyHeadMeta(SkullMeta meta, BountyHeadData data, PlayerProfile profile, BountyHeadMetadata metadata) {

        if (profile != null) {

            meta.setPlayerProfile(profile);

        }

        meta.displayName(buildCanonicalLine(data));
        metadata.write(meta.getPersistentDataContainer(), data);

    }

    public TextComponent buildCanonicalLine(BountyHeadData data) {

        return UtilitiesOG.trueogColorize(resolvePlayerDisplay(data.targetUuid(), data.targetName()) + " &cslain by "
                + resolveClaimantDisplay(data) + " &cfor &b" + formatDiamonds(data.bountyAmount()) + " &bDiamonds");

    }

    public String resolveClaimantDisplay(BountyHeadData data) {

        if (data == null) {

            return "&fUnknown";

        }

        return resolvePlayerDisplay(data.claimantUuid(), data.claimantName());

    }

    public String resolvePlayerDisplay(UUID targetUuid, String fallbackName) {

        final String playerName = StringUtils.defaultIfBlank(fallbackName, "Unknown Player");
        if (targetUuid == null || luckPerms == null) {

            return "&f" + playerName;

        }

        final User loadedUser = luckPerms.getUserManager().getUser(targetUuid);
        if (loadedUser != null) {

            return formatFromUser(loadedUser, playerName);

        }

        final CachedDisplay cached = offlineDisplayCache.get(targetUuid);
        if (cached != null && !cached.isExpired()) {

            return cached.display();

        }

        scheduleOfflineUserLoad(targetUuid, playerName);

        // Serve the stale entry while we refresh so labels do not flicker back to
        // the unformatted name each TTL expiry.
        if (cached != null) {

            return cached.display();

        }

        return "&f" + playerName;

    }

    private void scheduleOfflineUserLoad(UUID targetUuid, String playerName) {

        if (luckPerms == null) {

            return;

        }

        if (offlineLoadsInFlight.putIfAbsent(targetUuid, Boolean.TRUE) != null) {

            return;

        }

        final UserManager userManager = luckPerms.getUserManager();
        userManager.loadUser(targetUuid).whenComplete((user, throwable) -> {

            try {

                if (user == null) {

                    return;

                }

                final String display = formatFromUser(user, playerName);
                offlineDisplayCache.put(targetUuid,
                        new CachedDisplay(display, System.currentTimeMillis() + DISPLAY_CACHE_TTL_MILLIS));

                // LuckPerms keeps loaded users resident until explicitly released.
                // Release only when the UUID is not currently online so we do not
                // evict live data another system may depend on.
                if (Bukkit.getPlayer(targetUuid) == null) {

                    userManager.cleanupUser(user);

                }

            } finally {

                offlineLoadsInFlight.remove(targetUuid);

            }

        });

    }

    private String formatFromUser(User user, String playerName) {

        final CachedMetaData meta = user.getCachedData().getMetaData();
        final String prefix = StringUtils.trim(StringUtils.defaultString(meta.getPrefix()).replace('§', '&'));
        final String suffix = StringUtils.trim(StringUtils.defaultString(meta.getSuffix()).replace('§', '&'));
        final boolean metaContainsName = containsVisibleName(prefix, playerName)
                || containsVisibleName(suffix, playerName);

        final StringBuilder out = new StringBuilder();
        if (!prefix.isBlank()) {

            out.append(prefix).append(' ');

        }

        if (!metaContainsName) {

            out.append(playerName);

        }

        if (!suffix.isBlank()) {

            out.append(' ').append(suffix);

        }

        return sanitizeDisplay(out.toString(), playerName);

    }

    public String formatDiamonds(double diamonds) {

        if (diamondBankAPI == null) {

            return String.valueOf(diamonds);

        }

        try {

            return diamondBankAPI.shardsToDiamonds(diamondBankAPI.diamondsToShards(diamonds));

        } catch (RuntimeException runtimeException) {

            return String.valueOf(diamonds);

        }

    }

    private static String sanitizeDisplay(String formatted, String fallbackName) {

        final String cleaned = StringUtils.trimToEmpty(formatted).replaceAll("(?i)(?:\\s*(?:<reset>|[&§]r))+$", "")
                .replaceAll("\\s*>$", "");

        return cleaned.isBlank() ? fallbackName : cleaned;

    }

    private static boolean containsVisibleName(String text, String playerName) {

        if (text == null || playerName == null || playerName.isBlank()) {

            return false;

        }

        final String normalizedText = StringUtils.lowerCase(UtilitiesOG.stripFormatting(text), Locale.ROOT);
        final String normalizedName = StringUtils.lowerCase(playerName, Locale.ROOT);

        return StringUtils.contains(normalizedText, normalizedName);

    }

    private record CachedDisplay(String display, long expiresAtMillis) {

        boolean isExpired() {

            return System.currentTimeMillis() >= expiresAtMillis;

        }

    }

}