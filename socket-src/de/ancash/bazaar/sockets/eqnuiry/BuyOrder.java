package de.ancash.bazaar.sockets.eqnuiry;

import java.util.UUID;

public class BuyOrder extends Enquiry{

	private static final long serialVersionUID = 2415558578835637510L;

	public BuyOrder(int amount, double price, UUID owner, int category, int a, int b) {
		this(amount, price, owner, category, a, b, amount, UUID.randomUUID(), 0, System.nanoTime());
	}
	
	public BuyOrder(int amount, double price, UUID owner, int category, int a, int b, int left, UUID id, int claimable, long creation) {
		setAmount(amount);
		setCategory(category);
		setPrice(price);
		setOwner(owner);
		setSubCategory(a);
		setSubSubCategory(b);
		setId(id);
		setLeft(left);
		setClaimable(claimable);
		setType(EnquiryType.BUY_ORDER);
	}
}
