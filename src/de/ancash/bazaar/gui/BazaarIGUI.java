package de.ancash.bazaar.gui;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.Clickable;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;
import de.ancash.misc.MathsUtils;
import de.ancash.misc.Validate;

public class BazaarIGUI extends IGUI{

	public static final int[] INVENTORY_SIZE_NN;
	public static final int[] INVENTORY_SIZE_GHT_TN;
	public static final int[] INVENTORY_SIZE_TWNTY_SVN;
	public static final int[] INVENTORY_SIZE_THRTY_SX;
	public static final int[] INVENTORY_SIZE_FRTY_FV;
	
	static {
		INVENTORY_SIZE_NN = IntStream.range(0, 9).toArray();
		INVENTORY_SIZE_GHT_TN = IntStream.range(0, 18).toArray();
		INVENTORY_SIZE_TWNTY_SVN = IntStream.range(0, 27).toArray();
		INVENTORY_SIZE_THRTY_SX = IntStream.range(0, 36).toArray();
		INVENTORY_SIZE_FRTY_FV = IntStream.range(0, 45).toArray();
	}
	
	/*
	 * categories X
	 * sub X
	 * subsub X
	 * info X
	 * create buy order X
	 * create sell offer X
	 * insta sell X
	 * insta buy X
	 * manage
	 */
	static ItemStack closeInventoryItem;
	static ItemStack backGroundItem;
	static ItemStack createSellOfferItem;
	static ItemStack createBuyOrderItem;
	static ItemStack sellInstantlyItem;
	static ItemStack buyInstantlyItem;
	static ItemStack CUSTOM_AMOUNT;
	static ItemStack PICK_AMOUNT;
	
	public static void load(Bazaar pl) {
		Validate.notNull(pl);
		PICK_AMOUNT = new ItemStack(XMaterial.OAK_SIGN.parseMaterial());
		ItemMeta im = PICK_AMOUNT.getItemMeta();
		im.setDisplayName("0");
		PICK_AMOUNT.setItemMeta(im);
		closeInventoryItem = ItemStackUtils.get(pl.getInvConfig(), "inventory.close");
		backGroundItem = ItemStackUtils.get(pl.getInvConfig(), "inventory.background");		
		createSellOfferItem = ItemStackUtils.get(pl.getInvConfig(), "inventory.opt_inv.create_sell_offer");
		createBuyOrderItem= ItemStackUtils.get(pl.getInvConfig(), "inventory.opt_inv.create_buy_order");
		sellInstantlyItem= ItemStackUtils.get(pl.getInvConfig(), "inventory.opt_inv.sellInsta");
		buyInstantlyItem = ItemStackUtils.get(pl.getInvConfig(), "inventory.opt_inv.buyInsta");
		CUSTOM_AMOUNT = ItemStackUtils.get(pl.getInvConfig(), "inventory.custom-amount");
		BazaarIGUICreateBuyOrder.INSTANCE.load(pl);
		BazaarIGUICreateSellOffer.INSTANCE.load(pl);
		BazaarIGUIBuyInstantly.INSTANCE.load(pl);
		BazaarIGUIManageEnquiries.INSTANCE.load(pl);
	}
	
	BazaarIGUIType currentGUIType;
	final Bazaar plugin;
	int currentCategory;
	int currentSub;
	int currentSubSub;
	String title;
	int enquiryAmount = 0;
	double enquiryPrice = 0;
	
	private boolean locked = false;
	/**
	 * Constructor
	 * 
	 * @param pl
	 * @param id
	 * @param size
	 * @param title
	 */
	public BazaarIGUI(Bazaar pl, UUID id, int size, String title) {
		super(id, size, title);
		this.plugin = pl;
		this.title = title;
		IGUIManager.register(this, id);
		setCategory(1);
	}

	/**
	 * Sets background for slots
	 * 
	 * @param slots
	 */
	public void setBackground(int...slots) {
		for(int slot : slots)
			setItem(backGroundItem, slot);
	}
	
