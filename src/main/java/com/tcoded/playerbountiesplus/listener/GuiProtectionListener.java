package com.tcoded.playerbountiesplus.listener;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

import net.trueog.utilitiesog.UtilitiesOG;

public class GuiProtectionListener implements Listener {

    private final PlayerBountiesOG plugin;

    public GuiProtectionListener(PlayerBountiesOG plugin) {

        this.plugin = plugin;

    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {

        if (!isMainBountyGui(event.getView())) {

            return;

        }

        if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {

            event.setCancelled(true);

        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {

        if (!isMainBountyGui(event.getView())) {

            return;

        }

        final int topInventorySize = event.getView().getTopInventory().getSize();
        for (int rawSlot : event.getRawSlots()) {

            if (rawSlot < topInventorySize) {

                event.setCancelled(true);
                return;

            }

        }

    }

    private boolean isMainBountyGui(InventoryView view) {

        final HumanEntity player = view.getPlayer();
        if (player == null) {

            return false;

        }

        return UtilitiesOG.stripFormatting(view.title().examinableName())
                .equals(UtilitiesOG.stripFormatting(plugin.getLang().getColored("gui.main.title")));

    }

}