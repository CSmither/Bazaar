package de.ancash.bazaar.gui.base;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.anvilgui.AnvilGUI;
import de.ancash.minecraft.inventory.Clickable;

public abstract class AbstractBazaarBuyInstantlyInv {
		
	private final BazaarInventoryClassManager clazzManager;
	
	protected AbstractBazaarBuyInstantlyInv(BazaarInventoryClassManager clazzManager) {
		this.clazzManager = clazzManager;
	}
	
	private final Map<UUID, AnvilGUI> guis = new HashMap<>();
	
	/**
	 * async
	 * 
	 * @param igui
	 * @param amount
	 */
	public abstract void onBuyInstantly(AbstractBazaarIGUI igui, int amount);
	
	public abstract Map<String, String> getPlaceholder(AbstractBazaarIGUI igui);
	
	public abstract boolean sellOfferExist(AbstractBazaarIGUI igui);
	
	/**
	 * Opens a new {@link Inventory} and sets basic items
	 * 
	 * @param igui
	 * @param hasSellOffer
	 */
	public final void openBuyInstantlyInventory(AbstractBazaarIGUI igui) {
		if(Bukkit.isPrimaryThread()) {
			igui.plugin.submit(new BazaarRunnable(igui.getId()) {
				
				@Override
				public void run() {
					if(!sellOfferExist(igui)) {
						clazzManager.get(AbstractBazaarSubSubInv.class).openSubSub(igui, igui.currentSubSub);
						Bukkit.getPlayer(igui.getId()).sendMessage(igui.plugin.getResponse().NO_SELL_OFFER_AVAILABLE);
						return;
					}
					Map<String, String> placeholder = getPlaceholder(igui);
					new BukkitRunnable() {
						
						@Override
						public void run() {
							openBuyInstantlyInventorySync(igui, placeholder);
						}
					}.runTaskLater(igui.plugin, 1);
				}
			});
		} else {
			if(!sellOfferExist(igui)) {
				clazzManager.get(AbstractBazaarSubSubInv.class).openSubSub(igui, igui.currentSubSub);
				Bukkit.getPlayer(igui.getId()).sendMessage(igui.plugin.getResponse().NO_SELL_OFFER_AVAILABLE);
				return;
			}
			Map<String, String> placeholder = getPlaceholder(igui);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					openBuyInstantlyInventorySync(igui, placeholder);
				}
			}.runTaskLater(igui.plugin, 1);
		}
	}
	
	private final void openBuyInstantlyInventorySync(AbstractBazaarIGUI igui, Map<String, String> placeholders) {
		igui.lock();
		igui.currentGUIType = BazaarInventoryType.BUY_INSTANTLY;
		igui.newInventory(BazaarInventoryObjects.BUY_INSTANTLY_INVENTORY_TITLE.asString(), 27);
		igui.clearInventoryItems();
		igui.setBackground(AbstractBazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		try {
			igui.instaPrice = Double.valueOf(placeholders.get(BazaarPlaceholder.OFFERS_PRICE_LOWEST));
		} catch(Exception ex) {
			igui.instaPrice = -1;
		}
		
		igui.add(ItemStackUtils.replacePlaceholder(igui.setType(BazaarInventoryObjects.BUY_INSTANTLY_ONE_ITEM.asItem().clone()), placeholders), 10, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				onPreBuyInstantly(igui, 1);
			}
		});
		igui.add(ItemStackUtils.replacePlaceholder(igui.setType(BazaarInventoryObjects.BUY_INSTANTLY_STACK_ITEM.asItem().clone()), placeholders), 12, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				onPreBuyInstantly(igui, 64);
			}
		});
		/*igui.add(ItemStackUtils.replacePlaceholder(setType(igui, bI_FILL_INVENTORY.clone()), placeholders), 14, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				process(igui, 1);
			}
		});*/
		
		igui.add(BazaarInventoryObjects.CUSTOM_AMOUNT_ITEM.asItem(), 16, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.lock();
				AnvilGUI gui = new AnvilGUI.Builder().itemLeft(BazaarInventoryObjects.PICK_AMOUNT_ITEM.asItem()).plugin(igui.plugin).onComplete((player, str) ->{
					int temp = -1;
					try {
						temp = Integer.valueOf(str);
					} catch(NumberFormatException ex) {}
					final int amount = temp;
					
					if(amount <= 0) {
						player.sendMessage("§cInvalid Input: " + amount);
						guis.remove(igui.getId()).closeInventory();
						openBuyInstantlyInventory(igui);
						return AnvilGUI.Response.text("");
					} else {
						guis.remove(igui.getId()).closeInventory();
						
						onPreBuyInstantly(igui, amount);
						
						return AnvilGUI.Response.text("");
					}
					
				}).open(Bukkit.getPlayer(igui.getId()));
				guis.put(igui.getId(), gui);
			}
		});
		igui.unlock();
	}
	
	private final void onPreBuyInstantly(AbstractBazaarIGUI igui, int amount) {
		igui.plugin.submit(new BazaarRunnable(igui.getId()) {
			
			@Override
			public void run() {
				onBuyInstantly(igui, amount);
			}
			
		});
	}
}