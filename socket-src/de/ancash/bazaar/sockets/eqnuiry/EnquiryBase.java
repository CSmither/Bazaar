package de.ancash.bazaar.sockets.eqnuiry;

import java.io.Serializable;
import java.util.UUID;

public abstract class EnquiryBase implements Serializable{

	private static final long serialVersionUID = 5993068422634340605L;
	
	protected double price = -1;
	protected int left = -1;
	protected int claimable = -1;
	protected UUID owner;
	protected UUID id;
	protected int sub = -1;
	protected int subsub = -1;
	protected int category = -1;
	protected int amount = -1;
	protected long creationTimeStamp = -1;
	protected byte type = -1;
	
	public boolean isFilled() {
		return left == 0;
	}
	
	public EnquiryBase setTimeStamp(long l) {
		this.creationTimeStamp = l;
		return this;
	}
	
	public long getTimeStamp() {
		return creationTimeStamp;
	}
	
	public EnquiryBase setPrice(double d) {
		this.price = d;
		return this;
	}
	
	public double getPrice() {
		return price;
	}
	
	public EnquiryBase setId(UUID id) {
		this.id = id;
		return this;
	}
	
	public UUID getID() {
		return id;
	}
	
	public EnquiryBase setOwner(UUID id) {
		this.owner = id;
		return this;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public EnquiryBase setLeft(int i) {
		left = i;
		return this;
	}
	
	public int getLeft() {
		return left;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public EnquiryBase setAmount(int a) {
		this.amount = a;
		return this;
	}
	
	public EnquiryBase setCategory(int c) {
		this.category = c;
		return this;
	}
	
	public int getCategory() {
		return category;
	}
	
	public EnquiryBase setSubCategory(int sc) {
		this.sub = sc;
		return this;
	}
	
	public int getSubCategory() {
		return sub;
	}
	
	public EnquiryBase setSubSubCategory(int ssc) {
		this.subsub = ssc;
		return this;
	}
	
	public int getSubSubCategory() {
		return subsub;
	}
	
	public EnquiryType getType() {
		return EnquiryType.fromByte(type);
	}
	
	public EnquiryBase setType(EnquiryType type) {
		this.type = type.asByte();
		return this;
	}
	
	public boolean hasClaimable() {
		return claimable != 0;
	}
	
	public EnquiryBase setClaimable(int i) {
		this.claimable = i;
		return this;
	}
	
	public EnquiryBase addClaimable(int i) {
		this.claimable += i;
		return this;
	}
	
	public int getClaimable() {
		return claimable;
	}
}