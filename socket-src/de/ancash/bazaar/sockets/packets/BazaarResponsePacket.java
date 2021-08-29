package de.ancash.bazaar.sockets.packets;

import java.io.Serializable;
import java.util.HashMap;

import de.ancash.sockets.packet.Packet;

public class BazaarResponsePacket extends BazaarPacket{
	
	private static final long serialVersionUID = -5834002349842910676L;
	
	private final Serializable value;
	private final transient long id;
	
	public BazaarResponsePacket(Serializable value, long id) {
		super(BazaarHeader.RESPONSE);
		this.value = value;
		this.id = id;
	}
	
	public Serializable get() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public <K, V> HashMap<K, V> asMap() {
		return (HashMap<K, V>) value;
	}
	
	@Override
	public Packet getPacket() {
		Packet packet = new Packet(BazaarHeader.RESPONSE);
		packet.setLong(id);
		packet.setSerializable(this);
		return packet;
	}
}
