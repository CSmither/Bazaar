package de.ancash.bazaar.listeners;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.files.Files;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.utils.BuyOrder;
import de.ancash.bazaar.utils.CreateEnquiry;
import de.ancash.bazaar.utils.Enquiry;
import de.ancash.bazaar.utils.InventoryUtils;
import de.ancash.bazaar.utils.ItemFromFile;
import de.ancash.bazaar.utils.ItemStackUtils;
import de.ancash.bazaar.utils.MathsUtils;
import de.ancash.bazaar.utils.Pair;
import de.ancash.bazaar.utils.Response;
import de.tr7zw.nbtapi.NBTItem;

public class InvClickCreateBuyOrder{
	
	private static final ItemStack[] amountInv = new ItemStack[36];
	private static final ItemStack[] priceInv = new ItemStack[36];
	
	static {
		for(int i = 0; i<36; i++) {
			amountInv[i] = ItemFromFile.get(Files.getInvConfig(), "inventory.background");
			priceInv[i] = ItemFromFile.get(Files.getInvConfig(), "inventory.background");
		}
		amountInv[10] = ItemFromFile.get(Files.getInvConfig(), "inventory.createBuyOrder.opt1");
		amountInv[12] = ItemFromFile.get(Files.getInvConfig(), "inventory.createBuyOrder.opt2");
		amountInv[14] = ItemFromFile.get(Files.getInvConfig(), "inventory.createBuyOrder.opt3");
		amountInv[31] = ItemFromFile.get(Files.getInvConfig(), "inventory.close");
		
		priceInv[10] = ItemFromFile.get(Files.getInvConfig(), "inventory.createBuyOrder.sameAsTopOrder");
		priceInv[12] = ItemFromFile.get(Files.getInvConfig(), "inventory.createBuyOrder.topOrder_01");
		priceInv[14] = ItemFromFile.get(Files.getInvConfig(), "inventory.createBuyOrder.ofSpread5");
		priceInv[31] = ItemFromFile.get(Files.getInvConfig(), "inventory.close");
	}
	
