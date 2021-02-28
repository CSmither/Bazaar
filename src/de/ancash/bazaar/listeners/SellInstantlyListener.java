package de.ancash.bazaar.listeners;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.events.SellInstaEvent;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.utils.InventoryUtils;
import de.ancash.bazaar.utils.ItemStackUtils;
import de.ancash.bazaar.utils.Pair;
import de.ancash.bazaar.utils.Response;
import de.tr7zw.nbtapi.NBTItem;

public class SellInstantlyListener implements Listener{

	@EventHandler
	public void onSellInsta(SellInstaEvent e) {
		if(e.getSlot() == 31) {
			e.getPlayer().closeInventory();
			return;
		}
		SelfBalancingBST rootBuyOrder = Category.getCategory(e.getCat()).getBuyOrder(e.getItemId());
		int toSell = InventoryUtils.getContentAmount(e.getPlayer().getInventory(), e.getInv().getItem(13).clone());
		if(toSell == 0) {
			e.getPlayer().sendMessage(Response.NO_ITEMS_TO_SELL);
		} else if(rootBuyOrder == null || rootBuyOrder.isEmpty()) {
				e.getPlayer().sendMessage(Response.NO_BUY_ORDER_AVAILABLE);
			} else {
				Pair<Integer, Double> pair = rootBuyOrder.processInstaSell(toSell);
				Bazaar.getEconomy().depositPlayer(e.getPlayer(), pair.getValue());
				InventoryUtils.removeItemAmount(pair.getKey(), e.getInv().getItem(13), e.getPlayer());
			}
	}
	
	public static ItemStack createSellInsta(int cat, int item_id, ItemStack sellInsta, Inventory inv, Player p) {
		SelfBalancingBST rootBuyOrder = Category.getCategory(cat).getBuyOrder(item_id);
		HashMap<String, String> placeholder = new HashMap<String, String>();
		SelfBalancingBSTNode max = rootBuyOrder.isEmpty() ? null : rootBuyOrder.getMax();
		int toSell = InventoryUtils.getContentAmount(p.getInventory(), inv.getItem(13).clone());
		placeholder.put("%inventory_content%", (toSell > 1 ? toSell + " items" : toSell == 0 ? "§cNone!" : "1 item"));
		placeholder.put("%orders_price_highest%", (rootBuyOrder.isEmpty() ? "§cN/A" : max.get().size() != 0 ? max.getKey() + " coins": "§cN/A"));
		sellInsta = ItemStackUtils.replacePlaceholder(sellInsta, placeholder);
		
		NBTItem info = new NBTItem(sellInsta);
		info.setInteger("bazaar.category", cat);
		info.setInteger("bazaar.item.id", item_id);
		return info.getItem();
	}
}
