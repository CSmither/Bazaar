package de.ancash.bazaar.gui.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.anvilgui.AnvilGUI;
import de.ancash.minecraft.inventory.Clickable;

import static de.ancash.misc.MathsUtils.round;

public abstract class AbstractBazaarCreateSellOfferInv {

	private final BazaarInventoryClassManager clazzManager;
	
	protected AbstractBazaarCreateSellOfferInv(BazaarInventoryClassManager clazzManager) {
		this.clazzManager = clazzManager;
	}

	private Map<UUID, AnvilGUI> guis = new HashMap<>();
	
	/**
	 * async
	 * 
	 * @param igui
	 * @param price
	 */
	public abstract void onCreationConfirm(AbstractBazaarIGUI igui);
	
	/**
	 * async
	 * 
	 * @param igui
	 */
	public abstract Duplet<Double, Double> getPrices(AbstractBazaarIGUI igui);
	
	public final void openCreateSellOffer(AbstractBazaarIGUI igui) {
		if(Bukkit.isPrimaryThread()) {
			clazzManager.get(AbstractBazaarIGUI.class).submit(new BazaarRunnable(igui.getId()) {
				
				@Override
				public void run() {
					Duplet<Double, Double> prices = getPrices(igui);
					new BukkitRunnable() {
						
						@Override
						public void run() {
							openCreateSellOfferSync(igui, prices.getFirst(), prices.getSecond());
						}
					}.runTask(igui.plugin);
				}
			});
		} else {
			Duplet<Double, Double> prices = getPrices(igui);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					openCreateSellOfferSync(igui, prices.getFirst(), prices.getSecond());
				}
			}.runTask(igui.plugin);
		}
	}
	
	private final void openCreateSellOfferSync(AbstractBazaarIGUI igui, double sellOfferMin, double buyOrderMax) {
		igui.currentGUIType = BazaarInventoryType.CREATE_SELL_OFFER;

		if(InventoryUtils.getContentAmount(Bukkit.getPlayer(igui.getId()).getInventory(), Category.getCategory(igui.currentCategory).getOriginal(igui.currentSub, igui.currentSubSub)) == 0) {
			clazzManager.get(AbstractBazaarSubSubInv.class).openSubSub(igui, igui.currentSubSub);
			return;
		}
		igui.unlock();
		
		igui.newInventory(BazaarInventoryObjects.CREATE_SELL_OFFER_INVENTORY_TITLE.asString(), 27);
		igui.clearInventoryItems();
		igui.setBackground(AbstractBazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		igui.enquiryAmount = InventoryUtils.getContentAmount(Bukkit.getPlayer(igui.getId()).getInventory(), Category.getCategory(igui.currentCategory).getOriginal(igui.currentSub, igui.currentSubSub));
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put(BazaarPlaceholder.INVENTORY_CONTENT, igui.enquiryAmount + "");
		
		placeholders.put(BazaarPlaceholder.UNIT_PRICE, round(sellOfferMin, 1) + "");
		placeholders.put(BazaarPlaceholder.PRICE_TOTAL, round(sellOfferMin, 1) * igui.enquiryAmount + "");
		
		igui.add(igui.setType(ItemStackUtils.replacePlaceholder(BazaarInventoryObjects.CREATE_SELL_OFFER_SAME_AS_TOP_OFFER_ITEM.asItem().clone(), placeholders)), 10, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				openConfirm(igui, sellOfferMin);
			}
			
		});
		placeholders.put(BazaarPlaceholder.UNIT_PRICE, round((sellOfferMin - 0.1D), 1) + "");
		placeholders.put(BazaarPlaceholder.PRICE_TOTAL, round((sellOfferMin - 0.1D) * igui.enquiryAmount, 1) + "");
		igui.add(igui.setType(ItemStackUtils.replacePlaceholder(BazaarInventoryObjects.CREATE_SELL_OFFER_TOP_ORDER_PLUS_ZERO_POINT_ONE_ITEM.asItem().clone(), placeholders)), 12, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				openConfirm(igui, round(sellOfferMin - 0.1D, 1));
			}
		
		});
		final Duplet<Double, String> spread = AbstractBazaarIGUI.getSpread(sellOfferMin, buyOrderMax, 10);
		placeholders.put(BazaarPlaceholder.UNIT_PRICE, round((sellOfferMin - spread.getFirst()), 1) + "");
		placeholders.put(BazaarPlaceholder.PRICE_TOTAL, round((sellOfferMin - spread.getFirst()) * igui.enquiryAmount, 1) + "");
		placeholders.put(BazaarPlaceholder.OFFERS_PRICE_LOWEST, round(sellOfferMin, 1) + "");
		placeholders.put(BazaarPlaceholder.ORDERS_PRICE_HIGHEST, round(buyOrderMax, 1) + "");
		placeholders.put(BazaarPlaceholder.SPREAD, spread.getSecond());
		igui.add(igui.setType(ItemStackUtils.replacePlaceholder(BazaarInventoryObjects.CREATE_SELL_OFFER_TEN_PERCENT_OF_SPREAD_ITEM.asItem().clone(), placeholders)), 14, new Clickable() {
	
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				openConfirm(igui, round(sellOfferMin - spread.getFirst(), 1));
			}
		
		});
		
		igui.add(BazaarInventoryObjects.CUSTOM_AMOUNT_ITEM.asItem(), 16, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.lock();
				AnvilGUI gui = new AnvilGUI.Builder().itemLeft(BazaarInventoryObjects.PICK_AMOUNT_ITEM.asItem()).plugin(igui.plugin).onComplete((player, str) ->{
					double price = -1;
					try {
						price = Double.valueOf(str);
					} catch(NumberFormatException ex) {}
					
					if(price < 0.1D) {
						player.sendMessage("§cInvalid Input: " + price);
						guis.remove(igui.getId()).closeInventory();
						openCreateSellOffer(igui);
						return AnvilGUI.Response.text("");
					} else {
						igui.enquiryPrice = price;
						guis.remove(igui.getId()).closeInventory();
						openConfirm(igui, igui.enquiryPrice);
						return AnvilGUI.Response.text("");
					}
					
				}).open(Bukkit.getPlayer(igui.getId()));
				guis.put(igui.getId(), gui);
			}
		});
	}
	
	public final void openConfirm(AbstractBazaarIGUI igui, double pricePreUnit) {
		if(Bukkit.isPrimaryThread())
			openConfirmSync(igui, pricePreUnit);
		else 
			new BukkitRunnable() {
				
				@Override
				public void run() {
					openConfirmSync(igui, pricePreUnit);
				}
			}.runTask(igui.plugin);
	}
	
	private final void openConfirmSync(AbstractBazaarIGUI igui, double pricePerUnit) {
		igui.unlock();
		igui.newInventory(BazaarInventoryObjects.CREATE_SELL_OFFER_CONFIRM_INVENTORY_TITLE.asString(), 27);
		igui.setBackground(AbstractBazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		igui.enquiryPrice = pricePerUnit;
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put(BazaarPlaceholder.INVENTORY_CONTENT, igui.enquiryAmount + "");
		placeholders.put(BazaarPlaceholder.UNIT_PRICE, pricePerUnit + "");
		placeholders.put(BazaarPlaceholder.PRICE_TOTAL, pricePerUnit * igui.enquiryAmount + "");
		placeholders.put(BazaarPlaceholder.DISPLAY_NAME, Category.getCategory(igui.currentCategory).getSubSub()[igui.currentSub - 1][igui.currentSubSub - 1].getItemMeta().getDisplayName());
		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(igui.setType(BazaarInventoryObjects.CREATE_SELL_OFFER_CONFIRM_ITEM.asItem().clone()), placeholders), 13, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				
				clazzManager.get(AbstractBazaarIGUI.class).submit(new BazaarRunnable(igui.getId()) {
					
					@Override
					public void run() {
						onCreationConfirm(igui);
					}
				});
				igui.closeAll();
			}
		}).add();
	}
	
}