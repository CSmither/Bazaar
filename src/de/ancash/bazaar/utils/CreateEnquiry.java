package de.ancash.bazaar.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CreateEnquiry {

	private Inventory inv;
	private int slot;
	private int item_id;
	private int cat;
	private Player p;
	private String name;
	public Player getPlayer() {return p;}
	public int getSlot() {return slot;}
	public int getItemId() {return item_id;}
	public int getCat() {return cat;}
	public Inventory getInv() {return inv;}
	public String getTitle() {return name;}
	
	public CreateEnquiry(Inventory inv, int selected_slot, int item_id, int cat, Player p, String invname) {
		this.inv = inv;
		this.slot = selected_slot;
		this.item_id = item_id;
		this.p = p;
		this.cat = cat;
		name = invname;
	}
}
