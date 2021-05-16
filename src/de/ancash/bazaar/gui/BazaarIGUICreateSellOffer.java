package de.ancash.bazaar.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.management.SellOffer;
import de.ancash.bazaar.utils.InventoryUtils;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.anvilgui.AnvilGUI;
import de.ancash.minecraft.inventory.Clickable;

import static de.ancash.bazaar.gui.BazaarIGUI.*;
import static de.ancash.misc.MathsUtils.round;

import java.util.UUID;

class BazaarIGUICreateSellOffer {

	private static ItemStack cSO_SAME_AS_TOP_ORDER;
	private static ItemStack cSO_TOP_ORDER_PLUS_ZERO_POINT_ONE;
	private static ItemStack cSO_TEN_PERCENT_OF_SPREAD;
	private static ItemStack cSO_CONFIRM;
	private static String TITLE;
	private static String TITLE_CONFIRM;
		
	static final ItemStack pickAmount;
	
	static {
		pickAmount = new ItemStack(Material.SIGN);
		ItemMeta im = pickAmount.getItemMeta();
		im.setDisplayName("0");
		pickAmount.setItemMeta(im);
	}
	
	static void load(Bazaar instance) {
		cSO_CONFIRM = ItemStackUtils.get(instance.getInvConfig(), "inventory.sell-offer.confirm.item");
		
		cSO_SAME_AS_TOP_ORDER = ItemStackUtils.get(instance.getInvConfig(), "inventory.sell-offer.sameAsBestOffer");
		cSO_TOP_ORDER_PLUS_ZERO_POINT_ONE = ItemStackUtils.get(instance.getInvConfig(), "inventory.sell-offer.bestOffer_01");
		cSO_TEN_PERCENT_OF_SPREAD = ItemStackUtils.get(instance.getInvConfig(), "inventory.sell-offer.ofSpread10");
		TITLE = instance.getInvConfig().getString("inventory.sell-offer.title");
		TITLE_CONFIRM = instance.getInvConfig().getString("inventory.sell-offer.confirm.title");
	}
	
	public static void openCreateSellOffer(BazaarIGUI igui) {
		//check if player has any items to sell
		if(InventoryUtils.getContentAmount(Bukkit.getPlayer(igui.getId()).getInventory(), Category.getCategory(igui.currentCategory).getOriginal(igui.currentSub, igui.currentSubSub)) == 0)
			return;
		
		igui.unlock();
		//instant select price, no option to select how many to sell
		openSelectOPT(igui);
	}
	
	
	
