package de.ancash.bazaar.gui.inventory;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.management.Category;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.Clickable;

public abstract class AbstractBazaarSubSubInv {

	private final BazaarInventoryClassManager clazzManager;
	
	protected AbstractBazaarSubSubInv(BazaarInventoryClassManager clazzManager) {
		this.clazzManager = clazzManager;
	}

	public abstract Map<String, String> getPlaceholder(AbstractBazaarIGUI igui);
	
	public final void openSubSub(AbstractBazaarIGUI igui, int subsub) {
		igui.currentGUIType = BazaarInventoryType.SUBSUB;
		igui.currentSubSub = subsub;
		if(Bukkit.isPrimaryThread()) {
			clazzManager.get(AbstractBazaarIGUI.class).submit(new BazaarRunnable(igui.getId()) {
				
				@Override
				public void run() {
					Map<String, String> placeholder = getPlaceholder(igui);
					new BukkitRunnable() {
						
						@Override
						public void run() {
							openSubSubSync(igui, placeholder);
						}
					}.runTask(igui.plugin);
				}
			});
		} else {
			Map<String, String> placeholder = getPlaceholder(igui);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					openSubSubSync(igui, placeholder);
				}
			}.runTask(igui.plugin);
		}
			
	}
	
	private final void openSubSubSync(AbstractBazaarIGUI igui, Map<String, String> placeholder) {
		
		Category category = Category.getCategory(igui.currentCategory);

		igui.title = igui.title.split("§r-> ")[1] + " §r-> " + category.getSubSub()[igui.currentSub - 1][igui.currentSubSub - 1].getItemMeta().getDisplayName();
		igui.newInventory(igui.title, 27);
		igui.clearInventoryItems();
		igui.setBackground(AbstractBazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setItem(category.getOriginal(igui.currentSub, igui.currentSubSub), 13); //sets the item the player wants to buy
		igui.setCloseItem(22);
		placeholder.put("%inventory_content%", InventoryUtils.getContentAmount(Bukkit.getPlayer(igui.getId()).getInventory(), category.getOriginal(igui.currentSub, igui.currentSubSub)) + "");
		
		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(BazaarInventoryObjects.CREATE_BUY_ORDER_ITEM.asItem().clone(), placeholder), 15, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory) clazzManager.get(AbstractBazaarCreateBuyOrderInv.class).openCreateBuyOrder(igui);
			}
		
		}).add();

		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(BazaarInventoryObjects.CREATE_SELL_OFFER_ITEM.asItem().clone(), placeholder), 16, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory) clazzManager.get(AbstractBazaarCreateSellOfferInv.class).openCreateSellOffer(igui);
			}
		
		}).add();
		
		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(BazaarInventoryObjects.BUY_INSTANTLY_ITEM.asItem().clone(), placeholder), 10, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory) clazzManager.get(AbstractBazaarBuyInstantlyInv.class).openBuyInstantlyInventory(igui);
			}
		
		}).add();
		
		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(BazaarInventoryObjects.SELL_INSTANTLY_ITEM.asItem().clone(), placeholder), 11, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory) clazzManager.get(AbstractBazaarSellInstantlyInv.class).sellInstantly(igui);
			}
		
		}).add();
	}
}