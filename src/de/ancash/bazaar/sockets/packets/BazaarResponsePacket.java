package de.ancash.bazaar.sockets.packets;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import de.ancash.sockets.packet.Packet;

public class BazaarResponsePacket implements Serializable, BazaarPacket{
	
	private static final long serialVersionUID = -5834002349842910676L;
	
	private final HashMap<String, Number> values;
	private final transient long id;
	
	public BazaarResponsePacket(HashMap<String, Number> values, long id) {
		this.values = values;
		this.id = id;
	}
	
	public boolean hasKey(String key) {
		return values.containsKey(key);
	}
	
	public Set<Entry<String, Number>> getEntries() {
		return values.entrySet();
	}
	
	public Set<String> getKeys() {
		return values.keySet();
	}
	
	public Collection<Number> getValues() {
		return values.values();
	}
	
	public double getDouble(String key) {
		return (double) values.get(key);
	}
	
	public int getInteger(String key) {
		return (int) values.get(key);
	}
	
	@Override
	public Packet getPacket() {
		Packet packet = new Packet(BazaarHeader.RESPONSE.getHeader());
		packet.setLong(id);
		packet.setSerializable(this);
		return packet;
	}
}
