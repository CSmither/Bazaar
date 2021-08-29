package de.ancash.bazaar.management;

import java.util.Arrays;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class SubCategory{

	private final SubSubCategory[] subSubCategories = new SubSubCategory[9];
	private final ItemStack show;
	
	public List<SubSubCategory> getAllSubSubCategories() {
		return Arrays.asList(subSubCategories);
	}
	
	public SubCategory(ItemStack show) {
		this.show = show;
	}
	
	public void setSubSubCategory(int a, SubSubCategory ssc) {
		subSubCategories[a - 1] = ssc;
	}
	
	public SubSubCategory getSubSubCategory(int a) {
		return subSubCategories[a - 1];
	}

	public ItemStack getShow() {
		return show;
	}
}