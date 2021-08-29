package de.ancash.bazaar.sockets.eqnuiry;

public class Enquiry extends EnquiryBase{

	private static final long serialVersionUID = -4494129805053608570L;
	
	@Override
	public String toString() {
		return "cat=" + getCategory() + ",sub=" + super.getSubCategory() + ",subsub=" + super.getSubSubCategory() + ",price=" + getPrice() + ",amt=" + getAmount()
				+ ",left=" + getLeft() + ",claimable=" + getClaimable() + ",owner=" + owner.toString() + ",id=" + id.toString() + ",timestamp" + creationTimeStamp;
	}
}