package de.ancash.bazaar.gui;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.AbstractBazaarManageEnquiriesInv;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.Enquiry;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.management.Enquiry.EnquiryType;
import de.ancash.minecraft.InventoryUtils;
import de.ancash.bazaar.utils.MathsUtils;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;

public class BazaarManageEnquiriesImpl extends AbstractBazaarManageEnquiriesInv{
	
	public BazaarManageEnquiriesImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public Duplet<Map<String, Map<String, Number>>, Map<String, Map<String, Number>>> getPlaceholder(AbstractBazaarIGUI igui) {
		return Tuple.of(PlayerManager.get(igui.getId()).getSellOffer(), PlayerManager.get(igui.getId()).getBuyOrder());
	}

	@Override
	public void collect(AbstractBazaarIGUI igui, double price, int category, int sub, int subsub, String uuid,	EnquiryType type) {
		synchronized (igui.getPlugin().getEnquiryUtils().getLock()) {
			Bazaar pl = igui.getPlugin();
			Player player = Bukkit.getPlayer(igui.getId());
			File f = new File("plugins/Bazaar/player/" + player.getUniqueId().toString() + "/" + (type.equals(EnquiryType.BUY_ORDER) ? "buy_order.yml" : "sell_offer.yml"));
			YamlFile fc = pl.getEnquiryUtils().getYamlFile(f);
			if(fc.getInt(uuid + ".claimable") == 0) {
				player.sendMessage(pl.getResponse().NOTHIN_TO_CLAIM);
				super.manageEnquiries(igui);
				return;
			}
			int claimable = -1;
			switch (type) {
			case SELL_OFFER:
				claimable = fc.getInt(uuid + ".claimable");
				double coins = MathsUtils.round(claimable * price * (1D - (double) pl.getTax() / 100D), 1);
				pl.getEconomy().depositPlayer(player, coins);
				player.sendMessage("§6Bazaar! §7Claimed §6" + coins + " coins §7from selling §a" + claimable + "§7x " + Category.getCategory(category).getSubSubShow(sub, subsub).getItemMeta().getDisplayName() + " §7at §6" + price + " §7each!");
				fc.set(uuid + ".claimable", 0);
				fc.set(uuid + ".lastEdit", System.currentTimeMillis());
				SelfBalancingBSTNode node = Category.getCategory(category).get(EnquiryType.SELL_OFFER, sub, subsub, price);
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
				node = Category.getCategory(category).get(EnquiryType.BUY_ORDER, sub, subsub, price);
				Enquiry e = null;
				if(node != null && node.contains(UUID.fromString(uuid)))
					e = node.get(UUID.fromString(uuid));
				long now = System.currentTimeMillis();
				if(claimable > freeSlots*Category.getCategory(category).getOriginal(sub, subsub).getMaxStackSize()) {
					adding = freeSlots*Category.getCategory(category).getOriginal(sub, subsub).getMaxStackSize();
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
				InventoryUtils.addItemAmount(adding, Category.getCategory(category).getOriginal(sub, subsub), player);
				player.sendMessage("§6Bazaar! §7Claimed §a" + adding + "§7x " + Category.getCategory(category).getSubSubShow(sub, subsub).getItemMeta().getDisplayName() + " §7worth §6" + (adding * price) + " coins §7bought for §6" + price + " §7each!");
				break;
			default:
				break;
			}
			super.manageEnquiries(igui);
			return;
		}
	}
}