	public static void handle(CreateEnquiry e) {
		if(e.getSlot() == 31) {
			e.getPlayer().closeInventory();
			return;
		}
		NBTItem old_info = new NBTItem(e.getInv().getItem(11));
		int item_id = old_info.getInteger("bazaar.item.id");
		int category = old_info.getInteger("bazaar.category");
		Player p = e.getPlayer();
		//open inv for creating so
		if(e.getTitle().contains("->")) {
			Inventory inv = PlayerManager.get(p.getUniqueId()).getHowManyDoYouWant();
			inv.clear();
			inv.setContents(amountInv.clone());
			
			inv.setItem(10, getOptAmount(e.getInv().getItem(13), 10, 4));
			
			inv.setItem(12, getOptAmount(e.getInv().getItem(13), 12, 10));
	
			inv.setItem(14, getOptAmount(e.getInv().getItem(13), 14, 64));
			
			NBTItem nbt = new NBTItem(inv.getItem(11));
			nbt.setInteger("bazaar.item.id", item_id);
			nbt.setInteger("bazaar.category", category);
			inv.setItem(11, nbt.getItem());
			
			p.openInventory(inv);
			return;
		}
		
		if(e.getTitle().equals("How many do you want?") && (e.getSlot() == 10 || e.getSlot() == 12 || e.getSlot() == 14)) {
			HashMap<String, String> placeholder = new HashMap<String, String>();
			Category cat = Category.getCategory(category);
			SelfBalancingBST rootBuyOrder = cat.getBuyOrder(item_id);
			SelfBalancingBST rootSellOffer= cat.getSellOffer(item_id);
			SelfBalancingBSTNode buyOrderMax = (rootBuyOrder != null && !rootBuyOrder.isEmpty()) ? rootBuyOrder.getMax() : null;
			SelfBalancingBSTNode sellOfferMin = (rootSellOffer != null && !rootSellOffer.isEmpty()) ? rootSellOffer.getMin() : null;
			
			int want_to_buy = e.getSlot() == 10 ? 64 : (e.getSlot() == 12 ? 160 : 1024);
			
			if(buyOrderMax == null) p.sendMessage(Response.BUY_ORDER_USING_PREDEFINED_PRICE);
			if(sellOfferMin == null) p.sendMessage(Response.SELL_OFFER_USING_PREDEFINED_PRICE);
						
			double minSellOfferPrice = (sellOfferMin == null) ? MathsUtils.round(cat.getPriceEmpty()[Category.getSlotByID(item_id)], 2) : MathsUtils.round(sellOfferMin.getKey(), 2);
			double maxBuyOrderPrice= (buyOrderMax == null) ? MathsUtils.round(cat.getPriceEmpty()[Category.getSlotByID(item_id)], 2) : MathsUtils.round(buyOrderMax.getKey(), 2);
			
			placeholder.put("%offers_price_lowest%", (sellOfferMin != null ) ? "§6" + minSellOfferPrice + " coins" : "§cN/A");
			placeholder.put("%orders_price_highest%", (buyOrderMax != null) ? "§6" + maxBuyOrderPrice + " coins": "§cN/A");
			placeholder.put("%ordering%", "" + want_to_buy);
			Inventory inv = PlayerManager.get(p.getUniqueId()).getHowMuchDoYouWantToPay();
			inv.clear();
			inv.setContents(amountInv.clone());
			
			NBTItem nbt = new NBTItem(inv.getItem(11));
			nbt.setInteger("bazaar.want_to_buy", want_to_buy);
			
			placeholder.put("%unit_price%", "§6" + MathsUtils.round(maxBuyOrderPrice, 2) + " coins");
			placeholder.put("%price_total%", "§6" + MathsUtils.round(maxBuyOrderPrice * want_to_buy, 2) + " coins");
			
			inv.setItem(10, getOptPrice(e.getInv().getItem(10), 10, 1, placeholder));
			
			placeholder.put("%unit_price%", "§6" + MathsUtils.round(maxBuyOrderPrice + 0.1, 2) + " coins");
			placeholder.put("%price_total%", "§6" + MathsUtils.round(((maxBuyOrderPrice + 0.1) * want_to_buy), 2) + " coins");
			inv.setItem(12, getOptPrice(priceInv[12].clone(), 12, 1, placeholder));
	
			Pair<Double, String> spread = InventoryUtils.getSpread(minSellOfferPrice, maxBuyOrderPrice, 5);
			placeholder.put("%price_total%", "§6" + MathsUtils.round(((maxBuyOrderPrice + spread.getKey()) * want_to_buy), 2) + " coins");
			placeholder.put("%unit_price%", "§6" + MathsUtils.round((maxBuyOrderPrice + spread.getKey()), 2)+ " coins");
			placeholder.put("%spread%", spread.getValue());
			inv.setItem(14, getOptPrice(priceInv[14].clone(), 14, 1, placeholder));
			
			nbt.setInteger("bazaar.item.id", item_id);
			nbt.setInteger("bazaar.category", category);
			nbt.setDouble("bazaar.buy_order.price.1", maxBuyOrderPrice);
			nbt.setDouble("bazaar.buy_order.price.2", MathsUtils.round(maxBuyOrderPrice + 0.1, 2));
			nbt.setDouble("bazaar.buy_order.price.3", MathsUtils.round(maxBuyOrderPrice + spread.getKey(), 2));
			
			
			inv.setItem(11, nbt.getItem());
			
			p.openInventory(inv);
			return;
		}
		
		if(e.getTitle().equals("How much do you want to pay?") && (e.getSlot() == 10 || e.getSlot() == 12 || e.getSlot() == 14)) {
			if(PlayerManager.get(e.getPlayer().getUniqueId()).getEnquiries() >= 21) {
				e.getPlayer().sendMessage(Response.CANNOT_CREATE_ENQUIRY);
				return;
			}
			double price = e.getSlot() == 10 ? old_info.getDouble("bazaar.buy_order.price.1") : e.getSlot() == 12 ? old_info.getDouble("bazaar.buy_order.price.2") : old_info.getDouble("bazaar.buy_order.price.3");
			int to_buy = old_info.getInteger("bazaar.want_to_buy");
			if(Bazaar.getEconomy().getBalance(p) < price*to_buy) {
				p.sendMessage(Response.NO_MONEY);
				return;
			}
			Bazaar.getEconomy().withdrawPlayer(p, price*to_buy);
			Enquiry.insert(new BuyOrder(to_buy, price, p.getUniqueId(), category, item_id));
			p.sendMessage(Response.BUY_ORDER_SETUP.replace("%amount%", ""+to_buy).replace("%displayname%", Category.getCategory(category).getShowcase()[Category.getSlotByID(item_id)].getItemMeta().getDisplayName()).replace("%price%", ""+price*to_buy));
			p.closeInventory();
		}
	}
	
	private static ItemStack getOptPrice(ItemStack is, int content, int amount, HashMap<String, String> placeholder) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(priceInv[content].getItemMeta().getDisplayName());
		im.setLore(priceInv[content].getItemMeta().getLore());
		is.setItemMeta(im);
		is.setAmount(amount);
		return ItemStackUtils.replacePlaceholder(is, placeholder);
	}
	
	private static ItemStack getOptAmount(ItemStack is, int content, int amount) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(amountInv[content].getItemMeta().getDisplayName());
		im.setLore(amountInv[content].getItemMeta().getLore());
		is.setItemMeta(im);
		is.setAmount(amount);
		return is;
	}
}
