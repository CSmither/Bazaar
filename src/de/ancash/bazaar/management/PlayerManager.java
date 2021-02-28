package de.ancash.bazaar.management;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import de.ancash.bazaar.utils.BuyOrder;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.bazaar.utils.SellOffer;

public final class PlayerManager {
	
	private static HashMap<UUID, PlayerManager> registered = new HashMap<UUID, PlayerManager>();
	
	private final UUID id;
	private final File sellOfferFile;
	private final File buyOrderFile;
	private Inventory bazaar_main = Bukkit.createInventory(null, 5 * 9, "Bazaar");
	private Inventory how_many_do_you_want = Bukkit.createInventory(null, 36, "How many do you want?");
	private Inventory how_much_do_you_want_to_pay = Bukkit.createInventory(null, 36, "How much do you want to pay?");
	private Inventory at_what_price_are_you_selling = Bukkit.createInventory(null, 36, "At what price are you selling?");
	private Inventory confirm_sell_offer = Bukkit.createInventory(null, 36, "Confirm Sell Offer");
	private Inventory manageEnquiries = Bukkit.createInventory(null, 5 * 9, "Your Bazaar Enquiries");
	
	public Inventory getConfirmSellOffer() {return confirm_sell_offer;}
	public Inventory getAtWhatPriceAreYouSelling() {return at_what_price_are_you_selling;}
	public Inventory getHowMuchDoYouWantToPay() {return how_much_do_you_want_to_pay;}
	public Inventory getHowManyDoYouWant() {return how_many_do_you_want;}
	public Inventory getBazaarMain() {return bazaar_main;}
	public Inventory getManageEnquiries() {return manageEnquiries;}
	
	public PlayerManager(Player p) {
		
		this.id = p.getUniqueId();
		sellOfferFile = new File("plugins/Bazaar/player/" + p.getUniqueId().toString() + "/sell_offer.yml");
		buyOrderFile = new File("plugins/Bazaar/player/" + p.getUniqueId().toString() + "/buy_order.yml");
		if(!sellOfferFile.exists()) {
			try {
				sellOfferFile.mkdirs();
				sellOfferFile.delete();
				sellOfferFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!buyOrderFile.exists()) {
			try {
				buyOrderFile.mkdirs();
				buyOrderFile.delete();
				buyOrderFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		registered.put(p.getUniqueId(), this);
	}
	
	public int getEnquiries() {
		return YamlConfiguration.loadConfiguration(sellOfferFile).getKeys(false).size() + YamlConfiguration.loadConfiguration(buyOrderFile).getKeys(false).size();
	}
	
	public static void remove(UUID id) {
		registered.remove(id);
	}
	
	public UUID getId() {
		return id;
	}
	
	public static PlayerManager get(UUID id) {
		return registered.get(id);
	}	
	
	public static void clear() {
		registered.clear();
	}
	
	public int getClaimableItems() {
		int claimable = 0;
		FileConfiguration fc = YamlConfiguration.loadConfiguration(buyOrderFile);
		for(String id : fc.getKeys(false)) {
			SelfBalancingBST root = Category.getCategory(fc.getInt(id + ".category")).getBuyOrder(fc.getInt(id + ".item_id"));
			SelfBalancingBSTNode buyOrder = root.get(fc.getDouble(id + ".price"), root.getRoot());
			
			if(buyOrder != null && buyOrder.get(UUID.fromString(id)) != null) {
				claimable += buyOrder.get(UUID.fromString(id)).getClaimable();
			} else {
				claimable += fc.getInt(id + ".claimable");
			}
		}
		return claimable;
	}
	
	public double getClaimableCoins() {
		double claimable = 0;
		FileConfiguration fc = YamlConfiguration.loadConfiguration(sellOfferFile);
		for(String id : fc.getKeys(false)) {
			SelfBalancingBST root = Category.getCategory(fc.getInt(id + ".category")).getSellOffer(fc.getInt(id + ".item_id"));
			SelfBalancingBSTNode sellOffer = root.get(fc.getDouble(id + ".price"), root.getRoot());
			
			if(sellOffer != null && sellOffer.get(UUID.fromString(id)) != null) {
				claimable += sellOffer.get(UUID.fromString(id)).getClaimable() * fc.getDouble(id + ".price");
			} else {
				claimable += fc.getInt(id + ".claimable") * fc.getDouble(id + ".price");
			}
		}
		return claimable;
	}
	
	public HashMap<String, HashMap<String, Number>> getSellOffer() {
		HashMap<String, HashMap<String, Number>> allSellOffer = new HashMap<String, HashMap<String, Number>>();
		FileConfiguration fc = YamlConfiguration.loadConfiguration(sellOfferFile);
		for(String id : fc.getKeys(false)) {
			int total = fc.getInt(id + ".amount");
			double price = fc.getDouble(id + ".price");
			int category = fc.getInt(id + ".category");
			int item_id = fc.getInt(id + ".item_id");
			int claimable = fc.getInt(id + ".claimable");
			
			HashMap<String, Number> datas = new HashMap<String, Number>();
			datas.put("total", total);
			datas.put("price", price);
			datas.put("category", category);
			datas.put("item_id", item_id);
			
			if(fc.getInt(id + ".left") == 0) {
				datas.put("left", 0);
				datas.put("claimable", claimable);
			} else {
				SelfBalancingBST root = Category.getCategory(category).getSellOffer(item_id);
				SelfBalancingBSTNode sellOffer = root.get(fc.getDouble(id + ".price"), root.getRoot());
				SellOffer sO = (SellOffer) sellOffer.get(UUID.fromString(id));
				if(sO == null) {
					Chat.sendMessage("NPE While Getting All Enquiries For A Player! ID: " + id, ChatLevel.WARN);
					continue;
				}
				datas.put("left", sO.getLeft());
				datas.put("claimable", sO.getClaimable());
			}
			allSellOffer.put(id, datas);
		}
		return allSellOffer;
	}
	
	public HashMap<String, HashMap<String, Number>> getBuyOrder() {
		HashMap<String, HashMap<String, Number>> allBuyOrder = new HashMap<String, HashMap<String, Number>>();
		FileConfiguration fc = YamlConfiguration.loadConfiguration(buyOrderFile);
		for(String id : fc.getKeys(false)) {
			int total = fc.getInt(id + ".amount");
			double price = fc.getDouble(id + ".price");
			int category = fc.getInt(id + ".category");
			int item_id = fc.getInt(id + ".item_id");
			int claimable = fc.getInt(id + ".claimable");
			
			HashMap<String, Number> datas = new HashMap<String, Number>();
			datas.put("total", total);
			datas.put("price", price);
			datas.put("category", category);
			datas.put("item_id", item_id);
			
			if(fc.getInt(id + ".left") == 0) {
				datas.put("left", 0);
				datas.put("claimable", claimable);
			} else {
				SelfBalancingBST root = Category.getCategory(category).getBuyOrder(item_id);
				SelfBalancingBSTNode buyOrder = root.get(fc.getDouble(id + ".price"), root.getRoot());
				if(buyOrder == null || buyOrder.get(UUID.fromString(id)) == null) {
					Chat.sendMessage("NPE While Getting All Enquiries For A Player! ID: " + id, ChatLevel.WARN);
					continue;
				}
				BuyOrder sO = (BuyOrder) buyOrder.get(UUID.fromString(id));
				datas.put("left", sO.getLeft());
				datas.put("claimable", sO.getClaimable());
			}
			allBuyOrder.put(id, datas);
		}
		return allBuyOrder;
	}
}
