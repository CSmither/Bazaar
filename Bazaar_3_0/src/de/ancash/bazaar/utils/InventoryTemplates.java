package de.ancash.bazaar.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryTemplates {

	private final Inventory original;
	
	public InventoryTemplates(Inventory inv) {
		this.original = inv;
	}
	
	public Inventory getOriginal() {
		return original;
	}
	
	public ItemStack[] getContents() {
		return original.getContents().clone();
	}
}
