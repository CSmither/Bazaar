package de.ancash.bazaar.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.ItemStackUtils;

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
			if(ItemStackUtils.isSimilar(is, inv)) {
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
			ItemStack item = p.getInventory().getItem(s);
			if(item == null || !ItemStackUtils.isSimilar(is, item)) continue;
			int dif = i;
			if(dif > item.getAmount()) {
				dif = item.getAmount();
			}
			if(item.getAmount() == dif) {
				p.getInventory().setItem(s, null);
			} else {
				item.setAmount(item.getAmount() - dif);
			}
			i -= dif;
			if(i == 0) break;
		}
	}
	
	public static int getContentAmount(Inventory inv, ItemStack is) {
		int i = 0;
		for(int t = 0; t<inv.getSize(); t++) {
			ItemStack cont = inv.getItem(t);
			if(cont == null || cont.getType().equals(Material.AIR)) 
				continue;
			if(ItemStackUtils.isSimilar(is, cont)) {
				i += cont.getAmount();
			}
		}
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
