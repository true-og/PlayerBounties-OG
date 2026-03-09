package com.tcoded.playerbountiesplus.hook.team;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.tcoded.playerbountiesplus.PlayerBountiesOG;

import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;

public class SimpleClansHook extends AbstractTeamHook {

    public SimpleClansHook(PlayerBountiesOG plugin, Plugin teamPlugin) {

        super(plugin, teamPlugin);

    }

    @Override
    public boolean isFriendly(Player player1, Player player2) {

        final ClanManager clanManager = SimpleClans.getInstance().getClanManager();

        final UUID uuid1 = player1.getUniqueId();
        final UUID uuid2 = player2.getUniqueId();

        final Clan clan1 = clanManager.getClanByPlayerUniqueId(uuid1);
        final Clan clan2 = clanManager.getClanByPlayerUniqueId(uuid2);
        if (clan1 == null) {

            return false;

        }

        if (clan2 == null) {

            return false;

        }

        return clan1 == clan2;

    }

}