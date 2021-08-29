package de.ancash.bazaar.sockets.management;

import java.util.UUID;

import de.ancash.bazaar.sockets.eqnuiry.Enquiry;
import de.ancash.bazaar.sockets.eqnuiry.EnquiryType;

public class SellOffer extends Enquiry{
	
	private static final long serialVersionUID = -4581539683818857090L;

	public SellOffer(int amount, double price, UUID owner, int category, int a, int b) {
		this(amount, price, owner, category, a, b, amount, UUID.randomUUID(), 0, System.nanoTime());
	}
	
	public SellOffer(int amount, double price, UUID owner, int category, int a, int b, int left, UUID id, int claimable, long creation) {
		setTimeStamp(creation);
		setAmount(amount);
		setCategory(category);
		setPrice(price);
		setOwner(owner);
		setSubCategory(a);
		setSubSubCategory(b);
		setId(id);
		setLeft(left);
		setClaimable(claimable);
		setType(EnquiryType.SELL_OFFER);
	}
}
