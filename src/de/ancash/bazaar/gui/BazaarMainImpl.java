package de.ancash.bazaar.gui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.AbstractBazaarMainInv;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.utils.BazaarPlaceholder;

public class BazaarMainImpl extends AbstractBazaarMainInv{
	
	public BazaarMainImpl(BazaarInventoryClassManager clazzManager) {
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
		Map<String, String> placeholder = new HashMap<>();
		PlayerManager pm = PlayerManager.get(igui.getId());
		placeholder.put(BazaarPlaceholder.ENQUIRIES, pm.getEnquiries() + "");
		placeholder.put(BazaarPlaceholder.CLAIMABLE_COINS, pm.getClaimableCoins() + "");
		placeholder.put(BazaarPlaceholder.CLAIMABLE_ITEMS, pm.getClaimableItems() + "");
		return placeholder;
	}
}