package de.ancash.bazaar.management;

import java.util.UUID;

public class SellOffer extends Enquiry{
	
	public SellOffer(int amount, double price, UUID id, int category, int a, int b) {
		super(amount, price, id, category, a, b);
		type = Enquiry.EnquiryType.SELL_OFFER;
	}
	
	public SellOffer(int amount, double price, UUID owner, int category, int a, int b, int left, long timestamp, UUID id, int claimable, long lastEdit) {
		super(amount, price, owner, category, a, b, left, timestamp, id, claimable, lastEdit);
		type = Enquiry.EnquiryType.SELL_OFFER;
	}
}
