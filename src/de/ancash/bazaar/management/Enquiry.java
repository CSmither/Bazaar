package de.ancash.bazaar.management;

import java.io.File;
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

import de.ancash.bazaar.utils.BuyOrder;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.SellOffer;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.ilibrary.datastructures.maps.CompactMap;

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
	private final UUID id;
	private final int show;
	private final int sub;
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
	public int getShow() {return show;}
	public int getSub() {return sub;}
	public int getClaimable() {return claimable;}
	public EnquiryTypes getType() {return type;}
	public boolean hasClaimable() {return claimable != 0;}
	
	public int claim() {
		int temp = this.claimable;
		this.claimable = 0;
		return temp;
	}
	
	public void addClaimable(int i) {this.claimable += i;}
	
	public Enquiry(int amount, double p, UUID i, int category, int a, int b) {
		this.amount = amount;
		this.price = p;
		this.owner = i;
		this.category = category;
		this.show = a;
		this.sub = b;
		this.left = amount;
		this.id = UUID.randomUUID();
		this.claimable = 0;
		this.time_stamp = System.currentTimeMillis();
	}
	
	public Enquiry(int amount, double price, UUID owner, int category, int a, int b, int left, long timestamp, UUID id, int claimable) {
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
	}
	
	static CompactMap<File, FileConfiguration> alreadyLoaded = new CompactMap<File, FileConfiguration>();
	
	@Override
	public String toString() {
		return "cat=" + getCategory() + ", show=" + getShow() + ", sub=" + getSub() + ", price=" + getPrice() + ", amt=" + getAmount() + ", left=" + getLeft() + ", claimable=" + getClaimable();
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
			File yamlSellOffer = new File("plugins/Bazaar/player/" + p.getName() + "/sell_offer.yml");
			File yamlBuyOrder = new File("plugins/Bazaar/player/" + p.getName() + "/buy_order.yml");

			FileConfiguration fcSO = YamlConfiguration.loadConfiguration(yamlSellOffer);
			FileConfiguration fcBO = YamlConfiguration.loadConfiguration(yamlBuyOrder);
			try {
				fcBO.load(yamlBuyOrder);
				fcSO.load(yamlSellOffer);
			} catch (IOException | InvalidConfigurationException e2) {
				e2.printStackTrace();
			}
			
			ArrayList<Enquiry> buy_order = get(fcBO, EnquiryTypes.BUY_ORDER, UUID.fromString(p.getName()));
			buy_order.forEach(e -> Enquiry.insert(e));
			count = count + buy_order.size();
			
			ArrayList<Enquiry> sell_offer = get(fcSO, EnquiryTypes.SELL_OFFER, UUID.fromString(p.getName()));
			sell_offer.forEach(e -> Enquiry.insert(e));
			count = count + sell_offer.size();
			
			alreadyLoaded.put(yamlBuyOrder, fcBO);
			alreadyLoaded.put(yamlSellOffer, fcSO);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	private static ArrayList<Enquiry> get(FileConfiguration fc, EnquiryTypes type, UUID owner) {
		ArrayList<Enquiry> e = new ArrayList<Enquiry>();
		if(type.equals(EnquiryTypes.BUY_ORDER)) {
			Iterator<String> ids = fc.getKeys(false).iterator();
			String id = null;
			while(ids.hasNext()) {
				id = ids.next();
				int category = Integer.valueOf(fc.getInt(id + ".category"));
				int show = fc.getInt(id + ".show");
				int sub = fc.getInt(id + ".sub");
				if(!check(category, show, sub)) continue;
				if(fc.getInt(id + ".left") == 0) continue;
				e.add(new BuyOrder(fc.getInt(id + ".amount"), fc.getDouble(id + ".price"), owner,category, show,
						sub, fc.getInt(id + ".left"), fc.getLong(id + ".timestamp"),UUID.fromString(id), fc.getInt(id + ".claimable")));
			}
		}
		if(type.equals(EnquiryTypes.SELL_OFFER)) {
			Iterator<String> ids = fc.getKeys(false).iterator();
			String id = null;
			while(ids.hasNext()) {
				id = ids.next();
				int category = Integer.valueOf(fc.getInt(id + ".category"));
				int show = fc.getInt(id + ".show");
				int sub = fc.getInt(id + ".sub");
				if(!check(category, show, sub)) continue;
				if(fc.getInt(id + ".left") == 0) continue;
				e.add(new SellOffer(fc.getInt(id + ".amount"), fc.getDouble(id + ".price"), owner,category, show,
						sub, fc.getInt(id + ".left"), fc.getLong(id + ".timestamp"),UUID.fromString(id), fc.getInt(id + ".claimable")));
			}
			
		}
		return e;
	}
	
	public static void save(Enquiry e) {
		if(e.getLeft() == e.getAmount()) return;
		save(e, false);
	}
	
	private static void save(Enquiry e, boolean b) {
		File yamlFile = null;
		if(e instanceof SellOffer) {
			yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/sell_offer.yml");
		}
		if(e instanceof BuyOrder) {
			yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/buy_order.yml");
		}
		FileConfiguration fc = alreadyLoaded.get(yamlFile);
		fc.set(e.getID().toString() + ".left", e.getLeft());
		fc.set(e.getID().toString() + ".claimable", e.getClaimable());
	}
	
	public static void saveAll(Enquiry e, boolean stop) {
		File yamlFile = null;
		if(e instanceof SellOffer) {
			yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/sell_offer.yml");
		}
		if(e instanceof BuyOrder) {
			yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/buy_order.yml");
		}
		try {
			FileConfiguration fc = alreadyLoaded.get(yamlFile);
			if(fc.getInt(e.getID().toString() + ".left") == e.getLeft() && e.getLeft() != 0) {
				if(stop) {
					fc.save(yamlFile);
					alreadyLoaded.get(yamlFile).save(yamlFile);
					alreadyLoaded.remove(yamlFile);
				}
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
			if(stop) {
				fc.save(yamlFile);
				alreadyLoaded.get(yamlFile).save(yamlFile);
				alreadyLoaded.remove(yamlFile);
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
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
			deleteEnquiry(e);
		}
	}
	
	public static void deleteEnquiry(Enquiry e) {
		if(!(e instanceof SellOffer) && !(e instanceof BuyOrder)) return;
		File yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/" + (e instanceof SellOffer ? "sell_offer.yml" : e instanceof BuyOrder ? "buy_order.yml" : ""));
		FileConfiguration fc = alreadyLoaded.get(yamlFile);
		/*try {
			fc.load(yamlFile);
		} catch (IOException | InvalidConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		fc.set(e.getID().toString(), null);
		/*try {
			fc.save(yamlFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}*/
		
	}
	
	public static void delete(UUID owner, String uuid, EnquiryTypes type) {
		File yamlFile = new File("plugins/Bazaar/player/" + owner.toString() + "/" + (type == EnquiryTypes.SELL_OFFER ? "sell_offer.yml" : "buy_order.yml"));
		FileConfiguration fc = alreadyLoaded.get(yamlFile);
		/*try {
			fc.load(yamlFile);
		} catch (IOException | InvalidConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		fc.set(uuid, null);
		/*try {
			fc.save(yamlFile);
		} catch (IOException e) {
			e.printStackTrace();
		}*/	
	}
	
	//checking if cat and item are valid
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
		save(bo);
		save(so);
		checkEnquiry(bo);
		checkEnquiry(so);
	}
	
	public boolean reduce(int subtract) {
		left = left - subtract;
		return left >= 0;
	}
}
