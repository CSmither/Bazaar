package de.ancash.bazaar.gui.base;

import static de.ancash.bazaar.gui.base.BazaarInventoryObjects.*;

import java.util.Map;
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
import de.ancash.bazaar.management.SellOffer;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.Clickable;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.IGUIManager;
import de.ancash.minecraft.inventory.InventoryItem;
import de.ancash.misc.MathsUtils;

public abstract class AbstractBazaarIGUI extends IGUI{
	
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
	
	BazaarInventoryType currentGUIType;
	Bazaar plugin;
	int currentCategory = -1;
	int currentSub;
	int currentSubSub;
	String title;
	int enquiryAmount = 0;
	double enquiryPrice = 0;
	double instaPrice = 0;
	int inventoryContent = 0;
	
	private boolean locked = false;
	private final BazaarInventoryClassManager clazzManager;
	
	public AbstractBazaarIGUI(BazaarInventoryClassManager clazzManager) {
		super(null, 9, "Loading...");
		this.clazzManager = clazzManager;
	}
	
	public BazaarInventoryType getBazaarInventoryType() {return currentGUIType;}
	public Bazaar getPlugin() {return plugin;}
	public int getCurrentCategory() {return currentCategory;}
	public int getCurrentSub() {return currentSub;}
	public int getCurrentSubSub() {return currentSubSub;}
	public int getEnquiryAmount() {return enquiryAmount;}
	public double getEnquiryPrice() {return enquiryPrice;}
	public double getInstaPrice() {return instaPrice;}
	public void setPlugin(Bazaar pl) {this.plugin = pl;}
	public void setTitle(String title) {this.title = title;}
	public int getInventoryContent() {return inventoryContent;}
	public void register(UUID id) {
		super.setUUID(id);
		lock();
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
			setItem(BACKGROUND_ITEM.asItem(), slot);
	}
	
	/**
	 * Sets close button
	 * 
	 * @param slot
	 */
	public final void setCloseItem(int slot) {
		new InventoryItem(this, CLOSE_INVENTORY_ITEM.asItem(), slot, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory) closeAll();
			}
			
		}).add();
	}
	
	/**
	 * Add a new {@link BazaarInventoryItem}
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
		return clazzManager.get(AbstractBazaarMainInv.class).openCategory(this, c);
	}
	
	public void openSub(int sub) {
		clazzManager.get(AbstractBazaarSubInv.class).openSub(this, sub);
	}

	public void openSubSub(int subsub) {
		clazzManager.get(AbstractBazaarSubSubInv.class).openSubSub(this, subsub);
	}
	
	/**
	 * 
	 * @param is
	 * @param cat
	 * @param sub
	 * @return
	 */
	public ItemStack setEnquiriesInLore(ItemStack is, int cat, int sub) {
		return setEnquiriesInLoreExact(is, cat, sub, -1, false, 0, false, 0);
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
	public ItemStack setEnquiriesInLoreExact(ItemStack is, int cat, int sub, int subsub, boolean topSellOffers, int tSOcnt, boolean topBuyOrders, int tBOcnt) {
		return ItemStackUtils.replacePlaceholder(is, getPlaceholders(cat, sub, subsub, topSellOffers, tSOcnt, topBuyOrders, tBOcnt));
	}
	
	/**
	 * Get the current {@link BazaarInventoryType}
	 * 
	 * @return
	 */
	public BazaarInventoryType getCurrentGUIType() {
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
		IGUIManager.register(this, getId());
		locked = true;
	}
	
	public void unlock() {
		locked = false;
	}
	
	public ItemStack setType(ItemStack item) {
		ItemStack original = Category.getCategory(currentCategory).getOriginal(currentSub, currentSubSub).clone();
		ItemMeta im = original.getItemMeta();
		im.setLore(item.getItemMeta().getLore());
		im.setDisplayName(item.getItemMeta().getDisplayName());
		original.setItemMeta(im);
		return original;
	}
	
	public abstract double getHighestBuyOrderPrice();
	
	public abstract double getHighestSellOfferPrice();
	
	public abstract double getLowestBuyOrderPrice();
	
	public abstract double getLowestSellOfferPrice();
	
	/**
	 * Get all placeholders for enquires
	 * 
	 * @param cat
	 * @param sub
	 * @param subsub
	 * @return
	 */
	public abstract Map<String, String> getPlaceholders(int cat, int sub, int subsub, boolean topSellOffers, int topSellOfferCount, boolean topBuyOrders, int topBuyOrderCount);
	
	/**
	 * Get the placeholders for the top x {@link BuyOrder}s
	 * 
	 * @param placeholder
	 * @param cnt
	 * @return
	 */
	//public abstract Map<String, String> getTopBuyOrders(Map<String, String> placeholder, int cnt);
	
	/**
	 * Get the placeholders for the top x {@link SellOffer}s
	 * 
	 * @param placeholder
	 * @param cnt
	 * @return
	 */
	//public abstract Map<String, String> getTopSellOffers(Map<String, String> placeholder, int cnt);
	
	public static Duplet<Double, String> getSpread(double minSellOfferPrice, double maxBuyOrderPrice, int percentage) {
		StringBuilder builder = new StringBuilder();
		double spread = MathsUtils.round(minSellOfferPrice - maxBuyOrderPrice, 2);
		builder.append("§6" + minSellOfferPrice + " §7- §6" + maxBuyOrderPrice + " §7= §6" + spread);
		Duplet<Double, String> pair = Tuple.of(spread*((double) percentage/100), builder.toString());
		return pair;
	}
}