package com.tcoded.playerbountiesplus.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

public class MainBountyGui implements IPbpGui {

    private final Player viewer;
    private final Inventory inventory;

    public MainBountyGui(PlayerBountiesOG plugin, Player viewer) {

        this.viewer = viewer;

        // Create inventory
        this.inventory = Bukkit.createInventory(this, 9 * 3, plugin.getLang().getColored("gui.main.title"));

    }

    @Override
    public Inventory getInventory() {

        return this.inventory;

    }

    @Override
    public void open() {

        setContents();

        // Open the inventory for the viewer
        viewer.openInventory(this.inventory);

    }

    @Override
    public void setContents() {

        // TODO: Populate the inventory

    }

    @Override
    public void handleClick(InventoryClickEvent event) {

        // TODO: Handle inventory clicks

    }

}