	private static void openSelectOPT(BazaarIGUI igui) {
		igui.unlock();
		igui.newInventory(TITLE, 27);
		igui.clearInventoryItems();
		igui.setBackground(BazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		igui.enquiryAmount = InventoryUtils.getContentAmount(Bukkit.getPlayer(igui.getId()).getInventory(), Category.getCategory(igui.currentCategory).getOriginal(igui.currentSub, igui.currentSubSub));
		CompactMap<String, String> placeholders = new CompactMap<>();
		double sellOfferMin = getMinSellOfferPrice(igui);
		double buyOrderMax = getMaxBuyOrderPrice(igui);
		placeholders.put("%inventory_content%", igui.enquiryAmount + "");
		
		placeholders.put("%unit_price%", round(sellOfferMin, 1) + "");
		placeholders.put("%price_total%", round(sellOfferMin, 1) * igui.enquiryAmount + "");
		
		igui.add(setType(igui, ItemStackUtils.replacePlaceholder(cSO_SAME_AS_TOP_ORDER.clone(), placeholders)), 10, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				confirm(igui, sellOfferMin);
			}
			
		});
		placeholders.put("%unit_price%", round((sellOfferMin - 0.1D), 1) + "");
		placeholders.put("%price_total%", round((sellOfferMin - 0.1D) * igui.enquiryAmount, 1) + "");
		igui.add(setType(igui, ItemStackUtils.replacePlaceholder(cSO_TOP_ORDER_PLUS_ZERO_POINT_ONE.clone(), placeholders)), 12, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				confirm(igui, round(sellOfferMin - 0.1D, 1));
			}
		
		});
		final Duplet<Double, String> spread = InventoryUtils.getSpread(sellOfferMin, buyOrderMax, 10);
		placeholders.put("%unit_price%", round((sellOfferMin - spread.getFirst()), 1) + "");
		placeholders.put("%price_total%", round((sellOfferMin - spread.getFirst()) * igui.enquiryAmount, 1) + "");
		placeholders.put("%offers_price_lowest%", round(sellOfferMin, 1) + "");
		placeholders.put("%orders_price_highest%", round(buyOrderMax, 1) + "");
		placeholders.put("%spread%", spread.getSecond());
		igui.add(setType(igui, ItemStackUtils.replacePlaceholder(cSO_TEN_PERCENT_OF_SPREAD.clone(), placeholders)), 14, new Clickable() {
	
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				confirm(igui, round(sellOfferMin - spread.getFirst(), 1));
			}
		
		});
		
		igui.add(CUSTOM_AMOUNT, 16, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				igui.lock();
				AnvilGUI gui = new AnvilGUI.Builder().itemLeft(pickAmount.clone()).plugin(igui.plugin).onComplete((player, str) ->{
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
						confirm(igui, igui.enquiryPrice);
						return AnvilGUI.Response.text("");
					}
					
				}).open(Bukkit.getPlayer(igui.getId()));
				guis.put(igui.getId(), gui);
			}
		});
	}
	
	private static CompactMap<UUID, AnvilGUI> guis = new CompactMap<>();
	
	private static void confirm(BazaarIGUI igui, double pricePerUnit) {
		igui.unlock();
		igui.newInventory(TITLE_CONFIRM, 27);
		igui.setBackground(BazaarIGUI.INVENTORY_SIZE_TWNTY_SVN);
		igui.setCloseItem(22);
		igui.enquiryPrice = pricePerUnit;
		CompactMap<String, String> placeholders = new CompactMap<>();
		placeholders.put("%inventory_content%", igui.enquiryAmount + "");
		placeholders.put("%unit_price%", pricePerUnit + "");
		placeholders.put("%price_total%", pricePerUnit * igui.enquiryAmount + "");
		placeholders.put("%displayname%", Category.getCategory(igui.currentCategory).getSubSub()[igui.currentSub - 1][igui.currentSubSub - 1].getItemMeta().getDisplayName());
		new BazaarInventoryItem(igui, ItemStackUtils.replacePlaceholder(setType(igui, cSO_CONFIRM.clone()), placeholders), 13, new Clickable() {
			
			@Override
			public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
				Player player = Bukkit.getPlayer(igui.getId());
				if(PlayerManager.get(player.getUniqueId()).getEnquiries() >= 27) {
					player.sendMessage(igui.plugin.getResponse().CANNOT_CREATE_ENQUIRY);
					igui.closeAll();
					return;
				}
				InventoryUtils.removeItemAmount(igui.enquiryAmount, Category.getCategory(igui.currentCategory).getOriginal(igui.currentSub, igui.currentSubSub), player);
				SellOffer sellOffer = new SellOffer(igui.enquiryAmount, igui.enquiryPrice, igui.getId(), igui.currentCategory, igui.currentSub, igui.currentSubSub);
				igui.plugin.getEnquiryUtils().insert(sellOffer);
				
				igui.getInventory().getViewers().stream().findFirst().get().sendMessage(igui.plugin.getResponse().SELL_OFFER_SETUP
						.replace("%amount%", igui.enquiryAmount + "")
						.replace("%price%", round(igui.enquiryAmount * igui.enquiryPrice, 1) + "")
						.replace("%displayname%", Category.getCategory(igui.currentCategory).getSubSub()[igui.currentSub - 1][igui.currentSubSub - 1].getItemMeta().getDisplayName()));
				igui.closeAll();
			}
		}).add();
	}
}