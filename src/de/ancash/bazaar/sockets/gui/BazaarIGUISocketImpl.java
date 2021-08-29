package de.ancash.bazaar.sockets.gui;

import java.io.IOException;
import java.util.Map;

import org.bukkit.Bukkit;

import de.ancash.ILibrary;
import de.ancash.bazaar.exception.AsyncBazaarException;
import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.sockets.packets.BazaarHeader;
import de.ancash.bazaar.sockets.packets.BazaarRequestPacket;
import de.ancash.bazaar.sockets.packets.BazaarResponsePacket;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.sockets.packet.Packet;

public class BazaarIGUISocketImpl extends AbstractBazaarIGUI{

	public BazaarIGUISocketImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	/**
	 * Get all placeholders for enquires
	 * 
	 * @param cat
	 * @param sub
	 * @param subsub
	 * @return
	 */
	public Map<String, String> getPlaceholders(int cat, int sub, int subsub, boolean topSellOffers, int tSOcnt, boolean topBuyOrders, int tBOcnt) {
		BazaarRequestPacket brp = new BazaarRequestPacket(BazaarHeader.GET_PLACEHOLDERS, getId());
		brp.setCategory(cat);
		brp.setSub(sub);
		brp.setSubSub(subsub);
		brp.setGetTopEnquiries(true);
		brp.setTopBuyOrder(tBOcnt);
		brp.setTopSellOffer(tSOcnt);
		if(subsub > 0) brp.setEmptyPrice(Category.getCategory(cat).getEmptyPrice(sub, subsub));
		
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
		Map<String, String> map = reso.asMap();
		if(subsub > 0) map.put(BazaarPlaceholder.INVENTORY_CONTENT, InventoryUtils.getFreeSpaceExact(Bukkit.getPlayer(getId()).getInventory(), Category.getCategory(cat).getOriginal(sub, subsub)) + "");
		return map;
	}
	
	public double getHighestBuyOrderPrice() {
		Packet packet = getPacket(BazaarHeader.GET_HIGHEST_BUY_ORDER).getPacket();
		
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
			return Category.getCategory(getCurrentCategory()).getEmptyPrice(getCurrentSub(), getCurrentSubSub());
		}
		return val;
	}
	
	public double getHighestSellOfferPrice() {
		Packet packet = getPacket(BazaarHeader.GET_HIGHEST_SELL_OFFER).getPacket();
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
			return Category.getCategory(getCurrentCategory()).getEmptyPrice(getCurrentSub(), getCurrentSubSub());
		}
		return val;
	}
	
	public double getLowestBuyOrderPrice() {
		Packet packet = getPacket(BazaarHeader.GET_LOWEST_BUY_ORDER).getPacket();
		
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
			return Category.getCategory(getCurrentCategory()).getEmptyPrice(getCurrentSub(), getCurrentSubSub());
		}
		return val;
	}
	
	public double getLowestSellOfferPrice() {
		Packet packet = getPacket(BazaarHeader.GET_LOWEST_SELL_OFFER).getPacket();
		
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
			return Category.getCategory(getCurrentCategory()).getEmptyPrice(getCurrentSub(), getCurrentSubSub());
		}
		return val;
	}
	
	private BazaarRequestPacket getPacket(short header) {
		BazaarRequestPacket brp = new BazaarRequestPacket(header, getId());
		brp.setCategory(getCurrentCategory());
		brp.setSub(getCurrentSub());
		brp.setSubSub(getCurrentSubSub());
		return brp;
	}
}
