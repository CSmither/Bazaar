package de.ancash.bazaar.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.utils.Chat.ChatLevel;

public class Enquiry implements Cloneable{

	private static boolean send_msg = false;
	
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
	private boolean saved = false;
	private final UUID id;
	private final int item_id;
	private final int category;
	private final int amount;
	private final long time_stamp;
	protected EnquiryTypes type;
	
	
	public long getTimeStamp() {return time_stamp;}
	public void setLeft(int i) {left = i;}
	public double getPrice() {return price;}
	public UUID getID() {return id;}
	public UUID getOwner() {return owner;}
	public int getLeft() {return left;}
	public int getAmount() {return amount;}
	public int getCategory() {return category;}
	public int getItemId() {return item_id;}
	public int getClaimable() {return claimable;}
	public EnquiryTypes getType() {return type;}
	public boolean hasClaimable() {return claimable != 0;}
	
	public int claim() {
		int temp = this.claimable;
		this.claimable = 0;
		return temp;
	}
	
	public void addClaimable(int i) {this.claimable += i;}
	
	public Enquiry(int a, double p, UUID i, int c, int ci) {
		if(!check(c, ci))
			throw new NullPointerException();
		this.amount = a;
		this.price = p;
		this.owner = i;
		this.category = c;
		this.item_id = ci;
		this.left = amount;
		this.id = UUID.randomUUID();
		this.claimable = 0;
		this.time_stamp = System.currentTimeMillis();
		saveAll(this);
	}
	
	public Enquiry(int amount, double price, UUID owner, int category, int item_id, int left, long timestamp, UUID id, int claimable) {
		if(!check(category, item_id))
			throw new NullPointerException();
		this.amount = amount;
		this.price = price;
		this.owner = owner;
		this.category = category;
		this.item_id = item_id;
		this.left = left;
		this.id = id;
		this.saved = true;
		this.time_stamp = timestamp;
		this.claimable = claimable;
	}
	
