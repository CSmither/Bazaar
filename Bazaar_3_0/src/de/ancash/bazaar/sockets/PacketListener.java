package de.ancash.bazaar.sockets;

import java.io.IOException;

import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.Bazaar;
import de.ancash.ilibrary.events.IEventHandler;
import de.ancash.ilibrary.events.IListener;
import de.ancash.ilibrary.events.events.ChatClientPacketReceiveEvent;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

public class PacketListener implements IListener{

	@IEventHandler
	public void onPacketReceive(final ChatClientPacketReceiveEvent e) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if(!((String)e.getPacket().getObject()).contains("Bazaar")) return;
				try {
					new EnquiryUpdater((String) e.getPacket().getObject()).process();
				} catch (InvalidConfigurationException | IOException e1) {
					
				};
			}
		}.runTaskAsynchronously(Bazaar.getInstance());
	} 
}
