package de.ancash.bazaar.gui.inventory;

import java.util.UUID;

public abstract class BazaarRunnable implements Runnable{

	private final UUID uuid;
	
	public BazaarRunnable(UUID player) {
		this.uuid = player;
	}

	public UUID getUUID() {
		return uuid;
	}
	
}