package de.ancash.bazaar.listeners;

import java.io.IOException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.ancash.bazaar.management.PlayerManager;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

public class PlayerJoin implements Listener{

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) throws InvalidConfigurationException, IOException {
		new PlayerManager(e.getPlayer().getUniqueId());
	}
}
