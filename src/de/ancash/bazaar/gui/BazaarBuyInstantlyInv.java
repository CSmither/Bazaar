package de.ancash.bazaar.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.gui.inventory.AbstractBazaarBuyInstantlyInv;
import de.ancash.bazaar.gui.inventory.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.inventory.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.management.SellOffer;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.minecraft.InventoryUtils;
import net.milkbowl.vault.economy.Economy;

import java.util.HashMap;
import java.util.Map;

public class BazaarBuyInstantlyInv extends AbstractBazaarBuyInstantlyInv{

	public BazaarBuyInstantlyInv(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public synchronized void onBuyInstantly(AbstractBazaarIGUI igui, int amount) {
		igui.unlock();
		synchronized (Category.getCategory(igui.getCurrentCategory()).getSellOffers(igui.getCurrentSub(), igui.getCurrentSubSub())) {
			Player player = Bukkit.getPlayer(igui.getId());
			SelfBalancingBST tree = Category.getCategory(igui.getCurrentCategory()).getSellOffers(igui.getCurrentSub(), igui.getCurrentSubSub());
			SelfBalancingBSTNode node = tree.isEmpty() ? null : tree.getMin();
			if(node == null) {
				player.sendMessage(igui.getPlugin().getResponse().NO_SELL_OFFER_AVAILABLE);
				openBuyInstantlyInventory(igui);
				return;
			}
			ItemStack original = Category.getCategory(igui.getCurrentCategory()).getOriginal(igui.getCurrentSub(), igui.getCurrentSubSub());
			if(InventoryUtils.getFreeSlots(player.getInventory()) * original.getMaxStackSize() < amount) {
				player.sendMessage(igui.getPlugin().getResponse().INVENTORY_FULL);
				openBuyInstantlyInventory(igui);
				return;
			}
			SellOffer sellOffer = (SellOffer) node.getByTimeStamp();
			Economy eco = igui.getPlugin().getEconomy();
			while(amount > 0 && sellOffer != null && player.getInventory().firstEmpty() != -1 && eco.getBalance(player) >= sellOffer.getPrice()) {
				if(player.getInventory().firstEmpty() == -1) {
					player.sendMessage(igui.getPlugin().getResponse().INVENTORY_FULL);
					return;
				}
				if(eco.getBalance(player) < node.getKey()) {
					player.sendMessage(igui.getPlugin().getResponse().NO_MONEY);
					return;
				}
				int reducable = amount > sellOffer.getLeft() ? sellOffer.getLeft() : amount;
				int freeSlots = InventoryUtils.getFreeSlots(player.getInventory());
				if(reducable > freeSlots * original.getMaxStackSize()) reducable = freeSlots * original.getMaxStackSize();
				InventoryUtils.addItemAmount(reducable, original.clone(), player);
				igui.getPlugin().getEconomy().withdrawPlayer(player, sellOffer.getPrice() * reducable);
				sellOffer.setLeft(sellOffer.getLeft() - reducable);
				sellOffer.addClaimable(reducable);
				if(sellOffer.getLeft() == 0) {
					igui.getPlugin().getEnquiryUtils().save(sellOffer);
					igui.getPlugin().getEnquiryUtils().checkEnquiry(sellOffer);
					sellOffer = (SellOffer) node.getByTimeStamp();
				}
				amount -= reducable;
			}
		}
	}

	@Override
	public Map<String, String> getPlaceholder(AbstractBazaarIGUI igui) {
		Map<String, String> placeholders = new HashMap<>();
		double sellOfferLowest = igui.getLowestSellOfferPrice();
		placeholders.put(BazaarPlaceholder.OFFERS_PRICE_LOWEST, sellOfferLowest + "");
		placeholders.put(BazaarPlaceholder.PRICE_TOTAL, sellOfferLowest * 64 + "");
		
		return null;
	}

	@Override
	public boolean sellOfferExist(AbstractBazaarIGUI igui) {
		return !Category.getCategory(igui.getCurrentCategory()).getSellOffers(igui.getCurrentSub(), igui.getCurrentSubSub()).isEmpty();
	}
}