package de.ancash.bazaar.utils;

import java.util.UUID;

public class BuyOrder extends Enquiry{
	
	public BuyOrder(int amount, double price, UUID id, int category, int item) {
		super(amount, price, id, category, item);
		type = Enquiry.EnquiryTypes.BUY_ORDER;
		Enquiry.saveAll(this);
	}
	
	public BuyOrder(int amount, double price, UUID owner, int category, int item_id, int left, long timestamp, UUID id, int claimable) {
		super(amount, price, owner, category, item_id, left, timestamp, id, claimable);
		type = Enquiry.EnquiryTypes.BUY_ORDER;
	}
	
	public void add(int i) {
		reduce(i - 2 * i);
	}
}
