package de.ancash.bazaar.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.sockets.EnquiryUpdater;
import de.ancash.bazaar.sockets.EnquiryUpdater.UpdateType;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.yaml.configuration.file.YamlFile;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

public class Enquiry implements Cloneable{
	
	public enum EnquiryTypes{
		SELL_OFFER("Sell Offer"),
		BUY_ORDER("Buy Order");
		
		private String name;
		
		public String getName() {
			return name;
		}
		
		EnquiryTypes(String name) {
			this.name = name;
		}
	}
	
	private double price;
	private int left;
	private int claimable;
	private UUID owner;
	private UUID id;
	private int show;
	private int sub;
	private int category;
	private int amount;
	private long time_stamp;
	private long lastEdit;
	EnquiryTypes type;
	
	public Enquiry(int amount, double p, UUID i, int category, int a, int b) {
		this(amount, p, i, category, a, b, amount, System.currentTimeMillis(), UUID.randomUUID(), 0, System.currentTimeMillis());
	}
	
	public Enquiry(int amount, double price, UUID owner, int category, int a, int b, int left, long timestamp, UUID id, int claimable, long lastEdit) {
		this.amount = amount;
		this.price = price;
		this.owner = owner;
		this.category = category;
		this.show = a;
		this.sub = b;
		this.left = left;
		this.id = id;
		this.time_stamp = timestamp;
		this.claimable = claimable;
		this.lastEdit = lastEdit;
		saveAll(this);
	}
	
	public long getTimeStamp() {return time_stamp;}
	public void setLeft(int i) {left = i;}
	public double getPrice() {return price;}
	public UUID getID() {return id;}
	public UUID getOwner() {return owner;}
	public int getLeft() {return left;}
	public int getAmount() {return amount;}
	public int getCategory() {return category;}
	public int getShow() {return show;}
	public int getSub() {return sub;}
	public int getClaimable() {return claimable;}
	public EnquiryTypes getType() {return type;}
	public boolean hasClaimable() {return claimable != 0;}
	public long getLastEdit() {return lastEdit;}
	public void setClaimable(int i) {this.claimable = i;}
	public void setLastEdit(long now) {this.lastEdit = now;}
	
	public int claim() {
		int temp = this.claimable;
		this.claimable = 0;
		return temp;
	}
	
	public void addClaimable(int i) {this.claimable += i;}
	
	void updateLastEdit() {
		this.lastEdit = System.currentTimeMillis();
	}
	
	private static CompactMap<File, YamlFile> alreadyLoaded = new CompactMap<File, YamlFile>();
	
	public static YamlFile getYamlFile(File file) {return alreadyLoaded.get(file);}
	public static boolean isYamlFileLoaded(File file) {return alreadyLoaded.containsKey(file);}
	public static void addYamlFile(File file, YamlFile fc) {alreadyLoaded.put(file, fc);}
	
	@Override
	public String toString() {
		return "cat=" + getCategory() + ",show=" + getShow() + ",sub=" + getSub() + ",price=" + getPrice() + ",amt=" + getAmount()
				+ ",left=" + getLeft() + ",claimable=" + getClaimable() + ",owner=" + owner.toString() + ",id=" + id.toString() + ",lastEdit" + lastEdit;
	}
	
	public static void load() {
		Chat.sendMessage("Loading Enquiries from Files...", ChatLevel.INFO);
		File playerDir = new File("plugins/Bazaar/player");
		if(!playerDir.exists() || playerDir.listFiles().length == 0) {
			Chat.sendMessage("No Files found!", ChatLevel.INFO);
			return;
		}
		int count = 0;
		
		for(File p : playerDir.listFiles()) {
			File sellOfferFile = new File("plugins/Bazaar/player/" + p.getName() + "/sell_offer.yml");
			File buyOrderFile = new File("plugins/Bazaar/player/" + p.getName() + "/buy_order.yml");
			YamlFile yamlSellOffer = new YamlFile(sellOfferFile);
			YamlFile yamlBuyOrder = new YamlFile(new File("plugins/Bazaar/player/" + p.getName() + "/buy_order.yml"));

			try {
				yamlBuyOrder.loadWithComments();
				yamlSellOffer.loadWithComments();
			} catch (InvalidConfigurationException | IOException e1) {
				e1.printStackTrace();
			}
			
			ArrayList<Enquiry> buy_order = get(yamlBuyOrder, EnquiryTypes.BUY_ORDER, UUID.fromString(p.getName()));
			buy_order.forEach(e -> Enquiry.insert(e));
			count = count + buy_order.size();
			
			ArrayList<Enquiry> sell_offer = get(yamlSellOffer, EnquiryTypes.SELL_OFFER, UUID.fromString(p.getName()));
			sell_offer.forEach(e -> Enquiry.insert(e));
			count = count + sell_offer.size();
			
			alreadyLoaded.put(sellOfferFile, yamlSellOffer);
			alreadyLoaded.put(buyOrderFile, yamlBuyOrder);
			/*try {
				fcSO.save(yamlSellOffer);
				fcBO.save(yamlBuyOrder);
			} catch (IOException e1) {
				e1.printStackTrace();
			}*/
		}
		Chat.sendMessage("Loaded total of " + count + " Enquiries", ChatLevel.INFO);
	}
	
