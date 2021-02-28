package de.ancash.bazaar.management;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.files.Files;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Enquiry;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.bazaar.utils.Enquiry.EnquiryTypes;
import de.ancash.bazaar.utils.FileUtils;
import de.ancash.bazaar.utils.ItemFromFile;

public class Category {
	
	private static Category[] categories = new Category[5];
	
	private final int category;
	private HashMap<Integer, SelfBalancingBST> sellOffer = new HashMap<Integer, SelfBalancingBST>();
	private HashMap<Integer, SelfBalancingBST> buyOrder = new HashMap<Integer, SelfBalancingBST>();
	private ItemStack[] contents = new ItemStack[18];
	@SuppressWarnings("unused")
	private ItemStack[] subContent = new ItemStack[9];
	ItemStack[] showcase = new ItemStack[45];
	Double[] priceWhenEmpty = new Double[45];
	
	
	public List<SelfBalancingBST> getAll() {
		List<SelfBalancingBST> all = new ArrayList<SelfBalancingBST>();
		for(int i : sellOffer.keySet()) {
			all.add(sellOffer.get(i));
		}
		for(int i : buyOrder.keySet()) {
			all.add(buyOrder.get(i));
		}
		/*for(int i : subcategoryBuyOrder.keySet()) {
			for(int t : subcategoryBuyOrder.get(i).keySet()) {
				all.add(subcategoryBuyOrder.get(i).get(t));
			}
		}
		for(int i : subcategorySellOffer.keySet()) {
			for(int t : subcategorySellOffer.get(i).keySet()) {
				all.add(subcategorySellOffer.get(i).get(t));
			}
		}*/
		return all;
	}
	
	public static int getItemIDBySlot(int slot) {
		int t = 0;
		while(slot > 9) {
			slot = slot - 9;
			t = t + 1;
		}
		return slot - 1 + (t - 1) * 6;
	}
	
	public static int getSlotByID(int i) {
		int t = 1;
		while(i > 6) {
			i = i - 6;
			t = t + 1;
		}
		return 9 * t + i + 1;
	}
	
	public static void init(Plugin pl) throws IOException, InvalidConfigurationException {
		for(int i = 1; i<=5; i++) {
			File catFile = new File(Files.getInvConfig().getString("inventory.categories." + i + ".file"));
			if(!catFile.exists()) {
				Chat.sendMessage("No File found for Category " + i + " in " + Files.getInvConfig().getString("inventory.categories." + i + ".file"), ChatLevel.WARN);
				Chat.sendMessage("Creating new preconfigured File " + catFile.getName(), ChatLevel.INFO);
				catFile.mkdirs();
				catFile.delete();
				FileUtils.copyInputStreamToFile(pl.getResource("resources/category_" + i + ".yml"), catFile);
			}
			
			FileConfiguration fc = YamlConfiguration.loadConfiguration(catFile);
			fc.load(catFile);
			//loop through each item in the category
			if(fc.getKeys(false).isEmpty()) {
				Chat.sendMessage("No Items found for Category " + i + " in File " + Files.getInvConfig().getString("inventory.categories." + i + ".file"), ChatLevel.WARN);
				continue;
			}
			
			Category cat = new Category(i);
			Inventory inv = Bukkit.createInventory(null, 5 * 9, Files.getInvConfig().getString("inventory.name"));
			inv.setContents(Bazaar.bazaarTemplate.getContents().clone());
			cat.showcase = Bazaar.bazaarTemplate.getContents().clone();
			for(String id : fc.getKeys(false)) {
				try {
					cat.showcase[getSlotByID(Integer.valueOf(id))] = ItemFromFile.get(fc, id + ".showcase");
					inv.setItem(getSlotByID(Integer.valueOf(id)), ItemFromFile.get(fc, id + ".showcase"));
					cat.put(Integer.valueOf(id), ItemFromFile.get(fc, id + ".original"));
					cat.priceWhenEmpty[getSlotByID(Integer.valueOf(id))] = fc.getDouble(id + ".emptyPrice");
				} catch (Exception e) {
					Chat.sendMessage("Error While Loading Item with Id " + id + " in Category " + i, ChatLevel.WARN);
					continue;
				}
			}
			categories[i -1] = cat;
		}
	}
	
	public SelfBalancingBSTNode get(EnquiryTypes t, int item_id, double value) {
		if(t.equals(EnquiryTypes.BUY_ORDER)) return buyOrder.get(item_id).get(value, buyOrder.get(item_id).getRoot());
		if(t.equals(EnquiryTypes.SELL_OFFER)) return sellOffer.get(item_id).get(value, sellOffer.get(item_id).getRoot());
		return null;
	}
	
	public Category(int category) {
		this.category = category;
		categories[category -1] = this;
		for(int i = 1; i<=18; i++) {
			sellOffer.put(i, new SelfBalancingBST());
			buyOrder.put(i, new SelfBalancingBST());
		}
		categories[category - 1] = this;
	}
	
