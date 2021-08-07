package de.ancash.bazaar.sockets.packets;

import java.io.Serializable;
import java.util.UUID;

import de.ancash.sockets.packet.Packet;

public class BazaarMessagePacket implements Serializable, BazaarPacket{
	
	private static final long serialVersionUID = -6565667465876109565L;
	private final String msg;
	private final UUID target;
	private final boolean toPlayer;
	private final boolean broadcast;
	private final int category;
	private final int sub;
	private final int subsub;
	
	public BazaarMessagePacket(String msg, UUID id, boolean toPlayer, boolean broadcast) {
		this(msg, id, toPlayer, broadcast, -1, -1, -1);

	}
	
	public BazaarMessagePacket(String msg, UUID id, boolean toPlayer, boolean broadcast, int cat, int sub, int subsub) {
		this.msg = msg;
		this.target = id;
		this.toPlayer = toPlayer;
		this.broadcast = broadcast;
		this.category = cat;
		this.sub = sub;
		this.subsub = subsub;
	}
	
	public String getString() {
		return msg;
	}
	
	public boolean broadcast() {
		return broadcast;
	}
	
	public UUID getTarget() {
		return target;
	}
	
	public boolean sendToPlayer() {
		return toPlayer;
	}

	@Override
	public Packet getPacket() {
		Packet packet = new Packet(BazaarHeader.MESSAGE.getHeader());
		packet.setSerializable(this);
		return packet;
	}

	public int getSubSub() {
		return subsub;
	}

	public int getSub() {
		return sub;
	}

	public int getCategory() {
		return category;
	}
	
}
