package de.ancash.bazaar.gui.safe;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.gui.inventory.BazaarInventoryItem;
import de.ancash.bazaar.gui.inventory.BazaarInventoryType;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.Clickable;

enum BazaarIGUIMain {
	
	INSTANCE;
	
	public boolean setCategory(BazaarIGUI igui, int newCat) {
		if(!Category.exists(newCat)) return false;
		boolean switchCat = newCat != igui.currentCategory;
		igui.currentCategory = newCat;
		igui.currentGUIType = BazaarInventoryType.MAIN;
		if(switchCat) {
			igui.title = igui.plugin.getTemplate().getContents()[(newCat - 1) * 9].getItemMeta().getDisplayName();
			igui.newInventory(igui.title, 45);
		} else {
			igui.clearInventoryItems();
		}
		igui.setContents(igui.plugin.getTemplate().getContents());
		igui.clearInventoryItems();
		igui.setCloseItem(40);
		Category category = Category.getCategory(newCat);
		for(int sub = 1; sub <= 18; sub++) {
			ItemStack is = category.getSub()[sub - 1];
			if(is == null || is.getType() == XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial()) continue;
			is = igui.setEnquiriesInLore(is.clone(), newCat, sub);
			new BazaarInventoryItem(igui, is, Category.getSlotByID(sub), new Clickable() {
				
				@Override
				public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
					//opens sub inventory 
					if(topInventory) igui.openSub(Category.getSubBySlot(slot) - 1);
				}
				
			}).add();
		}
		for(int cat = 0; cat<5; cat++) {
			ItemStack is = igui.getItem(cat * 9);
			if(cat + 1 == newCat) is.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
			new BazaarInventoryItem(igui, is, cat * 9, new Clickable() {
				
				@Override
				public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
					//switches category	
					if(topInventory && !igui.setCategory(slot / 9 + 1)) igui.getInventory().getViewers().stream().forEach(player -> player.sendMessage(igui.plugin.getResponse().CATEGORY_IN_PROCESS));
				}
				
			}).add();
		}
		CompactMap<String, String> placeholders = new CompactMap<>();
		placeholders.put("%enquiries%", PlayerManager.get(igui.getId()).getEnquiries() + "");
		placeholders.put("%coins_to_claim%", PlayerManager.get(igui.getId()).getClaimableCoins() + "");
		placeholders.put("%items_to_claim%", PlayerManager.get(igui.getId()).getClaimableItems() +  "");
		igui.add(ItemStackUtils.replacePlaceholder(igui.getItem(41).clone(), placeholders), 41, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				BazaarIGUIManageEnquiries.INSTANCE.manageEnquiries(igui);
			}
		});
		return true;
	}
	
}
