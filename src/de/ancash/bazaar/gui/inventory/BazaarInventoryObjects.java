package de.ancash.bazaar.gui.inventory;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import de.ancash.minecraft.ItemBuilder;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.XMaterial;

public enum BazaarInventoryObjects {
	
	CLOSE_INVENTORY_ITEM("inventory.close"),
	BACKGROUND_ITEM("inventory.background"),
	CREATE_SELL_OFFER_ITEM("inventory.opt_inv.create_sell_offer"),
	CREATE_BUY_ORDER_ITEM("inventory.opt_inv.create_buy_order"),
	SELL_INSTANTLY_ITEM("inventory.opt_inv.sellInsta"),
	BUY_INSTANTLY_ITEM("inventory.opt_inv.buyInsta"),
	CUSTOM_AMOUNT_ITEM("inventory.custom-amount"),
	PICK_AMOUNT_ITEM(new ItemBuilder(XMaterial.OAK_SIGN).setDisplayname("0").build()),

	CREATE_BUY_ORDER_SIXTY_FOUR_ITEM("inventory.buy-order.opt1"),
	CREATE_BUY_ORDER_ONE_HUNDRED_SIXTY_ITEM("inventory.buy-order.opt2"),
	CREATE_BUY_ORDER_ONE_THOUSAND_ITEM("inventory.buy-order.opt3"),
	CREATE_BUY_ORDER_SAME_AS_TOP_ORDER_ITEM("inventory.buy-order.price.sameAsTopOrder"),
	CREATE_BUY_ORDER_TOP_ORDER_PLUS_ZERO_POINT_ONE_ITEM("inventory.buy-order.price.topOrder_01"),
	CREATE_BUY_ORDER_FIVE_PERCENT_OF_SPREAD_ITEM("inventory.buy-order.price.ofSpread5"),
	CREATE_BUY_ORDER_CONFIRM_ITEM("inventory.buy-order.confirm.item"),
	CREATE_BUY_ORDER_MAX("inventory.buy-order.max"),
	CREATE_BUY_ORDER_INVENTORY_TITLE("inventory.buy-order.title"),
	CREATE_BUY_ORDER_PRICE_INVENTORY_TITLE("inventory.buy-order.price.title"),
	CREATE_BUY_ORDER_CONFIRM_INVENTORY_TITLE("inventory.buy-order.confirm.title"),
	
	CREATE_SELL_OFFER_SAME_AS_TOP_OFFER_ITEM("inventory.sell-offer.sameAsBestOffer"),
	CREATE_SELL_OFFER_TOP_ORDER_PLUS_ZERO_POINT_ONE_ITEM("inventory.sell-offer.bestOffer_01"),
	CREATE_SELL_OFFER_TEN_PERCENT_OF_SPREAD_ITEM("inventory.sell-offer.ofSpread10"),
	CREATE_SELL_OFFER_CONFIRM_ITEM("inventory.sell-offer.confirm.item"),
	CREATE_SELL_OFFER_INVENTORY_TITLE("inventory.sell-offer.title"),
	CREATE_SELL_OFFER_CONFIRM_INVENTORY_TITLE("inventory.sell-offer.confirm.title"),
	
	BUY_INSTANTLY_ONE_ITEM("inventory.buy-instantly.opt1"),
	BUY_INSTANTLY_STACK_ITEM("inventory.buy-instantly.opt2"),
	BUY_INSTANTLY_FILL_INVENTORY_ITEM("inventory.buy-instantly.fillInv"),
	BUY_INSTANTLY_INVENTORY_TITLE("inventory.buy-instantly.title"),
	
	MANAGE_ENQUIRIES_INVENTORY_TITLE("inventory.manage.title"),
	MANAGE_ENQUIRIES_SELL_OFFER_TEMPLATE_ITEM("inventory.manage.sell-offer"),
	MANAGE_ENQUIRIES_BUY_ORDER_TEMPLATE_ITEM("inventory.manage.buy-order");
	
	private static final EnumSet<BazaarInventoryObjects> objects = EnumSet.allOf(BazaarInventoryObjects.class);
	
	public static void reload() throws IOException, InvalidConfigurationException {
		load();
	}
	
	public static synchronized void load() throws IOException, InvalidConfigurationException {
		File file = new File("plugins/Bazaar/inventory.yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		fc.load(file);
		objects.forEach(e ->{
			try {
				if(e.path != null)
					if(fc.isString(e.path)) 
						e.o = fc.getString(e.path);
					else if(fc.isInt(e.path))
						e.o = fc.getInt(e.path);
					else
						e.o = ItemStackUtils.get(fc, e.path);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		});
		fc.save(file);
	}
	
	private Object o;
	private String path = null;
	
	BazaarInventoryObjects(String path) {
		this.path = path;
	}
	
	BazaarInventoryObjects(ItemStack item) {
		this.o = item;
	}
	
	public ItemStack asItem() {
		return ((ItemStack) o);
	}
	
	public int asInt() {
		return (int) o;
	}
	
	public String asString() {
		return (String) o;
	}
}