package de.ancash.bazaar.management;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.management.Enquiry.EnquiryType;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.SerializableItemStack;
import de.ancash.misc.FileUtils;

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
	
	@SuppressWarnings("deprecation")
	private void init() {
		try {
			Chat.sendMessage("Loading Categories...");
			for(int i = 1; i<=5; i++) {
				Chat.sendMessage("Loading items for Category " + i);
				String catFilePath = plugin.getInvConfig().getString("inventory.categories." + i + ".file");
				File catFile = new File(catFilePath);
				if(!catFile.exists()) {
					Chat.sendMessage("No File found for Category " + i + " in " + catFilePath, ChatLevel.WARN);
					Chat.sendMessage("Creating new preconfigured File " + catFile.getName());
					catFile.mkdirs();
					catFile.delete();
					FileUtils.copyInputStreamToFile(plugin.getResource("resources/category_" + i + ".yml"), catFile);
				}
				
				FileConfiguration fc = YamlConfiguration.loadConfiguration(catFile);
				fc.load(catFile);
				if(fc.getKeys(false).isEmpty()) {
					Chat.sendMessage("No Items found for Category " + i + " in File " + catFilePath, ChatLevel.WARN);
					continue;
				}
				
				Category cat = new Category(i);
				for(String path : fc.getKeys(false)) {
					try {
						int id = Integer.valueOf(path);
						cat.subCategories[id - 1] = new SubCategory(ItemStackUtils.get(fc, id + ".sub"));
						
						for(int sub = 1; sub<=9; sub++) {
							if(fc.getString(path + ".subsub." + sub + ".original.type") == null && fc.getItemStack(path + ".subsub." + sub + ".original") == null && !fc.isString(path + ".subsub." + sub + ".original")) continue;
							this.convertToBase64(fc, path + ".subsub." + sub + ".original");
							ItemStack subSubShow = ItemStackUtils.get(fc, path + ".subsub." + sub + ".show");
							ItemStack original = ItemStackUtils.get(fc, path + ".subsub." + sub + ".original");
							Validate.notNull(subSubShow, "Could not load item with path: " + path + ".subsub." + sub + ".show (" + fc.getString(path + ".subsub." + sub + ".show") + ")");	
							Validate.notNull(original, "Could not load item with path: " + path + ".subsub." + sub + ".original (" + fc.getString(path + ".subsub." + sub + ".original") + ")");
							cat.subCategories[id - 1].setSubSubCategory(sub, new SubSubCategory(subSubShow, original, fc.getDouble(path + ".subsub." + sub + ".default-price")));
						}
					} catch (Exception e) {
						Chat.sendMessage("Error While Loading Item with Id " + path + " in Category " + i + ": " + e, ChatLevel.WARN);
						try {
							cat.subCategories[Integer.valueOf(path) - 1] = null;
						} catch(NumberFormatException exx) {}
						continue;
					}
				}
				categories[i - 1] = cat;
				fc.save(catFile);
			}
		} catch(IOException | InvalidConfigurationException ex) {
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void convertToBase64(FileConfiguration fc, String path) throws ClassNotFoundException, IOException {
		if(!fc.isString(path)) {
			fc.set(path, new SerializableItemStack(ItemStackUtils.get(fc, path)).asBase64());
			Chat.sendMessage("Converted original item (" + path + ")to Base64 String!");
		}
	}
	
	public Category(int category) {
		this.category = category;
		categories[category -1] = this;
	}

	public int getCategory() {
		return category;
	}
	
	private final int category;
	
	private final SubCategory[] subCategories = new SubCategory[18];
	
	public double getEmptyPrice(int a, int b) {
		if(subCategories[a - 1] == null) return -1;
		return subCategories[a - 1].getSubSubCategory(b).getEmptyPrice();
	}
	
	public ItemStack getSubShow(int a) {
		if(subCategories[a - 1] == null) return null;
		return subCategories[a - 1].getShow();
	}
	
	public ItemStack getSubSubShow(int a, int subsub) {
		if(subCategories[a - 1] == null || subCategories[a - 1].getSubSubCategory(subsub) == null) return null;
		return subCategories[a - 1].getSubSubCategory(subsub).getShow();
	}
	
	public ItemStack getOriginal(int a, int b) {
		if(subCategories[a - 1] == null || subCategories[a - 1].getSubSubCategory(b) == null) return null;
		return subCategories[a - 1].getSubSubCategory(b).getOriginal();
	}
	
	public List<SelfBalancingBST> getAllSellOffers(int a) {
		if(subCategories[a - 1] == null) return null;
		return subCategories[a - 1].getAllSubSubCategories().stream().filter(s -> s != null).map(SubSubCategory::getSellOfferTree).collect(Collectors.toList());
	}
	
	public List<SelfBalancingBST> getAllBuyOrders(int a) {
		if(subCategories[a - 1] == null) return null;
		return subCategories[a - 1].getAllSubSubCategories().stream().filter(s -> s != null).map(SubSubCategory::getBuyOrderTree).collect(Collectors.toList());
	}
	
	public SelfBalancingBSTNode get(EnquiryType t, int a, int b, double value) {
		SelfBalancingBST tree = null;
		tree = getTree(t, a, b);
		if(tree == null) return null;
		return tree.get(value, tree.getRoot());
	}
	
	public SelfBalancingBST getTree(EnquiryType type, int a, int b) {
		if(type.equals(EnquiryType.SELL_OFFER)) return getSellOffers(a, b);
		if(type.equals(EnquiryType.BUY_ORDER)) return getBuyOrders(a, b);
		return null;
	}
	
	public SelfBalancingBST getSellOffers(int a, int b) {
		return subCategories[a - 1].getSubSubCategory(b).getSellOfferTree();
	}
	
	public SelfBalancingBST getBuyOrders(int a, int b) {
		return subCategories[a - 1].getSubSubCategory(b).getBuyOrderTree();
	}
	
	public Map<UUID, Enquiry> getLowest(EnquiryType type, int a, int sub) {
		SelfBalancingBST tree = getTree(type, a, sub);
		if(tree == null) return null;
		return tree.getMin().get();
	}
	
	public Map<UUID, Enquiry> getHighest(EnquiryType type, int a, int sub) {
		SelfBalancingBST tree = getTree(type, a, sub);
		if(tree == null) return null;
		return tree.getMax().get();
	}
}