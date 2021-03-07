package de.ancash.bazaar.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.bazaar.utils.FileUtils;

public class Files {

	private static File inventoryFile = new File("plugins/Bazaar/inventory.yml");
	private static FileConfiguration invConfig = YamlConfiguration.loadConfiguration(inventoryFile);
	private static File config = new File("plugins/Bazaar/config.yml");
	private static FileConfiguration cfg = YamlConfiguration.loadConfiguration(config);
	
	public Files(Plugin plugin) {
		Chat.sendMessage("Loading Files... ", ChatLevel.INFO);
		try {
			if(!config.exists()) {
				FileUtils.copyInputStreamToFile(plugin.getResource("resources/config.yml"), config);
				Chat.sendMessage("Create new Config File", ChatLevel.INFO);
			}
			cfg.load(config);
			if(!inventoryFile.exists()) {
				FileUtils.copyInputStreamToFile(plugin.getResource("resources/inventory.yml"), inventoryFile);
				Chat.sendMessage("Create new Inventory File", ChatLevel.INFO);
			}
			invConfig.load(inventoryFile);
		} catch (IOException | InvalidConfigurationException e) {
			Chat.sendMessage("Error while loading files. Disabling plugin...", ChatLevel.FATAL);
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
		}
	}

	public static FileConfiguration getInvConfig() {return invConfig;}
	public static FileConfiguration getConfig() {return cfg;}
	/* PLACE HOLDER
	 * 
	 * %buy_price_lowest%
	 * %buy_price_highest%
	 * %sell_price_lowest%
	 * %sell_price_highest%
	 * %orders_total%
	 * %orders_content%
	 * %offers_total%
	 * %offers_content%
	 * 
	 */
	/*
	 *  item:
	 *  	type: LOL
	 *  	amount: 12
	 *  	meta:
	 *  		displayname: lol
	 *  		lore: 
	 */
}
