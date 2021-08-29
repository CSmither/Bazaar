package de.ancash.bazaar.sockets.eqnuiry;

import java.util.HashMap;
import java.util.Map;

public enum EnquiryType{
	BUY_ORDER("Buy Order", (byte) 0),
	SELL_OFFER("Sell Offer", (byte) 1);
	
	private String name;
	private byte asByte;
	
	public String getName() {
		return name;
	}
	
	public byte asByte() {
		return asByte;
	}
	
	EnquiryType(String name, byte asByte) {
		this.name = name;
		this.asByte = asByte;
	}
	
	private static final Map<Byte, EnquiryType> byByte = new HashMap<>();
	
	static {
		for(EnquiryType type : values()) byByte.put(type.asByte, type);
	}
	
	public static EnquiryType fromByte(byte b) {
		return byByte.get(b);
	}
}