package com.tcoded.playerbountiesplus.listener;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
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

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import net.trueog.utilitiesog.UtilitiesOG;

public class DeathListener implements Listener {

	private final PlayerBountiesOG plugin;
	private final DiamondBankAPIJava diamondBankAPI;
	private final LuckPerms luckPerms;

	public DeathListener(PlayerBountiesOG plugin, DiamondBankAPIJava diamondBankAPI, LuckPerms luckPerms) {

		this.plugin = plugin;
		this.diamondBankAPI = diamondBankAPI;
		this.luckPerms = luckPerms;

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent event) {

		final Player victim = event.getEntity();
		final Player killer = victim.getKiller();

		if (killer == null) {

			return;

		}

		if (!killer.hasPermission("playerbountiesplus.event.claim")) {

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

		if (plugin.getConfig().getBoolean("bounty-claimed-announce", true)) {

			final String killerDisplay = formatLuckPermsDisplay(killer);
			final String victimDisplay = formatLuckPermsDisplay(victim);
			final String bountyDisplay = formatDiamonds(bounty);
			final String awardedDisplay = formatDiamonds(awardedAmount);

			final String message;
			if (bountyClaimable && awardedAmount > 0D) {

				if (Double.compare(awardedAmount, bounty) == 0) {

					message = killerDisplay + " &akilled " + victimDisplay + " &aand earned the &b" + awardedDisplay
							+ " &abounty!";

				} else {

					message = killerDisplay + " &akilled " + victimDisplay + " &aand claimed the &b" + bountyDisplay
							+ " &abounty for &b" + awardedDisplay + "&a!";

				}

			} else {

				message = killerDisplay + " &akilled " + victimDisplay + " &aand claimed the &b" + bountyDisplay
						+ " &abounty!";

			}

			Bukkit.getOnlinePlayers().forEach(player -> UtilitiesOG.trueogMessage(player, message));

		}

		bountyDataManager.removeBounty(victimId);
		bountyDataManager.saveBountiesAsync();

	}

	private String formatDiamonds(double diamonds) {

		try {

			return diamondBankAPI.shardsToDiamonds(diamondBankAPI.diamondsToShards(diamonds));

		} catch (RuntimeException runtimeException) {

			return String.valueOf(diamonds);

		}

	}

	private String formatLuckPermsDisplay(Player player) {

		final String prefix = stripLeadingReset(stripTrailingReset(getLuckPermsPrefixLegacy(player)));
		final String leadingColorCodes = extractLeadingColorCodes(prefix);

		if (prefix.isBlank()) {

			return player.getName();

		}

		if (leadingColorCodes.isBlank()) {

			return prefix + " " + player.getName();

		}

		return prefix + " " + leadingColorCodes + player.getName();

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

		return out.toString();

	}

}