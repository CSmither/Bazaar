package de.ancash.bazaar.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.AbstractBazaarSellInstantlyInv;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.misc.MathsUtils;
import de.ancash.datastructures.tuples.Duplet;

public class BazaarSellInstantlyImpl extends AbstractBazaarSellInstantlyInv{
	
	public BazaarSellInstantlyImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public void onSellInstantly(AbstractBazaarIGUI igui, int amount) {
		Player player = Bukkit.getPlayer(igui.getId());
		Category cat = Category.getCategory(igui.getCurrentCategory());
		SelfBalancingBST rootBuyOrder = cat.getBuyOrders(igui.getCurrentSub(), igui.getCurrentSubSub());
		synchronized (igui.getPlugin().getEnquiryUtils().getLock()) {
			int toSell = igui.getInventoryContent();
			if(toSell <= 0) {
				player.sendMessage(igui.getPlugin().getResponse().NO_ITEMS_TO_SELL);
			} else if(rootBuyOrder == null || rootBuyOrder.isEmpty()) {
				player.sendMessage(igui.getPlugin().getResponse().NO_BUY_ORDER_AVAILABLE);
			} else {
				Duplet<Integer, Double> pair = rootBuyOrder.processInstaSell(igui.getInstaPrice(), toSell);
				igui.getPlugin().getEconomy().depositPlayer(player, MathsUtils.round(pair.getSecond() * (1D - igui.getPlugin().getTax() / 100D), 1));
				InventoryUtils.removeItemAmount(pair.getFirst(), cat.getOriginal(igui.getCurrentSub(), igui.getCurrentSubSub()), player);
				Bukkit.getPlayer(igui.getId()).sendMessage(igui.getPlugin().getResponse().SELL_INSTANTLY.replace(BazaarPlaceholder.AMOUNT, pair.getFirst() + "")
						.replace(BazaarPlaceholder.PRICE, MathsUtils.round(pair.getSecond(), 1) + "")
						.replace(BazaarPlaceholder.DISPLAY_NAME, 
								Category.getCategory(igui.getCurrentCategory()).getSubSubShow(igui.getCurrentSub(), igui.getCurrentSubSub()).getItemMeta().getDisplayName()));
			}
			igui.openSubSub(igui.getCurrentSubSub());
		}
	}
}
