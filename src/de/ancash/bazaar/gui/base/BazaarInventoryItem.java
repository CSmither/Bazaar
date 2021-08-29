package de.ancash.bazaar.gui.base;

import org.bukkit.inventory.ItemStack;

import de.ancash.minecraft.inventory.Clickable;
import de.ancash.minecraft.inventory.IGUI;
import de.ancash.minecraft.inventory.InventoryItem;

public class BazaarInventoryItem extends InventoryItem{
	
	public BazaarInventoryItem(IGUI igui, ItemStack item, int slot, Clickable clickable) {
		super(igui, item, slot, clickable);
	}
}
