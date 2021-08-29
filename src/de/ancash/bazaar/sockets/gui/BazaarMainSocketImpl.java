package de.ancash.bazaar.sockets.gui;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.AbstractBazaarMainInv;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;

public class BazaarMainSocketImpl extends AbstractBazaarMainInv{
	
	public BazaarMainSocketImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public ItemStack[] getSubItems(AbstractBazaarIGUI igui, int newCat) {
		ItemStack[] items = new ItemStack[18];
		Category category = Category.getCategory(newCat);
		for(int sub = 1; sub <= 18; sub++) {
			ItemStack is = category.getSubShow(sub);
			if(is == null ) continue;
			is = igui.setEnquiriesInLore(is.clone(), newCat, sub);
			items[sub - 1] = is;
		}
		return items;
	}

	@Override
	public Map<String, String> getManageEnquiriesPlaceholder(AbstractBazaarIGUI igui, int newCat) {
		return igui.getPlaceholders(-1, -1, -1, false, -1, false, -1);
	}
}