package com.tcoded.playerbountiesplus.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import net.trueog.utilitiesog.UtilitiesOG;

public class BountyHeadListener implements Listener {

    private final DiamondBankAPIJava diamondBankAPI;
    private final NamespacedKey targetKey;
    private final NamespacedKey amountKey;

    public BountyHeadListener(PlayerBountiesOG plugin, DiamondBankAPIJava diamondBankAPI) {

        this.diamondBankAPI = diamondBankAPI;
        this.targetKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_NAME_KEY);
        this.amountKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_AMOUNT_KEY);

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

        final String targetName = itemMeta.getPersistentDataContainer().get(targetKey, PersistentDataType.STRING);
        final Double bountyAmount = itemMeta.getPersistentDataContainer().get(amountKey, PersistentDataType.DOUBLE);

        if (targetName == null || bountyAmount == null) {

            return;

        }

        final BlockState state = event.getBlockPlaced().getState();
        if (!(state instanceof Skull skull)) {

            return;

        }

        final PersistentDataContainer blockData = skull.getPersistentDataContainer();
        blockData.set(targetKey, PersistentDataType.STRING, targetName);
        blockData.set(amountKey, PersistentDataType.DOUBLE, bountyAmount);
        skull.update(true, false);

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {

        if (!isHeadMaterial(event.getBlock().getType())) {

            return;

        }

        final BlockState state = event.getBlock().getState();
        if (!(state instanceof Skull skull)) {

            return;

        }

        final String targetName = skull.getPersistentDataContainer().get(targetKey, PersistentDataType.STRING);
        final Double bountyAmount = skull.getPersistentDataContainer().get(amountKey, PersistentDataType.DOUBLE);

        if (targetName == null || bountyAmount == null) {

            return;

        }

        final ItemStack drop = new ItemStack(Material.PLAYER_HEAD, 1);
        final SkullMeta meta = (SkullMeta) drop.getItemMeta();

        meta.setPlayerProfile(skull.getPlayerProfile());

        applyBountyData(meta, targetName, bountyAmount);

        drop.setItemMeta(meta);

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

        if (event.getClickedBlock() == null || !isHeadMaterial(event.getClickedBlock().getType())) {

            return;

        }

        final BlockState state = event.getClickedBlock().getState();
        if (!(state instanceof Skull skull)) {

            return;

        }

        sendBountyHeadActionbar(event.getPlayer(), skull.getPersistentDataContainer());

    }

    @EventHandler(ignoreCancelled = true)
    public void onHover(PlayerMoveEvent event) {

        final Player player = event.getPlayer();
        final Block target = player.getTargetBlockExact(6);
        if (target == null || !isHeadMaterial(target.getType())) {

            return;

        }

        final BlockState state = target.getState();
        if (!(state instanceof Skull skull)) {

            return;

        }

        sendBountyHeadActionbar(player, skull.getPersistentDataContainer());

    }

    private void sendBountyHeadActionbar(Player player, ItemMeta itemMeta) {

        if (itemMeta == null) {

            return;

        }

        sendBountyHeadActionbar(player, itemMeta.getPersistentDataContainer());

    }

    private void sendBountyHeadActionbar(Player player, PersistentDataContainer dataContainer) {

        final String targetName = dataContainer.get(targetKey, PersistentDataType.STRING);
        final Double bountyAmount = dataContainer.get(amountKey, PersistentDataType.DOUBLE);

        if (targetName == null || bountyAmount == null) {

            return;

        }

        final String diamonds = formatDiamonds(bountyAmount);
        player.sendActionBar(Component.text("Bounty Head: ", NamedTextColor.RED)
                .append(Component.text(targetName, NamedTextColor.WHITE))
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(diamonds + " diamonds", NamedTextColor.AQUA)));

    }

    private void applyBountyData(SkullMeta meta, String targetName, double bountyAmount) {

        meta.displayName(UtilitiesOG.trueogColorize("&c" + targetName + "'s Bounty Head"));

        final List<TextComponent> lore = new ArrayList<>();
        lore.add(UtilitiesOG.trueogColorize("&6Player: &f" + targetName));
        lore.add(UtilitiesOG.trueogColorize("&cBeheaded for: &b" + formatDiamonds(bountyAmount)));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(targetKey, PersistentDataType.STRING, targetName);
        meta.getPersistentDataContainer().set(amountKey, PersistentDataType.DOUBLE, bountyAmount);

    }

    private String formatDiamonds(double diamonds) {

        try {

            return diamondBankAPI.shardsToDiamonds(diamondBankAPI.diamondsToShards(diamonds));

        } catch (RuntimeException runtimeException) {

            return String.valueOf(diamonds);

        }

    }

    private static boolean isHeadMaterial(Material material) {

        return material == Material.PLAYER_HEAD || material == Material.PLAYER_WALL_HEAD;

    }

}