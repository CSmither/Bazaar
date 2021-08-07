package de.ancash.bazaar.gui.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.datastructures.tuples.Triplet;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.anvilgui.AnvilGUI;
import de.ancash.minecraft.inventory.Clickable;

public abstract class AbstractBazaarCreateBuyOrderInv {
	
	private final BazaarInventoryClassManager clazzManager;
	
	protected AbstractBazaarCreateBuyOrderInv(BazaarInventoryClassManager clazzManager) {
		this.clazzManager = clazzManager;
	}

	private Map<UUID, AnvilGUI> guis = new HashMap<>();
	
	/**
	 * async
	 * 
	 * @param igui
	 */
	public abstract void onCreationConfirm(AbstractBazaarIGUI igui);
	
	/**
	 * Called when placeholders for {@link AbstractBazaarCreateBuyOrderInv#openSelectPrice(AbstractBazaarIGUI, Map, Map, Map)} are needed.
	 * Must be called after/in this method.
	 * async
	 * 
	 * @param igui
	 */
	public abstract Triplet<Map<String, String>, Map<String, String>, Map<String, String>> getPlaceholders(AbstractBazaarIGUI igui);
	
	
	/**
	 * Opens the new inventory and sets ALL items.
	 * 
	 * @param igui
	 */
	public final void openCreateBuyOrder(AbstractBazaarIGUI igui) {
		if(Bukkit.isPrimaryThread())
			openCreateBuyOrderSync(igui);
		else
			new BukkitRunnable() {
				
				@Override
				public void run() {
					openCreateBuyOrderSync(igui);
				}
			}.runTask(igui.plugin);
	}
	
	private final void openCreateBuyOrderSync(AbstractBazaarIGUI igui) {	
		igui.unlock();
		igui.currentGUIType = BazaarInventoryType.CREATE_BUY_ORDER;
		igui.newInventory(BazaarInventoryObjects.CREATE_BUY_ORDER_INVENTORY_TITLE.asString(), 27);
		igui.clearInventoryItems();
		igui.setBackground(AbstractBazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		
		igui.add(igui.setType(BazaarInventoryObjects.CREATE_BUY_ORDER_SIXTY_FOUR_ITEM.asItem().clone()), 10, new Clickable() {
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.enquiryAmount = 64;
				preOpenSelectPrice(igui);
			}
		});
		
		igui.add(igui.setType(BazaarInventoryObjects.CREATE_BUY_ORDER_ONE_HUNDRED_SIXTY_ITEM.asItem().clone()), 12, new Clickable() {
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.enquiryAmount = 160;
				preOpenSelectPrice(igui);
			}
		});
		
