package de.ancash.bazaar.listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.events.BuyInstaEvent;
import de.ancash.bazaar.files.Files;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.utils.Enquiry;
import de.ancash.bazaar.utils.InventoryUtils;
import de.ancash.bazaar.utils.ItemFromFile;
import de.ancash.bazaar.utils.ItemStackUtils;
import de.ancash.bazaar.utils.Response;
import de.ancash.bazaar.utils.SellOffer;
import de.tr7zw.nbtapi.NBTItem;

public class BuyInstantlyListener implements Listener {

	private static final ItemStack[] inv_content = new ItemStack[36];
	static {
		for(int i = 0; i<36; i++) {
			inv_content[i] = ItemFromFile.get(Files.getInvConfig(), "inventory.background");
		}
		inv_content[14] = ItemFromFile.get(Files.getInvConfig(), "inventory.buyInsta.fillInv");
		inv_content[12] = ItemFromFile.get(Files.getInvConfig(), "inventory.buyInsta.opt2");
		inv_content[10] = ItemFromFile.get(Files.getInvConfig(), "inventory.buyInsta.opt1");
		inv_content[31] = ItemFromFile.get(Files.getInvConfig(), "inventory.close");
	}
	
	@EventHandler
	public void onInvInteractBuyInsta(BuyInstaEvent e) {
		
		int slot = e.getSlot();
		Player p = e.getPlayer();
		//Category cat = Category.getCategory(e.getCat());
		if(e.getTitle().contains(Files.getInvConfig().getString("inventory.name"))) {
			HashMap<String, String> placeholder = new HashMap<String, String>();
			Inventory inv = Bukkit.createInventory(null, 4 * 9, Category.getCategory(e.getCat()).getShowcase()[Category.getSlotByID(e.getItemId() + 1)].getItemMeta().getDisplayName() + " §r-> Instant Buy");
			SelfBalancingBST sellOffer = Category.getCategory(e.getCat()).getSellOffer(e.getItemId());
			SelfBalancingBSTNode lowest = sellOffer.getMin();
			
			inv.setContents(inv_content.clone());
			
		
			placeholder.put("%price_total%", (lowest == null ? "§cN/A" : "§6" + lowest.getKey() + " coins"));
			placeholder.put("%offers_price_lowest%", (lowest == null ? "§cN/A" : "§6" + lowest.getKey() + " coins"));
			NBTItem info = new NBTItem(prepareOpt(e.getInv(), placeholder, 10, 1));
			info.setItemStack("original", e.getInv().getItem(13));
			info.setInteger("bazaar.category", e.getCat());
			info.setInteger("bazaar.item.id", e.getItemId());
			inv.setItem(10, info.getItem());
						
			placeholder.put("%price_total%", (lowest == null ? "§cN/A" : "§6" + lowest.getKey()*64 + " coins"));
			placeholder.put("%offers_price_lowest%", (lowest == null ? "§cN/A" : "§6" + lowest.getKey() + " coins"));
			ItemStack opt2 = prepareOpt(e.getInv(), placeholder, 12, 64);
			inv.setItem(12, opt2.clone());
						
			ItemStack fill_inv = inv_content[14].clone();
			int freeSlots = InventoryUtils.getFreeSlots(p);
			placeholder.put("%offers_price_lowest%", lowest == null ? "§cN/A" : "§6" + lowest.getKey() + " coins");
			placeholder.put("%price_total%", lowest == null ? "§cN/A" : "§6" + lowest.getKey()*freeSlots*64 + " coins");
			placeholder.put("%inventory_free%", freeSlots * 64 + "");
			fill_inv = ItemStackUtils.replacePlaceholder(fill_inv, placeholder);
			fill_inv.setAmount((freeSlots == 0 ? 1 : freeSlots));
			inv.setItem(14, fill_inv);
			
			p.openInventory(inv);
			return;
		}
		if(e.getTitle().contains("§r-> Instant Buy") && (slot == 10 || slot == 12)) {
			
			SelfBalancingBST root = Category.getCategory(e.getCat()).getSellOffer(e.getItemId());
			if(root == null || root.isEmpty()) {
				p.sendMessage(Response.NO_SELL_OFFER_AVAILABLE);
				return;
			}
			SelfBalancingBSTNode sellOffer = root.getMin();
			SellOffer so = (SellOffer) sellOffer.getByTimeStamp();
			Category cat = Category.getCategory(e.getCat());
			
			if(sellOffer == null || sellOffer.get().isEmpty()) {
				p.sendMessage(Response.NO_SELL_OFFER_AVAILABLE);
				return;
			}
			int amount = slot == 10 ? 1 : 64;
			
			
			if(so == null) {
				p.sendMessage(Response.NO_SELL_OFFER_AVAILABLE);
				return;
			}
			if(p.getInventory().firstEmpty() != -1) {
				if(Bazaar.getEconomy().getBalance(p) >= sellOffer.getKey() * amount) {
					if(so != null && so.getLeft() >= amount) {

						InventoryUtils.addItemAmount(amount, cat.getContents()[e.getItemId() - 1].clone(), p);
						Bazaar.getEconomy().withdrawPlayer(p, so.getPrice());
						so.setLeft(so.getLeft() - amount);
						so.addClaimable(amount);
						if(so.getLeft() == 0) {
							Enquiry.save(so);
							Enquiry.checkEnquiry(so);
						}
						return;
					}
				} else {
					p.sendMessage(Response.NO_MONEY);
					return;
				}
			}
			for(int i = 0; i<amount; i++) {
				if(so != null && p.getInventory().firstEmpty() != -1) {
					if(Bazaar.getEconomy().getBalance(p) >= sellOffer.getKey()) {
						
						//InventoryUtils.addItemAmount(1, cat.getContents()[e.getItemId() - 1].clone(), p);
						Bazaar.getEconomy().withdrawPlayer(p, so.getPrice());
						so.setLeft(so.getLeft() - 1);
						so.addClaimable(1);
						if(so.getLeft() == 0) {
							Enquiry.save(so);
							Enquiry.checkEnquiry(so);
							
							sellOffer = root.getMin();
							if(sellOffer != null) {
								so = (SellOffer) root.getMin().getByTimeStamp();
							} else {
								so = null;
							}
							
						}
					} else {
						p.sendMessage(Response.NO_MONEY);
						return;
					}
				} else {
					p.sendMessage(Response.INVENTORY_FULL);
					return;
				}
			}
			
			
		}
	}

