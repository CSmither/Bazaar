package de.ancash.bazaar.sockets.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import de.ancash.ILibrary;
import de.ancash.bazaar.exception.AsyncBazaarException;
import de.ancash.bazaar.gui.base.AbstractBazaarBuyInstantlyInv;
import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.sockets.packets.BazaarHeader;
import de.ancash.bazaar.sockets.packets.BazaarRequestPacket;
import de.ancash.bazaar.sockets.packets.BazaarResponsePacket;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.misc.MathsUtils;
import de.ancash.sockets.packet.Packet;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BazaarBuyInstantlySocketImpl extends AbstractBazaarBuyInstantlyInv{

	public BazaarBuyInstantlySocketImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public void onBuyInstantly(AbstractBazaarIGUI igui, int amount) {
		
		if(igui.getInstaPrice() * igui.getEnquiryAmount() > igui.getPlugin().getEconomy().getBalance(Bukkit.getPlayer(igui.getId()))) {
			Bukkit.getPlayer(igui.getId()).sendMessage(igui.getPlugin().getResponse().NO_MONEY);
			igui.openSubSub(igui.getCurrentSubSub());
			return;
		}
		
		if(igui.getInstaPrice() <= 0) {
			igui.openSubSub(igui.getCurrentSubSub());
			return;
		}
		
		ItemStack original = Category.getCategory(igui.getCurrentCategory()).getOriginal(igui.getCurrentSub(), igui.getCurrentSubSub());
		int freeSpace = InventoryUtils.getFreeSpaceExact(Bukkit.getPlayer(igui.getId()).getInventory(), original);
		
		int canBuy = freeSpace > amount ? amount : freeSpace;
		
		BazaarRequestPacket brp = new BazaarRequestPacket(BazaarHeader.BUY_INSTANTLY, igui.getId());
		brp.setCategory(igui.getCurrentCategory());
		brp.setSub(igui.getCurrentSub());
		brp.setSubSub(igui.getCurrentSubSub());
		brp.setAmount(canBuy);
		brp.setPrice(igui.getInstaPrice());
		
		Packet packet = brp.getPacket();
		
		try {
			ILibrary.getInstance().send(packet);
		} catch (IOException e) {
			throw new AsyncBazaarException("Failed to send packet!", e);
		}
		
		BazaarResponsePacket reso;
		try {
			reso = (BazaarResponsePacket) packet.awaitResponse(1000).get().getSerializable();
		} catch (Exception e) {
			throw new AsyncBazaarException("Did not receive response from socket!", e);
		}
		
		HashMap<String, Serializable> map = reso.asMap();
		
		if(map.containsKey("message")) {
			Bukkit.getPlayer(igui.getId()).sendMessage((String) map.get("message"));
			return;
		}
			
		if(map.containsKey("amount")) {
			int bought = (int) map.get("amount");
			InventoryUtils.addItemAmount(bought, original.clone(), Bukkit.getPlayer(igui.getId()));
			igui.getPlugin().getEconomy().withdrawPlayer(Bukkit.getPlayer(igui.getId()), MathsUtils.round(bought * brp.getPrice(), 2));
			
			Bukkit.getPlayer(igui.getId()).sendMessage(igui.getPlugin().getResponse().BUY_INSTANTLY.replace(BazaarPlaceholder.AMOUNT, bought + "")
					.replace(BazaarPlaceholder.PRICE, bought * brp.getPrice() + "")
					.replace(BazaarPlaceholder.DISPLAY_NAME, 
							Category.getCategory(brp.getCategory()).getSubSubShow(brp.getSub(), brp.getSubSub()).getItemMeta().getDisplayName()));
		}
		
		openBuyInstantlyInventory(igui);
	}

	@Override
	public Map<String, String> getPlaceholder(AbstractBazaarIGUI igui) {
		Map<String, String> placeholders = new HashMap<>();
		double sellOfferLowest = igui.getLowestSellOfferPrice();
		placeholders.put(BazaarPlaceholder.OFFERS_PRICE_LOWEST, sellOfferLowest + "");
		placeholders.put(BazaarPlaceholder.PRICE_TOTAL, sellOfferLowest * 64 + "");
		return placeholders;
	}

	@Override
	public boolean sellOfferExist(AbstractBazaarIGUI igui) {
		BazaarRequestPacket brp = new BazaarRequestPacket(BazaarHeader.GET_LOWEST_SELL_OFFER, igui.getId());
		brp.setCategory(igui.getCurrentCategory());
		brp.setSub(igui.getCurrentSub());
		brp.setSubSub(igui.getCurrentSubSub());
		
		Packet packet = brp.getPacket();
		
		try {
			ILibrary.getInstance().send(packet);
		} catch (IOException e) {
			throw new AsyncBazaarException("Failed to send packet!", e);
		}
		BazaarResponsePacket reso;
		try {
			reso = (BazaarResponsePacket) packet.awaitResponse(1000).get().getSerializable();
		} catch (Exception e) {
			throw new AsyncBazaarException("Did not receive response from socket!", e);
		}
		double val = (double) reso.get();
		if(val <= 0) {
			return false;
		}
		return true;
	}
}