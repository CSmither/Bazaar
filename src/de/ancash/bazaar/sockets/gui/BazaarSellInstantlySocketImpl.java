package de.ancash.bazaar.sockets.gui;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.bukkit.Bukkit;

import de.ancash.ILibrary;
import de.ancash.bazaar.exception.AsyncBazaarException;
import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.AbstractBazaarSellInstantlyInv;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.sockets.packets.BazaarHeader;
import de.ancash.bazaar.sockets.packets.BazaarRequestPacket;
import de.ancash.bazaar.sockets.packets.BazaarResponsePacket;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.misc.MathsUtils;
import de.ancash.sockets.packet.Packet;

public class BazaarSellInstantlySocketImpl extends AbstractBazaarSellInstantlyInv{
	
	public BazaarSellInstantlySocketImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public void onSellInstantly(AbstractBazaarIGUI igui, int amount) {
		BazaarRequestPacket brp = new BazaarRequestPacket(BazaarHeader.SELL_INSTANTLY, igui.getId());
		brp.setCategory(igui.getCurrentCategory());
		brp.setSub(igui.getCurrentSub());
		brp.setSubSub(igui.getCurrentSubSub());
		brp.setAmount(amount);
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
			
		int sold = (int) map.get("amount");
		
		igui.getPlugin().getEconomy().depositPlayer(Bukkit.getPlayer(igui.getId()), MathsUtils.round(brp.getPrice() * sold * (1D - igui.getPlugin().getTax() / 100D), 1));
		InventoryUtils.removeItemAmount(sold, Category.getCategory(brp.getCategory()).getOriginal(brp.getSub(), brp.getSubSub()), Bukkit.getPlayer(igui.getId()));
		
		Bukkit.getPlayer(igui.getId()).sendMessage(igui.getPlugin().getResponse().SELL_INSTANTLY.replace(BazaarPlaceholder.AMOUNT, sold + "")
				.replace(BazaarPlaceholder.PRICE, MathsUtils.round(sold * brp.getPrice(), 1) + "")
				.replace(BazaarPlaceholder.DISPLAY_NAME, 
						Category.getCategory(brp.getCategory()).getSubSubShow(brp.getSub(), brp.getSubSub()).getItemMeta().getDisplayName()));
		
		igui.openSubSub(igui.getCurrentSubSub());
	}
}
