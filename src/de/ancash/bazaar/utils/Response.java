package de.ancash.bazaar.utils;

import de.ancash.bazaar.files.Files;

public class Response {
	
	public static final String CATEGORY_IN_PROCESS = Files.getConfig().getString("categoryInProcess").replace("&", "§");
	public static final String NO_MONEY = Files.getConfig().getString("noMoney").replace("&", "§");
	public static final String INVENTORY_FULL = Files.getConfig().getString("inventoryFull").replace("&", "§");
	public static final String NO_ITEMS_TO_SELL = Files.getConfig().getString("noItemsToSell").replace("&", "§");
	public static final String CANNOT_CREATE_ENQUIRY= Files.getConfig().getString("cannotCreateEnquiry").replace("&", "§");
	public static final String NOTHIN_TO_CLAIM = Files.getConfig().getString("nothingToClaim").replace("&", "§");
	
	public static final String NO_SELL_OFFER_AVAILABLE = Files.getConfig().getString("noSellOffers").replace("&", "§");
	public static final String SELL_OFFER_USING_PREDEFINED_PRICE = Files.getConfig().getString("noSellOffersAvailablePrice").replace("&", "§");
	public static final String NO_BUY_ORDER_AVAILABLE = Files.getConfig().getString("noBuyOrders").replace("&", "§");
	public static final String BUY_ORDER_SETUP = Files.getConfig().getString("buyOrderSetup").replace("&", "§");
	public static final String BUY_ORDER_USING_PREDEFINED_PRICE = Files.getConfig().getString("noBuyOrdersAvailablePrice").replace("&", "§");
	
}
