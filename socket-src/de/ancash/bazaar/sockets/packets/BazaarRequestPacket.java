package de.ancash.bazaar.sockets.packets;

import java.util.UUID;

import de.ancash.sockets.packet.Packet;

public class BazaarRequestPacket extends BazaarPacket{

	private static final long serialVersionUID = 5467314237006552099L;
	
	private final short header;
	private final UUID player;
	private UUID uuid;
	private int category;
	private int sub;
	private int subSub;
	private double emptyprice = -1;
	private boolean getTopEnquiries;
	private int topBuyOrder;
	private int topSellOffer;
	private int type = -1;
	private double price;
	private int amount;
	
	public BazaarRequestPacket(short header, UUID player) {
		super(header);
		this.header = header;
		this.player = player;
		setCategory(-1);
		setSub(-1);
		setSubSub(-1);
	}
	
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	public boolean isSellOffer() {
		return type == 1;
	}
	
	public boolean isBuyOrder() {
		return type == 0;
	}
	
	public UUID getPlayer() {
		return player;
	}
	
	public short getHeader() {
		return header;
	}
	
	public int getCategory() {
		return category;
	}

	public void setCategory(int value) {
		this.category = value;
	}
	
	public int getSub() {
		return sub;
	}
	
	public void setSub(int value) {
		this.sub = value;
	}

	public int getSubSub() {
		return subSub;
	}
	
	public void setSubSub(int value) {
		this.subSub = value;
	}
	
	public void setPrice(double value) {
		this.price = value;
	}
	
	public double getPrice() {
		return price;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int value) {
		this.amount = value;
	}
	
	@Override
	public Packet getPacket() {
		Packet p = super.getPacket();
		p.setAwaitResponse(true);
		return p;
	}

	public void setEmptyPrice(double d) {
		this.emptyprice = d;
	}
	
	public double getEmptyPrice() {
		return emptyprice;
	}

	public boolean getTopEnquiries() {
		return getTopEnquiries;
	}

	public void setGetTopEnquiries(boolean getTopEnquiries) {
		this.getTopEnquiries = getTopEnquiries;
	}

	public int getTopSellOffer() {
		return topSellOffer;
	}

	public void setTopSellOffer(int topSellOffer) {
		this.topSellOffer = topSellOffer;
	}

	public int getTopBuyOrder() {
		return topBuyOrder;
	}

	public void setTopBuyOrder(int topBuyOrder) {
		this.topBuyOrder = topBuyOrder;
	}
}