package de.ancash.bazaar.management;

import java.io.File;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.datastructures.tuples.ImmutableTriplet;
import de.ancash.ilibrary.datastructures.tuples.Tuple;
import de.ancash.ilibrary.minecraft.nbt.NBTItem;
import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.files.Files;
import de.ancash.bazaar.management.Enquiry.EnquiryTypes;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.InventoryUtils;
import de.ancash.bazaar.utils.ItemFromFile;
import de.ancash.bazaar.utils.ItemStackUtils;
import de.ancash.bazaar.utils.MathsUtils;
import de.ancash.bazaar.utils.Chat.ChatLevel;

import static de.ancash.bazaar.utils.Response.*;

public class ManageEnquiriesListener {

	private static final ItemStack[] manageEnquiries = new ItemStack[45];
	private static final ItemStack manageSellOffer = ItemFromFile.get(Files.getInvConfig(), "inventory.manage.sellOffer");
	private static final ItemStack manageBuyOrder = ItemFromFile.get(Files.getInvConfig(), "inventory.manage.buyOrder");
	
	static {
		ItemStack background = ItemFromFile.get(Files.getInvConfig(), "inventory.background");
		for(int i = 0; i<9; i++) {
			manageEnquiries[i] = background;
			manageEnquiries[i + 36] = background;
		}
		for(int i = 0; i<5; i++) {
			manageEnquiries[i*9] = background;
			manageEnquiries[i*9 + 8] = background;
		}
		manageEnquiries[40] = ItemFromFile.get(Files.getInvConfig(), "inventory.close");
	}
	
	public static void handle(Player p, Inventory inv, int slot, String invName, InventoryAction a) {
		if(!invName.equals("Your Bazaar Enquiries")) {
			openInv(p);
			return;
		}
		if(slot == -999) return;
		if(slot == 40 && (a.equals(InventoryAction.PICKUP_ALL) || a.equals(InventoryAction.PICKUP_HALF))) {
			p.closeInventory();
			return;
		}
		
		ItemStack click = inv.getItem(slot);
		if(click == null || click.getType().equals(Material.AIR)) return;
		if(slot <= 9 || slot >= 35 || slot == 18 || slot == 17 || slot == 27 || slot == 26) return;
		NBTItem nbt = new NBTItem(click);
		collect(nbt.getDouble("price"), nbt.getInteger("category"), nbt.getInteger("show"), nbt.getInteger("sub"), nbt.getString("id"), p, EnquiryTypes.valueOf(nbt.getString("type")), slot);
	}
	
	private static void collect(double price, int category, int show, int sub, String uuid, Player p, EnquiryTypes type, int slot) {
		if(category == 0) {
			openInv(p);
			return;
		}
		Category cat = Category.getCategory(category);
		SelfBalancingBST bst = null;
		if(type.equals(EnquiryTypes.BUY_ORDER)) bst = cat.getBuyOrders(show, sub);
		if(type.equals(EnquiryTypes.SELL_OFFER)) bst = cat.getSellOffers(show, sub);
		
		if(bst == null) {
			Chat.sendMessage("Player Tried To Collect Invalid Enquiry! Cat: " + category + " ID: " + show + ", " + sub + " UUID: " + uuid, ChatLevel.WARN);
			openInv(p);
			return;
		}
		SelfBalancingBSTNode root = bst.getRoot();
		SelfBalancingBSTNode node = bst.get(price, root);
		
		if(node != null) {
			Enquiry e = node.get(UUID.fromString(uuid));
			if(e == null) {
				collectFromFile(price, category, show, sub, uuid, p, type);
				return;
			}
			if(!e.hasClaimable()) {
				p.sendMessage(NOTHIN_TO_CLAIM);
				openInv(p);
				return;
			}
			int claimable = -1;
			switch (type) {
			case SELL_OFFER:
				claimable = e.claim();
				Bazaar.getEconomy().depositPlayer(p, claimable * price);
				p.sendMessage("§6Bazaar! §7Claimed §6" + MathsUtils.round(claimable * price, 2) + " coins §7from selling §a" + claimable + "§7x " + Category.getCategory(category).getSubShow()[show - 1][sub - 1].getItemMeta().getDisplayName() + " §7at §6" + price + " §7each!");
				Enquiry.save(e);
				Enquiry.checkEnquiry(e);
				break;
			case BUY_ORDER:
				int freeSlots = InventoryUtils.getFreeSlots(p);
				if(freeSlots == 0) {
					p.sendMessage(INVENTORY_FULL);
					openInv(p);
					return;
				}
				claimable = e.getClaimable();
				int adding = 0;
				if(e.getClaimable() > freeSlots*64) {
					adding = freeSlots*64;
					int temp = e.claim();
					e.addClaimable(temp - adding);
				} else {
					adding = e.claim();
				}
				InventoryUtils.addItemAmount(adding, Category.getCategory(category).getOriginial(show, sub), p);
				p.sendMessage("§6Bazaar! §7Claimed §a" + adding + "x  " + Category.getCategory(category).getSubShow()[show - 1][sub - 1].getItemMeta().getDisplayName() + " §7worth §6" + (adding * price) + " coins §7bought for §6" + price + " §7each!");
				Enquiry.save(e);
				Enquiry.checkEnquiry(e);
				break;
			default:
				break;
			}
			openInv(p);
			return;
		} else {
			collectFromFile(price, category, show, sub, uuid, p, type);
		}
		
	}
	
