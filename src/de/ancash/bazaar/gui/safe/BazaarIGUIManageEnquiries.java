package de.ancash.bazaar.gui.safe;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.simpleyaml.configuration.file.YamlFile;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.Enquiry;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.management.Enquiry.EnquiryType;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.bazaar.utils.MathsUtils;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.datastructures.tuples.Triplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.inventory.Clickable;

enum BazaarIGUIManageEnquiries {
	
	INSTANCE;
	
	private String TITLE;
	private ItemStack SELL_OFFER_TEMPLATE;
	private ItemStack BUY_ORDER_TEMPLATE;
	
	void load(Bazaar pl) {
		TITLE = pl.getInvConfig().getString("inventory.manage.title");
		SELL_OFFER_TEMPLATE = ItemStackUtils.get(pl.getInvConfig(), "inventory.manage.sell-offer");
		BUY_ORDER_TEMPLATE = ItemStackUtils.get(pl.getInvConfig(), "inventory.manage.buy-order");
	}
	
	public void manageEnquiries(BazaarIGUI igui) {
		igui.newInventory(TITLE, 45);
		igui.clearInventoryItems();
		igui.setBackground(IntStream.range(0, 9).toArray());
		igui.setBackground(IntStream.range(36, 45).toArray());
		igui.setCloseItem(40);
		Player player = Bukkit.getPlayer(igui.getId());
		Map<String, Map<String, Number>> sellOffer = PlayerManager.get(player.getUniqueId()).getSellOffer();
		Map<String, Map<String, Number>> buyOrder = PlayerManager.get(player.getUniqueId()).getBuyOrder();
		
		int slot = 9;
		
		for(String key : sellOffer.keySet()) {
			final Triplet<Integer, Integer, Integer> infos = Tuple.of((int) sellOffer.get(key).get("category"),(int)  sellOffer.get(key).get("show"),(int)  sellOffer.get(key).get("sub"));
			Category category = Category.getCategory(infos.getFirst());
			ItemStack is = category.getSubSub()[infos.getSecond()- 1][infos.getThird()- 1].clone();
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(SELL_OFFER_TEMPLATE.getItemMeta().getDisplayName().replace("%displayname%", im.getDisplayName()));
			im.setLore(SELL_OFFER_TEMPLATE.getItemMeta().getLore());
			
			is.setItemMeta(im);
			
			CompactMap<String, String> placeholder = new CompactMap<String, String>();
			final double price = (double) sellOffer.get(key).get("price");
			final int total = (int) sellOffer.get(key).get("total");
			final int left = (int) sellOffer.get(key).get("left");
			final int claimable = (int) sellOffer.get(key).get("claimable");

			placeholder.put("%price_total%", "" + (double) (total * price));
			placeholder.put("%selling%", "" + (int) total);
			placeholder.put("%sold%", "" + (int) (total - left));
			placeholder.put("%unit_price%", "" + price);
			placeholder.put("%percentage%" , "" + (left == 0 ? 100 : MathsUtils.round(((double) (total-left) / total) * 100, 1)));
			placeholder.put("%coins_to_claim%", "" + (claimable * price));
			
			igui.add(ItemStackUtils.replacePlaceholder(is, placeholder), slot, new Clickable() {
				
				@Override
				public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
					collectFromFile(igui, price, category.getCategory(), infos.getSecond(), infos.getThird(), key, EnquiryType.SELL_OFFER);
				}
			});
			slot++;
		}
		
