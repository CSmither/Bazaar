package de.ancash.bazaar.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

public class SellInstaEvent extends Event{

	private static final HandlerList handlers = new HandlerList();
	Inventory inv;
	int slot;
	int item_id;
	int cat;
	Player p;
	String name;
	public Player getPlayer() {return p;}
	public int getSlot() {return slot;}
	public int getItemId() {return item_id;}
	public int getCat() {return cat;}
	public Inventory getInv() {return inv;}
	public String getTitle() {return name;}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public SellInstaEvent(Inventory inv, int selected_slot, int item_id, int cat, Player p, String invname) {
		this.inv = inv;
		this.slot = selected_slot;
		this.item_id = item_id;
		this.p = p;
		this.cat = cat;
		name = invname;
	}
	
}