	private static void collectFromFile(double price, int category, int show, int sub, String uuid, Player p, EnquiryTypes type) {
		File f = new File("plugins/Bazaar/player/" + p.getUniqueId().toString() + "/" + (type.equals(EnquiryTypes.BUY_ORDER) ? "buy_order.yml" : "sell_offer.yml"));
		FileConfiguration fc = Enquiry.alreadyLoaded.get(f);
		if(fc.getInt(uuid + ".claimable") == 0) {
			p.sendMessage(NOTHIN_TO_CLAIM);
			openInv(p);
			return;
		}
		int claimable = -1;
		switch (type) {
		case SELL_OFFER:
			claimable = fc.getInt(uuid + ".claimable");
			Bazaar.getEconomy().depositPlayer(p, claimable * price);
			p.sendMessage("§6Bazaar! §7Claimed §6" + MathsUtils.round(claimable * price, 2) + " coins §7from selling §a" + claimable + "§7x " + Category.getCategory(category).getSubShow()[show - 1][sub - 1].getItemMeta().getDisplayName() + " §7at §6" + price + " §7each!");
			fc.set(uuid + ".claimable", 0);
			Enquiry.delete(p.getUniqueId(), uuid, type);
			break;
		case BUY_ORDER:
			int freeSlots = InventoryUtils.getFreeSlots(p);
			if(freeSlots == 0) {
				p.sendMessage(INVENTORY_FULL);
				openInv(p);
				return;
			}
			claimable = fc.getInt(uuid + ".claimable");
			int adding = 0;
			if(claimable > freeSlots*64) {
				adding = freeSlots*64;
				int temp = claimable;
				fc.set(uuid + ".claimable", temp - adding);
			} else {
				adding = claimable;
			}
			if(claimable - adding == 0) Enquiry.delete(p.getUniqueId(), uuid, type);
			InventoryUtils.addItemAmount(adding, Category.getCategory(category).getOriginial(show, sub), p);
			p.sendMessage("§6Bazaar! §7Claimed §a" + adding + "x  " + Category.getCategory(category).getSubShow()[show - 1][sub - 1].getItemMeta().getDisplayName() + " §7worth §6" + (adding * price) + " coins §7bought for §6" + price + " §7each!");
			break;
		default:
			break;
		}
		openInv(p);
		return;
	}
	
	//XXX
	
