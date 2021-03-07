package de.ancash.bazaar.utils;

import java.util.UUID;

public class SellOffer extends Enquiry{
	
	public SellOffer(int amount, double price, UUID id, int category, int a, int b) {
		super(amount, price, id, category, a, b);
		type = Enquiry.EnquiryTypes.SELL_OFFER;
		saveAll(this);
	}
	
	public SellOffer(int amount, double price, UUID owner, int category, int a, int b, int left, long timestamp, UUID id, int claimable) {
		super(amount, price, owner, category, a, b, left, timestamp, id, claimable);
		type = Enquiry.EnquiryTypes.SELL_OFFER;
	}	
}
