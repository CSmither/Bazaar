package de.ancash.bazaar.management;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;

public final class PlayerManager {
	
	private static Map<UUID, PlayerManager> registered = new HashMap<UUID, PlayerManager>();
	
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
	
	public static PlayerManager newPlayerManager(UUID id){
		return new PlayerManager(id);
	}
	
	public PlayerManager(UUID uuid) {
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
		try {
			if(!EnquiryUtils.instance.isYamlFileLoaded(buyOrderFile)){
				YamlFile yamlFile = new YamlFile(buyOrderFile);
				yamlFile.load();
				EnquiryUtils.instance.addYamlFile(buyOrderFile, yamlFile);
			}
			if(!EnquiryUtils.instance.isYamlFileLoaded(sellOfferFile)) {
				YamlFile yamlFile = new YamlFile(sellOfferFile);
				yamlFile.load();
				EnquiryUtils.instance.addYamlFile(sellOfferFile, yamlFile);
			}
		} catch(IOException | InvalidConfigurationException ex) {
			ex.printStackTrace();
		}
		
		registered.put(uuid, this);
	}
	
	public int getEnquiries() {
		return EnquiryUtils.instance.getYamlFile(buyOrderFile).getKeys(false).size() + EnquiryUtils.instance.getYamlFile(sellOfferFile).getKeys(false).size();
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
		
		YamlFile fc = EnquiryUtils.instance.getYamlFile(buyOrderFile);
		
		for(String id : fc.getKeys(false)) {
			if(fc.getString(id) == null) continue;
			SelfBalancingBST root = Category.getCategory(fc.getInt(id + ".category")).getBuyOrders(fc.getInt(id + ".sub"), fc.getInt(id + ".subsub"));
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
		YamlFile fc = EnquiryUtils.instance.getYamlFile(sellOfferFile);
		for(String id : fc.getKeys(false)) {
			if(fc.getString(id) == null) continue;
			SelfBalancingBST root = Category.getCategory(fc.getInt(id + ".category")).getSellOffers(fc.getInt(id + ".sub"), fc.getInt(id + ".subsub"));
			SelfBalancingBSTNode sellOffer = root.get(fc.getDouble(id + ".price"), root.getRoot());
			
			if(sellOffer != null && sellOffer.get(UUID.fromString(id)) != null) {
				claimable += sellOffer.get(UUID.fromString(id)).getClaimable() * fc.getDouble(id + ".price");
			} else {
				claimable += fc.getInt(id + ".claimable") * fc.getDouble(id + ".price");
			}
		}
		return claimable;
	}
	
	public Map<String, Map<String, Number>> getSellOffer() {
		Map<String, Map<String, Number>> allSellOffer = new HashMap<String, Map<String, Number>>();
		YamlFile fc = EnquiryUtils.instance.getYamlFile(sellOfferFile);
		for(String id : fc.getKeys(false)) {
			if(fc.getString(id) != null && fc.getString(id).equals("null")) continue;
			int total = fc.getInt(id + ".amount");
			double price = fc.getDouble(id + ".price");
			int category = fc.getInt(id + ".category");
			int item_id = fc.getInt(id + ".item_id");
			int claimable = fc.getInt(id + ".claimable");
			int show = fc.getInt(id + ".sub");
			int sub = fc.getInt(id + ".subsub");
			
			Map<String, Number> datas = new HashMap<String, Number>();
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
					Chat.sendMessage("NPE While Getting All Sell OffersFor A Player! (" + category + ", " + show + ", " + sub + ". Deleting it...", ChatLevel.WARN);
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
	
	public Map<String, Map<String, Number>> getBuyOrder() {
		Map<String, Map<String, Number>> allBuyOrder = new HashMap<String, Map<String, Number>>();
		YamlFile fc = EnquiryUtils.instance.getYamlFile(buyOrderFile);
		for(String id : fc.getKeys(false)) {
			if(fc.getString(id) != null && fc.getString(id).equals("null")) continue;
			int total = fc.getInt(id + ".amount");
			double price = fc.getDouble(id + ".price");
			int category = fc.getInt(id + ".category");
			int item_id = fc.getInt(id + ".item_id");
			int claimable = fc.getInt(id + ".claimable");
			int show = fc.getInt(id + ".sub");
			int sub = fc.getInt(id + ".subsub");
			
			Map<String, Number> datas = new HashMap<String, Number>();
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
					Chat.sendMessage("NPE While Getting All Buy Orders For A Player! (" + category + ", " + show + ", " + sub + ". Deleting it...", ChatLevel.WARN);
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
