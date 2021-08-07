package de.ancash.bazaar.gui.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.Enquiry.EnquiryType;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.bazaar.utils.MathsUtils;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Triplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.Clickable;

public abstract class AbstractBazaarManageEnquiriesInv {

	private final BazaarInventoryClassManager clazzManager;
	
	protected AbstractBazaarManageEnquiriesInv(BazaarInventoryClassManager clazzManager) {
		this.clazzManager = clazzManager;
	}
	
	public abstract Duplet<Map<String, Map<String, Number>>, Map<String, Map<String, Number>>> getPlaceholder(AbstractBazaarIGUI igui);
	
	public abstract void collect(AbstractBazaarIGUI igui, double price, int category, int sub, int subsub, String uuid, EnquiryType type);
	
	public final void manageEnquiries(AbstractBazaarIGUI igui) {
		if(Bukkit.isPrimaryThread()) {
			clazzManager.get(AbstractBazaarIGUI.class).submit(new BazaarRunnable(igui.getId()) {
				
				@Override
				public void run() {
					Duplet<Map<String, Map<String, Number>>, Map<String, Map<String, Number>>> placeholder = getPlaceholder(igui);
					new BukkitRunnable() {
						
						@Override
						public void run() {
							manageEnquiriesSync(igui, placeholder.getFirst(), placeholder.getSecond());
						}
					}.runTask(igui.plugin);
				}
			});
		} else {
			Duplet<Map<String, Map<String, Number>>, Map<String, Map<String, Number>>> placeholder = getPlaceholder(igui);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					manageEnquiriesSync(igui, placeholder.getFirst(), placeholder.getSecond());
				}
			}.runTask(igui.plugin);
		}
	}
	
	private final void manageEnquiriesSync(AbstractBazaarIGUI igui, Map<String, Map<String, Number>> sellOffer, Map<String, Map<String, Number>> buyOrder) {
		igui.newInventory(BazaarInventoryObjects.MANAGE_ENQUIRIES_INVENTORY_TITLE.asString(), 45);
		igui.clearInventoryItems();
		igui.setBackground(IntStream.range(0, 9).toArray());
		igui.setBackground(IntStream.range(36, 45).toArray());
		igui.setCloseItem(40);
		
		int slot = 9;
		
		for(String key : sellOffer.keySet()) {
			final Triplet<Integer, Integer, Integer> infos = Tuple.of((int) sellOffer.get(key).get("category"),(int)  sellOffer.get(key).get("show"),(int)  sellOffer.get(key).get("sub"));
			Category category = Category.getCategory(infos.getFirst());
			ItemStack is = category.getSubSub()[infos.getSecond()- 1][infos.getThird()- 1].clone();
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(BazaarInventoryObjects.MANAGE_ENQUIRIES_SELL_OFFER_TEMPLATE_ITEM.asItem().getItemMeta().getDisplayName().replace("%displayname%", im.getDisplayName()));
			im.setLore(BazaarInventoryObjects.MANAGE_ENQUIRIES_SELL_OFFER_TEMPLATE_ITEM.asItem().getItemMeta().getLore());
			
			is.setItemMeta(im);
			
			Map<String, String> placeholder = new HashMap<String, String>();
			final double price = (double) sellOffer.get(key).get("price");
			final int total = (int) sellOffer.get(key).get("total");
			final int left = (int) sellOffer.get(key).get("left");
			final int claimable = (int) sellOffer.get(key).get("claimable");

			placeholder.put(BazaarPlaceholder.PRICE_TOTAL, "" + (double) (total * price));
			placeholder.put(BazaarPlaceholder.SELLING, "" + (int) total);
			placeholder.put(BazaarPlaceholder.SOLD, "" + (int) (total - left));
			placeholder.put(BazaarPlaceholder.UNIT_PRICE, "" + price);
			placeholder.put(BazaarPlaceholder.PERCENTAGE , "" + (left == 0 ? 100 : MathsUtils.round(((double) (total-left) / total) * 100, 1)));
			placeholder.put(BazaarPlaceholder.COINS_TO_CLAIM, "" + (claimable * price));
			
			igui.add(ItemStackUtils.replacePlaceholder(is, placeholder), slot, new Clickable() {
				
				@Override
				public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
					clazzManager.get(AbstractBazaarIGUI.class).submit(new BazaarRunnable(igui.getId()) {
						
						@Override
						public void run() {
							collect(igui, price, category.getCategory(), infos.getSecond(), infos.getThird(), key, EnquiryType.SELL_OFFER);
						}
					});
				}
			});
			slot++;
		}
		
		for(String key : buyOrder.keySet()) {
			Triplet<Integer, Integer, Integer> infos = Tuple.of((int) buyOrder.get(key).get("category"),(int)  buyOrder.get(key).get("show"),(int)  buyOrder.get(key).get("sub"));
			Category category = Category.getCategory(infos.getFirst());
			ItemStack is = category.getSubSub()[infos.getSecond()- 1][infos.getThird()- 1].clone();
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(BazaarInventoryObjects.MANAGE_ENQUIRIES_BUY_ORDER_TEMPLATE_ITEM.asItem().getItemMeta().getDisplayName().replace("%displayname%", im.getDisplayName()));
			im.setLore(BazaarInventoryObjects.MANAGE_ENQUIRIES_BUY_ORDER_TEMPLATE_ITEM.asItem().getItemMeta().getLore());
			
			is.setItemMeta(im);
			
			Map<String, String> placeholder = new HashMap<String, String>();
			double price = (double) buyOrder.get(key).get("price");
			int total = (int) buyOrder.get(key).get("total");
			int left = (int) buyOrder.get(key).get("left");
			int claimable = (int) buyOrder.get(key).get("claimable");

			placeholder.put(BazaarPlaceholder.PRICE_TOTAL, "" + (double) (total * price));
			placeholder.put(BazaarPlaceholder.BUYING, "" + (int) total);
			placeholder.put(BazaarPlaceholder.BOUGHT, "" + (int) (total - left));
			placeholder.put(BazaarPlaceholder.UNIT_PRICE, "" + price);
			placeholder.put(BazaarPlaceholder.PERCENTAGE , "" + (left == 0 ? 100 : MathsUtils.round(((double) (total-left) / total) * 100, 1)));
			placeholder.put(BazaarPlaceholder.ITEMS_TO_CLAIM, "" + (int) (claimable));
			
			igui.add(ItemStackUtils.replacePlaceholder(is, placeholder), slot, new Clickable() {
	
				@Override
				public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
					clazzManager.get(AbstractBazaarIGUI.class).submit(new BazaarRunnable(igui.getId()) {
						
						@Override
						public void run() {
							collect(igui, price, category.getCategory(), infos.getSecond(), infos.getThird(), key, EnquiryType.BUY_ORDER);
						}
					});
				}
			});
			slot++;
		}
	}
}