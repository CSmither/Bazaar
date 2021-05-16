package de.ancash.bazaar.management;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.management.Enquiry.EnquiryTypes;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.misc.FileUtils;

import static de.ancash.minecraft.ItemStackUtils.legacyToNormal;

public class Category {
		
	public static int getSubBySlot(int slot) {
		int t = 0;
		while(slot > 9) {
			slot -= 9;
			t++;
		}
		return slot + (t - 1) * 6;
	}
	
	public static int getSlotByID(int i) {
		int t = 1;
		while(i > 6) {
			i -= 6;
			t++;
		}
		return 9 * t + i + 1;
	}
	
	public static boolean exists(int i) {
		return i < 6 && i > 0 && categories[i - 1] != null;
	}
	public static Category getCategory(int category) {
		if(category > 5 || category < 1) throw new IllegalArgumentException("Only Category 1-5. Not " + category);
		return categories[category - 1];
	}
	
	private static Category[] categories = new Category[5];
	private Bazaar plugin;
	
	public Category(Bazaar pl) {
		this.plugin = pl;
		this.category = -1;
		init();
	}
	
	private void init() {
		try {
			Chat.sendMessage("Loading Categories...", ChatLevel.INFO);
			for(int i = 1; i<=5; i++) {
				String catFilePath = plugin.getInvConfig().getString("inventory.categories." + i + ".file");
				File catFile = new File(catFilePath);
				if(!catFile.exists()) {
					Chat.sendMessage("No File found for Category " + i + " in " + catFilePath, ChatLevel.WARN);
					Chat.sendMessage("Creating new preconfigured File " + catFile.getName(), ChatLevel.INFO);
					catFile.mkdirs();
					catFile.delete();
					FileUtils.copyInputStreamToFile(plugin.getResource("resources/category_" + i + ".yml"), catFile);
				}
				
				FileConfiguration fc = YamlConfiguration.loadConfiguration(catFile);
				fc.load(catFile);
				//loop through each item in the category
				if(fc.getKeys(false).isEmpty()) {
					Chat.sendMessage("No Items found for Category " + i + " in File " + catFilePath, ChatLevel.WARN);
					continue;
				}
				
				Category cat = new Category(i);
				Inventory inv = Bukkit.createInventory(null, 5 * 9, "UHUHUHUHUHUHUHU");
				inv.setContents(plugin.bazaarTemplate.getContents().clone());
				cat.sub = plugin.bazaarTemplate.getContents().clone();
				for(String path : fc.getKeys(false)) {
					try {
						int id = Integer.valueOf(path);
						cat.sub[id - 1] = ItemStackUtils.get(fc, id + ".sub");
						inv.setItem(getSlotByID(id), ItemStackUtils.get(fc, id + ".subsub"));
						for(int sub = 1; sub<=9; sub++) {
							if(fc.getString(path + ".subsub." + sub + ".original.type") == null && fc.getItemStack(path + ".subsub." + sub + ".original") == null) continue;
							cat.allOriginal[id - 1][sub - 1] = legacyToNormal(ItemStackUtils.get(fc, path + ".subsub." + sub + ".original"));
							cat.emptyPrices[id - 1][sub - 1] = fc.getDouble(path + ".subsub." + sub + ".default-price");
							cat.subSub[id - 1][sub - 1] = legacyToNormal(ItemStackUtils.get(fc, path + ".subsub." + sub + ".show"));
						}
					} catch (Exception e) {
						Chat.sendMessage("Error While Loading Item with Id " + path + " in Category " + i + ": " + e, ChatLevel.WARN);
						continue;
					}
				}
				categories[i -1] = cat;
				for(int a = 1; a<=9; a++) {
					cat.getSubBuyOrders(a);
					cat.getSubSellOffers(a);
				}
				fc.save(catFile);
			}
		} catch(IOException | InvalidConfigurationException ex) {
			ex.printStackTrace();
		}
	}
	
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
	
	private final int category;
	
	private ItemStack[] sub = new ItemStack[18];
	private ItemStack[][] allOriginal = new ItemStack[18][9];
	private ItemStack[][] subSub= new ItemStack[18][9];
	private Double[][] emptyPrices = new Double[18][9];
	private CompactMap<Integer, CompactMap<Integer, SelfBalancingBST>> allSellOffers = new CompactMap<Integer, CompactMap<Integer, SelfBalancingBST>>();
	private CompactMap<Integer, CompactMap<Integer, SelfBalancingBST>> allBuyOrders = new CompactMap<Integer, CompactMap<Integer, SelfBalancingBST>>();
	
	public Double[][] getEmptyPrices() {
		return emptyPrices;
	}
	
	public ItemStack[] getSub() {
		return sub;
	}
	
	public ItemStack[][] getSubSub() {
		return subSub;
	}
	
	public ItemStack[][] getAllOriginal() {
		return allOriginal;
	}
	
	public ItemStack getOriginal(int a, int b) {
		return allOriginal[a - 1][b - 1];
	}
	
	public List<SelfBalancingBST> getAll() {
		List<SelfBalancingBST> all = allBuyOrders.entrySet().stream().map(Map.Entry::getValue).map(CompactMap::values).flatMap(Collection::stream).collect(Collectors.toList());
		all.addAll(allSellOffers.entrySet().stream().map(Map.Entry::getValue).map(CompactMap::values).flatMap(Collection::stream).collect(Collectors.toList()));
		return all;
	}
		
	private CompactMap<Integer, List<SelfBalancingBST>> allSellOffersInList = new CompactMap<>();
	private CompactMap<Integer, List<SelfBalancingBST>> allBuyOrdersInList = new CompactMap<>();
	
	public List<SelfBalancingBST> getSubSellOffers(int show) {
		if(!allSellOffersInList.containsKey(show)) 
			allSellOffersInList.put(show,allSellOffers.entrySet().stream().filter(entry -> entry.getKey() == show).map(Map.Entry::getValue).map(CompactMap::values).flatMap(Collection::stream).collect(Collectors.toList()));
		return allSellOffersInList.get(show);
	}
	
	public List<SelfBalancingBST> getSubBuyOrders(int show) {
		if(!allBuyOrdersInList.containsKey(show)) 
			allBuyOrdersInList.put(show, allBuyOrders.entrySet().stream().filter(entry -> entry.getKey() == show).map(Map.Entry::getValue).map(CompactMap::values).flatMap(Collection::stream).collect(Collectors.toList()));
		return allBuyOrdersInList.get(show);
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
		return allSellOffers.get(a).get(b);
	}
	
	public SelfBalancingBST getBuyOrders(int a, int b) {
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
}
