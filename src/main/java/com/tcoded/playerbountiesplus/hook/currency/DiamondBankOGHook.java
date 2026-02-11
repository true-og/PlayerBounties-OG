package com.tcoded.playerbountiesplus.hook.currency;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

import net.trueog.diamondbankog.DiamondBankAPIJava;
import net.trueog.diamondbankog.DiamondBankException;
import net.trueog.diamondbankog.DiamondBankException.DatabaseException;
import net.trueog.diamondbankog.DiamondBankException.EconomyDisabledException;
import net.trueog.diamondbankog.DiamondBankException.InsufficientFundsException;
import net.trueog.diamondbankog.DiamondBankException.InsufficientInventorySpaceException;
import net.trueog.diamondbankog.DiamondBankException.InvalidPlayerException;
import net.trueog.diamondbankog.DiamondBankException.PlayerNotOnlineException;

public class DiamondBankOGHook implements EconomyHook {

	private static final long SHARDS_PER_DIAMOND = 9L;

	private final PlayerBountiesOG plugin;
	private final DiamondBankAPIJava diamondBank;

	public DiamondBankOGHook(PlayerBountiesOG plugin, DiamondBankAPIJava diamondBankAPITransporter) {
		this.plugin = plugin;
		this.diamondBank = diamondBankAPITransporter;
	}

	public boolean init() {
		return this.plugin.getServer().getPluginManager().getPlugin("Vault") != null;
	}

	@Override
	public boolean isValid() {
		return diamondBank != null;
	}

	@Override
	public void giveEco(Player player, Player victim, double amount) {
		long diamonds = toWholeDiamonds(amount);
		if (diamonds <= 0L) {
			return;
		}

		long shards;
		try {
			shards = Math.multiplyExact(diamonds, SHARDS_PER_DIAMOND);
		} catch (ArithmeticException e) {
			PlayerBountiesOG.disableSelf("[PlayerBounties-OG] Overflow converting diamonds to shards for payout.");
			return;
		}

		try {
			diamondBank.addToPlayerBankShards(
					player.getUniqueId(),
					shards,
					"Player " + player.getName() + " defeated " + victim.getName() + " and earned a bounty of " + diamonds + " Diamonds.",
					"Plugin: PlayerBounties-OG"
					);
		} catch (EconomyDisabledException | DatabaseException error) {
			PlayerBountiesOG.disableSelf("[PlayerBounties-OG] Paying " + player.getName() + " for killing " + victim.getName()
			+ " failed with error: " + error.getMessage());
			error.printStackTrace();
		}
	}

	@Override
	public void takeEco(Player player, Player victim, double amount) {
		this.takeEco(player, victim, amount, false);
	}

	@Override
	public boolean takeEco(Player player, Player victim, double amount, boolean force) {
		long diamonds = toWholeDiamonds(amount);
		if (diamonds <= 0L) {
			return true;
		}

		try {
			diamondBank.consumeFromPlayer(
					player.getUniqueId(),
					diamonds,
					"Player " + player.getName() + " contributed " + diamonds + " Diamonds toward a bounty on " + victim.getName() + ".",
					"Plugin: PlayerBounties-OG"
					);
			return true;
		} catch (InsufficientFundsException | InsufficientInventorySpaceException | InvalidPlayerException | PlayerNotOnlineException e) {
			return false;
		} catch (EconomyDisabledException | DatabaseException e) {
			PlayerBountiesOG.disableSelf("[PlayerBounties-OG] Collecting " + diamonds + " Diamonds from " + player.getName()
			+ " for a bounty on " + victim.getName() + " failed with error: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public CompletableFuture<Double> getBalanceAsync(UUID uuid) {
		CompletableFuture<Double> future = new CompletableFuture<>();

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				long totalShards = diamondBank.getTotalShards(uuid);
				future.complete(totalShards / (double) SHARDS_PER_DIAMOND);
			} catch (DiamondBankException.EconomyDisabledException error) {
				PlayerBountiesOG.disableSelf("The DiamondBank-OG economy is disabled — disabling " + plugin.getName() + "!");
				future.completeExceptionally(error);
			} catch (DiamondBankException.DatabaseException error) {
				PlayerBountiesOG.disableSelf("DiamondBank-OG database error — disabling " + plugin.getName()
				+ ". Cause: " + error.getMessage());
				future.completeExceptionally(error);
			} catch (Throwable t) {
				future.completeExceptionally(t);
			}
		});

		return future;
	}

	public CompletableFuture<Double> getBalanceAsync(Player player) {
		return getBalanceAsync(player.getUniqueId());
	}

	private static long toWholeDiamonds(double diamonds) {
		if (!Double.isFinite(diamonds) || diamonds <= 0.0) {
			return 0L;
		}
		long whole = (long) Math.floor(diamonds);
		return Math.max(0L, whole);
	}

}