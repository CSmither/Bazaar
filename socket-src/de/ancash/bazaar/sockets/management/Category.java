package de.ancash.bazaar.sockets.management;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import de.ancash.bazaar.sockets.BazaarSocketPlugin;
import de.ancash.bazaar.sockets.eqnuiry.Enquiry;
import de.ancash.bazaar.sockets.eqnuiry.EnquiryType;

public class Category {
		
	public static final int MAX_CATEGORIES = 5; //changeable
	public static final int MAX_SUB = 18; //do not change
	public static final int MAX_SUB_SUB = 9; //do not change
	
	public static int getSubBySlot(int slot) {
		int t = 0;
		while(slot > 9) {
			slot -= 9;
			t++;
		}
		return slot + (t - 1) * 6;
	}
	
	public static int getSlotByID(int i) {
		int t = 1;
		while(i > 6) {
			i -= 6;
			t++;
		}
		return 9 * t + i + 1;
	}
	
	public static boolean exists(int i) {
		return i <= Category.MAX_CATEGORIES && i > 0;
	}
	
	private static Category[] categories;
	
	public static Category getCategory(int a) {
		return categories[a - 1];
	}
	
	private final Map<Integer, SubCategory> subCategories = new HashMap<>();
	
	public Category(BazaarSocketPlugin pl) {
		this.category = -1;
		categories = new Category[Category.MAX_CATEGORIES];
		for(int i = 0; i < categories.length; i++)
			categories[i] = new Category(i + 1, pl);
	}
	
	private Category(int category, BazaarSocketPlugin pl) {
		this.category = category;
		for(int sub = 0; sub < Category.MAX_SUB; sub++) {
			subCategories.put(sub + 1, new SubCategory());
			for(int subsub = 0; subsub < Category.MAX_SUB_SUB; subsub++) 
				subCategories.get(sub + 1).setSubSubCategory(subsub + 1, new SubSubCategory());
		}
	}

	public List<SelfBalancingBST> getAllSubBuyOrders(int sub) {
		return subCategories.get(sub).getAllSubSubCategories().stream().map(SubSubCategory::getBuyOrderTree).collect(Collectors.toList());
	}
	
	public List<SelfBalancingBST> getAllSubSellOffers(int sub) {
		return subCategories.get(sub).getAllSubSubCategories().stream().map(SubSubCategory::getSellOfferTree).collect(Collectors.toList());
	}
	
	public int getCategory() {
		return category;
	}
	
	private final int category;
			
	public SelfBalancingBSTNode get(EnquiryType t, int a, int b, double value) {
		return getTree(t, a, b).get(value,  getTree(t, a, b).getRoot());
	}
	
	public SelfBalancingBST getTree(EnquiryType t, int a, int b) {
		if(t.equals(EnquiryType.SELL_OFFER)) return getSellOffers(a, b);
		if(t.equals(EnquiryType.BUY_ORDER)) return getBuyOrders(a, b);
		return null;
	}
	
	public SelfBalancingBST getSellOffers(int a, int b) {
		return subCategories.get(a).getSubSubCategory(b).getSellOfferTree();
	}
	
	public SelfBalancingBST getBuyOrders(int a, int b) {
		return subCategories.get(a).getSubSubCategory(b).getBuyOrderTree();
	}
	
	public Map<UUID, Enquiry> getLowest(EnquiryType t, int a, int sub) {
		return getTree(t, a, sub).getMin().get();
	}
	
	public Map<UUID, Enquiry> getHighest(EnquiryType t, int a, int sub) {
		return getTree(t, a, sub).getMax().get();
	}
}
