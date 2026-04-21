package com.tcoded.playerbountiesplus.listener;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.event.BountyClaimEvent;
import com.tcoded.playerbountiesplus.hook.team.TeamHook;
import com.tcoded.playerbountiesplus.manager.BountyDataManager;
import com.tcoded.playerbountiesplus.util.BountyHeadData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.trueog.utilitiesog.UtilitiesOG;

public class DeathListener implements Listener {

    public static final String BOUNTY_HEAD_NAME_KEY = "bounty_head_target";
    public static final String BOUNTY_HEAD_UUID_KEY = "bounty_head_target_uuid";
    public static final String BOUNTY_HEAD_AMOUNT_KEY = "bounty_head_diamonds";
    public static final String BOUNTY_HEAD_CLAIMANT_KEY = "bounty_head_claimant";
    public static final String BOUNTY_HEAD_CLAIMANT_UUID_KEY = "bounty_head_claimant_uuid";

    private final PlayerBountiesOG plugin;
    private final LuckPerms luckPerms;

    public DeathListener(PlayerBountiesOG plugin) {

        this.plugin = plugin;
        this.luckPerms = plugin.getLuckPerms();

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {

        final Player victim = event.getEntity();
        final Player killer = victim.getKiller();

        if (killer == null) {

            return;

        }

        if (!killer.hasPermission("playerbountiesog.event.claim")) {

            UtilitiesOG.trueogMessage(killer, plugin.getLang().getColored("death.no-permission"));
            return;

        }

        final BountyDataManager bountyDataManager = this.plugin.getBountyDataManager();
        final UUID victimId = victim.getUniqueId();

        if (!bountyDataManager.hasBounty(victimId)) {

            return;

        }

        final TeamHook teamHook = this.plugin.getTeamHook();
        if (teamHook != null && teamHook.isFriendly(killer, victim)) {

            UtilitiesOG.trueogMessage(killer, plugin.getLang().getColored("death.same-team"));
            return;

        }

        final double bounty = bountyDataManager.getBounty(victimId);
        if (bounty <= 0D) {

            bountyDataManager.removeBounty(victimId);
            bountyDataManager.saveBountiesAsync();
            return;

        }

        final BountyClaimEvent claimEvent = new BountyClaimEvent(killer, victim, bounty);
        this.plugin.getServer().getPluginManager().callEvent(claimEvent);

        if (claimEvent.isCancelled()) {

            return;

        }

        final boolean bountyClaimable = plugin.getConfig().getBoolean("bounty-claimable", true);
        final double claimMultiplier = plugin.getConfig().getDouble("bounty-claim-multiplier", 1.0D);
        final double awardedAmount = Math.max(0D, bounty * claimMultiplier);

        if (bountyClaimable && awardedAmount > 0D) {

            try {

                this.plugin.getEcoHook().giveEco(killer, victim, awardedAmount);

            } catch (RuntimeException runtimeException) {

                plugin.getLogger().severe("Failed to award bounty payout to " + killer.getName() + " for killing "
                        + victim.getName() + ": " + runtimeException.getMessage());
                runtimeException.printStackTrace();

                UtilitiesOG.trueogMessage(killer,
                        "&cAn error occurred while awarding the bounty payout. The bounty was not removed.");
                return;

            }

        }

        // Suppress the normal Minecraft death message because this death is being
        // handled as a bounty claim.
        event.deathMessage(null);

        final String killerDisplay = formatLuckPermsDisplay(killer);
        final String victimDisplay = formatLuckPermsDisplay(victim);

        final boolean beheaded = maybeDropPlayerHead(event, killer, victim, bounty);

        if (beheaded) {

            final String bountyDisplay = formatDiamonds(bounty);
            final String beheadedMessage = killerDisplay + "&r &abeheaded " + victimDisplay + "&r &afor &b"
                    + bountyDisplay + " &bDiamonds&a!";

            Bukkit.getOnlinePlayers().forEach(player -> UtilitiesOG.trueogMessage(player, beheadedMessage));
            playGlobalCelebrationEffects(killer, victim);

        } else if (plugin.getConfig().getBoolean("bounty-claimed-announce", true)) {

            final String bountyDisplay = formatDiamonds(bounty);
            final String awardedDisplay = formatDiamonds(awardedAmount);

            final String message;
            if (bountyClaimable && awardedAmount > 0D) {

                if (Double.compare(awardedAmount, bounty) == 0) {

                    message = killerDisplay + "&r &akilled " + victimDisplay + "&r &aand earned &b" + awardedDisplay
                            + " &bDiamonds &afor the bounty!";

                } else {

                    message = killerDisplay + "&r &akilled " + victimDisplay + "&r &aand claimed &b" + bountyDisplay
                            + " &bDiamonds &afor &b" + awardedDisplay + " &bDiamonds&a!";

                }

            } else {

                message = killerDisplay + "&r &akilled " + victimDisplay + "&r &aand claimed &b" + bountyDisplay
                        + " &bDiamonds &abounty!";

            }

            Bukkit.getOnlinePlayers().forEach(player -> UtilitiesOG.trueogMessage(player, message));
            playGlobalCelebrationEffects(killer, victim);

        }

        bountyDataManager.removeBounty(victimId);
        bountyDataManager.saveBountiesAsync();

    }

    private void playGlobalCelebrationEffects(Player killer, Player victim) {

        Bukkit.getOnlinePlayers().forEach(player -> {

            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);

            final Particle fireworkParticle = resolveFireworkParticle();
            player.spawnParticle(fireworkParticle, killer.getLocation().add(0D, 1D, 0D), 40, 0.6D, 0.8D, 0.6D, 0.01D);
            player.spawnParticle(fireworkParticle, victim.getLocation().add(0D, 1D, 0D), 40, 0.6D, 0.8D, 0.6D, 0.01D);
            player.spawnParticle(Particle.TOTEM, killer.getLocation().add(0D, 1D, 0D), 20, 0.5D, 0.7D, 0.5D, 0.01D);

        });

    }

