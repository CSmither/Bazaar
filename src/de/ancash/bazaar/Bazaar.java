package de.ancash.bazaar;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
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

import de.ancash.ILibrary;
import de.ancash.bazaar.commands.BazaarCMD;
import de.ancash.bazaar.gui.BazaarBuyInstantlyInv;
import de.ancash.bazaar.gui.BazaarCreateBuyOrderInv;
import de.ancash.bazaar.gui.BazaarCreateSellOfferInv;
import de.ancash.bazaar.gui.BazaarIGUI;
import de.ancash.bazaar.gui.BazaarMainInv;
import de.ancash.bazaar.gui.BazaarManageEnquiriesInv;
import de.ancash.bazaar.gui.BazaarSellInstantlyInv;
import de.ancash.bazaar.gui.BazaarSubInv;
import de.ancash.bazaar.gui.BazaarSubSubInv;import de.ancash.bazaar.gui.inventory.BazaarInventoryClassManager;
import de.ancash.bazaar.gui.inventory.BazaarInventoryObjects;
import de.ancash.bazaar.listeners.Listeners;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.EnquiryUtils;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.management.Enquiry.EnquiryType;
import de.ancash.bazaar.sockets.ChatClientPacketReceiveListener;
import de.ancash.bazaar.sockets.packets.BazaarEnquiryPacket;
import de.ancash.bazaar.sockets.packets.BazaarRequestPacket;
import de.ancash.bazaar.sockets.packets.BazaarResponsePacket;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.bazaar.utils.InventoryTemplates;
import de.ancash.bazaar.utils.Response;
import de.ancash.libs.org.bukkit.event.EventManager;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.misc.FileUtils;
import de.ancash.sockets.packet.PacketCallback;

import net.milkbowl.vault.economy.Economy;

import static de.ancash.bazaar.utils.Chat.*;

public class Bazaar extends JavaPlugin{
	
	private final BazaarInventoryClassManager clazzManager = new BazaarInventoryClassManager();
	private EnquiryUtils enquiryUtils;
	private Category category;
	
	private final File inventoryFile = new File("plugins/Bazaar/inventory.yml");
	private final FileConfiguration invConfig = YamlConfiguration.loadConfiguration(inventoryFile);
	private final File config = new File("plugins/Bazaar/config.yml");
	private final FileConfiguration cfg = YamlConfiguration.loadConfiguration(config);
	
	private Bazaar plugin;	
	private InventoryTemplates bazaarTemplate;
	private int TAX;
	
	private static final Logger log = Logger.getLogger("Minecraft");
    private Economy econ = null;
    private Response response;
        
	public void onEnable() {
		final long now = System.currentTimeMillis();
		plugin = this;
		sendMessage("Loading...", ChatLevel.INFO);
		if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		loadFiles();
		
		if(!cfg.getBoolean("multipleServers")) {
			setupSingleServer();
		} else {
			setupSocketsSupport();
		}
				
		new Thread(new Runnable() {
			
			@Override
			public synchronized void run() {
				
				if(cfg.getBoolean("multipleServers")) {
					EventManager.registerEvents(new ChatClientPacketReceiveListener(), this);
				}
				try {
					BazaarEnquiryPacket cep = new BazaarEnquiryPacket(EnquiryType.SELL_OFFER, UUID.fromString("64fce50d-e7b7-4c45-88cf-9ab759f411ca"), 99D, 100, 1, 1, 1);
					ILibrary.getInstance().send(cep.getPacket());
					cep = new BazaarEnquiryPacket(EnquiryType.BUY_ORDER, UUID.fromString("64fce50d-e7b7-4c45-88cf-9ab759f411ca"), 99D, 100, 1, 1, 1);
					ILibrary.getInstance().send(cep.getPacket());
					
					BazaarRequestPacket brp = new BazaarRequestPacket(BazaarRequestPacket.Type.CLAIMABLE, 1, 1, 1, new PacketCallback() {
						
						@Override
						public void call(Object arg0) {
							BazaarResponsePacket resp = (BazaarResponsePacket) arg0;
							System.out.println("Callback called:");
							resp.getEntries().forEach(System.out::println);
						}
					}, UUID.fromString("64fce50d-e7b7-4c45-88cf-9ab759f411ca"));
					ILibrary.getInstance().send(brp.getPacket());
				} catch(IOException e) {
					e.printStackTrace();
				}
				Chat.sendMessage("Done(" + ((double) (System.currentTimeMillis() - now) / 1000) + "s)!", ChatLevel.INFO);
			}
		}, "BazaarEnquiryLoader");
	}
	
	public void onDisable() {
		PlayerManager.clear();
		Chat.sendMessage("Saving Enquiries...", ChatLevel.INFO);
		long now = System.currentTimeMillis();
		enquiryUtils.save();
		Chat.sendMessage("Saving took " + (System.currentTimeMillis() - now) + "ms",ChatLevel.INFO);
	}

	private void setupSingleServer() {
		sendMessage("Registering classes for single server...");
		clazzManager.register(BazaarBuyInstantlyInv.class);
		clazzManager.register(BazaarCreateBuyOrderInv.class);
		clazzManager.register(BazaarCreateSellOfferInv.class);
		clazzManager.register(BazaarIGUI.class);
		clazzManager.register(BazaarMainInv.class);
		clazzManager.register(BazaarManageEnquiriesInv.class);
		clazzManager.register(BazaarSellInstantlyInv.class);
		clazzManager.register(BazaarSubInv.class);
		clazzManager.register(BazaarSubSubInv.class);
		sendMessage("Registered classes!");
		enquiryUtils = new EnquiryUtils(plugin);
		enquiryUtils.load();
		new Listeners(plugin);
		registerCommands();
		Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).forEach(PlayerManager::newPlayerManager);
	}
	
	private void setupSocketsSupport() {
		registerCommands();
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
			BazaarInventoryObjects.load();
			TAX = cfg.getInt("tax");
			response = new Response(this);
			loadTemplate();
			category = new Category(this);
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
	
	public BazaarInventoryClassManager getBazaarInvClassManager() {
		return clazzManager;
	}
	
	public InventoryTemplates getTemplate() {
		return bazaarTemplate;
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