package de.ancash.bazaar.sockets;

import java.util.Optional;

import org.bukkit.Bukkit;

import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.sockets.packets.BazaarHeader;
import de.ancash.bazaar.sockets.packets.BazaarMessagePacket;
import de.ancash.libs.org.bukkit.event.EventHandler;
import de.ancash.libs.org.bukkit.event.Listener;
import de.ancash.sockets.events.ClientPacketReceiveEvent;
import de.ancash.sockets.packet.Packet;

public class ChatClientPacketReceiveListener implements Listener{

	@EventHandler
	public void onPacket(ClientPacketReceiveEvent event) {
		Packet packet = event.getPacket();
		if(packet.getHeader() == BazaarHeader.MESSAGE.getHeader()) {
			BazaarMessagePacket bmp = (BazaarMessagePacket) packet.getSerializable();
			if(bmp.broadcast()) {
				Bukkit.broadcastMessage(bmp.getString());
			}
			if(bmp.sendToPlayer()) {
				Optional.ofNullable(Bukkit.getPlayer(bmp.getTarget())).ifPresent(player -> {
					if(bmp.getCategory() != -1) {
						player.sendMessage(bmp.getString().replace("%displayname%", Category.getCategory(bmp.getCategory()).getSubSub()[bmp.getSub() - 1][bmp.getSubSub() - 1].getItemMeta().getDisplayName()));
					} else {
						player.sendMessage(bmp.getString());
					}
				});
			}
		}
	}
	
}
