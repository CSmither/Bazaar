package de.ancash.bazaar.sockets.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.gui.inventory.BazaarInventoryItem;
import de.ancash.bazaar.gui.inventory.BazaarInventoryType;
import de.ancash.bazaar.management.BuyOrder;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.anvilgui.AnvilGUI;
import de.ancash.minecraft.inventory.Clickable;

import static de.ancash.bazaar.sockets.gui.BazaarIGUI.*;
import static de.ancash.misc.MathsUtils.round;

import java.util.UUID;

enum BazaarIGUICreateBuyOrder {
	
	INSTANCE;
	
	private ItemStack cBO_SIXTY_FOUR;
	private ItemStack cBO_ONE_HUNDRED_SIXTY;
	private ItemStack cBO_ONE_THOUSAND;
	private ItemStack cBO_SAME_AS_TOP_ORDER;
	private ItemStack cBO_TOP_ORDER_PLUS_ZERO_POINT_ONE;
	private ItemStack cBO_FIVE_PERCENT_OF_SPREAD;
	private ItemStack cBO_CONFIRM;
	private int MAX;
	private String TITLE;
	private String TITLE_PRICE;
	private String TITLE_CONFIRM;
	
	void load(Bazaar instance) {
		cBO_CONFIRM = ItemStackUtils.get(instance.getInvConfig(), "inventory.buy-order.confirm.item");
		
		cBO_SIXTY_FOUR = ItemStackUtils.get(instance.getInvConfig(), "inventory.buy-order.opt1");
		cBO_ONE_HUNDRED_SIXTY = ItemStackUtils.get(instance.getInvConfig(), "inventory.buy-order.opt2");
		cBO_ONE_THOUSAND = ItemStackUtils.get(instance.getInvConfig(), "inventory.buy-order.opt3");
		
		cBO_SAME_AS_TOP_ORDER = ItemStackUtils.get(instance.getInvConfig(), "inventory.buy-order.price.sameAsTopOrder");
		cBO_TOP_ORDER_PLUS_ZERO_POINT_ONE = ItemStackUtils.get(instance.getInvConfig(), "inventory.buy-order.price.topOrder_01");
		cBO_FIVE_PERCENT_OF_SPREAD = ItemStackUtils.get(instance.getInvConfig(), "inventory.buy-order.price.ofSpread5");
		MAX = instance.getInvConfig().getInt("inventory.buy-order.max");
		TITLE = instance.getInvConfig().getString("inventory.buy-order.title");
		TITLE_PRICE = instance.getInvConfig().getString("inventory.buy-order.price.title");
		TITLE_CONFIRM = instance.getInvConfig().getString("inventory.buy-order.confirm.title");
	}
	
	public void openCreateBuyOrder(BazaarIGUI igui) {
		igui.unlock();
		igui.currentGUIType = BazaarInventoryType.CREATE_BUY_ORDER;
		igui.newInventory(TITLE, 27);
		igui.clearInventoryItems();
		igui.setBackground(BazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		
		CompactMap<String, String> placeholders = new CompactMap<>();
		placeholders.put("%ordering%", igui.enquiryAmount + "");
		
		igui.add(setType(igui, cBO_SIXTY_FOUR.clone()), 10, new Clickable() {
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.enquiryAmount = 64;
				openSelectOPT(igui);
			}
		});
		
		igui.add(setType(igui, cBO_ONE_HUNDRED_SIXTY.clone()), 12, new Clickable() {
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.enquiryAmount = 160;
				openSelectOPT(igui);
			}
		});
		
