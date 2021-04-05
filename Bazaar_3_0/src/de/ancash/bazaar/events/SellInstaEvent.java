package de.ancash.bazaar.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

public class SellInstaEvent extends Event{

	private static final HandlerList handlers = new HandlerList();
	Inventory inv;
	int slot;
	int show;
	int sub;
	int cat;
	Player p;
	String name;
	
	public Player getPlayer() {return p;}
	public int getSlot() {return slot;}
	public int getShow() {return show;}
	public int getSub() {return sub;}
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

	public SellInstaEvent(Inventory inv, int selected_slot, int show, int sub, int cat, Player p, String invname) {
		this.inv = inv;
		this.slot = selected_slot;
		this.show = show;
		this.sub = sub;
		this.p = p;
		this.cat = cat;
		name = invname;
	}
	
}
