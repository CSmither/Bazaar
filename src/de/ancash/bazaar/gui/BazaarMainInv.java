package de.ancash.bazaar.gui;

import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.gui.inventory.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.inventory.AbstractBazaarMainInv;
import de.ancash.bazaar.gui.inventory.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.minecraft.XMaterial;

public class BazaarMainInv extends AbstractBazaarMainInv{
	
	public BazaarMainInv(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public ItemStack[] getSubItems(AbstractBazaarIGUI igui, int newCat) {
		ItemStack[] items = new ItemStack[18];
		Category category = Category.getCategory(newCat);
		for(int sub = 1; sub <= 18; sub++) {
			ItemStack is = category.getSub()[sub - 1];
			if(is == null ) continue;
			if(is.getType() == XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial()) System.out.println("!!!item is gray stained glass pane!!!");
			is = igui.setEnquiriesInLore(is.clone(), newCat, sub);
			items[sub - 1] = is;
		}
		return items;
	}
}