    private Particle resolveFireworkParticle() {

        try {

            return Particle.valueOf("FIREWORK");

        } catch (IllegalArgumentException exception) {

            return Particle.FIREWORKS_SPARK;

        }

    }

    private String formatDiamonds(double diamonds) {

        return plugin.getBountyHeadFormatter().formatDiamonds(diamonds);

    }

    private String formatLuckPermsDisplay(Player player) {

        final String playerName = player.getName();
        final String prefix = stripLeadingReset(stripTrailingReset(getLuckPermsPrefixLegacy(player)));
        final String suffix = stripLeadingReset(stripTrailingReset(getLuckPermsSuffixLegacy(player)));
        final String leadingColorCodes = extractLeadingColorCodes(prefix);

        final StringBuilder out = new StringBuilder();
        if (!prefix.isBlank()) {

            out.append(prefix).append(" ");

        }

        if (leadingColorCodes.isBlank()) {

            out.append(playerName);

        } else {

            out.append(leadingColorCodes).append(playerName);

        }

        if (!suffix.isBlank()) {

            out.append(" ").append(suffix);

        }

        return StringUtils.trim(out.toString()).replaceAll("\\s*>$", "");

    }

    private boolean maybeDropPlayerHead(PlayerDeathEvent event, Player killer, Player victim, double bounty) {

        final double dropChance = Math.max(0D,
                Math.min(100D, plugin.getConfig().getDouble("bounty-head-drop-chance", 50D)));
        if (ThreadLocalRandom.current().nextDouble(100D) >= dropChance) {

            return false;

        }

        final ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        final SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        final BountyHeadData headData = new BountyHeadData(victim.getUniqueId(), victim.getName(), bounty,
                killer.getUniqueId(), killer.getName());

        final PlayerProfile profile = Bukkit.createProfile(victim.getUniqueId());
        plugin.getBountyHeadFormatter().applyHeadMeta(headMeta, headData, profile, plugin.getBountyHeadMetadata());
        head.setItemMeta(headMeta);

        event.getDrops().add(head);

        return true;

    }

    private String getLuckPermsSuffixLegacy(Player player) {

        if (luckPerms == null) {

            return "";

        }

        final User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {

            return "";

        }

        final CachedMetaData meta = user.getCachedData().getMetaData();
        final String suffix = meta.getSuffix();

        if (suffix == null || suffix.isBlank()) {

            return "";

        }

        return StringUtils.trim(suffix).replace('§', '&');

    }

    private String getLuckPermsPrefixLegacy(Player player) {

        if (luckPerms == null) {

            return "";

        }

        final User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {

            return "";

        }

        final CachedMetaData meta = user.getCachedData().getMetaData();
        final String prefix = meta.getPrefix();

        if (prefix == null || prefix.isBlank()) {

            return "";

        }

        return StringUtils.trim(prefix).replace('§', '&');

    }

    private static String stripTrailingReset(String input) {

        if (input == null || input.isBlank()) {

            return "";

        }

        return StringUtils.trim(input.replaceAll("(?i)(?:\\s*(?:<reset>|[&§]r))+$", ""));

    }

    private static String stripLeadingReset(String input) {

        if (input == null || input.isBlank()) {

            return "";

        }

        return StringUtils.trim(input.replaceFirst("(?i)^(?:\\s*(?:<reset>|[&§]r))+", ""));

    }

    private static String extractLeadingColorCodes(String input) {

        if (input == null || input.length() < 2) {

            return "";

        }

        final StringBuilder out = new StringBuilder();
        for (int i = 0; i < input.length() - 1; i++) {

            final char current = input.charAt(i);
            if (current != '&' && current != '§') {

                break;

            }

            out.append(current).append(input.charAt(i + 1));

            i++;

        }

        return StringUtils.trim(out.toString()).replaceAll("\\s*>$", "");

    }

}
