package de.ancash.bazaar;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.commands.BazaarCMD;
import de.ancash.bazaar.gui.BazaarIGUI;
import de.ancash.bazaar.listeners.Listeners;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.EnquiryUtils;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.bazaar.utils.InventoryTemplates;
import de.ancash.bazaar.utils.Response;
import de.ancash.ILibrary;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.misc.FileUtils;
import de.ancash.sockets.client.NIOClient;
import net.milkbowl.vault.economy.Economy;

public class Bazaar extends JavaPlugin{
	
	private EnquiryUtils enquiryUtils;
	private Category category;
	
	private final File inventoryFile = new File("plugins/Bazaar/inventory.yml");
	private final FileConfiguration invConfig = YamlConfiguration.loadConfiguration(inventoryFile);
	private final File config = new File("plugins/Bazaar/config.yml");
	private final FileConfiguration cfg = YamlConfiguration.loadConfiguration(config);
	
	private Bazaar plugin;	
	public InventoryTemplates bazaarTemplate;
	private int TAX;
	
	private static final Logger log = Logger.getLogger("Minecraft");
    private Economy econ = null;
    private Response response;
    
	public void onEnable() {
		long now = System.currentTimeMillis();
		
		plugin = this;
		Chat.sendMessage("Loading..", ChatLevel.INFO);
		if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		loadFiles();
		response = new Response(this);
		BazaarIGUI.load(this);
		loadTemplate();
		category = new Category(this);
		
		enquiryUtils = new EnquiryUtils(this);
		new Listeners(this);
		
		registerCommands();
		
		enquiryUtils.load();
		
		TAX = cfg.getInt("tax");
		Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).forEach(PlayerManager::newPlayerManager);
        
		//if(cfg.getBoolean("multipleServers")) 
        //	setupMultipleServersSupport(this);
        
        Chat.sendMessage("Done(" + ((double) (System.currentTimeMillis() - now) / 1000) + "s)!", ChatLevel.INFO);
	}
	
	public void onDisable() {
		PlayerManager.clear();
		Chat.sendMessage("Saving Enquiries...", ChatLevel.INFO);
		long now = System.currentTimeMillis();
		enquiryUtils.stop();
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
	
	private void loadFiles() {
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
	
	private void registerCommands() {
		Chat.sendMessage("Registering Commands...", ChatLevel.INFO);
		getCommand("bz").setExecutor(new BazaarCMD(this));
	}
	
	
	private void loadTemplate() {
		Chat.sendMessage("Loading Template...", ChatLevel.INFO);
		Inventory inv = Bukkit.createInventory(null, 5 * 9, getInvConfig().getString("inventory.name"));
		ItemStack pane = ItemStackUtils.get(getInvConfig(), "inventory.background");
		for(int i = 0; i<5; i++) {
			ItemStack category = ItemStackUtils.get(getInvConfig(), "inventory.categories." + (i + 1) + ".item");
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
		inv.setItem(40, ItemStackUtils.get(getInvConfig(), "inventory.close"));
		inv.setItem(41, ItemStackUtils.get(getInvConfig(), "inventory.manageEnquiries"));
		bazaarTemplate = new InventoryTemplates(inv);
	}
	
	private static NIOClient client = null;
	
	
	public boolean canSendPackets() {
		return (client != null) || !ILibrary.getInstance().isChatClientNull();
	}
	
	public boolean send(String packet) {
		if(client == null && ILibrary.getInstance().isChatClientNull()) return false;
		if(!ILibrary.getInstance().isChatClientNull()) {
			ILibrary.getInstance().send(packet);
			return true;
		}
		return false;
	}
		
	protected void setupMultipleServersSupport(Bazaar instance){
		Chat.sendMessage("Setting up multiple servers support!", ChatLevel.INFO);
    	if(!ILibrary.getInstance().isSocketNull()) {
    		Chat.sendMessage("Active Server Socket running on this server!", ChatLevel.INFO);
    	} else {
    		Chat.sendMessage("No Server Socket running on this Sever!", ChatLevel.INFO);
    	}
    	if(!ILibrary.getInstance().isChatClientNull()) {
    		Chat.sendMessage("Chat Client running on this Server!", ChatLevel.INFO);
    	} else {
    		Chat.sendMessage("No Chat Client running on this server!", ChatLevel.INFO);
    	}
    	
    	new BukkitRunnable() {
			@Override
			public void run() {
				if(client != null) {
					Chat.sendMessage("Successfully started new Chat Client!", ChatLevel.INFO);
				}
				if((client != null) || !ILibrary.getInstance().isChatClientNull()) {
					/*Chat.sendMessage("Pushing everything...", ChatLevel.INFO);
					try {
						EnquiryUpdater.pushAll(plugin);
					} catch (de.ancash.yaml.exceptions.InvalidConfigurationException | IOException e) {
						e.printStackTrace();
					}
					Chat.sendMessage("Fetching everything...", ChatLevel.INFO);
					if(client != null) {
						client.send(PacketBuilder.fetchAll().getPacket().getString());
					} else {
						ILibrary.getInstance().send(PacketBuilder.fetchAll().getPacket().getString());
					}*/
				}
			}
    	}.runTaskAsynchronously(this);
	}
	
	
	public EnquiryUtils getEnquiryUtils() {
		return enquiryUtils;
	}
	
	public Response getResponse() {
		return response;
	}
	
	public FileConfiguration getInvConfig() {
		return invConfig;
	}
	
	public FileConfiguration getConfig() {
		return cfg;
	}
	
	public Economy getEconomy() {
		return econ;
	}

	public Category getCategoryManager() {
		return category;
	}

	public int getTax() {
		return TAX;
	}
}