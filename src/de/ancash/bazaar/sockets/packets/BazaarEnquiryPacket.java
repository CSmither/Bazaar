package de.ancash.bazaar.sockets.packets;

import java.io.Serializable;
import java.util.UUID;

import de.ancash.bazaar.management.Enquiry.EnquiryType;
import de.ancash.sockets.packet.Packet;

public class BazaarEnquiryPacket implements Serializable{
	
	private static final long serialVersionUID = 8170070621465342060L;
	
	private final short header;
	private final UUID owner;
	private final double price;
	private final int amount;
	private final int category;
	private final int sub;
	private final int subsub;
	
	public BazaarEnquiryPacket(EnquiryType type, UUID owner, double price, int amount, int category, int sub, int subsub) {
		this.header = type == EnquiryType.SELL_OFFER ? BazaarHeader.CREATE_SELL_OFFER.getHeader() : BazaarHeader.CREATE_BUY_ORDER.getHeader();
		this.owner = owner;
		this.price = price;
		this.amount = amount;
		this.category = category;
		this.sub = sub;
		this.subsub = subsub;
	}
	
	public short getHeader() {
		return header;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public double getPrice() {
		return price;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public int getCategory() {
		return category;
	}
	
	public int getSub() {
		return sub;
	}
	
	public int getSubSub() {
		return subsub;
	}
	
	public Packet getPacket() {
		Packet packet = new Packet(header);
		packet.setSerializable(this);
		packet.isClientTarget(false);
		return packet;
	}
}