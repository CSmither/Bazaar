package de.ancash.bazaar.management;

import java.util.UUID;

public class Enquiry{
	
	private double price;
	int left;
	private int claimable;
	private UUID owner;
	private UUID id;
	private int sub;
	private int subsub;
	private int category;
	private int amount;
	private long time_stamp;
	private long lastEdit;
	EnquiryType type;
	
	public Enquiry(int amount, double p, UUID i, int category, int a, int b) {
		this(amount, p, i, category, a, b, amount, System.nanoTime(), UUID.randomUUID(), 0, System.nanoTime());
	}
	
	public Enquiry(int amount, double price, UUID owner, int category, int a, int b, int left, long timestamp, UUID id, int claimable, long lastEdit) {
		this.amount = amount;
		this.price = price;
		this.owner = owner;
		this.category = category;
		this.sub = a;
		this.subsub = b;
		this.left = left;
		this.id = id;
		this.time_stamp = timestamp;
		this.claimable = claimable;
		this.lastEdit = lastEdit;
	}
	
	public int claim() {
		int temp = this.claimable;
		this.claimable = 0;
		return temp;
	}
	
	public void addClaimable(int i) {this.claimable += i;}
	
	void updateLastEdit() {
		this.lastEdit = System.nanoTime();
	}
	
	@Override
	public String toString() {
		return "cat=" + getCategory() + ",sub=" + getSub() + ",subsub=" + getSubSub() + ",price=" + getPrice() + ",amt=" + getAmount()
				+ ",left=" + getLeft() + ",claimable=" + getClaimable() + ",owner=" + owner.toString() + ",id=" + id.toString() + ",lastEdit" + lastEdit;
	}
	
	public boolean isFilled() {return left == 0;}
	public long getTimeStamp() {return time_stamp;}
	public void setLeft(int i) {left = i;}
	public double getPrice() {return price;}
	public UUID getID() {return id;}
	public UUID getOwner() {return owner;}
	public int getLeft() {return left;}
	public int getAmount() {return amount;}
	public int getCategory() {return category;}
	public int getSub() {return sub;}
	public int getSubSub() {return subsub;}
	public int getClaimable() {return claimable;}
	public EnquiryType getType() {return type;}
	public boolean hasClaimable() {return claimable != 0;}
	public long getLastEdit() {return lastEdit;}
	public void setClaimable(int i) {this.claimable = i;}
	public void setLastEdit(long now) {this.lastEdit = now;}
	
	public enum EnquiryType{
		SELL_OFFER("Sell Offer"),
		BUY_ORDER("Buy Order");
		
		private String name;
		
		public String getName() {
			return name;
		}
		
		EnquiryType(String name) {
			this.name = name;
		}
	}
}
