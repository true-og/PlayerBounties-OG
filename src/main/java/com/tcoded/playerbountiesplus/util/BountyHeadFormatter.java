package com.tcoded.playerbountiesplus.util;

import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.inventory.meta.SkullMeta;

import com.destroystokyo.paper.profile.PlayerProfile;

import net.kyori.adventure.text.TextComponent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import net.trueog.utilitiesog.UtilitiesOG;

public final class BountyHeadFormatter {

    private final DiamondBankAPIJava diamondBankAPI;
    private final LuckPerms luckPerms;

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

        final User user = luckPerms.getUserManager().getUser(targetUuid);
        if (user == null) {

            return "&f" + playerName;

        }

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

}