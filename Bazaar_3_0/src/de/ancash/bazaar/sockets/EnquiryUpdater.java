package de.ancash.bazaar.sockets;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.utils.BuyOrder;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Enquiry;
import de.ancash.bazaar.utils.Enquiry.EnquiryTypes;
import de.ancash.bazaar.utils.SellOffer;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.ilibrary.yaml.configuration.file.YamlFile;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

public class EnquiryUpdater {

	private String updateType;
	private UUID owner;
	private UUID id;
	private int category;
	private int claimable;
	private int show;
	private int sub;
	private double price;
	private int amount;
	private int left;
	private long timeStamp;
	private long lastEdit;
	private EnquiryTypes type;
	
	public EnquiryUpdater(String from) {
		for(String str : from.split(" -")) {
			if(str.split(" ").length != 2) continue;
					
			String tag = str.split(" ")[0];
			String value = str.split(" ")[1];
						
			switch (tag) {
			case "u":
				updateType = (String) value;
				break;
			case "o":
				owner = UUID.fromString(value);
				break;
			case "i":
				id = UUID.fromString(value);
				break;
			case "C":
				category = Integer.valueOf(value);
				break;
			case "c":
				claimable = Integer.valueOf(value);
				break;
			case "S":
				show = Integer.valueOf(value);
				break;
			case "s":
				sub = Integer.valueOf(value);
				break;
			case "p":
				price = Double.valueOf(value);
				break;
			case "a":
				amount = Integer.valueOf(value);
				break;
			case "l":
				left = Integer.valueOf(value);
				break;
			case "T":
				timeStamp = Long.valueOf(value);
				break;
			case "t":
				lastEdit = Long.valueOf(value);
				break;
			case "e":
				type = EnquiryTypes.valueOf(value);
				break;
			default:
				break;
			}
		}
	}

	public String getUpdateType() {
		return updateType;
	}

	public UUID getOwner() {
		return owner;
	}

	public UUID getID() {
		return id;
	}

	public int getCategory() {
		return category;
	}

	public int getClaimable() {
		return claimable;
	}

	public int getShow() {
		return show;
	}

	public int getSub() {
		return sub;
	}

	public double getPrice() {
		return price;
	}

	public int getAmount() {
		return amount;
	}

