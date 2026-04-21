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
    private final NamespacedKey claimantKey;
    private final NamespacedKey claimantUuidKey;

    public BountyHeadMetadata(PlayerBountiesOG plugin) {

        this.targetKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_NAME_KEY);
        this.targetUuidKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_UUID_KEY);
        this.amountKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_AMOUNT_KEY);
        this.claimantKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_CLAIMANT_KEY);
        this.claimantUuidKey = new NamespacedKey(plugin, DeathListener.BOUNTY_HEAD_CLAIMANT_UUID_KEY);

    }

    public BountyHeadData read(PersistentDataContainer dataContainer) {

        final String targetName = dataContainer.get(targetKey, PersistentDataType.STRING);
        final String targetUuid = dataContainer.get(targetUuidKey, PersistentDataType.STRING);
        final Double bountyAmount = dataContainer.get(amountKey, PersistentDataType.DOUBLE);
        final String claimantName = dataContainer.get(claimantKey, PersistentDataType.STRING);
        final String claimantUuid = dataContainer.get(claimantUuidKey, PersistentDataType.STRING);

        if (StringUtils.isBlank(targetName) || StringUtils.isBlank(targetUuid) || bountyAmount == null) {

            return null;

        }

        try {

            return new BountyHeadData(UUID.fromString(targetUuid), targetName, bountyAmount,
                    parseOptionalUuid(claimantUuid), claimantName);

        } catch (IllegalArgumentException ignored) {

            return null;

        }

    }

    public void write(PersistentDataContainer dataContainer, BountyHeadData data) {

        dataContainer.set(targetKey, PersistentDataType.STRING, data.targetName());
        dataContainer.set(targetUuidKey, PersistentDataType.STRING, data.targetUuid().toString());
        dataContainer.set(amountKey, PersistentDataType.DOUBLE, data.bountyAmount());
        if (StringUtils.isBlank(data.claimantName()) || data.claimantUuid() == null) {

            dataContainer.remove(claimantKey);
            dataContainer.remove(claimantUuidKey);
            return;

        }

        dataContainer.set(claimantKey, PersistentDataType.STRING, data.claimantName());
        dataContainer.set(claimantUuidKey, PersistentDataType.STRING, data.claimantUuid().toString());

    }

    private static UUID parseOptionalUuid(String value) {

        if (StringUtils.isBlank(value)) {

            return null;

        }

        try {

            return UUID.fromString(value);

        } catch (IllegalArgumentException ignored) {

            return null;

        }

    }

}
