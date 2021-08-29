package de.ancash.bazaar.gui.base;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.management.Category;
import de.ancash.minecraft.XMaterial;
import de.ancash.minecraft.inventory.Clickable;

public abstract class AbstractBazaarSubInv {

	@SuppressWarnings("unused")
	private final BazaarInventoryClassManager clazzManager;
	
	protected AbstractBazaarSubInv(BazaarInventoryClassManager clazzManager) {
		this.clazzManager = clazzManager;
	}
	
	/**
	 * async
	 * 
	 * @param igui
	 * @param sub
	 * @return
	 */
	public abstract ItemStack[] getSubSubItems(AbstractBazaarIGUI igui, int sub);
	
	public void openSub(AbstractBazaarIGUI igui, int sub) {
		if(Bukkit.isPrimaryThread()) {
			igui.plugin.submit(new BazaarRunnable(igui.getId()) {
				
				@Override
				public void run() {
					ItemStack[] subSubItems = getSubSubItems(igui, sub);
					new BukkitRunnable() {
						
						@Override
						public void run() {
							openSubSync(igui, subSubItems, sub);
						}
					}.runTaskLater(igui.plugin, 1);
				}
			});
		} else {
			ItemStack[] subSubItems = getSubSubItems(igui, sub);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					openSubSync(igui, subSubItems, sub);
				}
			}.runTaskLater(igui.plugin, 1);
		}
	}
	
	private final void openSubSync(AbstractBazaarIGUI igui, ItemStack[] subSubItems, int sub) {
		igui.lock();
		igui.currentGUIType = BazaarInventoryType.SUB;
		igui.currentSub = sub;
		Category category = Category.getCategory(igui.currentCategory);
		igui.title = igui.plugin.getTemplate().getContents()[(igui.getCurrentCategory() - 1) * 9].getItemMeta().getDisplayName() + " §r-> " + category.getSubShow(igui.currentSub).getItemMeta().getDisplayName();
		igui.newInventory(igui.title, 27);
		igui.clearInventoryItems();
		igui.setBackground(AbstractBazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		for(int subsub = 0; subsub<9; subsub++) {
			ItemStack item = subSubItems[subsub];
			if(item == null || item.getType() == XMaterial.AIR.parseMaterial()) continue;
			new BazaarInventoryItem(igui, item, subsub + 9, new Clickable() {
				
				@Override
				public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
					if(topInventory) igui.openSubSub(slot - 8);
				}
			}).add();
		}
		igui.unlock();
	}
}
