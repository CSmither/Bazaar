package de.ancash.bazaar.sockets.packets;

public enum BazaarHeader {

	MESSAGE((short) 200),
	RESPONSE((short) 201),
	CREATE_SELL_OFFER((short) 202),
	CREATE_BUY_ORDER((short) 203),
	
	//main inventory
	COUNT_SELL_OFFERS((short) 206),
	COUNT_BUY_ORDERS((short) 207),
	
	COUNT_SELL_OFFERS_EXACT((short) 208),
	COUNT_BUY_ORDERS_EXACT((short) 209),
	
	CLAIMABLE((short) 210),
	
	//sub inventory & subsub inventory
	HIGHEST_SELL_OFFER_PRICE((short) 211),
	LOWEST_SELL_OFFER_PRICE((short) 212),
	HIGHEST_BUY_ORDER_PRICE((short) 213),
	LOWEST_BUY_ORDER_PRICE((short) 214),
	
	//subsub inventory
	TOP_SELL_OFFER((short) 215),
	TOP_BUY_ORDER((short) 216);
	
	private final short header;
	
	BazaarHeader(short header) {
		this.header = header;
	}

	public short getHeader() {
		return header;
	}
	
}
