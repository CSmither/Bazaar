package de.ancash.bazaar.sockets.listeners;

import de.ancash.Sockets;
import de.ancash.bazaar.sockets.eqnuiry.Enquiry;
import de.ancash.bazaar.sockets.events.EnquiryCreateEvent;
import de.ancash.bazaar.sockets.events.EnquiryFillEvent;
import de.ancash.bazaar.sockets.packets.BazaarMessagePacket;
import de.ancash.libs.org.bukkit.event.EventHandler;
import de.ancash.libs.org.bukkit.event.Listener;

public class EnquiryListener implements Listener{

	@EventHandler
	public void onEnquiryFill(EnquiryFillEvent event) {
		Sockets.writeAll(addAll(new BazaarMessagePacket(), event.getFilledEnquiry()).getPacket());
	}

	@EventHandler
	public void onEnquiryCreate(EnquiryCreateEvent event) {
		Sockets.writeAll(addAll(new BazaarMessagePacket(), event.getCreatedEnquiry()).getPacket());
	}
	
	private BazaarMessagePacket addAll(BazaarMessagePacket messagePacket, Enquiry enquiry) {
		messagePacket.setAmount(enquiry.getAmount());
		messagePacket.setCategory(enquiry.getCategory());
		messagePacket.setTarget(enquiry.getOwner());
		messagePacket.setPrice(enquiry.getPrice());
		messagePacket.setSubCategory(enquiry.getSubCategory());
		messagePacket.setSubSubCategory(enquiry.getSubSubCategory());
		messagePacket.setType(enquiry.getType());
		return messagePacket;
	}
}