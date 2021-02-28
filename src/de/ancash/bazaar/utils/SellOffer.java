package de.ancash.bazaar.utils;

import java.util.UUID;


public class SellOffer extends Enquiry{
	
	public SellOffer(int amount, double price, UUID id, int category, int item) {
		super(amount, price, id, category, item);
		type = Enquiry.EnquiryTypes.SELL_OFFER;
		saveAll(this);
	}
	
	public SellOffer(int amount, double price, UUID owner, int category, int item_id, int left, long timestamp, UUID id, int claimable) {
		super(amount, price, owner, category, item_id, left, timestamp, id, claimable);
		type = Enquiry.EnquiryTypes.SELL_OFFER;
	}	
}
