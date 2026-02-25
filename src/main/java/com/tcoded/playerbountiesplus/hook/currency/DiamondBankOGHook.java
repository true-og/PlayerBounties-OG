package com.tcoded.playerbountiesplus.hook.currency;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

import net.trueog.diamondbankog.DiamondBankException;
import net.trueog.diamondbankog.DiamondBankException.EconomyDisabledException;
import net.trueog.diamondbankog.DiamondBankException.InsufficientFundsException;
import net.trueog.diamondbankog.DiamondBankException.InsufficientInventorySpaceException;
import net.trueog.diamondbankog.DiamondBankException.InvalidPlayerException;
import net.trueog.diamondbankog.DiamondBankException.PlayerNotOnlineException;
import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import net.trueog.utilitiesog.UtilitiesOG;

public class DiamondBankOGHook implements EconomyHook {

	private final PlayerBountiesOG plugin;
	private final DiamondBankAPIJava diamondBank;

	public DiamondBankOGHook(PlayerBountiesOG plugin, DiamondBankAPIJava diamondBankAPITransporter) {

		this.plugin = plugin;
		this.diamondBank = diamondBankAPITransporter;

	}

	@Override
	public boolean giveEco(Player player, Player victim, double diamonds) {

		final long shards = diamondBank.diamondsToShards(diamonds);
		if (shards <= 0L) {

			return false;

		}

		try { 

			diamondBank.addToPlayerBankShards(
					player.getUniqueId(),
					shards,
					"Player " + player.getName() + " defeated " + victim.getName() + " and earned a bounty of "
							+ diamondBank.shardsToDiamonds(shards) + ".",
							"Plugin: PlayerBounties-OG"
					);

		} catch (EconomyDisabledException economyDisabledError) {

			PlayerBountiesOG.disableSelf("[PlayerBounties-OG] Paying " + player.getName() + " for killing "
					+ victim.getName() + " failed because the economy is disabled! " + economyDisabledError.getMessage());
			economyDisabledError.printStackTrace();

			return false;

		}

		return true;

	}

	@Override
	public boolean takeEco(Player player, Player victim, double diamonds) {

		final long shards = diamondBank.diamondsToShards(diamonds);
		if (shards <= 0L) {

			return false;

		}

		try {

			diamondBank.consumeFromPlayer(
					player.getUniqueId(),
					shards,
					"Player " + player.getName() + " contributed " + diamondBank.shardsToDiamonds(shards)
					+ " Diamonds toward a bounty on " + victim.getName() + ".",
					"Plugin: PlayerBounties-OG"
					);

		}
		catch (EconomyDisabledException economyDisabledError) {

			PlayerBountiesOG.disableSelf("[PlayerBounties-OG] DiamondBank-OG economy disabled: " + economyDisabledError.getMessage());
			economyDisabledError.printStackTrace();

			return false;

		}

		catch (InvalidPlayerException invalidPlayerError) {

			plugin.getLogger().warning("Invalid player for consumeFromPlayer: " + player.getName());
			UtilitiesOG.trueogMessage(player, "&cERROR: your player account could not be found by DiamondBank-OG. Contact an administrator!");

			return false;

		}
		catch (InsufficientFundsException insufficientFundsError) {

			UtilitiesOG.trueogMessage(player, "&6WARNING: You do not have enough Diamonds to contribute to that bounty.");

			return false;

		}
		// TODO: Remove InsufficientInventorySpaceException catch block after next DiamondBank-OG update.
		catch (InsufficientInventorySpaceException temporaryInsufficientInventorySpaceError) {

			plugin.getLogger().info("Player " + player.getName() + " has insufficient inventory space to receive items. This error is expected to be removed in the next DiamondBank-OG update.");
			temporaryInsufficientInventorySpaceError.printStackTrace();

			return false;

		}
		// TODO: Remove PlayerNotOnlineException catch block after next DiamondBank-OG update.
		catch (PlayerNotOnlineException temporaryPlayerNotOnlineError) {

			plugin.getLogger().warning("Player " + player.getName() + " not online when attempting to consumeFromPlayer. This error is expected to be removed in the next DiamondBank-OG update.");
			temporaryPlayerNotOnlineError.printStackTrace();

			return false;

		}

		return true;

	}

	public CompletableFuture<Long> getShardBalance(UUID uuid) {

		final CompletableFuture<Long> future = new CompletableFuture<>();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

			try {

				final long totalShards = diamondBank.getTotalShards(uuid);
				future.complete(totalShards);

			} catch (DiamondBankException.EconomyDisabledException error) {

				PlayerBountiesOG
				.disableSelf("The DiamondBank-OG economy is disabled — disabling " + plugin.getName() + "!");
				future.completeExceptionally(error);

			} catch (Throwable t) {

				future.completeExceptionally(t);

			}

		});

		return future;

	}

}