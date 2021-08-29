package de.ancash.bazaar;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import de.ancash.bazaar.commands.BazaarCommand;
import de.ancash.bazaar.exception.AsyncBazaarException;
import de.ancash.bazaar.gui.BazaarBuyInstantlyImpl;
import de.ancash.bazaar.gui.BazaarCreateBuyOrderImpl;
import de.ancash.bazaar.gui.BazaarCreateSellOfferImpl;
import de.ancash.bazaar.gui.BazaarIGUIImpl;
import de.ancash.bazaar.gui.BazaarMainImpl;
import de.ancash.bazaar.gui.BazaarManageEnquiriesImpl;
import de.ancash.bazaar.gui.BazaarSellInstantlyImpl;
import de.ancash.bazaar.gui.BazaarSubImpl;
import de.ancash.bazaar.gui.BazaarSubSubImpl;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.gui.base.BazaarInventoryObjects;
import de.ancash.bazaar.gui.base.BazaarRunnable;
import de.ancash.bazaar.listener.Listener;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.EnquiryUtils;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.sockets.gui.BazaarBuyInstantlySocketImpl;
import de.ancash.bazaar.sockets.gui.BazaarCreateBuyOrderSocketImpl;
import de.ancash.bazaar.sockets.gui.BazaarCreateSellOfferSocketImpl;
import de.ancash.bazaar.sockets.gui.BazaarIGUISocketImpl;
import de.ancash.bazaar.sockets.gui.BazaarMainSocketImpl;
import de.ancash.bazaar.sockets.gui.BazaarManageEnquiriesSocketImpl;
import de.ancash.bazaar.sockets.gui.BazaarSellInstantlySocketImpl;
import de.ancash.bazaar.sockets.gui.BazaarSubSocketImpl;
import de.ancash.bazaar.sockets.gui.BazaarSubSubSocketImpl;
import de.ancash.bazaar.sockets.listener.PacketListener;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.libs.org.bukkit.event.EventManager;
import de.ancash.bazaar.utils.InventoryTemplates;
import de.ancash.bazaar.utils.Response;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.misc.FileUtils;
import de.ancash.misc.MathsUtils;

import net.milkbowl.vault.economy.Economy;

import static de.ancash.bazaar.utils.Chat.*;

public class Bazaar extends JavaPlugin{
	
	private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
	private final Set<UUID> running = new HashSet<>();
	
	public void submit(BazaarRunnable r) {
		synchronized (running) {
			if(running.contains(r.getUUID())) {
				Bukkit.getPlayer(r.getUUID()).sendMessage("§cYou are clicking too fast!");
				return;
			} else {
				running.add(r.getUUID());
			}
		}
		asyncExecutor.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					r.run();
				} catch(Exception ex) {
					if(r.getUUID() != null) Bukkit.getPlayer(r.getUUID()).sendMessage("§cSomething went wrong!");
					new AsyncBazaarException("Error while executing async runnable:", ex).printStackTrace();	
				} finally {
					synchronized (running) {
						running.remove(r.getUUID());
					}
					
					new BukkitRunnable() {
						
						@Override
						public void run() {
							Bukkit.getPlayer(r.getUUID()).closeInventory();
						}
					}.runTask(plugin);
				}
			}
		});
	}
	
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
	
    private Economy econ = null;
    private Response response;
    private boolean multipleServer;    
    
	public void onEnable() {
		final long now = System.currentTimeMillis();
		plugin = this;
		sendMessage("Loading...", ChatLevel.INFO);
		if (!setupEconomy() ) {
            sendMessage("Disabled due to no Vault dependency found!", ChatLevel.FATAL);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		loadFiles();
		
		multipleServer = cfg.getBoolean("multipleServers");
		
		if(!multipleServer) {
			setupSingleServer();
		} else {
			setupSocketsSupport();
		}
		sendMessage("Done! " + MathsUtils.round(((double) (System.currentTimeMillis() - now) / 1000D), 3) + " s");
	}
	
	public void onDisable() {
		if(!multipleServer) {
			PlayerManager.clear();
			Chat.sendMessage("Saving Enquiries...", ChatLevel.INFO);
			long now = System.currentTimeMillis();
			enquiryUtils.save();
			Chat.sendMessage("Saving took " + (System.currentTimeMillis() - now) + " ms",ChatLevel.INFO);
		}
	}

	private void setupSingleServer() {
		sendMessage("Registering classes for single server...");
		clazzManager.register(BazaarBuyInstantlyImpl.class);
		clazzManager.register(BazaarCreateBuyOrderImpl.class);
		clazzManager.register(BazaarCreateSellOfferImpl.class);
		clazzManager.register(BazaarIGUIImpl.class);
		clazzManager.register(BazaarMainImpl.class);
		clazzManager.register(BazaarManageEnquiriesImpl.class);
		clazzManager.register(BazaarSellInstantlyImpl.class);
		clazzManager.register(BazaarSubImpl.class);
		clazzManager.register(BazaarSubSubImpl.class);
		sendMessage("Registered classes!");
		enquiryUtils = new EnquiryUtils(plugin);
		enquiryUtils.load();
		new Listener(plugin);
		registerCommands();
		Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).forEach(PlayerManager::newPlayerManager);
	}
	
	private void setupSocketsSupport() {
		sendMessage("Registering classes for socket support...");
		clazzManager.register(BazaarBuyInstantlySocketImpl.class);
		clazzManager.register(BazaarCreateBuyOrderSocketImpl.class);
		clazzManager.register(BazaarCreateSellOfferSocketImpl.class);
		clazzManager.register(BazaarIGUISocketImpl.class);
		clazzManager.register(BazaarMainSocketImpl.class);
		clazzManager.register(BazaarManageEnquiriesSocketImpl.class);
		clazzManager.register(BazaarSellInstantlySocketImpl.class);
		clazzManager.register(BazaarSubSocketImpl.class);
		clazzManager.register(BazaarSubSubSocketImpl.class);
		sendMessage("Registered classes!");
		sendMessage("Registering Listeners...", ChatLevel.INFO);
		EventManager.registerEvents(new PacketListener(this), this);
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
			invConfig.load(inventoryFile);
			BazaarInventoryObjects.load();
			TAX = cfg.getInt("tax");
			response = new Response(this);
			loadTemplate();
			category = new Category(this);
		} catch (IOException | InvalidConfigurationException | ClassNotFoundException e) {
			Chat.sendMessage("Error while loading files. Disabling plugin...", ChatLevel.FATAL);
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
		}
	}
	
	private void registerCommands() {
		Chat.sendMessage("Registering Commands...", ChatLevel.INFO);
		getCommand("bz").setExecutor(new BazaarCommand(this));
	}
	
	
	@SuppressWarnings("deprecation")
	private void loadTemplate() throws ClassNotFoundException, IOException {
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