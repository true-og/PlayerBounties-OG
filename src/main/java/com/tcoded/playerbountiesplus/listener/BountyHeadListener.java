package com.tcoded.playerbountiesplus.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.hook.logging.CoreProtectHook;

import net.kyori.adventure.text.TextComponent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.trueog.diamondbankog.api.DiamondBankAPIJava;
import net.trueog.utilitiesog.UtilitiesOG;

public class BountyHeadListener implements Listener {

    private final DiamondBankAPIJava diamondBankAPI;
    private final NamespacedKey targetKey;
    private final NamespacedKey targetUuidKey;
    private final NamespacedKey amountKey;
    private final LuckPerms luckPerms;
    private final CoreProtectHook coreProtectHook;

    public BountyHeadListener(PlayerBountiesOG plugin, DiamondBankAPIJava diamondBankAPI, LuckPerms luckPerms,
            CoreProtectHook coreProtectHook)
    {

        this.diamondBankAPI = diamondBankAPI;
        this.targetKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_NAME_KEY);
        this.targetUuidKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_UUID_KEY);
        this.amountKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_AMOUNT_KEY);
        this.luckPerms = luckPerms;
        this.coreProtectHook = coreProtectHook;

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
        final String targetUuid = itemMeta.getPersistentDataContainer().get(targetUuidKey, PersistentDataType.STRING);
        final Double bountyAmount = itemMeta.getPersistentDataContainer().get(amountKey, PersistentDataType.DOUBLE);

        if (targetName == null || targetUuid == null || bountyAmount == null) {

            return;

        }

        final BlockState state = event.getBlockPlaced().getState();
        if (!(state instanceof Skull skull)) {

            return;

        }

        final PersistentDataContainer blockData = skull.getPersistentDataContainer();
        blockData.set(targetKey, PersistentDataType.STRING, targetName);
        blockData.set(targetUuidKey, PersistentDataType.STRING, targetUuid);
        blockData.set(amountKey, PersistentDataType.DOUBLE, bountyAmount);
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

        final String targetName = skull.getPersistentDataContainer().get(targetKey, PersistentDataType.STRING);
        final String targetUuid = skull.getPersistentDataContainer().get(targetUuidKey, PersistentDataType.STRING);
        final Double bountyAmount = skull.getPersistentDataContainer().get(amountKey, PersistentDataType.DOUBLE);

        if (targetName == null || targetUuid == null || bountyAmount == null) {

            return;

        }

        final ItemStack drop = new ItemStack(Material.PLAYER_HEAD, 1);
        final SkullMeta meta = (SkullMeta) drop.getItemMeta();

        meta.setPlayerProfile(skull.getPlayerProfile());

        applyBountyData(meta, targetName, targetUuid, bountyAmount);

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

    private void sendBountyHeadActionbar(Player player, PersistentDataContainer dataContainer) {

        final String targetName = dataContainer.get(targetKey, PersistentDataType.STRING);
        final String targetUuid = dataContainer.get(targetUuidKey, PersistentDataType.STRING);
        final Double bountyAmount = dataContainer.get(amountKey, PersistentDataType.DOUBLE);

        if (targetName == null || targetUuid == null || bountyAmount == null) {

            return;

        }

        final UUID uuid = parseUuid(targetUuid);
        final String playerDisplay = formatLuckPermsDisplay(uuid, targetName);
        final String diamonds = formatDiamonds(bountyAmount);
        player.sendActionBar(
                UtilitiesOG.trueogColorize("&c" + playerDisplay + "'s Head &7- &b" + diamonds + " &bDiamonds"));

    }

    private void applyBountyData(SkullMeta meta, String targetName, String targetUuid, double bountyAmount) {

        meta.displayName(UtilitiesOG.trueogColorize("&c" + targetName + "'s Head"));

        final UUID uuid = parseUuid(targetUuid);
        final String playerDisplay = formatLuckPermsDisplay(uuid, targetName);

        final List<TextComponent> lore = new ArrayList<>();
        lore.add(UtilitiesOG.trueogColorize("&6Player: " + playerDisplay));
        lore.add(UtilitiesOG.trueogColorize("&cBeheaded for: &b" + formatDiamonds(bountyAmount) + " &bDiamonds"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(targetKey, PersistentDataType.STRING, targetName);
        meta.getPersistentDataContainer().set(targetUuidKey, PersistentDataType.STRING, targetUuid);
        meta.getPersistentDataContainer().set(amountKey, PersistentDataType.DOUBLE, bountyAmount);

    }

    private UUID parseUuid(String uuidString) {

        if (uuidString == null || uuidString.isBlank()) {

            return null;

        }

        try {

            return UUID.fromString(uuidString);

        } catch (IllegalArgumentException ignored) {

            return null;

        }

    }

    private String formatLuckPermsDisplay(UUID targetUuid, String fallbackName) {

        final String playerName = StringUtils.defaultIfBlank(fallbackName, "Unknown Player");
        if (targetUuid == null || luckPerms == null) {

            return "&f" + playerName;

        }

        final User user = luckPerms.getUserManager().getUser(targetUuid);
        if (user == null) {

            return "&f" + playerName;

        }

        final CachedMetaData meta = user.getCachedData().getMetaData();
        final String prefix = StringUtils.trim(StringUtils.defaultString(meta.getPrefix()).replace('§', '&'));
        final String suffix = StringUtils.trim(StringUtils.defaultString(meta.getSuffix()).replace('§', '&'));
        final boolean metaContainsName = containsVisibleName(prefix, playerName)
                || containsVisibleName(suffix, playerName);

        final StringBuilder out = new StringBuilder();
        if (!prefix.isBlank()) {

            out.append(prefix).append(' ');

        }

        if (!metaContainsName) {

            out.append(playerName);

        }

        if (!suffix.isBlank()) {

            out.append(' ').append(suffix);

        }

        return sanitizeDisplay(out.toString(), playerName);

    }

    private static String sanitizeDisplay(String formatted, String fallbackName) {

        final String cleaned = StringUtils.trimToEmpty(formatted).replaceAll("(?i)(?:\\s*(?:<reset>|[&§]r))+$", "")
                .replaceAll("\\s*>$", "");

        if (cleaned.isBlank()) {

            return fallbackName;

        }

        return cleaned;

    }

    private static boolean containsVisibleName(String text, String playerName) {

        if (text == null || playerName == null || playerName.isBlank()) {

            return false;

        }

        final String normalizedText = stripFormatting(text).toLowerCase(Locale.ROOT);
        final String normalizedName = playerName.toLowerCase(Locale.ROOT);

        return normalizedText.contains(normalizedName);

    }

    private static String stripFormatting(String input) {

        if (input == null || input.isBlank()) {

            return "";

        }

        return input.replaceAll("(?i)[&§][0-9A-FK-ORX]", "").replaceAll("<[^>]+>", "").trim();

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
