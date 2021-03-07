package de.ancash.bazaar.listeners;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.datastructures.tuples.Duplet;
import de.ancash.ilibrary.minecraft.nbt.NBTItem;
import de.ancash.bazaar.files.Files;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.utils.CreateEnquiry;
import de.ancash.bazaar.utils.Enquiry;
import de.ancash.bazaar.utils.InventoryUtils;
import de.ancash.bazaar.utils.ItemFromFile;
import de.ancash.bazaar.utils.ItemStackUtils;
import de.ancash.bazaar.utils.MathsUtils;
import de.ancash.bazaar.utils.Response;
import de.ancash.bazaar.utils.SellOffer;

public class InvClickCreateSellOffer {

	private static final ItemStack[] priceInv = new ItemStack[36];
	private static final ItemStack[] confirm = new ItemStack[36];
	
	static {
		for(int i = 0; i<36; i++) {
			priceInv[i] = ItemFromFile.get(Files.getInvConfig(), "inventory.background");
			confirm[i] = ItemFromFile.get(Files.getInvConfig(), "inventory.background");
		}
		
		priceInv[10] = ItemFromFile.get(Files.getInvConfig(), "inventory.createSellOffer.sameAsBestOffer");
		priceInv[12] = ItemFromFile.get(Files.getInvConfig(), "inventory.createSellOffer.bestOffer_01");
		priceInv[14] = ItemFromFile.get(Files.getInvConfig(), "inventory.createSellOffer.ofSpread10");
		priceInv[31] = ItemFromFile.get(Files.getInvConfig(), "inventory.close");
		confirm[13] = ItemFromFile.get(Files.getInvConfig(), "inventory.createSellOffer.confirm");
		confirm[31] = ItemFromFile.get(Files.getInvConfig(), "inventory.close");
	}
	
