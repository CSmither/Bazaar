package de.ancash.bazaar.management;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import de.ancash.ilibrary.datastructures.maps.CompactMap;

public class Category {
	
	private static Category[] categories = new Category[5];
	
	public Category(int category) {
		this.category = category;
		categories[category -1] = this;
		for(int i = 0; i<18; i++) {
			CompactMap<Integer, SelfBalancingBST> allSubSellOffer = new CompactMap<Integer, SelfBalancingBST>();
			CompactMap<Integer, SelfBalancingBST> allSubBuyOrder = new CompactMap<Integer, SelfBalancingBST>();
			for(int sub = 0; sub<9; sub++) {
				allSubSellOffer.put(sub + 1, new SelfBalancingBST());
				allSubBuyOrder.put(sub + 1, new SelfBalancingBST());
			}
			allBuyOrders.put(i + 1, allSubBuyOrder);
			allSellOffers.put(i + 1, allSubSellOffer);
		}
	}

	public int getCategory() {
		return category;
	}
	public static boolean exists(int i) {
		return i < 6 && i > 0 && categories[i - 1] != null;
	}
	public static Category getCategory(int category) {
		if(category > 5 || category < 1) throw new NullPointerException("Only Category 1-5. Not " + category);
		if(categories[category -1] == null) throw new NullPointerException("No Category Found for " + category);
		return categories[category - 1];
	}
	
	private final int category;
	
	//with sub
	private ItemStack[] show = new ItemStack[18];
	private ItemStack[][] allOriginal = new ItemStack[18][9];
	private ItemStack[][] subShow = new ItemStack[18][9];
	private Double[][] emptyPrices = new Double[18][9];
	private CompactMap<Integer, CompactMap<Integer, SelfBalancingBST>> allSellOffers = new CompactMap<Integer, CompactMap<Integer, SelfBalancingBST>>();
	private CompactMap<Integer, CompactMap<Integer, SelfBalancingBST>> allBuyOrders = new CompactMap<Integer, CompactMap<Integer, SelfBalancingBST>>();
	
	public Double[][] getEmptyPrices() {
		return emptyPrices;
	}
	
	public ItemStack[] getShow() {
		return show;
	}
	
	public void setShow(int a, ItemStack is) {
		show[a] = is;
	}
	
	public ItemStack[][] getSubShow() {
		return subShow;
	}
	
	public void setSubShow(int a, int b, ItemStack is) {
		subShow[a - 1][b - 1] = is;
	}
	
	public ItemStack[][] getAllOriginal() {
		return allOriginal;
	}
	
	public ItemStack getOriginial(int a, int b) {
		return allOriginal[a - 1][b - 1];
	}
	
	public void setOriginal(int a, int b, ItemStack is) {
		allOriginal[a - 1][b - 1] = is;
	}
	
	
	
	public List<SelfBalancingBST> getAll() {
		List<SelfBalancingBST> all = new ArrayList<SelfBalancingBST>();
		allBuyOrders.keySet().forEach(cat -> allBuyOrders.get(cat).keySet().forEach(id -> all.add(allBuyOrders.get(cat).get(id))));
		allSellOffers.keySet().forEach(cat -> allSellOffers.get(cat).keySet().forEach(id -> all.add(allSellOffers.get(cat).get(id))));
		return all;
	}
	
	public List<SelfBalancingBST> getAllSellOffers() {
		List<SelfBalancingBST> all = new ArrayList<SelfBalancingBST>();
		allSellOffers.keySet().forEach(cat -> allSellOffers.get(cat).keySet().forEach(id -> all.add(allSellOffers.get(cat).get(id))));
		return all;
	}
	
	public List<SelfBalancingBST> getAllBuyOrders() {
		List<SelfBalancingBST> all = new ArrayList<SelfBalancingBST>();
		allBuyOrders.keySet().forEach(cat -> allBuyOrders.get(cat).keySet().forEach(id -> all.add(allBuyOrders.get(cat).get(id))));
		return all;
	}
	
	public List<SelfBalancingBST> getSubSellOffers(int show) {
		List<SelfBalancingBST> trees = new ArrayList<>();
		allSellOffers.get(show).forEach((sub, tree) -> trees.add(tree));
		return trees;
	}
	
