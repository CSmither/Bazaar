package de.ancash.bazaar.gui.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.management.Category;
import de.ancash.minecraft.InventoryUtils;

public abstract class AbstractBazaarSellInstantlyInv {
	
	private final BazaarInventoryClassManager clazzManager;
	
	protected AbstractBazaarSellInstantlyInv(BazaarInventoryClassManager clazzManager) {
		this.clazzManager = clazzManager;
	}
	
	public abstract void onSellInstantly(AbstractBazaarIGUI igui, int amount);
	
	public final void sellInstantly(AbstractBazaarIGUI igui) {
		if(Bukkit.isPrimaryThread())
			sellInstantlySync(igui);
		else
			new BukkitRunnable() {
				
				@Override
				public void run() {
					sellInstantlySync(igui);
				}
			}.runTask(igui.plugin);
	}
	
	private final void sellInstantlySync(AbstractBazaarIGUI igui) {
		Player player = Bukkit.getPlayer(igui.getId());
		Category cat = Category.getCategory(igui.currentCategory);
		int toSell = InventoryUtils.getContentAmount(player.getInventory(), cat.getOriginal(igui.currentSub, igui.currentSubSub));
		if(toSell == 0) {
			player.sendMessage(igui.plugin.getResponse().NO_ITEMS_TO_SELL);
		} else {
			clazzManager.get(AbstractBazaarIGUI.class).submit(new BazaarRunnable(igui.getId()) {
				
				@Override
				public void run() {
					onSellInstantly(igui, toSell);
				}
			});
		}
	}	
}
