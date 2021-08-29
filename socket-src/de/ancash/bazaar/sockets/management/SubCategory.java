package de.ancash.bazaar.sockets.management;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SubCategory{

	private final Map<Integer, SubSubCategory> subSubCategories = new HashMap<>();
	
	public void setSubSubCategory(int a, SubSubCategory ssc) {
		subSubCategories.put(a, ssc);
	}
	
	public SubSubCategory getSubSubCategory(int a) {
		return subSubCategories.get(a);
	}
	
	public Collection<SubSubCategory> getAllSubSubCategories() {
		return subSubCategories.values();
	}
}