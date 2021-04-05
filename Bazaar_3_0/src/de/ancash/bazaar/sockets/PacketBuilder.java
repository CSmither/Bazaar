package de.ancash.bazaar.sockets;

import java.util.UUID;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.utils.Enquiry.EnquiryTypes;
import de.ancash.ilibrary.sockets.Packet;

public final class PacketBuilder {
		
	private static final PacketBuilder fetchAll = new PacketBuilder().setType("fetch");
	
	private StringBuilder builder = new StringBuilder();
	
	PacketBuilder() {
		builder.append("Bazaar");
	}
	
	public static PacketBuilder newPacketBuilder(String updateType) {
		return new PacketBuilder().setType(updateType);
	}
	
	public static PacketBuilder fetchAll() {
		return fetchAll;
	}
	
	public PacketBuilder setType(String type) {
		builder.append(" -u " + type );
		return this;
	}
	
	public PacketBuilder add(String txt) {
		builder.append(txt);
		return this;
	}
	
	public PacketBuilder setEnquiryType(EnquiryTypes type) {
		builder.append(" -e " + type.name() );
		return this;
	}
	
	public PacketBuilder setOwner(UUID id) {
		builder.append(" -o " + id.toString());
		return this;
	}
	
	public PacketBuilder setID(UUID id) {
		builder.append(" -i " + id.toString());
		return this;
	}
	
	public PacketBuilder setCategory(int i) {
		builder.append(" -C " + i);
		return this;
	}
	
	public PacketBuilder setShow(int i) {
		builder.append(" -S " + i);
		return this;
	}
	
	
	public PacketBuilder setSub(int i) {
		builder.append(" -s " + i);
		return this;
	}
	
	
	public PacketBuilder setPrice(double i) {
		builder.append(" -p " + i);
		return this;
	}
	
	
	public PacketBuilder setAmount(int i) {
		builder.append(" -a " + i);
		return this;
	}
	
	
	public PacketBuilder setLeft(int i) {
		builder.append(" -l " + i);
		return this;
	}
	
	
	public PacketBuilder setClaimable(int i) {
		builder.append(" -c " + i);
		return this;
	}
	
	public PacketBuilder setTimeStamp(long time) {
		builder.append(" -T " + time);
		return this;
	}
	
	public PacketBuilder setLastEdit(long time) {
		builder.append(" -t " + time);
		return this;
	}
	
	public Packet getPacket() {
		return new Packet(Bazaar.class.getName(), builder.toString());
	}
	/*
	 * -u = update type
	 * -e = enquiry type
	 * -o = owner
	 * -i = id
	 * -C = category
	 * -c = claimable
	 * -S = show
	 * -s = sub
	 * -p = price
	 * -a = amount
	 * -l = left
	 * -T = timestamp
	 * -t = lasst edit
	 */
}