	public int getLeft() {
		return left;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
	public EnquiryTypes getType() {
		return type;
	}
	
	public void process() throws InvalidConfigurationException, IOException {
		if(updateType == null) return;
		switch (updateType.toLowerCase()) {
		case "fetch":
			pushAll();
			break;
		case "update":
			updateEnquiry();
			break;
		case "delete":
			delete(owner, id, type.equals(EnquiryTypes.BUY_ORDER) ? "buy_order.yml" : "sell_offer.yml");
			break;
		case "create":
			create();
			break;
		default:
			break;
		}
	}
	
	void delete(UUID owner, UUID id, String file) {
		try {
			Enquiry.getYamlFile(new File("plugins/Bazaar/player/" + owner + "/" + file)).set(id.toString(), "null");
			if(getType() == EnquiryTypes.SELL_OFFER) {
				SelfBalancingBST tree = Category.getCategory(category).getSellOffers(show, sub);
				SelfBalancingBSTNode node = tree.get(price, tree.getRoot());
				if(node != null && node.contains(id)) {
					node.remove(id);
				}
			}
			if(getType() == EnquiryTypes.BUY_ORDER) {
				SelfBalancingBST tree = Category.getCategory(category).getBuyOrders(show, sub);
				SelfBalancingBSTNode node = tree.get(price, tree.getRoot());
				if(node != null && node.contains(id)) {
					node.remove(id);
				}
			}
		} catch(Exception e) {}
	}
	
	void updateEnquiry() throws InvalidConfigurationException, IOException {
		new PlayerManager(getOwner());
		if(!Category.exists(getCategory()) || getCategory() == 0) {
			return;
		}
		Category cat = Category.getCategory(getCategory());
		if(type == EnquiryTypes.BUY_ORDER) {
			File file = new File("plugins/Bazaar/player/" + getOwner().toString() + "/buy_order.yml");
			YamlFile yamlFile = Enquiry.getYamlFile(file);

			if(yamlFile.getString(getID().toString()) != null && yamlFile.getString(getID().toString()).equals("null")) return;
			
			if(!yamlFile.contains(getID().toString()) && (getLeft() == 0 || getClaimable() == 0)) {
				create();
				return;
			}
			
			if(yamlFile.getLong(getID().toString() + ".timestamp") == 0 && left == 0) {
				yamlFile.set(getID().toString(), "null");
				return;
			}

			if(yamlFile.getLong(getID() + ".lastEdit") >= lastEdit) {
				return;
			}
			
			yamlFile.set(getID() + ".left", getLeft());
			yamlFile.set(getID() + ".lastEdit", lastEdit);
			yamlFile.set(getID() + ".claimable", claimable);
			SelfBalancingBST tree = cat.getBuyOrders(show, sub);
			SelfBalancingBSTNode node = tree.get(price, tree.getRoot());
			if(node != null && node.contains(getID())) {
				Enquiry e = node.get(getID());
				e.setLeft(left);
				e.setLastEdit(lastEdit);
				e.setClaimable(claimable);
				Enquiry.saveAll(e);
				if(getLeft() == 0 && getClaimable() == 0) {
					yamlFile.set(getID().toString(), "null");
					Enquiry.deleteEnquiry(e);
					return;
				}
			}
			if(getLeft() == 0 && getClaimable() == 0) {
				yamlFile.set(getID().toString(), "null");
				delete(getOwner(), getID(), null);
				return;
			}
		}
		
		if(type == EnquiryTypes.SELL_OFFER) {
			File file = new File("plugins/Bazaar/player/" + getOwner().toString() + "/sell_offer.yml");
			YamlFile yamlFile = Enquiry.getYamlFile(file);

			if(yamlFile.getString(getID().toString()) != null && yamlFile.getString(getID().toString()).equals("null")) return;
			
			if(!yamlFile.contains(getID().toString()) && (getLeft() == 0 || getClaimable() == 0)) {
				create();
				return;
			}
			
			if(yamlFile.getLong(getID().toString() + ".timestamp") == 0 && left == 0) {
				yamlFile.set(getID().toString(), "null");
				return;
			}
			
			if(yamlFile.getLong(getID() + ".lastEdit") >= lastEdit) {
				return;
			}
			
			yamlFile.set(getID() + ".left", getLeft());
			yamlFile.set(getID() + ".lastEdit", lastEdit);
			yamlFile.set(getID() + ".claimable", claimable);
			SelfBalancingBST tree = cat.getBuyOrders(show, sub);
			SelfBalancingBSTNode node = tree.get(price, tree.getRoot());
			if(node != null && node.contains(getID())) {
				Enquiry e = node.get(getID());
				e.setLeft(left);
				e.setLastEdit(lastEdit);
				e.setClaimable(claimable);
				Enquiry.saveAll(e);
				if(getLeft() == 0 && getClaimable() == 0) {
					yamlFile.set(getID().toString(), "null");
					Enquiry.deleteEnquiry(e);
					return;
				}
			}
			if(getLeft() == 0 && getClaimable() == 0) {
				yamlFile.set(getID().toString(), "null");
				delete(getOwner(), getID(), null);
				return;
			}
		}
	}
	
	public Enquiry create() {
		if(getCategory() == 0 || (getLeft() == 0 && getClaimable() == 0)) return null;
		
		Enquiry e = null;
		if(type == EnquiryTypes.BUY_ORDER) {
			e = new BuyOrder(amount, price, owner, category, show, sub, left, getTimeStamp(), id, claimable, lastEdit);
			Category.getCategory(e.getCategory()).getBuyOrders(e.getShow(), e.getSub()).insert(e.getPrice(), e);
		}
		if(type == EnquiryTypes.SELL_OFFER) {
			e = new SellOffer(amount, price, owner, category, show, sub, left, getTimeStamp(), id, claimable, lastEdit);
			Category.getCategory(e.getCategory()).getSellOffers(e.getShow(), e.getSub()).insert(e.getPrice(), e);
		}
		Enquiry.saveAll(e);
		return e;
	}
	
	public static void pushAll() throws InvalidConfigurationException, IOException {
		if(!Bazaar.getInstance().canSendPackets()) {
			//Chat.sendMessage("Error while sending Packets!", ChatLevel.WARN);
			return;
		}
		Chat.sendMessage("Pushing everything...", ChatLevel.INFO);
		int cnt = 0;
		File playerDir = new File("plugins/Bazaar/player");
		if(playerDir.exists() && playerDir.listFiles().length != 0) {
			for(File playerFile : playerDir.listFiles()) {
				File sellOffer = new File(playerFile.getPath() + "/sell_offer.yml");
				File buyOrder = new File(playerFile.getPath() + "/buy_order.yml");
				new PlayerManager(UUID.fromString(playerFile.getName()));
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
						Bazaar.getInstance().sendPacket(PacketBuilder.newPacketBuilder("update")
								.setAmount(yamlSellOffer.getInt(key + ".amount"))
								.setCategory(yamlSellOffer.getInt(key + ".category"))
								.setClaimable(yamlSellOffer.getInt(key + ".claimable"))
								.setEnquiryType(EnquiryTypes.SELL_OFFER)
								.setID(UUID.fromString(key))
								.setLastEdit(yamlSellOffer.getLong(key + ".lastEdit"))
								.setLeft(yamlSellOffer.getInt(key + ".left"))
								.setOwner(UUID.fromString(playerFile.getName()))
								.setPrice(yamlSellOffer.getDouble(key + ".price"))
								.setShow(yamlSellOffer.getInt(key + ".show"))
								.setSub(yamlSellOffer.getInt(key + ".sub"))
								.setTimeStamp(yamlSellOffer.getLong(key + ".timestamp"))
								.getPacket());
						cnt++;
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
						Bazaar.getInstance().sendPacket(PacketBuilder.newPacketBuilder("update")
								.setAmount(yamlBuyOrder.getInt(key + ".amount"))
								.setCategory(yamlBuyOrder.getInt(key + ".category"))
								.setClaimable(yamlBuyOrder.getInt(key + ".claimable"))
								.setEnquiryType(EnquiryTypes.BUY_ORDER)
								.setID(UUID.fromString(key))
								.setLastEdit(yamlBuyOrder.getLong(key + ".lastEdit"))
								.setLeft(yamlBuyOrder.getInt(key + ".left"))
								.setOwner(UUID.fromString(playerFile.getName()))
								.setPrice(yamlBuyOrder.getDouble(key + ".price"))
								.setShow(yamlBuyOrder.getInt(key + ".show"))
								.setSub(yamlBuyOrder.getInt(key + ".sub"))
								.setTimeStamp(yamlBuyOrder.getLong(key + ".timestamp"))
								.getPacket());
						cnt++;
					}
				}
				
			}
		}
		Chat.sendMessage("Pushed total of " + cnt, ChatLevel.INFO);
	}
	
	public static void push(Enquiry e, UpdateType type, EnquiryTypes t) {
		Bazaar.getInstance().addToSend(PacketBuilder
				.newPacketBuilder(type.getType())
				.setAmount(e.getAmount())
				.setCategory(e.getCategory())
				.setClaimable(e.getClaimable())
				.setID(e.getID())
				.setLeft(e.getLeft())
				.setOwner(e.getOwner())
				.setPrice(e.getPrice())
				.setShow(e.getShow())
				.setSub(e.getSub())
				.setTimeStamp(e.getLastEdit())
				.setLastEdit(e.getLastEdit())
				.setEnquiryType(t == null ? e.getType() : t)
				.getPacket());
	}
	
	public enum UpdateType{
		
		UPDATE("update"),
		DELETE("delete"),
		CREATE("create");
		
		
		String type;
		
		UpdateType(String type) {
			this.type = type;
		}
		
		public String getType() {
			return type;
		}
	}
}
