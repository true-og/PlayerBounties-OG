package com.tcoded.playerbountiesplus.hook.currency;

import java.util.UUID;

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

    private final PlayerBountiesOG plugin;
    private final DiamondBankAPIJava diamondBank;
    private long playerShards;

    public DiamondBankOGHook(PlayerBountiesOG plugin, DiamondBankAPIJava diamondBankAPITransporter) {

        this.plugin = plugin;
        this.diamondBank = diamondBankAPITransporter;

    }

    public boolean init() {

        if (this.plugin.getServer().getPluginManager().getPlugin("Vault") == null) {

            return false;

        }

        return true;

    }

    @Override
    public boolean isValid() {

        return diamondBank != null;

    }

    @Override
    public void giveEco(Player player, Player victim, double amount) {

        // Player UUID, amount (as shards, as type long), reason, notes.
        try {

            diamondBank.addToPlayerBankShards(
                    player.getUniqueId(), (long) (amount / 9), ("Player " + player.getName() + "defeated "
                            + victim.getName() + " and earned a bounty of " + (amount / 9)),
                    "Plugin: PlayerBounties-OG");

        } catch (EconomyDisabledException | DatabaseException error) {

            PlayerBountiesOG.disableSelf("[PlayerBounties-OG] Giving " + player.getName() + "the bounty for killing "
                    + victim.getName() + "failed at the giving step with error: " + error.getMessage());
            error.printStackTrace();

        }

    }

    @Override
    public void takeEco(Player player, Player victim, double amount) {

        this.takeEco(player, victim, amount, false);

    }

    @Override
    public boolean takeEco(Player player, Player victim, double amount, boolean force) {

        final double balance = getBalance(player.getUniqueId());
        if (balance < (amount / 9) && !force) {

            return false;

        }

        try {

            diamondBank.consumeFromPlayer(victim.getUniqueId(), (long) (amount / 9), "", "");

        } catch (EconomyDisabledException | InvalidPlayerException | PlayerNotOnlineException
                | InsufficientFundsException | InsufficientInventorySpaceException | DatabaseException error)
        {

            PlayerBountiesOG.disableSelf("[PlayerBounties-OG] Giving " + player.getName() + "the bounty for killing "
                    + victim.getName() + "failed at the taking step with error: " + error.getMessage());
            error.printStackTrace();

        }

        return true;

    }

    public long getBalance(UUID uuid) {

        // Runs code off of the main server thread.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            try {

                // Ask DiamondBank-OG for the player's total shard count.
                final long totalShards = diamondBank.getTotalShards(uuid);

                playerShards = totalShards;

            }
            // If the DiamondBank-OG economy is disabled, do this...
            catch (DiamondBankException.EconomyDisabledException error) {

                // Commit sudoku, inform console of the DiamondBank-OG economy being disabled.
                PlayerBountiesOG
                        .disableSelf("The DiamondBank-OG economy is disabled — disabling " + plugin.getName() + "!");

            }
            // If the DiamondBank-OG database is having problems, do this...
            catch (DiamondBankException.DatabaseException error) {

                // Commit sudoku, inform console of the DiamondBank-OG database error.
                PlayerBountiesOG.disableSelf("DiamondBank-OG database error — disabling " + plugin.getName()
                        + ". Cause: " + error.getMessage());

            }

        });

        return playerShards;

    }

}