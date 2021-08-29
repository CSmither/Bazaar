package de.ancash.bazaar.utils;

import de.ancash.bazaar.Bazaar;

public class Response {
	
	public final String CATEGORY_IN_PROCESS;
	public final String NO_MONEY;
	public final String INVENTORY_FULL;
	public final String NO_ITEMS_TO_SELL;
	public final String CANNOT_CREATE_ENQUIRY;
	public final String NOTHIN_TO_CLAIM;
	
	public final String NO_SELL_OFFER_AVAILABLE;
	public final String SELL_OFFER_USING_PREDEFINED_PRICE;
	public final String SELL_OFFER_SETUP;
	public final String SELL_OFFER_FILLED;
	
	public final String NO_BUY_ORDER_AVAILABLE;
	public final String BUY_ORDER_SETUP;
	public final String BUY_ORDER_USING_PREDEFINED_PRICE;
	public final String BUY_ORDER_FILLED;
	
	public final String SELL_INSTANTLY;
	public final String BUY_INSTANTLY;
	
	public Response(Bazaar pl) {
		BUY_INSTANTLY = pl.getConfig().getString("buyInstantly").replace("&", "§");
		SELL_INSTANTLY = pl.getConfig().getString("sellInstantly").replace("&", "§");
		CATEGORY_IN_PROCESS = pl.getConfig().getString("categoryInProcess").replace("&", "§");
		NO_MONEY = pl.getConfig().getString("noMoney").replace("&", "§");
		INVENTORY_FULL = pl.getConfig().getString("inventoryFull").replace("&", "§");
		NO_ITEMS_TO_SELL = pl.getConfig().getString("noItemsToSell").replace("&", "§");
		CANNOT_CREATE_ENQUIRY = pl.getConfig().getString("cannotCreateEnquiry").replace("&", "§");
		NOTHIN_TO_CLAIM = pl.getConfig().getString("nothingToClaim").replace("&", "§");
		
		NO_SELL_OFFER_AVAILABLE = pl.getConfig().getString("noSellOffers").replace("&", "§");
		SELL_OFFER_USING_PREDEFINED_PRICE = pl.getConfig().getString("noSellOffersAvailablePrice").replace("&", "§");
		SELL_OFFER_SETUP = pl.getConfig().getString("sellOfferSetup").replace("&", "§");
		SELL_OFFER_FILLED = pl.getConfig().getString("sellOfferFilled").replace("&", "§");
		
		NO_BUY_ORDER_AVAILABLE = pl.getConfig().getString("noBuyOrders").replace("&","§");
		BUY_ORDER_SETUP = pl.getConfig().getString("buyOrderSetup").replace("&", "§");
		BUY_ORDER_USING_PREDEFINED_PRICE = pl.getConfig().getString("noBuyOrdersAvailablePrice").replace("&", "§");
		BUY_ORDER_FILLED = pl.getConfig().getString("buyOrderFilled").replace("&", "§");
	}
}
