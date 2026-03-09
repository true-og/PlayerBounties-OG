package com.tcoded.playerbountiesplus.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.command.bounty.BountyAddCmd;

import net.trueog.utilitiesog.UtilitiesOG;

public class BountyIncreasePromptListener implements Listener {

    private final PlayerBountiesOG plugin;
    private final Map<UUID, PendingIncreasePrompt> pendingPrompts = new ConcurrentHashMap<>();

    public BountyIncreasePromptListener(PlayerBountiesOG plugin) {

        this.plugin = plugin;

    }

    public void prompt(UUID setterUuid, UUID targetUuid, String targetName) {

        pendingPrompts.put(setterUuid, new PendingIncreasePrompt(targetUuid, targetName));

    }

    public boolean hasPrompt(UUID setterUuid) {

        return pendingPrompts.containsKey(setterUuid);

    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {

        final PendingIncreasePrompt prompt = pendingPrompts.get(event.getPlayer().getUniqueId());
        if (prompt == null) {

            return;

        }

        event.setCancelled(true);

        final String input = StringUtils.trimToEmpty(event.getMessage());
        if (input.equalsIgnoreCase("cancel")) {

            pendingPrompts.remove(event.getPlayer().getUniqueId());
            UtilitiesOG.trueogMessage(event.getPlayer(), plugin.getLang().getColored("command.bounty.add.cancelled"));
            return;

        }

        final double amount;
        try {

            if (!input.matches("^\\d+(?:\\.\\d)?$")) {

                throw new NumberFormatException("Invalid amount format");

            }

            amount = Double.parseDouble(input);

        } catch (NumberFormatException numberFormatException) {

            UtilitiesOG.trueogMessage(event.getPlayer(), plugin.getLang().getColored("command.bounty.add.amount-nan"));
            return;

        }

        pendingPrompts.remove(event.getPlayer().getUniqueId());
        plugin.getServer().getScheduler().runTask(plugin, () -> BountyAddCmd.addBounty(plugin, event.getPlayer(),
                prompt.targetUuid(), prompt.targetName(), amount, true));

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        pendingPrompts.remove(event.getPlayer().getUniqueId());

    }

    private static record PendingIncreasePrompt(UUID targetUuid, String targetName) {
    }

}