	public static void handle(CreateEnquiry e) {
		if(e.getSlot() == 31) {
			e.getPlayer().closeInventory();
			return;
		}
		NBTItem old_info = new NBTItem(e.getInv().getItem(11));
		int category = old_info.getInteger("bazaar.category");
		int show = old_info.getInteger("bazaar.item.show");
		int sub = old_info.getInteger("bazaar.item.sub");
		Player p = e.getPlayer();
		//open inv for creating so
		if(e.getTitle().contains("->")) {			
			int to_sell = InventoryUtils.getContentAmount(p.getInventory(), e.getInv().getItem(13));
			
			if(to_sell == 0) {
				p.sendMessage(Response.NO_ITEMS_TO_SELL);
				return;
			}
			
			Inventory inv = PlayerManager.get(p.getUniqueId()).getAtWhatPriceAreYouSelling();
			inv.clear();
			inv.setContents(priceInv.clone());
			
			CompactMap<String, String> placeholder = new CompactMap<String, String>();
			placeholder.put("%inventory_content%", "" + to_sell);
			
			Category cat = Category.getCategory(category);
			SelfBalancingBST rootSellOffer = Category.getCategory(category).getSellOffers(show, sub);
			SelfBalancingBST rootBuyOrder = Category.getCategory(category).getBuyOrders(show, sub);
			SelfBalancingBSTNode buyOrderMax = (rootBuyOrder != null && !rootBuyOrder.isEmpty()) ? rootBuyOrder.getMax() : null;
			SelfBalancingBSTNode sellOfferMin = (rootSellOffer != null && !rootSellOffer.isEmpty()) ? rootSellOffer.getMin() : null;
			
			if(buyOrderMax == null) p.sendMessage(Response.BUY_ORDER_USING_PREDEFINED_PRICE);
			if(sellOfferMin == null) p.sendMessage(Response.SELL_OFFER_USING_PREDEFINED_PRICE);
						
			double lowestSellOfferPrice = (sellOfferMin == null) ? MathsUtils.round(cat.getEmptyPrices()[show - 1][sub - 1], 2) : MathsUtils.round(sellOfferMin.getKey(), 2);
			double highestBuyOrderPrice= (buyOrderMax == null) ? MathsUtils.round(cat.getEmptyPrices()[show - 1][sub - 1], 2) : MathsUtils.round(buyOrderMax.getKey(), 2);
			
			placeholder.put("%offers_price_lowest%", "§6" + lowestSellOfferPrice + " coins");
			placeholder.put("%orders_price_highest%", "§6" + highestBuyOrderPrice + " coins");
			placeholder.put("%unit_price%", "§6" + (lowestSellOfferPrice) + " coins");
			placeholder.put("%price_total%", "§6" + (MathsUtils.round(lowestSellOfferPrice*to_sell, 2)) + " coins");
			
			ItemStack temp = null;
			ItemMeta tempMeta = null;
			
			//same as best offer
			temp = e.getInv().getItem(13).clone();
			tempMeta = temp.getItemMeta();
			tempMeta.setLore(priceInv[10].getItemMeta().getLore());
			tempMeta.setDisplayName(priceInv[10].getItemMeta().getDisplayName());
			temp.setItemMeta(tempMeta);
			inv.setItem(10, ItemStackUtils.replacePlaceholder(temp.clone(), placeholder));
			
			//best offer -0.1
			placeholder.put("%unit_price%", "§6" + MathsUtils.round((lowestSellOfferPrice - 0.1), 2) + " coins");
			placeholder.put("%price_total%", "§6" + (MathsUtils.round((lowestSellOfferPrice - 0.1) * to_sell, 2)) + " coins");
			inv.setItem(12, ItemStackUtils.replacePlaceholder(priceInv[12].clone(), placeholder));
			
			//10 spread
			Duplet<Double, String> spread = InventoryUtils.getSpread(lowestSellOfferPrice, highestBuyOrderPrice, 10);
			placeholder.put("%unit_price%", "§6" + MathsUtils.round(lowestSellOfferPrice - spread.getFirst(), 2) + " coins");
			placeholder.put("%price_total%", "§6" + (MathsUtils.round((lowestSellOfferPrice - spread.getFirst()) * to_sell, 2)) + " coins");
			placeholder.put("%spread%", spread.getSecond());
			inv.setItem(14, ItemStackUtils.replacePlaceholder(priceInv[14].clone(), placeholder));
			
			NBTItem nbt = new NBTItem(inv.getItem(11));
			nbt.setInteger("bazaar.sell_offer.sell", to_sell);
			nbt.setInteger("bazaar.item.show", show);
			nbt.setInteger("bazaar.item.sub", sub);
			nbt.setInteger("bazaar.category", category);
			nbt.setDouble("bazaar.sell_offer.price.1", lowestSellOfferPrice);
			nbt.setDouble("bazaar.sell_offer.price.2", MathsUtils.round(lowestSellOfferPrice - 0.1, 2));
			nbt.setDouble("bazaar.sell_offer.price.3", MathsUtils.round(lowestSellOfferPrice - spread.getFirst(), 2));
			inv.setItem(11, nbt.getItem());
			p.openInventory(inv);
			return;
		}
		
		if(e.getTitle().equals("At what price are you selling?") && (e.getSlot() == 10 || e.getSlot() == 12 || e.getSlot() == 14)) {
			Inventory inv = PlayerManager.get(p.getUniqueId()).getConfirmSellOffer();
			inv.clear();
			inv.setContents(confirm.clone());
			CompactMap<String, String> placeholder = new CompactMap<String, String>();
			double unit_price = e.getSlot() == 10 ? old_info.getDouble("bazaar.sell_offer.price.1") : (e.getSlot() == 12 ? old_info.getDouble("bazaar.sell_offer.price.2") : old_info.getDouble("bazaar.sell_offer.price.3"));
			placeholder.put("%unit_price%", "§6" + unit_price + " coins");
			placeholder.put("%price_total%", "§6" + MathsUtils.round(unit_price * old_info.getInteger("bazaar.sell_offer.sell"), 2) + " coins");
			placeholder.put("%inventory_content%", "" + old_info.getInteger("bazaar.sell_offer.sell"));
			placeholder.put("%displayname%", Category.getCategory(category).getSubShow()[show - 1][sub - 1].getItemMeta().getDisplayName());
			
			ItemStack c = e.getInv().getItem(10).clone();
			ItemMeta im = c.getItemMeta();
			im.setDisplayName(confirm[13].getItemMeta().getDisplayName());
			im.setLore(confirm[13].getItemMeta().getLore());
			c.setItemMeta(im);
			NBTItem nbt = new NBTItem(ItemStackUtils.replacePlaceholder(c, placeholder));
			nbt.setInteger("bazaar.category", category);
			nbt.setInteger("bazaar.sell_offer.sell", old_info.getInteger("bazaar.sell_offer.sell"));
			nbt.setInteger("bazaar.item.show", show);
			nbt.setInteger("bazaar.item.sub", sub);
			nbt.setDouble("bazaar.sell_offer.unit_price", unit_price);
			inv.setItem(13, nbt.getItem());
			p.openInventory(inv);
			return;
		}
		
		if(e.getTitle().equals("Confirm Sell Offer") && e.getSlot() == 13) {
			if(PlayerManager.get(e.getPlayer().getUniqueId()).getEnquiries() >= 21) {
				e.getPlayer().sendMessage(Response.CANNOT_CREATE_ENQUIRY);
				return;
			}
			NBTItem info = new NBTItem(e.getInv().getItem(13));
			int to_sell = info.getInteger("bazaar.sell_offer.sell");
			int cat = info.getInteger("bazaar.category");
			double unit_price = info.getDouble("bazaar.sell_offer.unit_price"); 
			show = info.getInteger("bazaar.item.show");
			sub = info.getInteger("bazaar.item.sub");
			InventoryUtils.removeItemAmount(to_sell, Category.getCategory(cat).getOriginial(show, sub).clone(), p);
			SellOffer so = new SellOffer(to_sell, unit_price, p.getUniqueId(), cat, show, sub);
			Enquiry.insert(so);
			p.closeInventory();
		}
	}
}
