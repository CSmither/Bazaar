package de.ancash.bazaar.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.ancash.bazaar.management.PlayerManager;

public class PlayerJoin implements Listener{

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		new PlayerManager(e.getPlayer());
	}
	
}