	private static void openInv(Player p) {
		Inventory mE = PlayerManager.get(p.getUniqueId()).getManageEnquiries();
		mE.setContents(manageEnquiries.clone());
		CompactMap<String, CompactMap<String, Number>> sellOffer = PlayerManager.get(p.getUniqueId()).getSellOffer();
		CompactMap<String, CompactMap<String, Number>> buyOrder = PlayerManager.get(p.getUniqueId()).getBuyOrder();
		
		for(String key : sellOffer.keySet()) {
			ImmutableTriplet<Integer, Integer, Integer> infos = Tuple.immutableOf((int) sellOffer.get(key).get("category"),(int)  sellOffer.get(key).get("show"),(int)  sellOffer.get(key).get("sub"));
			Category category = Category.getCategory(infos.getFirst());
			ItemStack is = category.getSubShow()[infos.getSecond() - 1][infos.getThird() - 1].clone();
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(manageSellOffer.getItemMeta().getDisplayName().replace("%displayname%", im.getDisplayName()));
			im.setLore(manageSellOffer.getItemMeta().getLore());
			
			is.setItemMeta(im);
			
			CompactMap<String, String> placeholder = new CompactMap<String, String>();
			double price = (double) sellOffer.get(key).get("price");
			int total = (int) sellOffer.get(key).get("total");
			int left = (int) sellOffer.get(key).get("left");
			int claimable = (int) sellOffer.get(key).get("claimable");
			placeholder.put("%price_total%", "" + (double) (total * price));
			placeholder.put("%selling%", "" + (int) total);
			placeholder.put("%sold%", "" + (int) (total - left));
			placeholder.put("%unit_price%", "" + price);
			placeholder.put("%percentage%" , "" + (left == 0 ? 100 : MathsUtils.round(((double) (total-left) / total) * 100, 1)));
			placeholder.put("%coins_to_claim%", "" + (claimable * price));
			
			NBTItem nbt = new NBTItem(ItemStackUtils.replacePlaceholder(is, placeholder));
			nbt.setInteger("category", infos.getFirst());
			nbt.setInteger("show", infos.getSecond());
			nbt.setInteger("sub", infos.getThird());
			nbt.setString("id", key);
			nbt.setString("type", "SELL_OFFER");
			nbt.setDouble("price", price);
			
			mE.addItem(nbt.getItem());
		}
		
		for(String key : buyOrder.keySet()) {
			ImmutableTriplet<Integer, Integer, Integer> infos = Tuple.immutableOf((int) buyOrder.get(key).get("category"),(int)  buyOrder.get(key).get("show"),(int)  buyOrder.get(key).get("sub"));
			Category category = Category.getCategory(infos.getFirst());
			ItemStack is = category.getSubShow()[infos.getSecond() - 1][infos.getThird() - 1].clone();
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(manageBuyOrder.getItemMeta().getDisplayName().replace("%displayname%", im.getDisplayName()));
			im.setLore(manageBuyOrder.getItemMeta().getLore());
			
			is.setItemMeta(im);
			
			CompactMap<String, String> placeholder = new CompactMap<String, String>();
			double price = (double) buyOrder.get(key).get("price");
			int total = (int) buyOrder.get(key).get("total");
			int left = (int) buyOrder.get(key).get("left");
			int claimable = (int) buyOrder.get(key).get("claimable");
			placeholder.put("%price_total%", "" + (double) (total * price));
			placeholder.put("%buying%", "" + (int) total);
			placeholder.put("%bought%", "" + (int) (total - left));
			placeholder.put("%unit_price%", "" + price);
			placeholder.put("%percentage%" , "" + (left == 0 ? 100 : MathsUtils.round(((double) (total-left) / total) * 100, 1)));
			placeholder.put("%items_to_claim%", "" + (int) (claimable));
			
			NBTItem nbt = new NBTItem(ItemStackUtils.replacePlaceholder(is, placeholder));
			nbt.setInteger("category", infos.getFirst());
			nbt.setInteger("show", infos.getSecond());
			nbt.setInteger("sub", infos.getThird());
			nbt.setString("id", key);
			nbt.setString("type", "BUY_ORDER");
			nbt.setDouble("price", price);
			
			mE.addItem(nbt.getItem());
			
			
		}
		
		p.openInventory(mE);
	}
}
