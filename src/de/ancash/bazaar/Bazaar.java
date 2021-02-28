package de.ancash.bazaar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.bazaar.commands.OpenBazaarCMD;
import de.ancash.bazaar.files.Files;
import de.ancash.bazaar.listeners.Listeners;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Enquiry;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.bazaar.utils.InventoryTemplates;
import de.ancash.bazaar.utils.ItemFromFile;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Bazaar extends JavaPlugin{

	private static Plugin plugin;
	public static InventoryTemplates bazaarTemplate;
	private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private static Permission perms = null;
	
	public void onEnable() {
		long now = System.currentTimeMillis();
		plugin = this;
		
		Chat.sendMessage("Loading..", ChatLevel.INFO);
		
		Chat.sendMessage("Loading Files... ", ChatLevel.INFO);
		new Files(plugin);
		
		Chat.sendMessage("Registering Listeners...", ChatLevel.INFO);
		new Listeners(plugin);
		
		Chat.sendMessage("Registering Commands...", ChatLevel.INFO);
		getCommand("bz").setExecutor(new OpenBazaarCMD());
		
		Chat.sendMessage("Loading Template...", ChatLevel.INFO);
		try {
			loadTemplates();
		} catch (IOException | InvalidConfigurationException e) {
			Chat.sendMessage("Error while loading files. Disabling plugin...", ChatLevel.FATAL);
			e.printStackTrace();
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return;
		}
		
		Chat.sendMessage("Loading Categories...", ChatLevel.INFO);
		try {
			Category.init(plugin);
		} catch (IOException | InvalidConfigurationException e) {
			Chat.sendMessage("Error while loading Categories. Disabling plugin...", ChatLevel.FATAL);
			e.printStackTrace();
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return;
		}
		
		Chat.sendMessage("Loading Enquiries from Files...", ChatLevel.INFO);
		Enquiry.load();
		
		if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
		
        for(Player p : Bukkit.getOnlinePlayers()) {
			new PlayerManager(p);
		}
		Chat.sendMessage("Done(" + ((double) (System.currentTimeMillis() - now) / 1000) + "s)!", ChatLevel.INFO);
	}
	
	public void onDisable() {
		PlayerManager.clear();
		Chat.sendMessage("Saving Enquiries...", ChatLevel.INFO);
		long now = System.currentTimeMillis();
		for(int c = 1; c<=5; c++) {
			if(Category.exists(c)) {
				Category cat = Category.getCategory(c);
				for(SelfBalancingBST root : cat.getAll()) {
					for(SelfBalancingBSTNode node : root.getAllNodes(root.getRoot())) {
						for(UUID id : node.get().keySet()) {
							Enquiry.save(node.get().get(id));
						}
					}
				}
			}
		}
		Chat.sendMessage("Saving took " + (System.currentTimeMillis() - now) + "ms",ChatLevel.INFO);
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
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
	
	private void loadTemplates() throws FileNotFoundException, IOException, InvalidConfigurationException {
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
    
    public static Permission getPermissions() {return perms;}
}
/*
OAK_LOG:
material: LOG
group: OAK_LOG
data: 0

SPRUCE_LOG:
material: LOG
group: SPRUCE_LOG
data: 1

BIRCH_LOG:
material: LOG
group: BIRCH_LOG
data: 2

DARK_OAK_LOG:
material: LOG_2
group: DARK_OAK_LOG
data: 1

ACACIA_LOG:
material: LOG_2
group: ACACIA_LOG
data: 0

JUNGLE_LOG:
material: LOG
group: JUNGLE_LOG
data: 3

RAW_FISH:
material: RAW_FISH
group: RAW_FISH
data: 0

SALMON:
material: RAW_FISH
group: SALMON
data: 1

CLOWNFISH:
material: RAW_FISH
group: CLOWNFISH
data: 2

PUFFERFISH:
material: RAW_FISH
group: PUFFERFISH
data: 3

*/