package de.ancash.bazaar.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.utils.InventoryUtils;
import de.ancash.datastructures.tuples.Duplet;

class BazaarIGUISellInstantly {
	
	static void sellInstantly(BazaarIGUI igui) {
		Player player = Bukkit.getPlayer(igui.getId());
		Category cat = Category.getCategory(igui.currentCategory);
		SelfBalancingBST rootBuyOrder = cat.getBuyOrders(igui.currentSub, igui.currentSubSub);
		int toSell = InventoryUtils.getContentAmount(player.getInventory(), cat.getOriginal(igui.currentSub, igui.currentSubSub));
		if(toSell == 0) {
			player.sendMessage(igui.plugin.getResponse().NO_ITEMS_TO_SELL);
		} else if(rootBuyOrder == null || rootBuyOrder.isEmpty()) {
			player.sendMessage(igui.plugin.getResponse().NO_BUY_ORDER_AVAILABLE);
		} else {
			Duplet<Integer, Double> pair = rootBuyOrder.processInstaSell(toSell);
			igui.plugin.getEconomy().depositPlayer(player, pair.getSecond());
			InventoryUtils.removeItemAmount(pair.getFirst(), cat.getOriginal(igui.currentSub, igui.currentSubSub), player);
		}
		BazaarIGUISubSub.openSubSub(igui, igui.currentSubSub);
	}	
}
