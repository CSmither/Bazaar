package de.ancash.bazaar.sockets;

import java.io.File;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.bazaar.sockets.eqnuiry.EnquiryUtils;
import de.ancash.bazaar.sockets.events.EnquiryFillEvent;
import de.ancash.bazaar.sockets.listeners.EnquiryListener;
import de.ancash.bazaar.sockets.listeners.PacketListener;
import de.ancash.bazaar.sockets.management.Category;
import de.ancash.bazaar.sockets.management.SellOffer;
import de.ancash.libs.org.bukkit.event.EventManager;

public class BazaarSocketPlugin extends JavaPlugin{

	public static final String FILE_PATH = "plugins/BazaarSocketPlugin";
	private final EnquiryUtils utils = new EnquiryUtils();
	
	@Override
	public void onEnable() {
		if(!new File(FILE_PATH).exists())
			new File(FILE_PATH).mkdir();
		if(!new File(FILE_PATH + "/player").exists())
			new File(FILE_PATH + "/player").mkdir();
		
		
		new Category(this);
		utils.load();
		EventManager.registerEvents(new EnquiryListener(), this);
		EventManager.registerEvents(new PacketListener(this), this);
		EventManager.callEvent(new EnquiryFillEvent(new SellOffer(1, 1, UUID.randomUUID(), 1, 1, 1)));
	}
	
	@Override
	public void onDisable() {
		utils.save();
	}
	
	public EnquiryUtils getEnquiryUtils() {
		return utils;
	}
}