	public List<SelfBalancingBST> getSubBuyOrders(int show) {
		List<SelfBalancingBST> trees = new ArrayList<>();
		allBuyOrders.get(show).forEach((sub, tree) -> trees.add(tree));
		return trees;
	}
	
	public SelfBalancingBSTNode get(EnquiryTypes t, int a, int b, double value) {
		SelfBalancingBST tree = null;
		tree = getTree(t, a, b);
		if(tree == null) return null;
		return tree.get(value, tree.getRoot());
	}
	
	public SelfBalancingBST getTree(EnquiryTypes type, int a, int b) {
		if(type.equals(EnquiryTypes.SELL_OFFER)) return getSellOffers(a, b);
		if(type.equals(EnquiryTypes.BUY_ORDER)) return getBuyOrders(a, b);
		return null;
	}
	
	public SelfBalancingBST getSellOffers(int a, int b) {
		if(!allSellOffers.containsKey(a) || !allSellOffers.get(a).containsKey(b)) return null;
		return allSellOffers.get(a).get(b);
	}
	
	public SelfBalancingBST getBuyOrders(int a, int b) {
		if(!allBuyOrders.containsKey(a) || !allBuyOrders.get(a).containsKey(b)) return null;
		return allBuyOrders.get(a).get(b);
	}
	
	public CompactMap<UUID, Enquiry> getLowest(EnquiryTypes type, int a, int sub) {
		SelfBalancingBST tree = getTree(type, a, sub);
		if(tree == null) return null;
		return tree.getMin().get();
	}
	
	public CompactMap<UUID, Enquiry> getHighest(EnquiryTypes type, int a, int sub) {
		SelfBalancingBST tree = getTree(type, a, sub);
		if(tree == null) return null;
		return tree.getMax().get();
	}
	
	public static int getItemIDBySlot(int slot) {
		int t = 0;
		while(slot > 9) {
			slot = slot - 9;
			t = t + 1;
		}
		return slot + (t - 1) * 6;
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
		Chat.sendMessage("Loading Categories...", ChatLevel.INFO);
		for(int i = 1; i<=5; i++) {
			String catFilePath = Files.getInvConfig().getString("inventory.categories." + i + ".file");
			File catFile = new File(catFilePath);
			if(!catFile.exists()) {
				Chat.sendMessage("No File found for Category " + i + " in " + catFilePath, ChatLevel.WARN);
				Chat.sendMessage("Creating new preconfigured File " + catFile.getName(), ChatLevel.INFO);
				catFile.mkdirs();
				catFile.delete();
				FileUtils.copyInputStreamToFile(pl.getResource("resources/category_" + i + ".yml"), catFile);
			}
			
			FileConfiguration fc = YamlConfiguration.loadConfiguration(catFile);
			fc.load(catFile);
			//loop through each item in the category
			if(fc.getKeys(false).isEmpty()) {
				Chat.sendMessage("No Items found for Category " + i + " in File " + catFilePath, ChatLevel.WARN);
				continue;
			}
			
			Category cat = new Category(i);
			Inventory inv = Bukkit.createInventory(null, 5 * 9, Files.getInvConfig().getString("inventory.name"));
			inv.setContents(Bazaar.bazaarTemplate.getContents().clone());
			cat.show = Bazaar.bazaarTemplate.getContents().clone();
			for(String path : fc.getKeys(false)) {
				try {
					int id = Integer.valueOf(path);
					cat.setShow(getSlotByID(id), ItemFromFile.get(fc, id + ".show"));
					inv.setItem(getSlotByID(id), ItemFromFile.get(fc, id + ".show"));
					for(int sub = 1; sub<=9; sub++) {
						if(fc.getString(path + ".sub." + sub + ".original.type") == null) continue;
						cat.setOriginal(id, sub, ItemFromFile.get(fc, path + ".sub." + sub + ".original"));
						cat.emptyPrices[id - 1][sub - 1] = fc.getDouble(path + ".sub." + sub + ".emptyPrice");
						cat.setSubShow(id, sub, ItemFromFile.get(fc, path + ".sub." + sub + ".show"));
					}
				} catch (Exception e) {
					Chat.sendMessage("Error While Loading Item with Id " + path + " in Category " + i + ": " + e, ChatLevel.WARN);
					if(e instanceof ArrayIndexOutOfBoundsException) e.printStackTrace();
					continue;
				}
			}
			categories[i -1] = cat;
			fc.save(catFile);
		}
	}
}
