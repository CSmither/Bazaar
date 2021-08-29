package de.ancash.bazaar.sockets.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.sockets.packets.BazaarMessagePacket;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.libs.org.bukkit.event.EventHandler;
import de.ancash.libs.org.bukkit.event.Listener;
import de.ancash.sockets.events.ClientPacketReceiveEvent;
import de.ancash.sockets.packet.Packet;

public class PacketListener implements Listener{

	private final Bazaar pl;
	
	public PacketListener(Bazaar pl) {
		this.pl = pl;
	}
	
	@EventHandler
	public void onPacket(ClientPacketReceiveEvent event) {
		Packet packet = event.getPacket();
		if(packet.getSerializable() instanceof BazaarMessagePacket ) {
			BazaarMessagePacket bmp = (BazaarMessagePacket) packet.getSerializable();
			
			if(BazaarMessagePacket.Type.ENQUIRY_FILLED == bmp.getMessageType()) {
				Player player = Bukkit.getPlayer(bmp.getTarget());
				if(player == null) return;
				if(!player.isOnline()) return;
				String msg = null;
				switch (bmp.getType()) {
				case SELL_OFFER:
					msg = pl.getResponse().SELL_OFFER_FILLED;
					break;
				case BUY_ORDER:
					msg = pl.getResponse().BUY_ORDER_FILLED;
					break;
				default:
					break;
				}
				
				player.sendMessage(msg.replace(BazaarPlaceholder.AMOUNT, bmp.getAmount() + "")
						.replace(BazaarPlaceholder.DISPLAY_NAME, Category.getCategory(bmp.getCategory())
								.getSubSubShow(bmp.getSubCategory(), bmp.getSubSubCategory()).getItemMeta().getDisplayName()));
			}
		}
	}
	
}