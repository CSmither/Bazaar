package de.ancash.bazaar.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.datastructures.tuples.Duplet;
import de.ancash.ilibrary.minecraft.nbt.NBTItem;
import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.events.SellInstaEvent;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.utils.InventoryUtils;
import de.ancash.bazaar.utils.ItemStackUtils;
import de.ancash.bazaar.utils.Response;

public class SellInstantlyListener implements Listener{

	@EventHandler
	public void onSellInsta(SellInstaEvent e) {
		if(e.getSlot() == 31) {
			e.getPlayer().closeInventory();
			return;
		}
		Category cat = Category.getCategory(e.getCat());
		SelfBalancingBST rootBuyOrder = cat.getBuyOrders(e.getShow(), e.getSub());
		int toSell = InventoryUtils.getContentAmount(e.getPlayer().getInventory(), cat.getOriginial(e.getShow(), e.getSub()));
		if(toSell == 0) {
			e.getPlayer().sendMessage(Response.NO_ITEMS_TO_SELL);
		} else if(rootBuyOrder == null || rootBuyOrder.isEmpty()) {
				e.getPlayer().sendMessage(Response.NO_BUY_ORDER_AVAILABLE);
			} else {
				Duplet<Integer, Double> pair = rootBuyOrder.processInstaSell(toSell);
				Bazaar.getEconomy().depositPlayer(e.getPlayer(), pair.getSecond());
				InventoryUtils.removeItemAmount(pair.getFirst(), cat.getOriginial(e.getShow(), e.getSub()), e.getPlayer());
			}
	}
	
	public static ItemStack createSellInsta(int cat, int show, int sub, ItemStack sellInsta, Inventory inv, Player p) {
		Category category = Category.getCategory(cat);
		SelfBalancingBST rootBuyOrder = category.getBuyOrders(show, sub);
		CompactMap<String, String> placeholder = new CompactMap<String, String>();
		SelfBalancingBSTNode max = rootBuyOrder.isEmpty() ? null : rootBuyOrder.getMax();
		int toSell = InventoryUtils.getContentAmount(p.getInventory(), category.getOriginial(show, sub));
		placeholder.put("%inventory_content%", (toSell > 1 ? toSell + " items" : toSell == 0 ? "§cNone!" : "1 item"));
		placeholder.put("%orders_price_highest%", (rootBuyOrder.isEmpty() ? "§cN/A" : max.get().size() != 0 ? max.getKey() + " coins": "§cN/A"));
		sellInsta = ItemStackUtils.replacePlaceholder(sellInsta, placeholder);
		
		NBTItem info = new NBTItem(sellInsta);
		info.setInteger("bazaar.category", cat);
		info.setInteger("bazaar.item.show", show);
		info.setInteger("bazaar.item.sub", sub);
		return info.getItem();
	}
}
