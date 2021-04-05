package de.ancash.bazaar;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import de.ancash.bazaar.commands.OpenBazaarCMD;
import de.ancash.bazaar.files.Files;
import de.ancash.bazaar.listeners.Listeners;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Enquiry;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.bazaar.utils.InventoryTemplates;
import de.ancash.bazaar.utils.ItemFromFile;
import net.milkbowl.vault.economy.Economy;

public class Bazaar extends BazaarManager{
	
	private static Bazaar plugin;
	public static InventoryTemplates bazaarTemplate;
	private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    
	public void onEnable() {
		long now = System.currentTimeMillis();
		plugin = this;
		
		Chat.sendMessage("Loading..", ChatLevel.INFO);
		
		new Files(plugin);
		new Listeners(plugin);
		registerCommands();
		
		loadTemplates();
		
		try {
			Category.init(plugin);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		
		Enquiry.load();
		
		if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
        for(Player p : Bukkit.getOnlinePlayers()) {
			try {
				new PlayerManager(p.getUniqueId());
			} catch (IOException | de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
        if(Files.getConfig().getBoolean("multipleServers")) {
        	setupMultipleServersSupport();
        }
        Chat.sendMessage("Done(" + ((double) (System.currentTimeMillis() - now) / 1000) + "s)!", ChatLevel.INFO);
	}
	
	public void onDisable() {
		PlayerManager.clear();
		Chat.sendMessage("Saving Enquiries...", ChatLevel.INFO);
		long now = System.currentTimeMillis();
		Enquiry.stop();
		Chat.sendMessage("Saving took " + (System.currentTimeMillis() - now) + "ms",ChatLevel.INFO);
		PlayerManager.clear();
	}
	
	private void registerCommands() {
		Chat.sendMessage("Registering Commands...", ChatLevel.INFO);
		getCommand("bz").setExecutor(new OpenBazaarCMD());
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	private void loadTemplates() {
		Chat.sendMessage("Loading Template...", ChatLevel.INFO);
		Inventory inv = Bukkit.createInventory(null, 5 * 9, Files.getInvConfig().getString("inventory.name"));
		ItemStack pane = ItemFromFile.get(Files.getInvConfig(), "inventory.background");
		for(int i = 0; i<5; i++) {
			ItemStack category = ItemFromFile.get(Files.getInvConfig(), "inventory.categories." + (i + 1) + ".item");
			for(Enchantment e : category.getEnchantments().keySet()) {
				category.removeEnchantment(e);
			}
			inv.setItem(i * 9, category);
			inv.setItem(i * 9 + 1, pane);
			inv.setItem(i * 9 + 8, pane);
		}
		for(int i = 1; i<9; i++) {
			inv.setItem(i, pane);
			inv.setItem(i + 36, pane);
		}
		inv.setItem(40, ItemFromFile.get(Files.getInvConfig(), "inventory.close"));
		inv.setItem(41, ItemFromFile.get(Files.getInvConfig(), "inventory.manageEnquiries"));
		bazaarTemplate = new InventoryTemplates(inv);
	}
	
	public static Economy getEconomy() {return econ;}
	public static Bazaar getInstance() {return plugin;}
}