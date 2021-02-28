package de.ancash.bazaar.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.events.BuyInstaEvent;
import de.ancash.bazaar.events.SellInstaEvent;
import de.ancash.bazaar.files.Files;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.utils.CreateEnquiry;
import de.ancash.bazaar.utils.InventoryUtils;
import de.ancash.bazaar.utils.ItemFromFile;
import de.ancash.bazaar.utils.ItemStackUtils;
import de.ancash.bazaar.utils.MathsUtils;
import de.ancash.bazaar.utils.Response;
import de.tr7zw.nbtapi.NBTItem;

public class BazaarInvClick implements Listener{

	private static final ItemStack[] info_about_item = new ItemStack[36];
	
	static {
		ItemStack background = ItemFromFile.get(Files.getInvConfig(), "inventory.background");
		for(int i = 0; i<36; i++) {
			info_about_item[i] = background;
		}
		info_about_item[16] = ItemFromFile.get(Files.getInvConfig(), "inventory.opt_inv.create_sell_offer");
		info_about_item[15] = ItemFromFile.get(Files.getInvConfig(), "inventory.opt_inv.create_buy_order");
		info_about_item[11] = ItemFromFile.get(Files.getInvConfig(), "inventory.opt_inv.sellInsta");
		info_about_item[10] = ItemFromFile.get(Files.getInvConfig(), "inventory.opt_inv.buyInsta");
		info_about_item[31] = ItemFromFile.get(Files.getInvConfig(), "inventory.close");
	}
	/*
	 * 
	 */
	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		
		if(e.getSlot() == 31 && e.getInventory().getSize() == 36) {
			e.getWhoClicked().closeInventory();
			return;
		}
		//buy insta
		if(e.getView().getTitle().contains("§r-> Instant Buy")) {
			e.setCancelled(true);
			NBTItem info = new NBTItem(e.getInventory().getItem(10).clone());
			BuyInstaEvent bih = new BuyInstaEvent(e.getInventory(), e.getSlot(), info.getInteger("bazaar.item.id"), info.getInteger("bazaar.category"), (Player) e.getWhoClicked(), e.getView().getTitle());
			Bukkit.getServer().getPluginManager().callEvent(bih);
			return;
		}
		//create buy order
		if(e.getView().getTitle().contains("How many do you want?") || e.getView().getTitle().contains("How much do you want to pay?")) {
			e.setCancelled(true);
			NBTItem info = new NBTItem(e.getInventory().getItem(10).clone());
			InvClickCreateBuyOrder.handle(new CreateEnquiry(e.getInventory(), e.getSlot(), info.getInteger("bazaar.item.id"), info.getInteger("bazaar.category"), (Player) e.getWhoClicked(), e.getView().getTitle()));
			return;
		}
		//create sell offer
		if(e.getView().getTitle().contains("At what price are you selling?") || e.getView().getTitle().contains("Confirm Sell Offer")) {
			e.setCancelled(true);
			NBTItem info = new NBTItem(e.getInventory().getItem(10).clone());
			InvClickCreateSellOffer.handle(new CreateEnquiry(e.getInventory(), e.getSlot(), info.getInteger("bazaar.item.id"), info.getInteger("bazaar.category"), (Player) e.getWhoClicked(), e.getView().getTitle()));
			return;
		}
		
		if(e.getView().getTitle().equals("Your Bazaar Enquiries")) {
			e.setCancelled(true);
			ManageEnquiriesListener.handle((Player) e.getWhoClicked(), e.getInventory(), e.getSlot(), e.getView().getTitle(), e.getAction());
			return;	
		}
		
