package de.ancash.bazaar.management;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import de.ancash.bazaar.utils.BuyOrder;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Enquiry;
import de.ancash.bazaar.utils.SellOffer;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.yaml.configuration.file.YamlFile;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

public final class PlayerManager {
	
	private static CompactMap<UUID, PlayerManager> registered = new CompactMap<UUID, PlayerManager>();
	
	private UUID id;
	private File sellOfferFile;
	private File buyOrderFile;
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
	
	public PlayerManager(UUID uuid) throws InvalidConfigurationException, IOException {
		if(registered.containsKey(uuid)) return;
		
		this.id = uuid;
		sellOfferFile = new File("plugins/Bazaar/player/" + uuid + "/sell_offer.yml");
		buyOrderFile = new File("plugins/Bazaar/player/" + uuid + "/buy_order.yml");
		
		if(!sellOfferFile.exists()) {
			sellOfferFile.mkdirs();
			sellOfferFile.delete();
			try {
				sellOfferFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!buyOrderFile.exists()) {
			buyOrderFile.mkdirs();
			buyOrderFile.delete();
			try {
				buyOrderFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!Enquiry.isYamlFileLoaded(buyOrderFile)){
			YamlFile yamlFile = new YamlFile(buyOrderFile);
			yamlFile.load();
			Enquiry.addYamlFile(buyOrderFile, yamlFile);
		}
		if(!Enquiry.isYamlFileLoaded(sellOfferFile)) {
			YamlFile yamlFile = new YamlFile(sellOfferFile);
			yamlFile.load();
			Enquiry.addYamlFile(sellOfferFile, yamlFile);
		}
		registered.put(uuid, this);
	}
	
	public int getEnquiries() {
		int cnt = 0;
		if(Enquiry.getYamlFile(buyOrderFile).getKeys(false).size() != 0) {
			for(String key : Enquiry.getYamlFile(buyOrderFile).getKeys(false)) {
				if(Enquiry.getYamlFile(buyOrderFile).getString(key) == null
						|| !Enquiry.getYamlFile(buyOrderFile).getString(key).contains("null")) cnt++;
			}
		}
		if(Enquiry.getYamlFile(sellOfferFile).getKeys(false).size() != 0) {
			for(String key : Enquiry.getYamlFile(buyOrderFile).getKeys(false)) {
				if(Enquiry.getYamlFile(buyOrderFile).getString(key) == null
						|| !Enquiry.getYamlFile(buyOrderFile).getString(key).contains("null")) cnt++;
			}
		}
		return cnt;
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
		
		YamlFile fc = Enquiry.getYamlFile(buyOrderFile);
		
		for(String id : fc.getKeys(false)) {
			if(fc.getString(id) != null && fc.getString(id).equals("null")) continue;
			SelfBalancingBST root = Category.getCategory(fc.getInt(id + ".category")).getBuyOrders(fc.getInt(id + ".show"), fc.getInt(id + ".sub"));
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
		YamlFile fc = Enquiry.getYamlFile(sellOfferFile);
		for(String id : fc.getKeys(false)) {
			if(fc.getString(id) != null && fc.getString(id).equals("null")) continue;
			SelfBalancingBST root = Category.getCategory(fc.getInt(id + ".category")).getSellOffers(fc.getInt(id + ".show"), fc.getInt(id + ".sub"));
			SelfBalancingBSTNode sellOffer = root.get(fc.getDouble(id + ".price"), root.getRoot());
			
			if(sellOffer != null && sellOffer.get(UUID.fromString(id)) != null) {
				claimable += sellOffer.get(UUID.fromString(id)).getClaimable() * fc.getDouble(id + ".price");
			} else {
				claimable += fc.getInt(id + ".claimable") * fc.getDouble(id + ".price");
			}
		}
		return claimable;
	}
	
	public CompactMap<String, CompactMap<String, Number>> getSellOffer() {
		CompactMap<String, CompactMap<String, Number>> allSellOffer = new CompactMap<String, CompactMap<String, Number>>();
		YamlFile fc = Enquiry.getYamlFile(sellOfferFile);
		for(String id : fc.getKeys(false)) {
			if(fc.getString(id) != null && fc.getString(id).equals("null")) continue;
			int total = fc.getInt(id + ".amount");
			double price = fc.getDouble(id + ".price");
			int category = fc.getInt(id + ".category");
			int item_id = fc.getInt(id + ".item_id");
			int claimable = fc.getInt(id + ".claimable");
			int show = fc.getInt(id + ".show");
			int sub = fc.getInt(id + ".sub");
			
			CompactMap<String, Number> datas = new CompactMap<String, Number>();
			datas.put("total", total);
			datas.put("price", price);
			datas.put("category", category);
			datas.put("item_id", item_id);
			datas.put("show", show);
			datas.put("sub", sub);
			
			if(fc.getInt(id + ".left") == 0) {
				datas.put("left", 0);
				datas.put("claimable", claimable);
			} else {
				SelfBalancingBST root = Category.getCategory(category).getSellOffers(show, sub);
				SelfBalancingBSTNode sellOffer = root.get(fc.getDouble(id + ".price"), root.getRoot());
				SellOffer sO = (SellOffer) sellOffer.get(UUID.fromString(id));
				if(sO == null) {
					Chat.sendMessage("NPE While Getting All Enquiries For A Player! ID: " + id + ". Deleting it...", ChatLevel.WARN);
					fc.set(id, null);
					continue;
				}
				datas.put("left", sO.getLeft());
				datas.put("claimable", sO.getClaimable());
			}
			allSellOffer.put(id, datas);
		}
		return allSellOffer;
	}
	
	public CompactMap<String, CompactMap<String, Number>> getBuyOrder() {
		CompactMap<String, CompactMap<String, Number>> allBuyOrder = new CompactMap<String, CompactMap<String, Number>>();
		YamlFile fc = Enquiry.getYamlFile(buyOrderFile);
		for(String id : fc.getKeys(false)) {
			if(fc.getString(id) != null && fc.getString(id).equals("null")) continue;
			int total = fc.getInt(id + ".amount");
			double price = fc.getDouble(id + ".price");
			int category = fc.getInt(id + ".category");
			int item_id = fc.getInt(id + ".item_id");
			int claimable = fc.getInt(id + ".claimable");
			int show = fc.getInt(id + ".show");
			int sub = fc.getInt(id + ".sub");
			
			CompactMap<String, Number> datas = new CompactMap<String, Number>();
			datas.put("total", total);
			datas.put("price", price);
			datas.put("category", category);
			datas.put("item_id", item_id);
			datas.put("show", show);
			datas.put("sub", sub);
			
			if(fc.getInt(id + ".left") == 0) {
				datas.put("left", 0);
				datas.put("claimable", claimable);
			} else {
				SelfBalancingBST root = Category.getCategory(category).getBuyOrders(show, sub);
				SelfBalancingBSTNode buyOrder = root.get(fc.getDouble(id + ".price"), root.getRoot());
				if(buyOrder == null || buyOrder.get(UUID.fromString(id)) == null) {
					Chat.sendMessage("NPE While Getting All Enquiries For A Player! ID: " + id + ". Deleting it...", ChatLevel.WARN);
					fc.set(id, null);
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
