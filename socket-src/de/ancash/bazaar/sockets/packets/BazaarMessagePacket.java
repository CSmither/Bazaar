package de.ancash.bazaar.sockets.packets;

import java.util.UUID;

import de.ancash.bazaar.sockets.eqnuiry.EnquiryType;

public class BazaarMessagePacket extends BazaarPacket{
	
	private static final long serialVersionUID = -3650840609255010009L;
	  
	private String message;
	private UUID target;  
	private int messageType;
	
	private int category, subCategory, subSubCategory, amount, enquiryType;
	private double price;
	
	public BazaarMessagePacket() {
		super(BazaarHeader.MESSAGE);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public UUID getTarget() {
		return target;
	}

	public void setTarget(UUID target) {
		this.target = target;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(int subCategory) {
		this.subCategory = subCategory;
	}

	public int getSubSubCategory() {
		return subSubCategory;
	}

	public void setSubSubCategory(int subSubCategory) {
		this.subSubCategory = subSubCategory;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public EnquiryType getType() {
		if(enquiryType == 0) return EnquiryType.BUY_ORDER;
		if(enquiryType == 1) return EnquiryType.SELL_OFFER;
		return null;
	}

	public void setType(EnquiryType type) {
		this.enquiryType = type == EnquiryType.BUY_ORDER ? 0 : 1;
	}
	
	public void setMessageType(BazaarMessagePacket.Type type) {
		this.messageType = type.asNumber();
	}
	
	public BazaarMessagePacket.Type getMessageType() {
		switch (messageType) {
		case 0:
			return Type.ENQUIRY_FILLED;
		default:
			break;
		}
		return null;
	}
	
	public enum Type{
		ENQUIRY_FILLED(0);
	
		private final int n;
		
		private Type(int n) {
			this.n = n;
		}
		
		public int asNumber() {
			return n;
		}
	}
}