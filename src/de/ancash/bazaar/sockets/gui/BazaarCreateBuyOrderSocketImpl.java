package de.ancash.bazaar.sockets.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.ILibrary;
import de.ancash.bazaar.exception.AsyncBazaarException;
import de.ancash.bazaar.gui.base.AbstractBazaarCreateBuyOrderInv;
import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.sockets.packets.BazaarHeader;
import de.ancash.bazaar.sockets.packets.BazaarRequestPacket;
import de.ancash.bazaar.sockets.packets.BazaarResponsePacket;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Triplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.sockets.packet.Packet;

import static de.ancash.misc.MathsUtils.round;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BazaarCreateBuyOrderSocketImpl extends AbstractBazaarCreateBuyOrderInv{

	public BazaarCreateBuyOrderSocketImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public void onCreationConfirm(AbstractBazaarIGUI igui) {
		Player player = Bukkit.getPlayer(igui.getId());
		
		if(igui.getEnquiryPrice() * igui.getEnquiryAmount() > igui.getPlugin().getEconomy().getBalance(player)) {
			player.sendMessage(igui.getPlugin().getResponse().NO_MONEY);
			return;
		}
		
		BazaarRequestPacket brp = new BazaarRequestPacket(BazaarHeader.CREATE_BUY_ORDER, player.getUniqueId());
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
		
		@SuppressWarnings("unchecked")
		HashMap<String, Serializable> socketResp = (HashMap<String, Serializable>) reso.get();
		
		if(socketResp.containsKey("message")) {
			player.sendMessage((String) socketResp.get("message"));
		}
		double price = brp.getAmount() * brp.getPrice();
		
		igui.getPlugin().getEconomy().withdrawPlayer(player, price);
		player.sendMessage(igui.getPlugin().getResponse().BUY_ORDER_SETUP
				.replace(BazaarPlaceholder.AMOUNT, brp.getAmount() + "")
				.replace(BazaarPlaceholder.PRICE, round(price, 1) + "")
				.replace(BazaarPlaceholder.DISPLAY_NAME, Category.getCategory(brp.getCategory()).getSubSubShow(brp.getSub(), brp.getSubSub()).getItemMeta().getDisplayName()));
	}

	@Override
	public Triplet<Map<String, String>, Map<String, String>, Map<String, String>> getPlaceholders(AbstractBazaarIGUI igui) {
		Map<String, String> placeholderA = new HashMap<>();
		final double highest_buy_order = igui.getHighestBuyOrderPrice();
		final double lowest_sell_offer = igui.getLowestSellOfferPrice();
		placeholderA.put(BazaarPlaceholder.ORDERING, igui.getEnquiryAmount() + "");
		placeholderA.put(BazaarPlaceholder.UNIT_PRICE, highest_buy_order + "");
		placeholderA.put(BazaarPlaceholder.PRICE_TOTAL, round(highest_buy_order, 1) * igui.getEnquiryAmount() + "");
		
		
		Map<String, String> placeholderB = new HashMap<>();
		placeholderB.put(BazaarPlaceholder.ORDERING, igui.getEnquiryAmount() + "");
		placeholderB.put(BazaarPlaceholder.UNIT_PRICE, round((highest_buy_order + 0.1), 1) + "");
		placeholderB.put(BazaarPlaceholder.PRICE_TOTAL, round((highest_buy_order + 0.1) * igui.getEnquiryAmount(), 1) + "");
		
		Map<String, String> placeholderC = new HashMap<>();
		placeholderC.put(BazaarPlaceholder.ORDERING, igui.getEnquiryAmount() + "");
		Duplet<Double, String> spread = AbstractBazaarIGUI.getSpread(lowest_sell_offer, highest_buy_order, 5);
		placeholderC.put(BazaarPlaceholder.UNIT_PRICE, round((highest_buy_order + spread.getFirst()), 1) + "");
		placeholderC.put(BazaarPlaceholder.PRICE_TOTAL, round((highest_buy_order + spread.getFirst()) * igui.getEnquiryAmount(), 1) + "");
		placeholderC.put(BazaarPlaceholder.OFFERS_PRICE_LOWEST, round(lowest_sell_offer, 1) + "");
		placeholderC.put(BazaarPlaceholder.ORDERS_PRICE_HIGHEST, round(highest_buy_order, 1) + "");
		placeholderC.put(BazaarPlaceholder.SPREAD, spread.getSecond());

		return Tuple.of(placeholderA, placeholderB, placeholderC);
	}
}