		if(e.getView().getTitle().contains(Files.getInvConfig().getString("inventory.name"))) {
			e.setCancelled(true);
			if(e.getAction().equals(InventoryAction.NOTHING) || e.getAction().equals(InventoryAction.UNKNOWN)) return;
			if(!e.getClickedInventory().equals(e.getInventory())) return;
			int slot = e.getSlot();
			Player p = (Player) e.getWhoClicked();
			
			//close inv?
			if(slot == 40 && (e.getAction().equals(InventoryAction.PICKUP_ALL) || e.getAction().equals(InventoryAction.PICKUP_HALF))) {
				p.closeInventory();
				return;
			}
			
			
			
			//manage enquiries
			if(slot == 41 && (e.getAction().equals(InventoryAction.PICKUP_ALL) || e.getAction().equals(InventoryAction.PICKUP_HALF))) {
				ManageEnquiriesListener.handle(p, e.getInventory(), slot, e.getView().getTitle(), e.getAction());
				return;
			}
			
			//change category if clicked slot matches
			if(e.getInventory().getSize() == 45 && (slot == 0 || slot == 9 || slot == 18 || slot == 27 || slot == 36)) {
				if(!Category.exists(1 + slot / 9)) {
					p.sendMessage(Response.CATEGORY_IN_PROCESS);
					return;
				}
				Inventory inv = e.getInventory();
				inv.clear();
				inv.setContents(Category.getCategory(1 + slot / 9).getShowcase().clone());
				inv = prepareMain(inv, 1 + slot / 9, slot, p);
				
				//e.getWhoClicked().openInventory(inv);
				//TitleUpdater.update((Player) e.getWhoClicked(), Files.getInvConfig().getString("inventory.name") + " -> " + Files.getInvConfig().getString("inventory.categories." + (1 * slot / 9) + ".item.meta.displayname"));
				return;
			}
			
			//maybe sub? else info about offers/orders
			if(e.getInventory().getSize() == 45 && ((slot > 10 && slot < 17) || (slot > 19 && slot < 26) || (slot > 28 && slot < 35))) {
				int item_id = Category.getItemIDBySlot(slot);
				int cat = 0;
				for(int i = 0; i<5; i++) {
					cat++;
					if(e.getInventory().getItem(i * 9).getEnchantmentLevel(Enchantment.DURABILITY) == 1) break;
				}
				//Category category = Category.getCategory(cat);
				//if(!category.hasSubcategory(item_id)) {
				if(true) {
					Inventory inv = Bukkit.createInventory(null, 4 * 9, Files.getInvConfig().getString("inventory.name") + " -> " + e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName());
					inv = prepareInfo(inv, item_id, cat, p);
					
					
					p.openInventory(inv);
				}
				return;
			}
			
			if(e.getInventory().getSize() == 36 && (slot == 10 || slot == 11 || slot == 15 || slot == 16)) {
				
				NBTItem info = new NBTItem(e.getInventory().getItem(11));
				int cat = info.getInteger("bazaar.category");
				int item_id = info.getInteger("bazaar.item.id");
				
				//create buy order
				if(slot == 15) {
					InvClickCreateBuyOrder.handle(new CreateEnquiry(e.getInventory(), slot, item_id, cat, p, e.getView().getTitle()));
					return;
				}
				
				//create sell offer
				if(slot == 16) {
					InvClickCreateSellOffer.handle(new CreateEnquiry(e.getInventory(), slot, item_id, cat, p, e.getView().getTitle()));
					return;
				}
				
				//buy insta
				if(slot == 10) {
					BuyInstaEvent bih = new BuyInstaEvent(e.getInventory(), slot, item_id, cat, p,e.getView().getTitle());
					Bukkit.getServer().getPluginManager().callEvent(bih);
					return;
				}
				//sell insta
				if(slot == 11) {
					SellInstaEvent sie = new SellInstaEvent(e.getInventory(), slot, item_id, cat, p, e.getView().getTitle());
					Bukkit.getServer().getPluginManager().callEvent(sie);
					return;
				}
				
				p.openInventory(prepareInfo(e.getInventory(), item_id, cat, p));
				
			}
		}
	}
	
	public static Inventory prepareInfo(Inventory inv, int item_id, int cat, Player p) {
		Category category = Category.getCategory(cat);
		inv.setContents(info_about_item.clone());
		inv.setItem(13, category.getContents()[item_id - 1].clone());
		
		inv = setCreateSellOffer(inv, item_id, cat, p);
		
		inv = setCreateBuyOrder(inv, item_id, cat, p);
		
		
		inv.setItem(10, BuyInstantlyListener.createBuyInsta(cat, item_id, info_about_item[10], inv, p));
		
		inv.setItem(11, SellInstantlyListener.createSellInsta(cat, item_id, info_about_item[11], inv, p));
		
		return inv;
	}
	
	private static Inventory setCreateBuyOrder(Inventory inv, int item_id, int cat, Player p) {
		SelfBalancingBST rootBuyOrder = Category.getCategory(cat).getBuyOrder(item_id);
		ItemStack create_buy_order = inv.getItem(15);
		List<String> temp = new ArrayList<String>();
		for(String str : create_buy_order.getItemMeta().getLore()) {
			if(!str.contains("%top_orders")) {
				temp.add(str);
				continue;
			}
			int amountOffers = Integer.valueOf(str.split("%")[1].replace("top_orders_", ""));
			for(int i = 1; i<=amountOffers; i++) {
				StringBuilder msg = new StringBuilder();
				msg.append("§8- §6");
				SelfBalancingBSTNode kthLargest = SelfBalancingBST.KthLargestUsingMorrisTraversal(rootBuyOrder.getRoot(), i);
				double value = kthLargest == null ? Double.MAX_VALUE : MathsUtils.round(kthLargest.getKey(), 2);
				if(value == Double.MAX_VALUE) break; 
				SelfBalancingBSTNode node = rootBuyOrder.get(value, rootBuyOrder.getRoot());
				msg.append(value + " coins §7each §f: §a" + node.sum() + "§7x from §f" + (node.get().size() != 1 ? node.get().size() + " §7orders": "1 §7order "));
				if(!temp.contains(msg.toString())) temp.add(msg.toString());
			}
		}
		create_buy_order = ItemStackUtils.setLore(temp, create_buy_order);
		inv.setItem(15, create_buy_order);
		return inv;
	}
	
	private static Inventory setCreateSellOffer(Inventory inv, int item_id, int cat, Player p) {
		SelfBalancingBST rootSellOffer = Category.getCategory(cat).getSellOffer(item_id);
		ItemStack create_sell_offer = inv.getItem(16);
		List<String> lore = create_sell_offer.getItemMeta().getLore();
		for(int l = 0;l < create_sell_offer.getItemMeta().getLore().size(); l++) {
			lore.set(l, lore.get(l).replace("%inventory_content%", "" + InventoryUtils.getContentAmount(p.getInventory(), inv.getItem(13).clone())));					
			
		}
		List<String> newLore = new ArrayList<String>();
		for(String str : lore) {
			if(!str.contains("%top_offers")) {
				newLore.add(str);
				continue;
			}
			int amountOffers = Integer.valueOf(str.split("%")[1].replace("top_offers_", ""));
			for(int i = 1; i<=amountOffers; i++) {
				StringBuilder msg = new StringBuilder();
				msg.append("§8- §6");
				double value = SelfBalancingBST.kthSmallest(rootSellOffer.getRoot(), i);
				if(value == Double.MAX_VALUE) break; 
				SelfBalancingBSTNode node = rootSellOffer.get(value, rootSellOffer.getRoot());
				msg.append(value + " coins §7each §f: §a" + node.sum() + "§7x from §f" + (node.get().size() != 1 ? node.get().size() + " §7offers": "1 §7offer"));
				if(!newLore.contains(msg.toString())) newLore.add(msg.toString());
			}
		}
		create_sell_offer = ItemStackUtils.setLore(newLore, create_sell_offer);
		inv.setItem(16, create_sell_offer);
		return inv;
	}
	
	public static Inventory prepareMain(Inventory inv, int cat, int slot, Player p) {
		inv.getItem(slot).addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		Category c = Category.getCategory(cat);
		PlayerManager pm = PlayerManager.get(p.getUniqueId());
		if(slot == 0 || slot == 9 || slot == 18 || slot == 27 || slot == 36) {
			HashMap<String, String> placeholder = new HashMap<String, String>();
			for(int i = 1; i<=18; i++) {
				ItemStack is = inv.getItem(Category.getSlotByID(i));
				if(is == null) continue;
				SelfBalancingBST buyOrder = c.getBuyOrder(i);
				SelfBalancingBST sellOffer = c.getSellOffer(i);
				placeholder.clear();
				placeholder.put("%orders_price_lowest%", (!buyOrder.isEmpty() ? "§6" + MathsUtils.round(buyOrder.getMin().getKey(), 2) + " coins" : "§cN/A"));
				placeholder.put("%orders_price_highest%", (!buyOrder.isEmpty() ? "§6" + MathsUtils.round(buyOrder.getMax().getKey(), 2) + " coins" : "§cN/A"));
				placeholder.put("%orders_total%", (!buyOrder.isEmpty() ? "" + (int) buyOrder.countNodes() : "§cN/A"));
				placeholder.put("%orders_content%", (!buyOrder.isEmpty() ? "" + (int) buyOrder.getAllContents() : "§cN/A"));
				
				placeholder.put("%offers_price_lowest%", (!sellOffer.isEmpty() ? "§6" + MathsUtils.round(sellOffer.getMin().getKey(), 2) + " coins" : "§cN/A"));
				placeholder.put("%offers_price_highest%", (!sellOffer.isEmpty() ? "§6" + MathsUtils.round(sellOffer.getMax().getKey(), 2) + " coins" : "§cN/A"));
				placeholder.put("%offers_total%", (!sellOffer.isEmpty() ? "" + (int) sellOffer.countNodes() : "§cN/A"));
				placeholder.put("%offers_content%", (!sellOffer.isEmpty() ? "" + (int) sellOffer.getAllContents() : "§cN/A"));
				
				is = ItemStackUtils.replacePlaceholder(is, placeholder);
				inv.setItem(Category.getSlotByID(i), is);
			}
			placeholder.clear();
			ItemStack manageEnquiries = inv.getItem(41);
			int claimableItems = pm.getClaimableItems();
			double coinsToClaim = pm.getClaimableCoins();
			placeholder.put("%enquiries%", "" + pm.getEnquiries());
			placeholder.put("%coins_to_claim%", "" + coinsToClaim);
			placeholder.put("%items_to_claim%", "" + claimableItems);
			if(claimableItems == 0) manageEnquiries = ItemStackUtils.removeLine("%items_to_claim%", manageEnquiries);
			if(coinsToClaim == 0) manageEnquiries = ItemStackUtils.removeLine("%coins_to_claim%", manageEnquiries);
			inv.setItem(41, ItemStackUtils.replacePlaceholder(manageEnquiries, placeholder));
		}
		return inv;
	}
}
