package de.ancash.bazaar.sockets.gui;

import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.AbstractBazaarSubInv;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.minecraft.XMaterial;

public class BazaarSubSocketImpl extends AbstractBazaarSubInv{

	public BazaarSubSocketImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public ItemStack[] getSubSubItems(AbstractBazaarIGUI igui, int sub) {
		ItemStack[] items = new ItemStack[9];
		Category category = Category.getCategory(igui.getCurrentCategory());
		for(int subsub = 0; subsub<9; subsub++) {
			ItemStack item = category.getSubSubShow(sub, subsub + 1);
			if(item == null || item.getType() == XMaterial.AIR.parseMaterial()) continue;
			item = igui.setEnquiriesInLoreExact(item.clone(), igui.getCurrentCategory(), sub, subsub + 1, false, 0, false, 0);
			items[subsub] = item;
		}
		return items;
	}	
}