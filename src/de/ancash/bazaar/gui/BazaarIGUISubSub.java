package de.ancash.bazaar.gui;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryAction;

import de.ancash.bazaar.management.Category;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.Clickable;

enum BazaarIGUISubSub {
	
	INSTANCE;
	
	public void openSubSub(BazaarIGUI igui, int subsub) {
		igui.currentGUIType = BazaarIGUIType.SUBSUB;
		igui.currentSubSub = subsub;
		Category category = Category.getCategory(igui.currentCategory);

		igui.title = igui.title.split("§r-> ")[1] + " §r-> " + category.getSubSub()[igui.currentSub - 1][igui.currentSubSub - 1].getItemMeta().getDisplayName();
		igui.newInventory(igui.title, 27);
		igui.clearInventoryItems();
		igui.setBackground(BazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setItem(category.getOriginal(igui.currentSub, igui.currentSubSub), 13); //sets the item the player wants to buy
		igui.setCloseItem(22);
		
		CompactMap<String, String> placeholder = igui.getPlaceholders(igui.currentCategory, igui.currentSub, igui.currentSubSub, 
				true, Integer.valueOf(BazaarIGUI.createBuyOrderItem.getItemMeta().getLore().stream().filter(str -> str.contains("%top_orders_")).findFirst().get().replace("%top_orders_", "").replace("%", "")), 
				true, Integer.valueOf(BazaarIGUI.createSellOfferItem.getItemMeta().getLore().stream().filter(str -> str.contains("%top_offers_")).findFirst().get().replace("%top_offers_", "").replace("%", "")));
		placeholder.put("%inventory_content%", InventoryUtils.getContentAmount(Bukkit.getPlayer(igui.getId()).getInventory(), category.getOriginal(igui.currentSub, subsub)) + "");
		
		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(BazaarIGUI.createBuyOrderItem.clone(), placeholder), 15, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory) BazaarIGUICreateBuyOrder.INSTANCE.openCreateBuyOrder(igui);
			}
		
		}).add();

		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(BazaarIGUI.createSellOfferItem.clone(), placeholder), 16, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory) BazaarIGUICreateSellOffer.INSTANCE.openCreateSellOffer(igui);
			}
		
		}).add();
		
		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(BazaarIGUI.buyInstantlyItem.clone(), placeholder), 10, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory) {
					BazaarIGUIBuyInstantly.INSTANCE.openBuyInstantlyInventory(igui);
				}
			}
		
		}).add();
		
		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(BazaarIGUI.sellInstantlyItem.clone(), placeholder), 11, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				if(topInventory) {
					BazaarIGUISellInstantly.INSTANCE.sellInstantly(igui);
				}
			}
		
		}).add();
	}
}
