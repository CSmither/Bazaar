package de.ancash.bazaar.sockets.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.ILibrary;
import de.ancash.bazaar.exception.AsyncBazaarException;
import de.ancash.bazaar.gui.base.AbstractBazaarCreateSellOfferInv;
import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.sockets.packets.BazaarHeader;
import de.ancash.bazaar.sockets.packets.BazaarRequestPacket;
import de.ancash.bazaar.sockets.packets.BazaarResponsePacket;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.sockets.packet.Packet;

import static de.ancash.misc.MathsUtils.round;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class BazaarCreateSellOfferSocketImpl extends AbstractBazaarCreateSellOfferInv{
	
	public BazaarCreateSellOfferSocketImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public void onCreationConfirm(AbstractBazaarIGUI igui) {
		Player player = Bukkit.getPlayer(igui.getId());
		BazaarRequestPacket brp = new BazaarRequestPacket(BazaarHeader.CREATE_SELL_OFFER, player.getUniqueId());
		brp.setAmount(igui.getEnquiryAmount());
		brp.setCategory(igui.getCurrentCategory());
		brp.setPrice(igui.getEnquiryPrice());
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
		
		HashMap<String, Serializable> socketRsp = reso.asMap();
		
		if(socketRsp.containsKey("message")) {
			player.sendMessage((String) socketRsp.get("message"));
			return;
		}
		
		InventoryUtils.removeItemAmount(igui.getEnquiryAmount(), Category.getCategory(brp.getCategory()).getOriginal(brp.getSub(), brp.getSubSub()), player);
		player.sendMessage(igui.getPlugin().getResponse().SELL_OFFER_SETUP
				.replace(BazaarPlaceholder.AMOUNT, igui.getEnquiryAmount() + "")
				.replace(BazaarPlaceholder.PRICE, round(igui.getEnquiryAmount() * igui.getEnquiryPrice(), 1) + "")
				.replace(BazaarPlaceholder.DISPLAY_NAME, Category.getCategory(brp.getCategory()).getSubSubShow(brp.getSub(), brp.getSubSub()).getItemMeta().getDisplayName()));
	}

	@Override
	public Duplet<Double, Double> getPrices(AbstractBazaarIGUI igui) {
		return Tuple.of(igui.getLowestSellOfferPrice(), igui.getHighestBuyOrderPrice());
	}
}