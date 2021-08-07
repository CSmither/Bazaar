package de.ancash.bazaar.sockets.packets;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.ancash.sockets.packet.Packet;
import de.ancash.sockets.packet.PacketCallback;

public class BazaarRequestPacket implements Serializable, BazaarPacket{

	private static final long serialVersionUID = 5467314237006552099L;
	
	private final transient PacketCallback callback;
	
	private final short header;
	private final int category;
	private final int sub;
	private final int subsub;
	private final int extra;
	private final UUID player;
	
	public BazaarRequestPacket(BazaarRequestPacket.Type type, int cat, int sub, PacketCallback callback) {
		this(type, cat, sub, -1, callback);
	}
	
	public BazaarRequestPacket(BazaarRequestPacket.Type type, int cat, int sub, int subsub, PacketCallback callback) {
		this(type, cat, sub, subsub, callback, 0);
	}
	
	public BazaarRequestPacket(BazaarRequestPacket.Type type, int cat, int sub, int subsub, PacketCallback callback, int extra) {
		this(type, cat, sub, subsub, callback, null, extra);
	}
	
	public BazaarRequestPacket(BazaarRequestPacket.Type type, int cat, int sub, int subsub, PacketCallback callback, UUID player) {
		this(type, cat, sub, subsub, callback, player, 0);
	}
	
	public BazaarRequestPacket(BazaarRequestPacket.Type type, int cat, int sub, int subsub, PacketCallback callback, UUID player, int extra) {
		this.category = cat;
		this.sub = sub;
		this.subsub = subsub;
		this.header = type.toHeader();
		this.callback = callback;
		this.extra = extra;
		this.player = player;
	}
	
	@Override
	public Packet getPacket() {
		Packet packet = new Packet(header);
		packet.setSerializable(this);
		packet.setPacketCallback(callback);
		packet.isClientTarget(false);
		return packet;
	}
	
	public UUID getPlayer() {
		return player;
	}
	
	public short getHeader() {
		return header;
	}

	public BazaarRequestPacket.Type getType() {
		return Type.byShort((short) (header - BazaarHeader.COUNT_SELL_OFFERS.getHeader() + 1));
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
	
	public boolean hasCategory() {
		return category != -1;
	}

	public int getExtra() {
		return extra;
	}

	public enum Type{
		
		//main inventory
		COUNT_SELL_OFFERS((short) 1),
		COUNT_BUY_ORDERS((short) 2),
		
		COUNT_SELL_OFFERS_EXACT((short) 3),
		COUNT_BUY_ORDERS_EXACT((short) 4),
		
		CLAIMABLE((short) 5),
		
		//sub inventory & subsub inventory
		HIGHEST_SELL_OFFER_PRICE((short) 6),
		LOWEST_SELL_OFFER_PRICE((short) 7),
		HIGHEST_BUY_ORDER_PRICE((short) 8),
		LOWEST_BUY_ORDER_PRICE((short) 9),
		
		//subsub inventory
		TOP_SELL_OFFER((short) 10),
		TOP_BUY_ORDER((short) 11);
		
		private final short num;
		
		Type(short num) {
			this.num = num;
		}
		
		public short getNumber() {
			return num;
		}
		
		public short toHeader() {
			return (short) (BazaarHeader.COUNT_SELL_OFFERS.getHeader() - 1 + num);
		}
		
		private static Map<Short, BazaarRequestPacket.Type> byShort = new HashMap<Short, BazaarRequestPacket.Type>();
		
		static {
			for(Type t : values()) byShort.put(t.getNumber(), t);
		}
		
		public static BazaarRequestPacket.Type byShort(short s) {
			return byShort.get(s);
		}
	}
}