		igui.add(igui.setType(BazaarInventoryObjects.CREATE_BUY_ORDER_ONE_THOUSAND_ITEM.asItem().clone()), 14, new Clickable() {
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.enquiryAmount = 1024;
				preOpenSelectPrice(igui);
			}
		});
		
		igui.add(BazaarInventoryObjects.CUSTOM_AMOUNT_ITEM.asItem(), 16, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.lock();
				AnvilGUI gui = new AnvilGUI.Builder().itemLeft(BazaarInventoryObjects.PICK_AMOUNT_ITEM.asItem()).plugin(igui.plugin).onComplete((player, str) ->{
					int amount = -1;
					try {
						amount = Integer.valueOf(str);
					} catch(NumberFormatException ex) {}
					
					if(amount <= 0) {
						player.sendMessage("§cInvalid Input: " + amount);
						guis.remove(player.getUniqueId()).closeInventory();
						openCreateBuyOrder(igui);
						return AnvilGUI.Response.text("");
					} else if(amount > BazaarInventoryObjects.CREATE_BUY_ORDER_MAX.asInt()){
						player.sendMessage("§cInvalid Input! Max " + BazaarInventoryObjects.CREATE_BUY_ORDER_MAX.asInt());
						guis.remove(player.getUniqueId()).closeInventory();
						openCreateBuyOrder(igui);
						return AnvilGUI.Response.text("");
					} else {
						igui.enquiryAmount = amount;
						guis.remove(player.getUniqueId()).closeInventory();
						preOpenSelectPrice(igui);
						
						return AnvilGUI.Response.text("");
					}
					
				}).open(Bukkit.getPlayer(igui.getId()));
				guis.put(igui.getId(), gui);
			}
		});
		
	}
	
	/**
	 * Opens the new inventory and sets the basic items.
	 * 
	 * @param igui
	 */
	public final void preOpenSelectPrice(AbstractBazaarIGUI igui) {
		if(Bukkit.isPrimaryThread())
			preOpenSelectPriceSync(igui);
		else 
			new BukkitRunnable() {
				
				@Override
				public void run() {
					preOpenSelectPriceSync(igui);
				}
			}.runTask(igui.plugin);
	}
	
	/**
	 * Opens the new inventory and sets the basic items.
	 * 
	 * @param igui
	 */
	private final void preOpenSelectPriceSync(AbstractBazaarIGUI igui) {
		igui.unlock();
		igui.newInventory(BazaarInventoryObjects.CREATE_BUY_ORDER_PRICE_INVENTORY_TITLE.asString(), 27);
		igui.setBackground(AbstractBazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		clazzManager.get(AbstractBazaarIGUI.class).submit(new BazaarRunnable(igui.getId()) {
			
			@Override
			public void run() {
				Triplet<Map<String, String>, Map<String, String>, Map<String, String>> placeholders = getPlaceholders(igui);
				openSelectPrice(igui, placeholders.getFirst(), placeholders.getSecond(), placeholders.getThird());
			}
			
		});
	}
	
	public final void openSelectPrice(AbstractBazaarIGUI igui, Map<String, String> placeholderA, Map<String, String> placeholderB, Map<String, String> placeholderC) {
		if(Bukkit.isPrimaryThread()) 
			openSelectPriceSync(igui, placeholderA, placeholderB, placeholderC);
		else 
			new BukkitRunnable() {
				
				@Override
				public void run() {
					openSelectPriceSync(igui, placeholderA, placeholderB, placeholderC);
				}
			}.runTask(igui.plugin);
	}
	
	private final void openSelectPriceSync(AbstractBazaarIGUI igui, Map<String, String> placeholderA, Map<String, String> placeholderB, Map<String, String> placeholderC) {
		
		igui.add(igui.setType(ItemStackUtils.replacePlaceholder(BazaarInventoryObjects.CREATE_BUY_ORDER_SAME_AS_TOP_ORDER_ITEM.asItem().clone(), placeholderA)), 10, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				openConfirmInventory(igui, Double.valueOf(placeholderA.get(BazaarPlaceholder.UNIT_PRICE)));
			}
			
		});
		
		igui.add(ItemStackUtils.replacePlaceholder(BazaarInventoryObjects.CREATE_BUY_ORDER_TOP_ORDER_PLUS_ZERO_POINT_ONE_ITEM.asItem().clone(), placeholderB), 12, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				openConfirmInventory(igui, Double.valueOf(placeholderB.get(BazaarPlaceholder.UNIT_PRICE)));
			}
		
		});
		
		igui.add(ItemStackUtils.replacePlaceholder(BazaarInventoryObjects.CREATE_BUY_ORDER_FIVE_PERCENT_OF_SPREAD_ITEM.asItem().clone(), placeholderC), 14, new Clickable() {
	
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				openConfirmInventory(igui, Double.valueOf(placeholderC.get(BazaarPlaceholder.UNIT_PRICE)));
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
						preOpenSelectPrice(igui);
						return AnvilGUI.Response.text("");
					} else {
						igui.enquiryPrice = price;
						guis.remove(igui.getId()).closeInventory();
						openConfirmInventory(igui, igui.enquiryPrice);
						return AnvilGUI.Response.text("");
					}
					
				}).open(Bukkit.getPlayer(igui.getId()));
				guis.put(igui.getId(), gui);
			}
		
		});
	}
	
	public final void openConfirmInventory(AbstractBazaarIGUI igui, double pricePerUnit) {
		if(Bukkit.isPrimaryThread())
			openConfirmInventorySync(igui, pricePerUnit);
		else 
			new BukkitRunnable() {
				
				@Override
				public void run() {
					openConfirmInventorySync(igui, pricePerUnit);
				}
			}.runTask(igui.plugin);
	}
		
	private final void openConfirmInventorySync(AbstractBazaarIGUI igui, double pricePerUnit) {
		igui.unlock();
		igui.newInventory(BazaarInventoryObjects.CREATE_BUY_ORDER_CONFIRM_INVENTORY_TITLE.asString(), 27);
		igui.setBackground(AbstractBazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		igui.enquiryPrice = pricePerUnit;
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put(BazaarPlaceholder.BUY_ORDER_ORDERING, igui.enquiryAmount + "");
		placeholders.put(BazaarPlaceholder.UNIT_PRICE, pricePerUnit + "");
		placeholders.put(BazaarPlaceholder.PRICE_TOTAL, pricePerUnit * igui.enquiryAmount + "");
		placeholders.put(BazaarPlaceholder.DISPLAY_NAME, Category.getCategory(igui.currentCategory).getSubSub()[igui.currentSub - 1][igui.currentSubSub - 1].getItemMeta().getDisplayName());

		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(igui.setType(BazaarInventoryObjects.CREATE_BUY_ORDER_CONFIRM_ITEM.asItem().clone()), placeholders), 13, new Clickable() {
			
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