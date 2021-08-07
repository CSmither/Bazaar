package de.ancash.bazaar.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.bazaar.gui.inventory.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.inventory.AbstractBazaarSellInstantlyInv;
import de.ancash.bazaar.gui.inventory.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.datastructures.tuples.Duplet;

public class BazaarSellInstantlyInv extends AbstractBazaarSellInstantlyInv{
	
	public BazaarSellInstantlyInv(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public synchronized void onSellInstantly(AbstractBazaarIGUI igui, int amount) {
		Player player = Bukkit.getPlayer(igui.getId());
		Category cat = Category.getCategory(igui.getCurrentCategory());
		SelfBalancingBST rootBuyOrder = cat.getBuyOrders(igui.getCurrentSub(), igui.getCurrentSubSub());
		int toSell = InventoryUtils.getContentAmount(player.getInventory(), cat.getOriginal(igui.getCurrentSub(), igui.getCurrentSubSub()));
		if(toSell == 0) {
			player.sendMessage(igui.getPlugin().getResponse().NO_ITEMS_TO_SELL);
		} else if(rootBuyOrder == null || rootBuyOrder.isEmpty()) {
			player.sendMessage(igui.getPlugin().getResponse().NO_BUY_ORDER_AVAILABLE);
		} else {
			Duplet<Integer, Double> pair = rootBuyOrder.processInstaSell(toSell);
			igui.getPlugin().getEconomy().depositPlayer(player, pair.getSecond());
			InventoryUtils.removeItemAmount(pair.getFirst(), cat.getOriginal(igui.getCurrentSub(), igui.getCurrentSubSub()), player);
		}
		igui.openSubSub(igui.getCurrentSubSub());
	}
}
