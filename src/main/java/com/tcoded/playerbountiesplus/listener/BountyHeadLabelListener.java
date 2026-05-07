package com.tcoded.playerbountiesplus.listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.util.BountyHeadData;

public final class BountyHeadLabelListener implements Listener {

    private static final long UPDATE_PERIOD_TICKS = 2L;

    // Two stacked armor stands per head: top = name + slayer, bottom = amount.
    // Spacing tuned for small marker stands so the hologram stays compact.
    private static final double LABEL_BOTTOM_Y_OFFSET = 0.85D;
    private static final double LABEL_TOP_Y_OFFSET = 1.10D;

    private final PlayerBountiesOG plugin;
    private final Map<HeadLabelKey, LabelPair> activeLabels = new HashMap<>();
    private final BukkitTask tickTask;

    public BountyHeadLabelListener(PlayerBountiesOG plugin) {

        this.plugin = plugin;
        this.tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, UPDATE_PERIOD_TICKS,
                UPDATE_PERIOD_TICKS);

    }

    public void shutdown() {

        if (tickTask != null) {

            tickTask.cancel();

        }

        clearLabels();

    }

    private void tick() {

        if (!isHoverModeEnabled()) {

            clearLabels();
            return;

        }

        final Set<HeadLabelKey> visibleTargets = new HashSet<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {

            final HeadTarget target = findTargetedHead(player);
            if (target == null) {

                continue;

            }

            visibleTargets.add(target.key());
            upsertLabel(target);

        }

        final Set<HeadLabelKey> staleLabels = new HashSet<>(activeLabels.keySet());
        staleLabels.removeAll(visibleTargets);
        staleLabels.forEach(this::removeLabel);

    }

    private HeadTarget findTargetedHead(Player player) {

        final double maxRange = Math.max(1D, plugin.getConfig().getDouble("bounty-head-label-range", 8D));
        final RayTraceResult rayTrace = player.getWorld().rayTraceBlocks(player.getEyeLocation(),
                player.getEyeLocation().getDirection(), maxRange, FluidCollisionMode.NEVER, true);
        if (rayTrace == null) {

            return null;

        }

        final Block hitBlock = rayTrace.getHitBlock();
        if (hitBlock == null || !isHeadMaterial(hitBlock.getType())) {

            return null;

        }

        final BlockState state = hitBlock.getState();
        if (!(state instanceof Skull skull)) {

            return null;

        }

        final BountyHeadData headData = plugin.getBountyHeadMetadata().read(skull.getPersistentDataContainer());
        if (headData == null) {

            return null;

        }

        return new HeadTarget(HeadLabelKey.fromBlock(hitBlock), hitBlock.getLocation(), headData);

    }

    private void upsertLabel(HeadTarget target) {

        final LabelPair existing = activeLabels.get(target.key());
        if (existing != null && existing.top().isValid() && existing.bottom().isValid()) {

            existing.top().customName(plugin.getBountyHeadFormatter().buildCanonicalLineTop(target.data()));
            existing.bottom().customName(plugin.getBountyHeadFormatter().buildCanonicalLineBottom(target.data()));
            return;

        }

        if (existing != null) {

            removeLabel(target.key());

        }

        final World world = target.location().getWorld();
        final Location bottomLocation = target.location().clone().add(0.5D, LABEL_BOTTOM_Y_OFFSET, 0.5D);
        final Location topLocation = target.location().clone().add(0.5D, LABEL_TOP_Y_OFFSET, 0.5D);

        final ArmorStand top = world.spawn(topLocation, ArmorStand.class, stand -> {

            configureLabelStand(stand);
            stand.customName(plugin.getBountyHeadFormatter().buildCanonicalLineTop(target.data()));

        });

        final ArmorStand bottom = world.spawn(bottomLocation, ArmorStand.class, stand -> {

            configureLabelStand(stand);
            stand.customName(plugin.getBountyHeadFormatter().buildCanonicalLineBottom(target.data()));

        });

        activeLabels.put(target.key(), new LabelPair(top, bottom));

    }

    private static void configureLabelStand(ArmorStand stand) {

        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setInvulnerable(true);
        stand.setSilent(true);
        stand.setPersistent(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setCustomNameVisible(true);

    }

    private void removeLabel(HeadLabelKey key) {

        final LabelPair pair = activeLabels.remove(key);
        if (pair == null) {

            return;

        }

        if (pair.top() != null && pair.top().isValid()) {

            pair.top().remove();

        }

        if (pair.bottom() != null && pair.bottom().isValid()) {

            pair.bottom().remove();

        }

    }

    private void clearLabels() {

        final Set<HeadLabelKey> keys = new HashSet<>(activeLabels.keySet());
        keys.forEach(this::removeLabel);

    }

    private boolean isHoverModeEnabled() {

        return StringUtils.equalsIgnoreCase(plugin.getConfig().getString("bounty-head-label-mode", "hover"), "hover");

    }

    private static boolean isHeadMaterial(Material material) {

        return material == Material.PLAYER_HEAD || material == Material.PLAYER_WALL_HEAD;

    }

    private record HeadTarget(HeadLabelKey key, Location location, BountyHeadData data) {
    }

    private record LabelPair(ArmorStand top, ArmorStand bottom) {
    }

    private record HeadLabelKey(UUID worldId, int x, int y, int z) {

        private static HeadLabelKey fromBlock(Block block) {

            final World world = block.getWorld();

            return new HeadLabelKey(world.getUID(), block.getX(), block.getY(), block.getZ());

        }

    }

}