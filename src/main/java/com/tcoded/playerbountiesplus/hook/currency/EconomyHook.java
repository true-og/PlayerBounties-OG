package com.tcoded.playerbountiesplus.hook.currency;

import org.bukkit.entity.Player;

public interface EconomyHook {

    boolean giveEco(Player killer, Player victim, double diamonds);

    boolean takeEco(Player killer, Player victim, double diamonds);

}