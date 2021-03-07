package de.ancash.bazaar.listeners;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;

public class Listeners {

	public Listeners(Plugin pl) {
		Chat.sendMessage("Registering Listeners...", ChatLevel.INFO);
		
		PluginManager pm = Bukkit.getServer().getPluginManager();
		
		pm.registerEvents(new BazaarInvClick(), pl);
		pm.registerEvents(new PlayerJoin(), pl);
		pm.registerEvents(new SellInstantlyListener(), pl);
		pm.registerEvents(new BuyInstantlyListener(), pl);
	}
	
}