	public static void stop() {
		alreadyLoaded.forEach((file, fc) ->{
			try {
				fc.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	private static ArrayList<Enquiry> get(YamlFile fc, EnquiryTypes type, UUID owner) {
		ArrayList<Enquiry> e = new ArrayList<Enquiry>();
		if(type.equals(EnquiryTypes.BUY_ORDER)) {
			Iterator<String> ids = fc.getKeys(false).iterator();
			String id = null;
			while(ids.hasNext()) {
				id = ids.next();
				if(fc.getString(id) != null && fc.getString(id).equals("null")) continue;
				int category = Integer.valueOf(fc.getInt(id + ".category"));
				int show = fc.getInt(id + ".show");
				int sub = fc.getInt(id + ".sub");
				if(!check(category, show, sub)) continue;
				if(fc.getInt(id + ".left") == 0) continue;
				e.add(new BuyOrder(fc.getInt(id + ".amount"), fc.getDouble(id + ".price"), owner,category, show,
						sub, fc.getInt(id + ".left"), fc.getLong(id + ".timestamp"),UUID.fromString(id), fc.getInt(id + ".claimable"),
						fc.getLong(id + ".lastEdit")));
			}
		}
		if(type.equals(EnquiryTypes.SELL_OFFER)) {
			Iterator<String> ids = fc.getKeys(false).iterator();
			String id = null;
			while(ids.hasNext()) {
				id = ids.next();
				if(fc.getString(id) != null && fc.getString(id).equals("null")) continue;
				int category = Integer.valueOf(fc.getInt(id + ".category"));
				int show = fc.getInt(id + ".show");
				int sub = fc.getInt(id + ".sub");
				if(!check(category, show, sub)) continue;
				if(fc.getInt(id + ".left") == 0) continue;
				e.add(new SellOffer(fc.getInt(id + ".amount"), fc.getDouble(id + ".price"), owner,category, show,
						sub, fc.getInt(id + ".left"), fc.getLong(id + ".timestamp"),UUID.fromString(id), fc.getInt(id + ".claimable"),
						fc.getLong(id + ".lastEdit")));
			}
			
		}
		return e;
	}
	
	//XXX
	public static void save(Enquiry e) {
		File yamlFile = null;
		if(e instanceof SellOffer) {
			yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/sell_offer.yml");
		}
		if(e instanceof BuyOrder) {
			yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/buy_order.yml");
		}
		YamlFile fc = alreadyLoaded.get(yamlFile);
		fc.set(e.getID().toString() + ".left", e.getLeft());
		fc.set(e.getID().toString() + ".claimable", e.getClaimable());
		fc.set(e.getID().toString() + ".lastEdit", e.getLastEdit());
	}
	
	//XXX
	public static void saveAll(Enquiry e) {
		File yamlFile = null;
		if(e instanceof SellOffer) {
			yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/sell_offer.yml");
		}
		if(e instanceof BuyOrder) {
			yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/buy_order.yml");
		}
		YamlFile fc = alreadyLoaded.get(yamlFile);
		if(fc == null) return;
		if(fc.getInt(e.getID().toString() + ".left") == e.getLeft() && e.getLeft() != 0) {
			return;
		}
		fc.set(e.getID().toString() + ".amount", e.getAmount());
		fc.set(e.getID().toString() + ".price", e.getPrice());
		fc.set(e.getID().toString() + ".category", e.getCategory());
		fc.set(e.getID().toString() + ".show", e.getShow());
		fc.set(e.getID().toString() + ".sub", e.getSub());
		fc.set(e.getID().toString() + ".amount", e.getAmount());
		fc.set(e.getID().toString() + ".timestamp", e.getTimeStamp());
		fc.set(e.getID().toString() + ".claimable", e.getClaimable());
		fc.set(e.getID().toString() + ".left", e.getLeft());
		fc.set(e.getID() + ".lastEdit", e.getLastEdit());
	}
	
	public static void checkEnquiry(Enquiry e) {
		if(e.getLeft() != 0) return;
		Category cat = Category.getCategory(e.getCategory());
		SelfBalancingBST root = null;
		SelfBalancingBSTNode node = null;
		
		if(e instanceof SellOffer) {
			root = cat.getSellOffers(e.getShow(), e.getSub());
			node = root.get(e.getPrice(), root.getRoot());
		}
		
		if(e instanceof BuyOrder) {
			root = cat.getBuyOrders(e.getShow(), e.getSub());
			node = root.get(e.getPrice(), root.getRoot());
		}
		
		
		
		if(node != null && e.getLeft() == 0) node.remove(e.getID()); 
		if(node != null && node.get().size() == 0) root.deleteKey(e.getPrice());
		Player p = Bukkit.getPlayer(e.getOwner());
		if(e.getLeft() == 0) {
			if(p != null) {
				if(e instanceof SellOffer) {
					String name = cat.getSubShow()[e.getShow() - 1][e.getSub() - 1].getItemMeta().getDisplayName();
					p.sendMessage("§6[Bazaar] §eYour §aSell Offer §efor §a" + e.getAmount() + "§7x " + name + " §ewas filled!");
				}
				
				if(e instanceof BuyOrder) {
					String name = cat.getSubShow()[e.getShow() - 1][e.getSub() - 1].getItemMeta().getDisplayName();
					p.sendMessage("§6[Bazaar] §eYour §aBuy Order §efor §a" + e.getAmount() + "§7x " + name + " §ewas filled!");
				}
			}
		}
		
		if(e.getLeft() == 0 && !e.hasClaimable()) {
			EnquiryUpdater.push(e, UpdateType.DELETE, e.getType());
			deleteEnquiry(e);
		}
	}
	
	//XXX
	public static void deleteEnquiry(Enquiry e) {
		if(!(e instanceof SellOffer) && !(e instanceof BuyOrder)) return;
		File yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/" + (e instanceof SellOffer ? "sell_offer.yml" : e instanceof BuyOrder ? "buy_order.yml" : ""));
		YamlFile fc = alreadyLoaded.get(yamlFile);
		fc.set(e.getID().toString(), "null");
	}
	
	//XXX
	public static void delete(UUID owner, String uuid, EnquiryTypes type) {
		File yamlFile = new File("plugins/Bazaar/player/" + owner.toString() + "/" + (type == EnquiryTypes.SELL_OFFER ? "sell_offer.yml" : "buy_order.yml"));
		YamlFile fc = alreadyLoaded.get(yamlFile);
		fc.set(uuid, "null");
	}
	
	static Enquiry getEnquiry(UUID id, int cat, int show, int sub, double price, EnquiryTypes type) {
		Category category = Category.getCategory(cat);
		SelfBalancingBST tree = category.getTree(type, show, cat);
		if(tree.get(price, tree.getRoot()) == null) return null;
		return tree.get(price, tree.getRoot()).get(id);
	}
	
	private static boolean check(int cat, int a, int b) {
		if(!Category.exists(cat)) {
			Chat.sendMessage("Cannot load Item(" + a + ", " + b + ") for non-existing Category(" + cat + ")!", ChatLevel.WARN);
			return false;
		}
		return a <= 18 && a > 0 && b >0 && b <= 9 && !Category.getCategory(cat).getSubShow()[a - 1][b - 1].getType().equals(Material.AIR);
	}
	
	public static void insert(Enquiry e) {
		if(e.getLeft() == 0) return;
		if(e instanceof SellOffer) {
			SellOffer so = (SellOffer) e;
			SelfBalancingBST tree = null;
			SelfBalancingBSTNode node_bo = null;
			tree = Category.getCategory(e.getCategory()).getBuyOrders(e.getShow(), e.getSub());
			node_bo = tree.get(e.getPrice(), tree.getRoot());
			while(node_bo != null && node_bo.get().size() != 0 && so.getLeft() != 0) {
				Enquiry.process(so, (BuyOrder) node_bo.getByTimeStamp());
			}
			if(e.getLeft() > 0) {
				Category.getCategory(e.getCategory()).getSellOffers(e.getShow(), e.getSub()).insert(e.getPrice(), e);
			}
		}
		if(e instanceof BuyOrder) {
			BuyOrder bo = (BuyOrder) e;
			SelfBalancingBSTNode node_so = null;
			SelfBalancingBST tree = null;
			tree = Category.getCategory(e.getCategory()).getSellOffers(e.getShow(), e.getSub());
			node_so = tree.get(e.getPrice(), tree.getRoot());
			while(node_so != null && node_so.get().size() != 0 && bo.getLeft() != 0) {
				Enquiry.process((SellOffer) node_so.getByTimeStamp(), bo);
			}
			if(e.getLeft() > 0) {
				Category.getCategory(e.getCategory()).getBuyOrders(e.getShow(), e.getSub()).insert(e.getPrice(), e);
			}
		}
	}
	
	public static void process(final SellOffer so, final BuyOrder bo) {
		int reducable = bo.getLeft() > so.getLeft() ? so.getLeft() : bo.getLeft();
		bo.reduce(reducable);
		so.reduce(reducable);
		bo.addClaimable(reducable);
		so.addClaimable(reducable);
		bo.setLastEdit(System.currentTimeMillis());
		so.setLastEdit(System.currentTimeMillis());
		save(bo);
		save(so);
		EnquiryUpdater.push(so, UpdateType.UPDATE, EnquiryTypes.SELL_OFFER);
		EnquiryUpdater.push(bo, UpdateType.UPDATE, EnquiryTypes.BUY_ORDER);
		checkEnquiry(bo);
		checkEnquiry(so);
	}
	
	public boolean reduce(int subtract) {
		left = left - subtract;
		return left >= 0;
	}
}
