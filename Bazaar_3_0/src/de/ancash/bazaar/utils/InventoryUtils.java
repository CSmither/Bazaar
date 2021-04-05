package de.ancash.bazaar.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.ancash.ilibrary.datastructures.tuples.Duplet;
import de.ancash.ilibrary.datastructures.tuples.Tuple;

public class InventoryUtils {

	public static int getFreeSlots(Player p) {
		int free = 0;
		for(int i = 0; i<36; i++) {
			if(p.getInventory().getItem(i) == null || p.getInventory().getItem(i).getType().equals(Material.AIR)) free++;
		}
		return free;
	}
	
	public static void addItemAmount(int i, ItemStack is, Player p) {
		for(int s = 0; s<p.getInventory().getSize(); s++) {
			if(i == 0) return;
			ItemStack inv = p.getInventory().getItem(s);
			if(ItemStackUtils.itemStackToString(inv).equals(ItemStackUtils.itemStackToString(is))) {
				if(inv.getAmount() == 64) continue;
				int canAdd= 64 - inv.getAmount();
				if(canAdd >= i) {
					inv.setAmount(inv.getAmount() + i);
					return;
				}
				if(canAdd < i) {
					inv.setAmount(64);
					i = i - canAdd;
					continue;
				}
				
			}
			if(inv == null || inv.getType().equals(Material.AIR)) {
				if(i >= 64) {
					ItemStack t = is.clone();
					t.setAmount(64);
					p.getInventory().addItem(t);
					i = i - 64;
					continue;
				}
				ItemStack t = is.clone();
				t.setAmount(i);
				p.getInventory().addItem(t);
				return;
			}
		}
	}
	
	public static void removeItemAmount(int i, ItemStack is, Player p) {
		for(int s = 0; s<p.getInventory().getSize(); s++) {
			if(ItemStackUtils.itemStackToString(is).equals(ItemStackUtils.itemStackToString(p.getInventory().getItem(s)))) {
				while(i > 0 && p.getInventory().getItem(s) != null && p.getInventory().getItem(s).getAmount() > 0) {
					ItemStack cont = p.getInventory().getItem(s);
					if(cont.getAmount() == 1) {
						p.getInventory().setItem(s, null);
					} else {
						cont.setAmount(cont.getAmount() - 1);
					}
					i--;
				}
				if(i == 0) break;
			}
		}
	}
	
	public static int getContentAmount(Inventory inv, ItemStack is) {
		int i = 0;
		for(int t = 0; t<inv.getSize(); t++) {
			ItemStack cont = inv.getItem(t);
			if(cont == null || cont.getType().equals(Material.AIR)) continue;
			if(ItemStackUtils.itemStackToString(is).equals(ItemStackUtils.itemStackToString(cont))) i = i + cont.getAmount();
		}
		/*for(ItemStack cont : inv.getContents()) {
			if(cont == null) continue;
			if(ItemStackUtils.itemStackToString(is).equals(ItemStackUtils.itemStackToString(cont))) i = i + cont.getAmount();
		}*/
		return i;
	}
	
	public static Duplet<Double, String> getSpread(double minSellOfferPrice, double maxBuyOrderPrice, int percentage) {
		StringBuilder builder = new StringBuilder();
		double spread = MathsUtils.round(minSellOfferPrice - maxBuyOrderPrice, 2);
		builder.append("§6" + minSellOfferPrice + " §7- §6" + maxBuyOrderPrice + " §7= §6" + spread + " coins");
		Duplet<Double, String> pair = Tuple.of(spread*((double) percentage/100), builder.toString());
		return pair;
	}
}
