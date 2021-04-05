package de.ancash.bazaar;
import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.bazaar.files.Files;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.sockets.EnquiryUpdater;
import de.ancash.bazaar.sockets.PacketBuilder;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Enquiry;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.bazaar.utils.Enquiry.EnquiryTypes;
import de.ancash.ilibrary.ILibrary;
import de.ancash.ilibrary.sockets.NIOClient;
import de.ancash.ilibrary.sockets.Packet;
import de.ancash.ilibrary.yaml.configuration.file.YamlFile;

public class BazaarManager extends JavaPlugin{

	private static NIOClient client = null;
	
	public boolean canSendPackets() {
		return (client != null && client.isActive()) || ILibrary.getInstance().isChatClientRunning();
	}
	
	public boolean sendPacket(Packet packet) {
		if(client == null && !ILibrary.getInstance().isChatClientRunning()) return false;
		if(client != null && client.isActive()) {
			client.send(packet);
			return true;
		}
		if(ILibrary.getInstance().isChatClientRunning()) {
			ILibrary.getInstance().sendPacket(packet);
			return true;
		}
		return false;
	}
	
	public void addToSend(Packet p) {
		toSend.add(p);
	}
	
	
	private Queue<Packet> toSend = new LinkedBlockingDeque<>();
	
	protected void setupMultipleServersSupport() {
		Chat.sendMessage("Setting up multiple servers support!", ChatLevel.INFO);
    	if(ILibrary.getInstance().isDefaultSocketRunning()) {
    		Chat.sendMessage("Active Server Socket running on this server!", ChatLevel.INFO);
    	} else {
    		Chat.sendMessage("No Server Socket running on this Sever!", ChatLevel.INFO);
    	}
    	if(ILibrary.getInstance().isChatClientRunning()) {
    		Chat.sendMessage("Chat Client running on this Server!", ChatLevel.INFO);
    	} else {
    		Chat.sendMessage("No Chat Client running on this server! Starting new one...", ChatLevel.INFO);
    		client = new NIOClient(Files.getConfig().getString("address"), Files.getConfig().getInt("port"), this.getClass().getName());
    	}
    	new BukkitRunnable() {
			
			@Override
			public void run() {
				if(client != null && client.isActive()) {
					Chat.sendMessage("Successfully started new Chat Client!", ChatLevel.INFO);
				}
				if((client != null && client.isActive()) || ILibrary.getInstance().isChatClientRunning()) {
					Chat.sendMessage("Pushing everything...", ChatLevel.INFO);
					try {
						EnquiryUpdater.pushAll();
					} catch (de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException | IOException e) {
						e.printStackTrace();
					}
					Chat.sendMessage("Fetching everything...", ChatLevel.INFO);
					if(client != null && client.isActive()) {
						client.send(PacketBuilder.fetchAll().getPacket());
					} else {
						ILibrary.getInstance().sendPacket(PacketBuilder.fetchAll().getPacket());
					}
				}
			}
    	}.runTaskAsynchronously(this);
    	
    	startSendingTaskAsync();
		
		startLoopingSendingTaskAsync();
	}
	
	private void startLoopingSendingTaskAsync() {
		new BukkitRunnable() {
			
			final File playerDir = new File("plugins/Bazaar/player");
			
			@Override
			public void run() {
				try {
					if(playerDir.exists() && playerDir.listFiles().length != 0) {
						for(File playerFile : playerDir.listFiles()) {
							File sellOffer = new File(playerFile.getPath() + "/sell_offer.yml");
							File buyOrder = new File(playerFile.getPath() + "/buy_order.yml");
							try {
								new PlayerManager(UUID.fromString(playerFile.getName()));
							} catch (de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException | IOException e) {
								e.printStackTrace();
							}
							YamlFile yamlSellOffer = Enquiry.getYamlFile(sellOffer);
							
							if(yamlSellOffer.getKeys(false).size() != 0) {
								for(String key : yamlSellOffer.getKeys(false)) {
									if(yamlSellOffer.getString(key) != null && yamlSellOffer.getString(key).equals("null")) {
										Bazaar.getInstance().sendPacket(PacketBuilder.newPacketBuilder("delete")
												.setID(UUID.fromString(key))
												.setOwner(UUID.fromString(playerFile.getName()))
												.setEnquiryType(EnquiryTypes.SELL_OFFER)
												.getPacket());
										continue;
									}
									Bazaar.getInstance().sendPacket(get(yamlSellOffer, key, UUID.fromString(playerFile.getName())));
									Thread.sleep(20);
								}
							}
							
							YamlFile yamlBuyOrder = Enquiry.getYamlFile(buyOrder);
							
							if(yamlBuyOrder.getKeys(false).size() != 0) {
								for(String key : yamlBuyOrder.getKeys(false)) {
									if(yamlBuyOrder.getString(key) != null && yamlBuyOrder.getString(key).equals("null")) {
										Bazaar.getInstance().sendPacket(PacketBuilder.newPacketBuilder("delete")
												.setID(UUID.fromString(key))
												.setOwner(UUID.fromString(playerFile.getName()))
												.setEnquiryType(EnquiryTypes.BUY_ORDER)
												.getPacket());
										continue;
									}
									Bazaar.getInstance().sendPacket(get(yamlBuyOrder, key, UUID.fromString(playerFile.getName())));
									Thread.sleep(20);
								}
							}
							
						}
					}
				} catch(Exception ex) {}
			}
			
			public Packet get(YamlFile yamlFile, String key, UUID owner) {
				return PacketBuilder.newPacketBuilder("update")
						.setAmount(yamlFile.getInt(key + ".amount"))
						.setCategory(yamlFile.getInt(key + ".category"))
						.setClaimable(yamlFile.getInt(key + ".claimable"))
						.setEnquiryType(EnquiryTypes.BUY_ORDER)
						.setID(UUID.fromString(key))
						.setLastEdit(yamlFile.getLong(key + ".lastEdit"))
						.setLeft(yamlFile.getInt(key + ".left"))
						.setOwner(owner)
						.setPrice(yamlFile.getDouble(key + ".price"))
						.setShow(yamlFile.getInt(key + ".show"))
						.setSub(yamlFile.getInt(key + ".sub"))
						.setTimeStamp(yamlFile.getLong(key + ".timestamp"))
						.getPacket();
			}
		}.runTaskTimerAsynchronously(Bazaar.getInstance(), 100, 1);
	}

	private void startSendingTaskAsync() {
		new BukkitRunnable() {
    		
    		@Override
			public void run() {
				if(!toSend.isEmpty()) {
					final Packet p = toSend.remove();
					for(int i = 0; i<10; i++)
						sendPacket(p);
				}
			}
		}.runTaskTimerAsynchronously(Bazaar.getInstance(), 100, 1);
	}
}
