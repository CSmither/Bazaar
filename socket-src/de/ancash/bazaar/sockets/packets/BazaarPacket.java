package de.ancash.bazaar.sockets.packets;

import java.io.Serializable;

import de.ancash.sockets.packet.Packet;

public abstract class BazaarPacket implements Serializable{

	private static final long serialVersionUID = 1809571516924650502L;
	
	private final short header;
	
	public BazaarPacket(short header) {
		this.header = header;
	}
	
	public Packet getPacket() {
		Packet packet = new Packet(header);
		packet.setSerializable(this);
		packet.isClientTarget(false);
		return packet;
	}
}