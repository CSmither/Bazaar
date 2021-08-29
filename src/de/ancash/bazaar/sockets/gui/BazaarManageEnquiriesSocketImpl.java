package de.ancash.bazaar.sockets.gui;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.ILibrary;
import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.exception.AsyncBazaarException;
import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.AbstractBazaarManageEnquiriesInv;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.Enquiry.EnquiryType;
import de.ancash.bazaar.sockets.packets.BazaarHeader;
import de.ancash.bazaar.sockets.packets.BazaarRequestPacket;
import de.ancash.bazaar.sockets.packets.BazaarResponsePacket;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.sockets.packet.Packet;
import de.ancash.bazaar.utils.MathsUtils;
import de.ancash.datastructures.tuples.Duplet;

public class BazaarManageEnquiriesSocketImpl extends AbstractBazaarManageEnquiriesInv{
	
	public BazaarManageEnquiriesSocketImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Duplet<Map<String, Map<String, Number>>, Map<String, Map<String, Number>>> getPlaceholder(AbstractBazaarIGUI igui) {
		BazaarRequestPacket brp = new BazaarRequestPacket(BazaarHeader.GET_CLAIMABLE, igui.getId());
		
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
		return (Duplet<Map<String, Map<String, Number>>, Map<String, Map<String, Number>>>) reso.get();
	}

	@Override
	public void collect(AbstractBazaarIGUI igui, double price, int category, int sub, int subsub, String uuid,	EnquiryType type) {
		Bazaar pl = igui.getPlugin();
		Player player = Bukkit.getPlayer(igui.getId());
		
		BazaarRequestPacket brp = new BazaarRequestPacket(BazaarHeader.CLAIM_ENQUIRY, igui.getId());
		
		brp.setType(type);
		brp.setUUID(UUID.fromString(uuid));
		brp.setAmount(InventoryUtils.getFreeSpaceExact(Bukkit.getPlayer(igui.getId()).getInventory(), Category.getCategory(category).getOriginal(sub, subsub)));
		
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
		Map<String, Serializable> map = reso.asMap();
		
		if(map.containsKey("message")) {
			player.sendMessage((String) map.get("message"));
		}
		switch (type) {
		case SELL_OFFER:
			double coins = MathsUtils.round((int) map.get("amount") * price * (1D - (double) pl.getTax() / 100D), 1);
			pl.getEconomy().depositPlayer(player, coins);
			player.sendMessage("§6Bazaar! §7Claimed §6" + coins + " coins §7from selling §a" + map.get("amount") + "§7x " + Category.getCategory(category).getSubSubShow(sub, subsub).getItemMeta().getDisplayName() + " §7at §6" + price + " §7each!");
			break;
		case BUY_ORDER:
			int adding = (int) map.get("amount");
			InventoryUtils.addItemAmount(adding, Category.getCategory(category).getOriginal(sub, subsub), player);
			player.sendMessage("§6Bazaar! §7Claimed §a" + adding + "§7x " + Category.getCategory(category).getSubSubShow(sub, subsub).getItemMeta().getDisplayName() + " §7worth §6" + (adding * price) + " coins §7bought for §6" + price + " §7each!");
			break;
		default:
			break;
		}
		super.manageEnquiries(igui);
		return;
	}
}