	public HashMap<UUID, Enquiry> getLowest(EnquiryTypes type, int id) {
		return getLowestSub(type, id, -1);
	}
	
	public HashMap<UUID, Enquiry> getLowestSub(EnquiryTypes type, int id, int sub) {
		if(type.equals(EnquiryTypes.SELL_OFFER)) {
			if(sub == -1) return sellOffer.get(id).getMin().getEnquiries();
			//return subcategorySellOffer.get(id).get(sub).getMin().getEnquiries();
		}
		if(type.equals(EnquiryTypes.BUY_ORDER)) {
			if(sub == -1) return buyOrder.get(id).getMin().getEnquiries();
			//return subcategoryBuyOrder.get(id).get(sub).getMin().getEnquiries();
		}
		return null;
	}
	
	public HashMap<UUID, Enquiry> getHighest(EnquiryTypes type, int id) {
		return getHighestSub(type, id, -1);
	}
	
	public HashMap<UUID, Enquiry> getHighestSub(EnquiryTypes type, int id, int sub) {
		if(type.equals(EnquiryTypes.SELL_OFFER)) {
			if(sub == -1) return sellOffer.get(id).getMax().getEnquiries();
			//return subcategorySellOffer.get(id).get(sub).getMax().getEnquiries();
		}
		if(type.equals(EnquiryTypes.BUY_ORDER)) {
			if(sub == -1) return buyOrder.get(id).getMax().getEnquiries();
			//return subcategoryBuyOrder.get(id).get(sub).getMax().getEnquiries();
		}
		return null;
	}
	
	public int firstEmpty() {
		int i = -1;
		for(int t = 0; t<18; t++) {
			if(contents[t] == null) {
				return t;
			}
		}
		return i;
	}
	
	public Double[] getPriceEmpty() {return priceWhenEmpty;}
	public ItemStack[] getShowcase() {return showcase;}
	public void put(int i, ItemStack is) {contents[i - 1] = is;}
	public SelfBalancingBST getSellOffer(int i) {return sellOffer.get(i);}
	public SelfBalancingBST getBuyOrder(int i) {return buyOrder.get(i);}
	public boolean hasSellOffer() {return sellOffer.isEmpty();}
	public boolean hasBuyOrder() {return buyOrder.isEmpty();}
	public void setContents(ItemStack[] c) {this.contents = c;}
	public ItemStack getItem(int i) {return contents[i - 1];}
	public ItemStack[] getContents() {return contents;}
	public int getCategory() {return category;}
	public static boolean exists(int i) {return i < 6 && i > 0 && categories[i - 1] != null;}
	public static Category getCategory(int category) {
		if(category > 5 || category < 1) throw new NullPointerException("Only Category 1-5. Not " + category);
		if(categories[category -1] == null) throw new NullPointerException("No Category Found for " + category);
		return categories[category - 1];
	}
	
	//private HashMap<Integer, HashMap<Integer, SelfBalancingBST>> subcategorySellOffer = new HashMap<Integer, HashMap<Integer, SelfBalancingBST>>();
	//private HashMap<Integer, HashMap<Integer, SelfBalancingBST>> subcategoryBuyOrder = new HashMap<Integer, HashMap<Integer, SelfBalancingBST>>();
	
	/*
	public SelfBalancingBSTNode getSub(EnquiryTypes t, int item_id, int sub, double value) {
		if(t.equals(EnquiryTypes.BUY_ORDER)) {
			return subcategoryBuyOrder.get(item_id).get(sub).get(value, subcategoryBuyOrder.get(item_id).get(sub).getRoot());
		}
		if(t.equals(EnquiryTypes.SELL_OFFER)) {
			return subcategorySellOffer.get(item_id).get(sub).get(value, subcategorySellOffer.get(item_id).get(sub).getRoot());
		}
		return null;
	}*/
	/*public HashMap<Integer, SelfBalancingBST> getSubcategorySellOffer(int item_id) {
		return subcategorySellOffer.get(item_id);
	  }

	public HashMap<Integer, SelfBalancingBST> getSubcategoryBuyOrder(int item_id) {
		return subcategoryBuyOrder.get(item_id);
	}



	public SelfBalancingBSTNode get(EnquiryTypes t, int item_id, int sub_id, double value) {
		if(t.equals(EnquiryTypes.BUY_ORDER)) {
			return subcategoryBuyOrder.get(item_id).get(sub_id).getRoot();
		}
		if(t.equals(EnquiryTypes.SELL_OFFER)) {
			return subcategorySellOffer.get(item_id).get(sub_id).getRoot();
		}
		return null;
	}

	public boolean hasSubcategory(int item_id) {
		return subcategoryBuyOrder.get(item_id) != null;
	}*/
}
