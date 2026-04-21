package com.tcoded.playerbountiesplus.listener;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.hook.logging.CoreProtectHook;
import com.tcoded.playerbountiesplus.util.BountyHeadData;

public class BountyHeadListener implements Listener {

    private final PlayerBountiesOG plugin;
    private final CoreProtectHook coreProtectHook;

    public BountyHeadListener(PlayerBountiesOG plugin) {

        this.plugin = plugin;
        this.coreProtectHook = plugin.getCoreProtectHook();

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {

        if (!isHeadMaterial(event.getBlockPlaced().getType())) {

            return;

        }

        final ItemMeta itemMeta = event.getItemInHand().getItemMeta();
        if (itemMeta == null) {

            return;

        }

        final BountyHeadData headData = plugin.getBountyHeadMetadata().read(itemMeta.getPersistentDataContainer());
        if (headData == null) {

            return;

        }

        final BlockState state = event.getBlockPlaced().getState();
        if (!(state instanceof Skull skull)) {

            return;

        }

        plugin.getBountyHeadMetadata().write(skull.getPersistentDataContainer(), headData);
        skull.update(true, false);

        if (this.coreProtectHook != null) {

            this.coreProtectHook.logPlacement(event.getPlayer().getName(), skull);

        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {

        if (!isHeadMaterial(event.getBlock().getType())) {

            return;

        }

        final BlockState state = event.getBlock().getState();
        final BlockState snapshot = state;
        if (!(state instanceof Skull skull)) {

            return;

        }

        final BountyHeadData headData = plugin.getBountyHeadMetadata().read(skull.getPersistentDataContainer());
        if (headData == null) {

            return;

        }

        final ItemStack drop = new ItemStack(Material.PLAYER_HEAD, 1);
        final SkullMeta meta = (SkullMeta) drop.getItemMeta();

        plugin.getBountyHeadFormatter().applyHeadMeta(meta, headData, skull.getPlayerProfile(),
                plugin.getBountyHeadMetadata());
        drop.setItemMeta(meta);

        if (this.coreProtectHook != null) {

            this.coreProtectHook.logRemoval(event.getPlayer().getName(), snapshot);

        }

        event.setDropItems(false);
        event.getBlock().setType(Material.AIR, false);
        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);

    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {

            return;

        }

        final ItemStack item = event.getItem();
        if (item != null && isHeadMaterial(item.getType())) {

            sendBountyHeadActionbar(event.getPlayer(), item.getItemMeta());

        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryHover(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {

            return;

        }

        final ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || !isHeadMaterial(currentItem.getType())) {

            return;

        }

        sendBountyHeadActionbar(player, currentItem.getItemMeta());

    }

    @EventHandler(ignoreCancelled = true)
    public void onHotbarScroll(PlayerItemHeldEvent event) {

        final ItemStack newSlotItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (newSlotItem == null || !isHeadMaterial(newSlotItem.getType())) {

            return;

        }

        sendBountyHeadActionbar(event.getPlayer(), newSlotItem.getItemMeta());

    }

    private void sendBountyHeadActionbar(Player player, ItemMeta itemMeta) {

        if (itemMeta == null) {

            return;

        }

        sendBountyHeadActionbar(player, itemMeta.getPersistentDataContainer());

    }

    private void sendBountyHeadActionbar(Player player, org.bukkit.persistence.PersistentDataContainer dataContainer) {

        final BountyHeadData headData = plugin.getBountyHeadMetadata().read(dataContainer);
        if (headData == null) {

            return;

        }

        player.sendActionBar(plugin.getBountyHeadFormatter().buildActionBar(headData));

    }

    private static boolean isHeadMaterial(Material material) {

        return material == Material.PLAYER_HEAD || material == Material.PLAYER_WALL_HEAD;

    }

}
