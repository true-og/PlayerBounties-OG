package com.tcoded.playerbountiesplus.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.event.BountyClaimEvent;
import com.tcoded.playerbountiesplus.hook.team.TeamHook;
import com.tcoded.playerbountiesplus.manager.BountyDataManager;

import net.trueog.utilitiesog.UtilitiesOG;

public class DeathListener implements Listener {

    private final PlayerBountiesOG plugin;

    public DeathListener(PlayerBountiesOG plugin) {

        this.plugin = plugin;

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {

        final Player victim = event.getEntity();
        final Player killer = victim.getKiller();

        if (killer == null) {

            return;

        }

        if (!killer.hasPermission("playerbountiesplus.event.claim")) {

            killer.sendMessage(plugin.getLang().getColored("death.no-permission"));
            return;

        }

        final BountyDataManager bountyDataManager = this.plugin.getBountyDataManager();
        final UUID victimId = victim.getUniqueId();

        if (!bountyDataManager.hasBounty(victimId)) {

            return;

        }

        final int bounty = bountyDataManager.getBounty(victimId);

        final TeamHook teamHook = this.plugin.getTeamHook();
        if (teamHook != null && teamHook.isFriendly(killer, victim)) {

            killer.sendMessage(plugin.getLang().getColored("death.same-team"));
            return;

        }

        final BountyClaimEvent claimEvent = new BountyClaimEvent(killer, victim, bounty);
        this.plugin.getServer().getPluginManager().callEvent(claimEvent);

        if (claimEvent.isCancelled()) {

            return;

        }

        if (plugin.getConfig().getBoolean("bounty-claimable", true)) {

            final double claimMultiplier = plugin.getConfig().getDouble("bounty-claim-multiplier", 1.0);
            final double awardedAmount = bounty * claimMultiplier;

            if (awardedAmount > 0) {

                this.plugin.getEcoHook().giveEco(killer, victim, awardedAmount);

            }

        }

        if (plugin.getConfig().getBoolean("bounty-claimed-announce", true)) {

            Bukkit.getOnlinePlayers().forEach((Player player) -> UtilitiesOG.trueogMessage(player, killer.getName()
                    + "killed " + victim.getName() + " and earned the " + String.valueOf(bounty) + " Diamond bounty!"));

        }

        bountyDataManager.removeBounty(victimId);
        bountyDataManager.saveBountiesAsync();

    }

}
