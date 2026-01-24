package com.tcoded.playerbountiesplus.hook.currency;

import org.bukkit.entity.Player;

public interface EconomyHook {

    boolean isValid();

    void giveEco(Player killer, Player victim, double amount);

    void takeEco(Player killer, Player victim, double amount);

    boolean takeEco(Player killer, Player victim, double amount, boolean force);

}