	public static void load() {
		File playerDir = new File("plugins/Bazaar/player");
		if(!playerDir.exists() || playerDir.listFiles().length == 0) {
			Chat.sendMessage("No Enquiries found.", ChatLevel.INFO);
			return;
		}
		int count = 0;
		
		for(File p : playerDir.listFiles()) {
			File sellOffer = new File("plugins/Bazaar/player/" + p.getName() + "/sell_offer.yml");
			File buyOrder = new File("plugins/Bazaar/player/" + p.getName() + "/buy_order.yml");
			
			FileConfiguration sellOfferFileConfiguration = YamlConfiguration.loadConfiguration(sellOffer);
			FileConfiguration buyOrderFileConfiguration = YamlConfiguration.loadConfiguration(buyOrder);
			
			try {
				sellOfferFileConfiguration.load(sellOffer);
				buyOrderFileConfiguration.load(buyOrder);
			} catch (IOException | InvalidConfigurationException e1) {
				e1.printStackTrace();
			}
			
			ArrayList<Enquiry> buy_order = get(buyOrderFileConfiguration, EnquiryTypes.BUY_ORDER, UUID.fromString(p.getName()));
			buy_order.forEach(e -> Enquiry.insert(e));
			count = count + buy_order.size();
			ArrayList<Enquiry> sell_offer = get(sellOfferFileConfiguration, EnquiryTypes.SELL_OFFER, UUID.fromString(p.getName()));
			sell_offer.forEach(e -> Enquiry.insert(e));
			count = count + sell_offer.size();
			
			try {
				sellOfferFileConfiguration.save(sellOffer);
				buyOrderFileConfiguration.save(buyOrder);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		send_msg = true;
		Chat.sendMessage("Loaded total of " + count + " Enquiries", ChatLevel.INFO);
	}
	
	private static ArrayList<Enquiry> get(FileConfiguration fc, EnquiryTypes type, UUID owner) {
		ArrayList<Enquiry> e = new ArrayList<Enquiry>();
		if(type.equals(EnquiryTypes.BUY_ORDER)) {
			Iterator<String> ids = fc.getKeys(false).iterator();
			String id = null;
			while(ids.hasNext()) {
				id = ids.next();
				if(!check(Integer.valueOf(fc.getInt(id + ".category")), fc.getInt(id + ".item_id")) ) continue;
				if(fc.getInt(id + ".left") == 0) continue;
				e.add(new BuyOrder(fc.getInt(id + ".amount"), fc.getDouble(id + ".price"), owner,fc.getInt(id + ".category"),
						fc.getInt(id + ".item_id"), fc.getInt(id + ".left"), fc.getLong(id + ".timestamp"),UUID.fromString(id), fc.getInt(id + ".claimable")));
			}
		}
		if(type.equals(EnquiryTypes.SELL_OFFER)) {
			Iterator<String> ids = fc.getKeys(false).iterator();
			String id = null;
			while(ids.hasNext()) {
				id = ids.next();
				if(!check(Integer.valueOf(fc.getInt(id + ".category")), fc.getInt(id + ".item_id"))) continue;
				if(fc.getInt(id + ".left") == 0) continue;
				e.add(new SellOffer(fc.getInt(id + ".amount"), fc.getDouble(id + ".price"), owner, fc.getInt(id + ".category"),
						fc.getInt(id + ".item_id"), fc.getInt(id + ".left"), fc.getLong(id + ".timestamp"), UUID.fromString(id), fc.getInt(id + ".claimable")));
			}
			
		}
		return e;
	}
	
	public static void save(Enquiry e) {
		if(e.getLeft() == e.getAmount()) return;
		save(e, false);
	}
	
	private static void save(Enquiry e, boolean b) {
		File f = null;
		if(e instanceof SellOffer) {
			f = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/sell_offer.yml");
		}
		if(e instanceof BuyOrder) {
			f = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/buy_order.yml");
		}
		FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
		try {
			fc.load(f);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		fc.set(e.getID().toString() + ".left", e.getLeft());
		fc.set(e.getID().toString() + ".claimable", e.getClaimable());
		try {
			fc.save(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void saveAll(Enquiry e) {
		File f = null;
		if(e instanceof SellOffer) {
			f = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/sell_offer.yml");
		}
		if(e instanceof BuyOrder) {
			f = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/buy_order.yml");
		}
		FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
		try {
			fc.load(f);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		if((fc.getInt(e.getID().toString() + ".left") == e.getLeft() && e.getLeft() != 0)) return;
		
		if(!e.saved) {
			fc.set(e.getID().toString() + ".amount", e.getAmount());
			fc.set(e.getID().toString() + ".price", e.getPrice());
			fc.set(e.getID().toString() + ".category", e.getCategory());
			fc.set(e.getID().toString() + ".item_id", e.getItemId());
			fc.set(e.getID().toString() + ".amount", e.getAmount());
			fc.set(e.getID().toString() + ".timestamp", e.getTimeStamp());
			e.saved = true;
		}
		fc.set(e.getID().toString() + ".claimable", e.getClaimable());
		fc.set(e.getID().toString() + ".left", e.getLeft());
		try {
			fc.save(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void checkEnquiry(Enquiry e) {
		if(e.getLeft() != 0) return;
		Category cat = Category.getCategory(e.getCategory());
		SelfBalancingBST root = null;
		SelfBalancingBSTNode node = null;
		if(e instanceof SellOffer) {
			root = Category.getCategory(e.getCategory()).getSellOffer(e.getItemId());
			node = cat.getSellOffer(e.getItemId()).get(e.getPrice(), root.getRoot());
		}
		if(e instanceof BuyOrder) {
			root = Category.getCategory(e.getCategory()).getBuyOrder(e.getItemId());
			node = cat.getBuyOrder(e.getItemId()).get(e.getPrice(), root.getRoot());
		}
		if(node != null && e.getLeft() == 0) node.remove(e.getID()); 
		if(node != null && node.get().size() == 0) root.deleteKey(e.getPrice());
		Player p = Bukkit.getPlayer(e.getOwner());
		if(p != null && e.getLeft() == 0 && send_msg) {
			if(e instanceof SellOffer) {
				String name = cat.getContents()[e.getItemId() - 1].getItemMeta().getDisplayName();
				if(name == null || name.length() == 0) name = cat.getShowcase()[Category.getSlotByID(e.getItemId())].getItemMeta().getDisplayName();
				p.sendMessage("§6[Bazaar] §eYour §aSell Offer §efor §a" + e.getAmount() + "§7x " + name + " §ewas filled!");
			}
			
			if(e instanceof BuyOrder) {
				String name = cat.getContents()[e.getItemId() - 1].getItemMeta().getDisplayName();
				if(name == null || name.length() == 0) name = cat.getShowcase()[Category.getSlotByID(e.getItemId())].getItemMeta().getDisplayName();
				p.sendMessage("§6[Bazaar] §eYour §aBuy Order §efor §a" + e.getAmount() + "§7x " + name + " §ewas filled!");
			}
		}
		
		if(e.getLeft() == 0 && !e.hasClaimable()) {
			System.out.println("deleting e " + e.getID().toString());
			deleteEnquiry(e);
		}
	}
	
	public static void deleteEnquiry(Enquiry e) {
		File f = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/" + (e instanceof SellOffer ? "sell_offer.yml" : e instanceof BuyOrder ? "buy_order.yml" : ""));
		if(!(e instanceof SellOffer) && !(e instanceof BuyOrder)) return;
		
		FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
		try {
			fc.load(f);
			fc.set(e.getID().toString(), null);
			fc.save(f);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		
	}
	
	public static void delete(UUID owner, String uuid, EnquiryTypes type) {
		File f = new File("plugins/Bazaar/player/" + owner.toString() + "/" + (type == EnquiryTypes.SELL_OFFER ? "sell_offer.yml" : "buy_order.yml"));
		
		FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
		try {
			fc.load(f);
			fc.set(uuid, null);
			fc.save(f);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
	}
	
	//checking if cat and item are valid
	private static boolean check(int i, int id) {
		if(!Category.exists(i)) {
			Chat.sendMessage("Cannot load Item(" + id + ") for non-existing Category(" + i + ")!", ChatLevel.WARN);
			return false;
		}
		return id <= 18 && !Category.getCategory(i).getContents()[id].getType().equals(Material.AIR);
	}
	
	public static void insert(Enquiry e) {
		//not sub
		insert(e, -1);
	}
	
	public static void insert(Enquiry e, int sub) {
		//sub if -1
		if(e.getLeft() == 0) return;
		if(e instanceof SellOffer) {
			SellOffer so = (SellOffer) e;
			SelfBalancingBSTNode node_bo = null;
			if(sub == -1) {
				node_bo = Category.getCategory(e.getCategory()).get(EnquiryTypes.BUY_ORDER, e.getItemId(), e.getPrice());
			} else {
				//node_bo = Category.getCategory(e.getCategory()).getSub(EnquiryTypes.BUY_ORDER, e.getItemId(), sub,e.getPrice());
			}
			while(node_bo != null && node_bo.get().size() != 0 && e.getLeft() != 0) {
				try {
					Enquiry.process(so, (BuyOrder) node_bo.getByTimeStamp());
				} catch (IOException | InvalidConfigurationException e1) {
					e1.printStackTrace();
				}
			}
			if(e.getLeft() > 0) {
				Category.getCategory(e.getCategory()).getSellOffer(e.getItemId()).insert(e.getPrice(), e);
			}
		}
		if(e instanceof BuyOrder) {
			BuyOrder bo = (BuyOrder) e;
			SelfBalancingBSTNode node_so = null;
			if(sub == -1) {
				node_so = Category.getCategory(e.getCategory()).get(EnquiryTypes.SELL_OFFER, e.getItemId(), e.getPrice());
			} else {
				//node_so = Category.getCategory(e.getCategory()).getSub(EnquiryTypes.SELL_OFFER, e.getItemId(), sub,e.getPrice());
			}
			while(node_so != null && node_so.get().size() != 0 && e.getLeft() != 0) {
				try {
					Enquiry.process((SellOffer) node_so.getByTimeStamp(), bo);
				} catch (IOException | InvalidConfigurationException e1) {
					e1.printStackTrace();
				}
			}
			if(e.getLeft() > 0) {
				Category.getCategory(e.getCategory()).getBuyOrder(e.getItemId()).insert(e.getPrice(), e);
			}
		}
	}
	
	public static void process(SellOffer so, BuyOrder bo) throws FileNotFoundException, IOException, InvalidConfigurationException {
		int reducable = bo.getLeft() > so.getLeft() ? so.getLeft() : bo.getLeft();
		bo.reduce(reducable);
		so.reduce(reducable);
		bo.addClaimable(reducable);
		so.addClaimable(reducable);
		save(bo);
		save(so);
		checkEnquiry(bo);
		checkEnquiry(so);
	}
	
	public boolean reduce(int subtract) {
		left = left - subtract;
		return left > 0;
	}
}
