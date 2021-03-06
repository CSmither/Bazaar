package de.ancash.bazaar.gui;

import java.util.Map;

import org.bukkit.Bukkit;

import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.AbstractBazaarSubSubInv;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.gui.base.BazaarInventoryObjects;
import de.ancash.bazaar.management.Category;
import de.ancash.minecraft.InventoryUtils;

public class BazaarSubSubImpl extends AbstractBazaarSubSubInv{
	
	public BazaarSubSubImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public Map<String, String> getPlaceholder(AbstractBazaarIGUI igui) {
		Map<String, String> placeholder = igui.getPlaceholders(igui.getCurrentCategory(), igui.getCurrentSub(), igui.getCurrentSubSub(), 
						true, Integer.valueOf(BazaarInventoryObjects.CREATE_BUY_ORDER_ITEM.asItem().getItemMeta().getLore().stream().filter(str -> str.contains("%top_orders_")).findFirst().get().replace("%top_orders_", "").replace("%", "")), 
						true, Integer.valueOf(BazaarInventoryObjects.CREATE_SELL_OFFER_ITEM.asItem().getItemMeta().getLore().stream().filter(str -> str.contains("%top_offers_")).findFirst().get().replace("%top_offers_", "").replace("%", "")));
		placeholder.put("%inventory_content%", InventoryUtils.getContentAmount(Bukkit.getPlayer(igui.getId()).getInventory(), Category.getCategory(igui.getCurrentCategory()).getOriginal(igui.getCurrentSub(), igui.getCurrentSubSub())) + "");
		return placeholder;
	}
}