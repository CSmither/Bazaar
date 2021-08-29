package de.ancash.bazaar.sockets.events;

import de.ancash.bazaar.sockets.eqnuiry.Enquiry;
import de.ancash.libs.org.bukkit.event.Event;
import de.ancash.libs.org.bukkit.event.HandlerList;

public class EnquiryCreateEvent extends Event{
	
	private static final HandlerList handlers = new HandlerList();
	
	public static HandlerList getHandlerList() {
        return handlers;
    }
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	private final Enquiry createdEnquiry;
	
	public EnquiryCreateEvent(Enquiry createdEnquiry) {
		this.createdEnquiry = createdEnquiry;
	}
	
	public Enquiry getCreatedEnquiry() {
		return createdEnquiry;
	}
}