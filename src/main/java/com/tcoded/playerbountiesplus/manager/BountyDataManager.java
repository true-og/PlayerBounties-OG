package com.tcoded.playerbountiesplus.manager;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

public class BountyDataManager {

    private final PlayerBountiesOG plugin;

    private File bountiesFile;
    private FileConfiguration bountiesConfig;

    private final Object savingFileLock = new Object();
    private final AtomicBoolean savingAsync = new AtomicBoolean(false);
    private final ConcurrentHashMap<UUID, Double> bounties;

    public BountyDataManager(PlayerBountiesOG plugin) {

        this.plugin = plugin;
        this.bounties = new ConcurrentHashMap<>();

    }

    public void init() {

        final String bountiesFileName = "bounties.yml";
        bountiesFile = new File(this.plugin.getDataFolder(), bountiesFileName);
        if (!bountiesFile.exists()) {

            this.plugin.saveResource(bountiesFileName, false);

        }

        bountiesConfig = YamlConfiguration.loadConfiguration(bountiesFile);

        final Set<String> keys = bountiesConfig.getKeys(false);

        keys.forEach(key -> this.bounties.put(UUID.fromString(key), bountiesConfig.getDouble(key)));

    }

    public ConcurrentHashMap<UUID, Double> getBounties() {

        return this.bounties;

    }

    public boolean hasBounty(UUID player) {

        return this.bounties.containsKey(player) && this.bounties.get(player) > 0;

    }

    public void setBounty(UUID player, double amount) {

        this.bounties.put(player, amount);

    }

    public double getBounty(UUID player) {

        return this.bounties.getOrDefault(player, 0.0);

    }

    public void removeBounty(UUID player) {

        this.bounties.remove(player);

    }

    public void saveBounties() {

        synchronized (this.savingFileLock) {

            final Set<String> keys = bountiesConfig.getKeys(false);
            keys.forEach(key -> bountiesConfig.set(key, null));

            this.bounties.entrySet()
                    .forEach(entry -> this.bountiesConfig.set(entry.getKey().toString(), entry.getValue()));

            try {

                this.bountiesConfig.save(this.bountiesFile);

            } catch (IOException ioException) {

                this.plugin.getLogger().log(Level.SEVERE,
                        "Failed to save bounties file: " + this.bountiesFile.getPath(), ioException);

            }

        }

    }

    public void saveBountiesAsync() {

        if (!this.savingAsync.compareAndSet(false, true)) {

            return;

        }

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

            try {

                this.saveBounties();

            } finally {

                this.savingAsync.set(false);

            }

        });

    }

}