	/**
	 * Sets close button
	 * 
	 * @param slot
	 */
	public final void setCloseItem(int slot) {
		new InventoryItem(this, closeInventoryItem.clone(), slot, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				//called on clicking the slot slot)
				if(topInventory) closeAll();
			}
			
		}).add();
	}
	
	/**
	 * Create new {@link BazaarInventoryItem}
	 * 
	 * @param item
	 * @param slot
	 * @param clickable
	 */
	public void add(ItemStack item, int slot, Clickable clickable) {
		new BazaarInventoryItem(this, item, slot, clickable).add();
	}
	
	/**
	 * Loads/Switches categories or opens sub category, if item is clicked or opened
	 * @param category
	 * @return
	 */
	public boolean setCategory(int c) {
		return BazaarIGUIMain.INSTANCE.setCategory(this, c);
	}
	
	/**
	 * Opens sub inventory
	 * 
	 * @param sub
	 */
	public void openSub(int sub) {
		BazaarIGUISub.INSTANCE.open(this, sub);
	}
	
	/**
	 * 
	 * @param cat
	 * @param sub
	 * @param subsub
	 */
	public void openSubSub(int subsub) {
		BazaarIGUISubSub.INSTANCE.openSubSub(this, subsub);
	}
	
	/**
	 * 
	 * @param is
	 * @param cat
	 * @param sub
	 * @return
	 */
	ItemStack setEnquiriesInLore(ItemStack is, int cat, int sub) {
		return setEnquiriesInLoreExact(is, cat, sub, -1, false, 0, false, 0);
	}
	
	/**
	 * Get all placeholders for enquires
	 * 
	 * @param cat
	 * @param sub
	 * @param subsub
	 * @return
	 */
	public CompactMap<String, String> getPlaceholders(int cat, int sub, int subsub, boolean topSellOffers, int tSOcnt, boolean topBuyOrders, int tBOcnt) {
		Category category = Category.getCategory(cat);
		
		List<SelfBalancingBST> allBuyOrders = subsub != -1 ? Arrays.asList(category.getBuyOrders(sub, subsub)): category.getSubBuyOrders(sub);
		List<SelfBalancingBST> allSellOffers = subsub != -1 ? Arrays.asList(category.getSellOffers(sub, subsub)) : category.getSubSellOffers(sub);
		CompactMap<String, String> placeholder = new CompactMap<>();
		
		for(SelfBalancingBST tree : allBuyOrders) 
			getTreeInfo(tree, placeholder, "orders");
		
		for(SelfBalancingBST tree : allSellOffers) 
			getTreeInfo(tree, placeholder, "offers");
		
		if(subsub != -1) {
			SelfBalancingBST sellOffer = category.getSellOffers(sub, subsub);
			SelfBalancingBST buyOrder = category.getBuyOrders(sub, subsub);
			double sellOfferLowest = sellOffer.isEmpty() ? 0 : sellOffer.getMin().getKey();
			double sellOfferHighest= sellOffer.isEmpty() ? 0 : sellOffer.getMax().getKey();
			double buyOrderLowest = buyOrder.isEmpty() ? 0 : buyOrder.getMin().getKey();
			double buyOrderHighest= buyOrder.isEmpty() ? 0 : buyOrder.getMax().getKey();
			placeholder.put("%offers_price_lowest%", sellOfferHighest == 0 ? "§cN/A" : "" + sellOfferLowest);
			placeholder.put("%offers_price_highest%", sellOfferHighest == 0 ? "§cN/A" : "" + sellOfferHighest);
			placeholder.put("%orders_price_lowest%", buyOrderLowest == 0 ? "§cN/A" : "" + buyOrderLowest);
			placeholder.put("%orders_price_highest%", buyOrderLowest == 0 ? "§cN/A" : "" + buyOrderHighest);
			placeholder.put("%offers_price_lowest_stack%", sellOfferHighest == 0 ? "§cN/A" : "" + MathsUtils.round(sellOfferLowest * 64, 2));
			placeholder.put("%offers_price_highest_stack%", sellOfferHighest == 0 ? "§cN/A" : "" + MathsUtils.round(sellOfferHighest * 64, 2));
			placeholder.put("%orders_price_lowest_stack%", buyOrderLowest == 0 ? "§cN/A" : "" + MathsUtils.round(buyOrderLowest * 64, 2));
			placeholder.put("%orders_price_highest_stack%", buyOrderLowest == 0 ? "§cN/A" : "" + MathsUtils.round(buyOrderHighest * 64, 2));
			
			if(topBuyOrders)
				getTopBuyOrders(placeholder, tBOcnt);
			if(topSellOffers)
				getTopSellOffers(placeholder, tSOcnt);
		}
		return placeholder;
	}
	
	private CompactMap<String, String> getTopBuyOrders(CompactMap<String, String> placeholder, int cnt) {
		SelfBalancingBST rootBuyOrder = Category.getCategory(currentCategory).getBuyOrders(currentSub, currentSubSub);
		StringBuilder builder = new StringBuilder();
		for(int i = 1; i<=cnt; i++) {
			SelfBalancingBSTNode kthLargest = SelfBalancingBST.KthLargestUsingMorrisTraversal(rootBuyOrder.getRoot(), i);
			double value = kthLargest == null ? -1D : MathsUtils.round(kthLargest.getKey(), 1);
			if(value == -1D) break; 
			builder.append("§8- §6");
			SelfBalancingBSTNode node = rootBuyOrder.get(value, rootBuyOrder.getRoot());
			builder.append(value + " coins §7each §f: §a" + node.sum() + "§7x from §f" + (node.get().size() != 1 ? node.get().size() + " §7orders": "1 §7order"));
			builder.append("\n");
		}
		if(builder.toString().endsWith("\n")) {
			placeholder.put("%top_orders_" + cnt + "%", builder.toString().substring(0, builder.toString().length() - 1));
		} else {
			placeholder.put("%top_orders_" + cnt + "%", builder.toString());
		}
		return placeholder;
	}
	
	private CompactMap<String, String> getTopSellOffers(CompactMap<String, String> placeholder, int cnt) {
		SelfBalancingBST rootSellOffer = Category.getCategory(currentCategory).getSellOffers(currentSub, currentSubSub);
		StringBuilder builder = new StringBuilder();
		for(int i = 1; i<=cnt; i++) {
			double value = SelfBalancingBST.kthSmallest(rootSellOffer.getRoot(), i);
			if(value == -1D) break;
			StringBuilder temp = new StringBuilder();
			temp.append("§8- §6");
			SelfBalancingBSTNode node = rootSellOffer.get(value, rootSellOffer.getRoot());
			temp.append(value + " coins §7each §f: §a" + node.sum() + "§7x from §f" + (node.get().size() != 1 ? node.get().size() + " §7offers": "1 §7offer"));
			temp.append("\n");
			if(!builder.toString().contains(temp.toString())) {
				builder.append(temp.toString());
			}
		}
		if(builder.toString().endsWith("\n")) {
			placeholder.put("%top_offers_" + cnt + "%", builder.toString().substring(0, builder.toString().length() - 1));
		} else {
			placeholder.put("%top_offers_" + cnt + "%", builder.toString());
		}
		return placeholder;
	}
	
	/**
	 *Replaces all bazaar placeholders exact. exact means only for ONE subsub item.
	 *If subsub == -1 placeholders will be replaced with all values of one sub category
	 * 
	 * @param is
	 * @param cat
	 * @param sub
	 * @param subsub
	 * @return
	 */
	ItemStack setEnquiriesInLoreExact(ItemStack is, int cat, int sub, int subsub, boolean topSellOffers, int tSOcnt, boolean topBuyOrders, int tBOcnt) {
		return ItemStackUtils.replacePlaceholder(is, getPlaceholders(cat, sub, subsub, topSellOffers, tSOcnt, topBuyOrders, tBOcnt));
	}
	
	/**
	 * 
	 * @param tree
	 * @param map
	 * @param type
	 */
	private void getTreeInfo(SelfBalancingBST tree, CompactMap<String, String> map, String type) {
		add(map, "%" + type + "_content%", tree == null || tree.isEmpty()? 0 : tree.getAllContents());
		add(map, "%" + type + "_total%", tree == null || tree.isEmpty() ? 0 : tree.getEnquiryCount());
	}
	
	/**
	 * 
	 * @param map
	 * @param key
	 * @param toAdd
	 */
	private void add(CompactMap<String, String> map, String key, int toAdd) {
		if(map.containsKey(key)) {
			map.put(key, (Integer.valueOf(map.get(key)) + toAdd) + "");
		} else {
			map.put(key, toAdd + "");
		}
	}
	
	/**
	 * Get the current {@link BazaarIGUIType}
	 * 
	 * @return
	 */
	public BazaarIGUIType getCurrentGUIType() {
		return currentGUIType;
	}
	
	/**
	 * Always cancelled
	 * 
	 */
	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
	}
	
	/**
	 *Unregisters this ({@link IGUI})
	 * 
	 */
	@Override
	public final void onInventoryClose(InventoryCloseEvent event) {
		if(!locked) IGUIManager.remove(getId());
	}

	/**
	 * Always cancelled
	 * 
	 */
	@Override
	public final void onInventoryDrag(InventoryDragEvent event) {
		event.setCancelled(true);
	}

	public void lock() {
		locked = true;
	}
	
	public void unlock() {
		locked = false;
	}
	
	static double getMaxBuyOrderPrice(BazaarIGUI igui) {
		SelfBalancingBSTNode max = Category.getCategory(igui.currentCategory).getBuyOrders(igui.currentSub, igui.currentSubSub).getMax();
		return max == null ? Category.getCategory(igui.currentCategory).getEmptyPrices()[igui.currentSub - 1][igui.currentSubSub - 1] : max.getKey();
	}
	
	static double getMaxSellOfferPrice(BazaarIGUI igui) {
		SelfBalancingBSTNode max = Category.getCategory(igui.currentCategory).getSellOffers(igui.currentSub, igui.currentSubSub).getMax();
		return max == null ? Category.getCategory(igui.currentCategory).getEmptyPrices()[igui.currentSub - 1][igui.currentSubSub - 1] : max.getKey();
	}
	
	static double getMinBuyOrderPrice(BazaarIGUI igui) {
		SelfBalancingBSTNode min = Category.getCategory(igui.currentCategory).getBuyOrders(igui.currentSub, igui.currentSubSub).getMin();
		return min == null ? Category.getCategory(igui.currentCategory).getEmptyPrices()[igui.currentSub - 1][igui.currentSubSub - 1] : min.getKey();
	}
	
	static double getMinSellOfferPrice(BazaarIGUI igui) {
		SelfBalancingBSTNode min = Category.getCategory(igui.currentCategory).getSellOffers(igui.currentSub, igui.currentSubSub).getMin();
		return min == null ? Category.getCategory(igui.currentCategory).getEmptyPrices()[igui.currentSub - 1][igui.currentSubSub - 1] : min.getKey();
	}
	
	static Duplet<Double, String> getSpread(double minSellOfferPrice, double maxBuyOrderPrice, int percentage) {
		StringBuilder builder = new StringBuilder();
		double spread = MathsUtils.round(minSellOfferPrice - maxBuyOrderPrice, 2);
		builder.append("§6" + minSellOfferPrice + " §7- §6" + maxBuyOrderPrice + " §7= §6" + spread);
		Duplet<Double, String> pair = Tuple.of(spread*((double) percentage/100), builder.toString());
		return pair;
	}
	
	static ItemStack setType(BazaarIGUI igui, ItemStack item) {
		ItemStack original = Category.getCategory(igui.currentCategory).getOriginal(igui.currentSub, igui.currentSubSub).clone();
		ItemMeta im = original.getItemMeta();
		im.setLore(item.getItemMeta().getLore());
		im.setDisplayName(item.getItemMeta().getDisplayName());
		original.setItemMeta(im);
		return original;
	}
}
