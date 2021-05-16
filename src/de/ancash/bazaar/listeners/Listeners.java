package de.ancash.bazaar.listeners;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;

public class Listeners {

	public Listeners(Bazaar pl) {
		Chat.sendMessage("Registering Listeners...", ChatLevel.INFO);
		
		PluginManager pm = Bukkit.getServer().getPluginManager();
		
		pm.registerEvents(new PlayerJoin(), pl);
	}
	
}
