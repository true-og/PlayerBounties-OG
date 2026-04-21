package com.tcoded.playerbountiesplus.util;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;
import com.tcoded.playerbountiesplus.listener.DeathListener;

public final class BountyHeadMetadata {

    private final NamespacedKey targetKey;
    private final NamespacedKey targetUuidKey;
    private final NamespacedKey amountKey;

    public BountyHeadMetadata(PlayerBountiesOG plugin) {

        this.targetKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_NAME_KEY);
        this.targetUuidKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_UUID_KEY);
        this.amountKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_AMOUNT_KEY);

    }

    public BountyHeadData read(PersistentDataContainer dataContainer) {

        final String targetName = dataContainer.get(targetKey, PersistentDataType.STRING);
        final String targetUuid = dataContainer.get(targetUuidKey, PersistentDataType.STRING);
        final Double bountyAmount = dataContainer.get(amountKey, PersistentDataType.DOUBLE);

        if (StringUtils.isBlank(targetName) || StringUtils.isBlank(targetUuid) || bountyAmount == null) {

            return null;

        }

        try {

            return new BountyHeadData(UUID.fromString(targetUuid), targetName, bountyAmount);

        } catch (IllegalArgumentException ignored) {

            return null;

        }

    }

    public void write(PersistentDataContainer dataContainer, BountyHeadData data) {

        dataContainer.set(targetKey, PersistentDataType.STRING, data.targetName());
        dataContainer.set(targetUuidKey, PersistentDataType.STRING, data.targetUuid().toString());
        dataContainer.set(amountKey, PersistentDataType.DOUBLE, data.bountyAmount());

    }

}