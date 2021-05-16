package de.ancash.bazaar.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.management.SellOffer;
import de.ancash.bazaar.utils.InventoryUtils;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.anvilgui.AnvilGUI;
import de.ancash.minecraft.inventory.Clickable;
import net.milkbowl.vault.economy.Economy;

import static de.ancash.bazaar.gui.BazaarIGUI.*;

public class BazaarIGUIBuyInstantly {
	
	private static ItemStack bI_ONE;
	private static ItemStack bI_STACK;
	@SuppressWarnings("unused")
	private static ItemStack bI_FILL_INVENTORY;
	private static String TITLE;
	
	static void load(Bazaar pl) {
		bI_ONE = ItemStackUtils.get(pl.getInvConfig(), "inventory.buy-instantly.opt1");
		bI_STACK = ItemStackUtils.get(pl.getInvConfig(), "inventory.buy-instantly.opt2");
		bI_FILL_INVENTORY= ItemStackUtils.get(pl.getInvConfig(), "inventory.buy-instantly.fillInv");
		TITLE = pl.getInvConfig().getString("inventory.buy-instantly.title");
	}
	
	static void openBuyInstantlyInventory(BazaarIGUI igui) {
		igui.unlock();
		if(Category.getCategory(igui.currentCategory).getSellOffers(igui.currentSub, igui.currentSubSub).isEmpty()) {
			BazaarIGUISubSub.openSubSub(igui, igui.currentSubSub);
			return;
		}
		igui.newInventory(TITLE, 27);
		igui.clearInventoryItems();
		igui.setBackground(INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		
		CompactMap<String, String> placeholders = new CompactMap<>();
		double sellOfferLowest = getMinSellOfferPrice(igui);
		placeholders.put("%offers_price_lowest%", sellOfferLowest + "");
		placeholders.put("%price_total%", sellOfferLowest * 64 + "");
		igui.add(ItemStackUtils.replacePlaceholder(setType(igui, bI_ONE.clone()), placeholders), 10, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				process(igui, 1);
			}
		});
		igui.add(ItemStackUtils.replacePlaceholder(setType(igui, bI_STACK.clone()), placeholders), 12, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				process(igui, 64);
			}
		});
		/*igui.add(ItemStackUtils.replacePlaceholder(setType(igui, bI_FILL_INVENTORY.clone()), placeholders), 14, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				process(igui, 1);
			}
		});*/
		
		igui.add(CUSTOM_AMOUNT, 16, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.lock();
				new AnvilGUI.Builder().itemLeft(CUSTOM_AMOUNT).plugin(igui.plugin).onComplete((player, str) ->{
					int amount = -1;
					try {
						amount = Integer.valueOf(str);
					} catch(NumberFormatException ex) {}
					
					if(amount <= 0) {
						player.sendMessage("§cInvalid Input: " + amount);
						openBuyInstantlyInventory(igui);
						return AnvilGUI.Response.text("");
					} else {
						final int buy = amount;
						new BukkitRunnable() {
							@Override 
							public void run() {	
								process(igui, buy);
							}
						}.runTaskLater(igui.plugin, 1);
						
						return AnvilGUI.Response.close();
					}
					
				}).open(Bukkit.getPlayer(igui.getId()));

			}
		});
	}
	
	private static void process(BazaarIGUI igui, int amount) {
		igui.unlock();
		synchronized (Category.getCategory(igui.currentCategory).getSellOffers(igui.currentSub, igui.currentSubSub)) {
			Player player = Bukkit.getPlayer(igui.getId());
			SelfBalancingBST tree = Category.getCategory(igui.currentCategory).getSellOffers(igui.currentSub, igui.currentSubSub);
			SelfBalancingBSTNode node = tree.isEmpty() ? null : tree.getMin();
			if(node == null) {
				player.sendMessage(igui.plugin.getResponse().NO_SELL_OFFER_AVAILABLE);
				openBuyInstantlyInventory(igui);
				return;
			}
			ItemStack original = Category.getCategory(igui.currentCategory).getOriginal(igui.currentSub, igui.currentSubSub);
			if(InventoryUtils.getFreeSlots(player) * original.getMaxStackSize() < amount) {
				player.sendMessage(igui.plugin.getResponse().INVENTORY_FULL);
				openBuyInstantlyInventory(igui);
				return;
			}
			SellOffer sellOffer = (SellOffer) node.getByTimeStamp();
			Economy eco = igui.plugin.getEconomy();
			while(amount > 0 && sellOffer != null && player.getInventory().firstEmpty() != -1 && eco.getBalance(player) >= sellOffer.getPrice()) {
				if(player.getInventory().firstEmpty() == -1) {
					player.sendMessage(igui.plugin.getResponse().INVENTORY_FULL);
					return;
				}
				if(eco.getBalance(player) < node.getKey()) {
					player.sendMessage(igui.plugin.getResponse().NO_MONEY);
					return;
				}
				int reducable = amount > sellOffer.getLeft() ? sellOffer.getLeft() : amount;
				int freeSlots = InventoryUtils.getFreeSlots(player);
				if(reducable > freeSlots * original.getMaxStackSize()) reducable = freeSlots * original.getMaxStackSize();
				InventoryUtils.addItemAmount(reducable, original.clone(), player);
				igui.plugin.getEconomy().withdrawPlayer(player, sellOffer.getPrice() * reducable);
				sellOffer.setLeft(sellOffer.getLeft() - reducable);
				sellOffer.addClaimable(reducable);
				if(sellOffer.getLeft() == 0) {
					igui.plugin.getEnquiryUtils().save(sellOffer);
					igui.plugin.getEnquiryUtils().checkEnquiry(sellOffer);
					sellOffer = (SellOffer) node.getByTimeStamp();
				}
				amount -= reducable;
			}
		}
	}
}
