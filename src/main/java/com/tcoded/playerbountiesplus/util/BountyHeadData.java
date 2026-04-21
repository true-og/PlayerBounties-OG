package com.tcoded.playerbountiesplus.util;

import java.util.UUID;

public record BountyHeadData(UUID targetUuid, String targetName, double bountyAmount, UUID claimantUuid,
        String claimantName)
{
}
