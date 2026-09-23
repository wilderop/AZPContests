package com.xai.contestplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final ContestManager manager;

    public PlayerListener(ContestManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        manager.givePendingPrizes(event.getPlayer());
        manager.notifyPendingWins(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Save player's score if they're in a contest
        manager.savePlayerScore(event.getPlayer());
    }
}
