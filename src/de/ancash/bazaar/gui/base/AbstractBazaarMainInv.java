package de.ancash.bazaar.gui.base;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.management.Category;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.Clickable;

public abstract class AbstractBazaarMainInv {
	
	private final BazaarInventoryClassManager clazzManager;
	
	protected AbstractBazaarMainInv(BazaarInventoryClassManager clazzManager) {
		this.clazzManager = clazzManager;
	}
	
	public abstract ItemStack[] getSubItems(AbstractBazaarIGUI igui, int newCat);
	
	public abstract Map<String, String> getManageEnquiriesPlaceholder(AbstractBazaarIGUI igui, int newCat);
	
	public boolean openCategory(AbstractBazaarIGUI igui, int newCat) {
		
		if(!Category.exists(newCat)) return false;

		if(!Bukkit.isPrimaryThread()) {
			Map<String, String> getManageEnquiriesPlaceholder = getManageEnquiriesPlaceholder(igui, newCat);
			ItemStack[] items = getSubItems(igui, newCat);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					openCategorySync(igui, newCat, items, getManageEnquiriesPlaceholder);
				}
			}.runTaskLater(igui.plugin, 1);
		} else {
			igui.plugin.submit(new BazaarRunnable(igui.getId()) {
				
				@Override
				public void run() {
					Map<String, String> manageEnquiriesPlaceholder = getManageEnquiriesPlaceholder(igui, newCat);
					ItemStack[] items = getSubItems(igui, newCat);
					new BukkitRunnable() {
						
						@Override
						public void run() {
							openCategorySync(igui, newCat, items, manageEnquiriesPlaceholder);
						}
					}.runTaskLater(igui.plugin, 1);
				}
			});
		}
		return true;
	}
	
	private final boolean openCategorySync(AbstractBazaarIGUI igui, int newCat, ItemStack[] subItems, Map<String, String> placeholder) {
		if(!Category.exists(newCat)) return false;
		igui.lock();
		igui.currentCategory = newCat;
		igui.currentGUIType = BazaarInventoryType.MAIN;
		igui.title = igui.plugin.getTemplate().getContents()[(newCat - 1) * 9].getItemMeta().getDisplayName();
		igui.newInventory(igui.title, 45);
		igui.setContents(igui.plugin.getTemplate().getContents());
		igui.setCloseItem(40);
		for(int sub = 1; sub <= 18; sub++) {
			ItemStack is = subItems[sub - 1];
			if(is == null) continue;
			if(is.getType() == XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial()) System.out.println("!!STAINED GLASS PAINE!!!");
			new BazaarInventoryItem(igui, is, Category.getSlotByID(sub), new Clickable() {
				
				@Override
				public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
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
					if(topInventory && !igui.setCategory(slot / 9 + 1)) igui.getInventory().getViewers().forEach(player -> player.sendMessage(igui.plugin.getResponse().CATEGORY_IN_PROCESS));
				}
				
			}).add();
		}
		
		igui.add(ItemStackUtils.replacePlaceholder(igui.getItem(41).clone(), placeholder), 41, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				clazzManager.get(AbstractBazaarManageEnquiriesInv.class).manageEnquiries(igui);
			}
		});
		igui.unlock();
		return true;
	}	
}