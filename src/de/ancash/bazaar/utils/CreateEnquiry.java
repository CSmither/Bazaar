package de.ancash.bazaar.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CreateEnquiry {

	private Inventory inv;
	private int slot;
	private int show;
	private int sub;
	private int cat;
	private Player p;
	private String name;
	public Player getPlayer() {return p;}
	public int getSlot() {return slot;}
	public int getShow() {return show;}
	public int getSub() {return sub;}
	public int getCat() {return cat;}
	public Inventory getInv() {return inv;}
	public String getTitle() {return name;}
	
	public CreateEnquiry(Inventory inv, int selected_slot, int show, int sub, int cat, Player p, String invname) {
		this.inv = inv;
		this.slot = selected_slot;
		this.show = show;
		this.sub = sub;
		this.p = p;
		this.cat = cat;
		name = invname;
	}
}