		igui.add(setType(igui, cBO_ONE_THOUSAND.clone()), 14, new Clickable() {
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.enquiryAmount = 1024;
				openSelectOPT(igui);
			}
		});
		
		igui.add(CUSTOM_AMOUNT, 16, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.lock();
				AnvilGUI gui = new AnvilGUI.Builder().itemLeft(PICK_AMOUNT.clone()).plugin(igui.plugin).onComplete((player, str) ->{
					int amount = -1;
					try {
						amount = Integer.valueOf(str);
					} catch(NumberFormatException ex) {}
					
					if(amount <= 0) {
						player.sendMessage("§cInvalid Input: " + amount);
						guis.remove(player.getUniqueId()).closeInventory();
						openCreateBuyOrder(igui);
						return AnvilGUI.Response.text("");
					} else if(amount > MAX){
						player.sendMessage("§cInvalid Input! Max " + MAX);
						guis.remove(player.getUniqueId()).closeInventory();
						openCreateBuyOrder(igui);
						return AnvilGUI.Response.text("");
					} else {
						igui.enquiryAmount = amount;
						guis.remove(player.getUniqueId()).closeInventory();
						openSelectOPT(igui);
						
						return AnvilGUI.Response.text("");
					}
					
				}).open(Bukkit.getPlayer(igui.getId()));
				guis.put(igui.getId(), gui);
			}
		
		});
	}
	
	private CompactMap<UUID, AnvilGUI> guis = new CompactMap<>();
	
	private void openSelectOPT(BazaarIGUI igui) {
		igui.unlock();
		igui.newInventory(TITLE_PRICE, 27);
		igui.setBackground(BazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		
		CompactMap<String, String> placeholders = new CompactMap<>();
		placeholders.put("%ordering%", igui.enquiryAmount + "");
		
		placeholders.put("%unit_price%", round(getMaxBuyOrderPrice(igui), 1) + "");
		placeholders.put("%price_total%", round(getMaxBuyOrderPrice(igui), 1) * igui.enquiryAmount + "");
		
		igui.add(setType(igui, ItemStackUtils.replacePlaceholder(cBO_SAME_AS_TOP_ORDER.clone(), placeholders)), 10, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				confirm(igui, getMaxBuyOrderPrice(igui));
			}
			
		});
		placeholders.put("%unit_price%", round((getMaxBuyOrderPrice(igui) + 0.1), 1) + "");
		placeholders.put("%price_total%", round((getMaxBuyOrderPrice(igui) + 0.1) * igui.enquiryAmount, 1) + "");
		igui.add(ItemStackUtils.replacePlaceholder(cBO_TOP_ORDER_PLUS_ZERO_POINT_ONE.clone(), placeholders), 12, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				confirm(igui, round(getMaxBuyOrderPrice(igui) + 0.1, 1));
			}
		
		});
		Duplet<Double, String> spread = getSpread(getMinSellOfferPrice(igui), getMaxBuyOrderPrice(igui), 5);
		placeholders.put("%unit_price%", round((getMaxBuyOrderPrice(igui) + spread.getFirst()), 1) + "");
		placeholders.put("%price_total%", round((getMaxBuyOrderPrice(igui) + spread.getFirst()) * igui.enquiryAmount, 1) + "");
		placeholders.put("%offers_price_lowest%", round(getMinSellOfferPrice(igui), 1) + "");
		placeholders.put("%orders_price_highest%", round(getMaxBuyOrderPrice(igui), 1) + "");
		placeholders.put("%spread%", spread.getSecond());
		igui.add(ItemStackUtils.replacePlaceholder(cBO_FIVE_PERCENT_OF_SPREAD.clone(), placeholders), 14, new Clickable() {
	
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				confirm(igui, round(getMaxBuyOrderPrice(igui) + getSpread(getMinSellOfferPrice(igui), getMaxBuyOrderPrice(igui), 5).getFirst(), 1));
			}
		
		});
		
		igui.add(CUSTOM_AMOUNT, 16, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.lock();
				AnvilGUI gui = new AnvilGUI.Builder().itemLeft(PICK_AMOUNT.clone()).plugin(igui.plugin).onComplete((player, str) ->{
					double price = -1;
					try {
						price = Double.valueOf(str);
					} catch(NumberFormatException ex) {}
					
					if(price < 0.1D) {
						player.sendMessage("§cInvalid Input: " + price);
						guis.remove(igui.getId()).closeInventory();
						openSelectOPT(igui);
						return AnvilGUI.Response.text("");
					} else {
						igui.enquiryPrice = price;
						guis.remove(igui.getId()).closeInventory();
						confirm(igui, igui.enquiryPrice);
						return AnvilGUI.Response.text("");
					}
					
				}).open(Bukkit.getPlayer(igui.getId()));
				guis.put(igui.getId(), gui);
			}
		
		});
	}
	
	private void confirm(BazaarIGUI igui, double pricePerUnit) {
		igui.unlock();
		igui.newInventory(TITLE_CONFIRM, 27);
		igui.setBackground(BazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		igui.enquiryPrice = pricePerUnit;
		CompactMap<String, String> placeholders = new CompactMap<>();
		placeholders.put("%ordering%", igui.enquiryAmount + "");
		placeholders.put("%unit_price%", pricePerUnit + "");
		placeholders.put("%price_total%", pricePerUnit * igui.enquiryAmount + "");
		placeholders.put("%displayname%", Category.getCategory(igui.currentCategory).getSubSub()[igui.currentSub - 1][igui.currentSubSub - 1].getItemMeta().getDisplayName());
		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(setType(igui, cBO_CONFIRM.clone()), placeholders), 13, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				Player player = Bukkit.getPlayer(igui.getId());
				if(PlayerManager.get(player.getUniqueId()).getEnquiries() >= 27) {
					player.sendMessage(igui.plugin.getResponse().CANNOT_CREATE_ENQUIRY);
					igui.closeAll();
					return;
				}
				if(igui.enquiryPrice * igui.enquiryAmount > igui.plugin.getEconomy().getBalance(player)) {
					player.sendMessage(igui.plugin.getResponse().NO_MONEY);
					igui.closeAll();
				} else {
					BuyOrder buyOrder = new BuyOrder(igui.enquiryAmount, igui.enquiryPrice, igui.getId(), igui.currentCategory, igui.currentSub, igui.currentSubSub);
					igui.plugin.getEnquiryUtils().insert(buyOrder);
					igui.getInventory().getViewers().forEach(p -> p.sendMessage(igui.plugin.getResponse().BUY_ORDER_SETUP
							.replace("%amount%", igui.enquiryAmount + "")
							.replace("%price%", round(igui.enquiryAmount * igui.enquiryPrice, 1) + "")
							.replace("%displayname%", Category.getCategory(igui.currentCategory).getSubSub()[igui.currentSub - 1][igui.currentSubSub - 1].getItemMeta().getDisplayName())));
					igui.closeAll();
				}
			}
		}).add();
	}
}