	private static ItemStack prepareOpt(Inventory inv, HashMap<String, String> placeholder, int content, int amount) {
		ItemStack opt = inv.getItem(13).clone();
		ItemMeta temp = opt.getItemMeta();
		temp.setLore(inv_content[content].getItemMeta().getLore());
		temp.setDisplayName(inv_content[content].getItemMeta().getDisplayName());
		opt.setItemMeta(temp);
		opt = ItemStackUtils.replacePlaceholder(opt, placeholder);
		opt.setAmount(amount);
		return opt;
	}
	
	public static ItemStack createBuyInsta(int cat, int item_id, ItemStack buyInsta, Inventory inv, Player p) {
		SelfBalancingBST rootSellOffer= Category.getCategory(cat).getSellOffer(item_id);
		HashMap<String, String> placeholder = new HashMap<String, String>();
		SelfBalancingBSTNode min = rootSellOffer.getMin();
		placeholder.put("%offers_price_lowest%", (!rootSellOffer.isEmpty() ? (min.get().size() != 0 ? "§6" + min.getKey() + " coins" : "§cN/A"): "§cN/A"));
		placeholder.put("%offers_price_lowest_stack%", (!rootSellOffer.isEmpty() ? (min.get().size() != 0 ? "§6" + min.getKey()*64 + " coins" : "§cN/A"): "§cN/A"));
		buyInsta = ItemStackUtils.replacePlaceholder(buyInsta, placeholder);
		return buyInsta;
	}
}