		for(String key : buyOrder.keySet()) {
			Triplet<Integer, Integer, Integer> infos = Tuple.of((int) buyOrder.get(key).get("category"),(int)  buyOrder.get(key).get("show"),(int)  buyOrder.get(key).get("sub"));
			Category category = Category.getCategory(infos.getFirst());
			ItemStack is = category.getSubSub()[infos.getSecond()- 1][infos.getThird()- 1].clone();
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(BUY_ORDER_TEMPLATE.getItemMeta().getDisplayName().replace("%displayname%", im.getDisplayName()));
			im.setLore(BUY_ORDER_TEMPLATE.getItemMeta().getLore());
			
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
			
			igui.add(ItemStackUtils.replacePlaceholder(is, placeholder), slot, new Clickable() {
	
				@Override
				public void onClick(int slot, boolean shift, InventoryAction action, boolean topInventory) {
					collectFromFile(igui, price, category.getCategory(), infos.getSecond(), infos.getThird(), key, EnquiryType.BUY_ORDER);
				}
			});
			slot++;
		}
	}
	
	private void collectFromFile(BazaarIGUI igui, double price, int category, int show, int sub, String uuid, EnquiryType type) {
		Bazaar pl = igui.plugin;
		Player player = Bukkit.getPlayer(igui.getId());
		File f = new File("plugins/Bazaar/player/" + player.getUniqueId().toString() + "/" + (type.equals(EnquiryType.BUY_ORDER) ? "buy_order.yml" : "sell_offer.yml"));
		YamlFile fc = pl.getEnquiryUtils().getYamlFile(f);
		if(fc.getInt(uuid + ".claimable") == 0) {
			player.sendMessage(pl.getResponse().NOTHIN_TO_CLAIM);
			manageEnquiries(igui);
			return;
		}
		int claimable = -1;
		switch (type) {
		case SELL_OFFER:
			claimable = fc.getInt(uuid + ".claimable");
			double coins = MathsUtils.round(claimable * price * (1D - (double) igui.plugin.getTax() / 100D), 1);
			pl.getEconomy().depositPlayer(player, coins);
			player.sendMessage("§6Bazaar! §7Claimed §6" + coins + " coins §7from selling §a" + claimable + "§7x " + Category.getCategory(category).getSubSub()[show - 1][sub - 1].getItemMeta().getDisplayName() + " §7at §6" + price + " §7each!");
			fc.set(uuid + ".claimable", 0);
			fc.set(uuid + ".lastEdit", System.currentTimeMillis());
			SelfBalancingBSTNode node = Category.getCategory(category).get(EnquiryType.SELL_OFFER, show, sub, price);
			if(node != null && node.contains(UUID.fromString(uuid))){
				Enquiry e = node.get(UUID.fromString(uuid));
				if(e.getLeft() == 0) {
					pl.getEnquiryUtils().deleteEnquiry(e);
					return;
				}
				e.setClaimable(0);
				e.setLastEdit(fc.getLong(uuid + ".lastEdit"));
			}
			if(fc.getInt(uuid + ".left") == 0 && fc.getInt(uuid + ".claimable") == 0) {
				fc.set(uuid, null);
			}
			break;
		case BUY_ORDER:
			int freeSlots = InventoryUtils.getFreeSlots(player.getInventory());
			if(freeSlots == 0) {
				player.sendMessage(pl.getResponse().INVENTORY_FULL);
				manageEnquiries(igui);
				return;
			}
			claimable = fc.getInt(uuid + ".claimable");
			int adding = 0;
			node = Category.getCategory(category).get(EnquiryType.BUY_ORDER, show, sub, price);
			Enquiry e = null;
			if(node != null && node.contains(UUID.fromString(uuid)))
				e = node.get(UUID.fromString(uuid));
			long now = System.currentTimeMillis();
			if(claimable > freeSlots*Category.getCategory(category).getOriginal(show, sub).getMaxStackSize()) {
				adding = freeSlots*Category.getCategory(category).getOriginal(show, sub).getMaxStackSize();
				int temp = claimable;
				fc.set(uuid + ".claimable", temp - adding);
			} else {
				fc.set(uuid + ".claimable", 0);
				adding = claimable;
			}
			fc.set(uuid + ".lastEdit", now);
			if(e != null) {
				e.setClaimable(claimable - adding);
				e.setLastEdit(now);
			}
			if((fc.getInt(uuid + ".left") == 0 || (e != null && e.getLeft() == 0)) && fc.getInt(uuid + ".claimable") == 0) {
				if(e != null)
					pl.getEnquiryUtils().deleteEnquiry(e);
				fc.set(uuid, null);
			}
			if(fc.getInt(uuid + ".left") == 0 && fc.getInt(uuid + ".claimable") == 0) pl.getEnquiryUtils().delete(player.getUniqueId(), uuid, type);
			InventoryUtils.addItemAmount(adding, Category.getCategory(category).getOriginal(show, sub), player);
			player.sendMessage("§6Bazaar! §7Claimed §a" + adding + "§7x " + Category.getCategory(category).getSubSub()[show - 1][sub - 1].getItemMeta().getDisplayName() + " §7worth §6" + (adding * price) + " coins §7bought for §6" + price + " §7each!");
			break;
		default:
			break;
		}
		manageEnquiries(igui);
		return;
	}
}
