package de.ancash.bazaar.sockets.packets;

public class BazaarHeader {

	public static final short MESSAGE = ((short) 200);
	public static final short RESPONSE = ((short) 201);
	public static final short GET_PLACEHOLDERS = ((short) 202);
	public static final short GET_HIGHEST_BUY_ORDER = ((short) 203);
	public static final short GET_LOWEST_BUY_ORDER = ((short) 204);
	public static final short GET_HIGHEST_SELL_OFFER = ((short) 205);
	public static final short GET_LOWEST_SELL_OFFER = ((short) 206);
	public static final short CREATE_BUY_ORDER = ((short) 207);
	public static final short CREATE_SELL_OFFER = ((short) 208);
	public static final short SELL_INSTANTLY = ((short) 209);
	public static final short BUY_INSTANTLY = ((short) 210);
	public static final short GET_CLAIMABLE = ((short) 211);
	public static final short CLAIM_ENQUIRY = ((short) 212);
}