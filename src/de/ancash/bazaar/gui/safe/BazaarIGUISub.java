package de.ancash.bazaar.gui.safe;

import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.gui.inventory.BazaarInventoryItem;
import de.ancash.bazaar.gui.inventory.BazaarInventoryType;
import de.ancash.bazaar.management.Category;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.Clickable;

enum BazaarIGUISub {

	INSTANCE;
	
	/**
	 * 
	 * @param igui
	 */
	public void open(BazaarIGUI igui, int sub) {
		igui.currentGUIType = BazaarInventoryType.SUB;
		igui.currentSub = sub;
		Category category = Category.getCategory(igui.currentCategory);
		igui.title = igui.title + " §r-> " + category.getSub()[igui.currentSub- 1].getItemMeta().getDisplayName();
		igui.newInventory(igui.title, 27);
		igui.clearInventoryItems();
		igui.setBackground(BazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		for(int subsub = 0; subsub<9; subsub++) {
			ItemStack item = category.getSubSub()[igui.currentSub - 1][subsub];
			if(item == null || item.getType() == XMaterial.AIR.parseMaterial()) continue;
			item = igui.setEnquiriesInLoreExact(item.clone(), igui.currentCategory, igui.currentSub, subsub + 1, false, 0, false, 0);
			new BazaarInventoryItem(igui, item, subsub + 9, new Clickable() {
				
				@Override
				public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
					//opens inv for specific item
					if(topInventory) igui.openSubSub(slot - 8);
				}
			}).add();;
		}
	}
	
}
