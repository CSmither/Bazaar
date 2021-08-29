package de.ancash.bazaar.sockets.eqnuiry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import de.ancash.bazaar.sockets.BazaarSocketPlugin;
import de.ancash.bazaar.sockets.events.EnquiryFillEvent;
import de.ancash.bazaar.sockets.management.Category;
import de.ancash.bazaar.sockets.management.SelfBalancingBST;
import de.ancash.bazaar.sockets.management.SelfBalancingBSTNode;
import de.ancash.bazaar.sockets.management.SellOffer;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.libs.org.bukkit.event.EventManager;

public class EnquiryUtils {

	private final Object LOCK = new Object();
	
	public File getSellOfferFile(UUID uuid) {
		return new File(BazaarSocketPlugin.FILE_PATH + "/player/" + uuid + "/sell_offer.yml");
	}
	
	public File getBuyOrderFile(UUID uuid) {
		return new File(BazaarSocketPlugin.FILE_PATH + "/player/" + uuid + "/buy_order.yml");
	}
	
	public void checkUUID(UUID uuid) {
		
		File sellOfferFile = getSellOfferFile(uuid);
		File buyOrderFile = getBuyOrderFile(uuid);
		
		synchronized (LOCK) {
			
			if(!sellOfferFile.exists()) {
				sellOfferFile.mkdirs();
				sellOfferFile.delete();
				try {
					sellOfferFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(!buyOrderFile.exists()) {
				buyOrderFile.mkdirs();
				buyOrderFile.delete();
				try {
					buyOrderFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				if(!isYamlFileLoaded(buyOrderFile)){
					YamlFile yamlFile = new YamlFile(buyOrderFile);
					yamlFile.load();
					addYamlFile(buyOrderFile, yamlFile);
				}
				if(!isYamlFileLoaded(sellOfferFile)) {
					YamlFile yamlFile = new YamlFile(sellOfferFile);
					yamlFile.load();
					addYamlFile(sellOfferFile, yamlFile);
				}
			} catch(IOException | InvalidConfigurationException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public Object getLock() {
		return LOCK;
	}
	
	private Map<File, YamlFile> alreadyLoaded = new CompactMap<File, YamlFile>();
	
	public YamlFile getYamlFile(File file) {return alreadyLoaded.get(file);}
	private boolean isYamlFileLoaded(File file) {return alreadyLoaded.containsKey(file);}
	private void addYamlFile(File file, YamlFile fc) {alreadyLoaded.put(file, fc);}
	
	public Duplet<Integer, Double> processInstaSell(int amount, SelfBalancingBSTNode sellOfferNode, SelfBalancingBSTNode buyOrderNode, double price) {
    	synchronized (LOCK) {
    		Duplet<Integer, Double> tuple = Tuple.of(0, 0D);
    		
    		if(sellOfferNode.getKey() != buyOrderNode.getKey() || sellOfferNode.getKey() != price) return tuple;
    		
    		if(sellOfferNode.get().isEmpty() || buyOrderNode.get().isEmpty()) return tuple;
    		
    		BuyOrder buyOrder = (BuyOrder) buyOrderNode.getByTimeStamp();
    		
    		int left = amount;
    		
    		while(buyOrder != null && left > 0) {
    			
    			int reducable = left;
    			if(left > buyOrder.getLeft()) reducable = buyOrder.getLeft();
    			
    			buyOrder.addClaimable(reducable);
    			buyOrder.setLeft(buyOrder.getLeft() - reducable);
    			
    			tuple.setFirst(tuple.getFirst() + reducable);
    			tuple.setSecond(tuple.getSecond() + (reducable * price));
    			
    			if(buyOrder.getLeft() <= 0) {
    				checkEnquiry(buyOrder);
    				buyOrder = (BuyOrder) buyOrderNode.getByTimeStamp();
    			}
    			left -= reducable;
    		}
    		
    		return tuple;
		}
    }
	
	public void load() {
		synchronized (LOCK) {
			System.out.println("Loading Enquiries from Files...");;
			File playerDir = new File(BazaarSocketPlugin.FILE_PATH + "/player");
			if(!playerDir.exists() || playerDir.listFiles().length == 0) {
				System.out.println("No Files found!");;
				return;
			}
			int count = 0;
			
			for(File p : playerDir.listFiles()) {
				File sellOfferFile = getSellOfferFile(UUID.fromString(p.getName()));
				File buyOrderFile = getBuyOrderFile(UUID.fromString(p.getName()));
				YamlFile yamlSellOffer = new YamlFile(sellOfferFile);
				YamlFile yamlBuyOrder = new YamlFile(buyOrderFile);
				
				try {
					yamlBuyOrder.createOrLoad();
					yamlSellOffer.createOrLoad();
				} catch (InvalidConfigurationException | IOException e1) {
					System.err.println("Could not load " + p.getName() + ": " + e1);
					continue;
				}
				
				ArrayList<Enquiry> buy_order = getEnquiriesFromFile(yamlBuyOrder, EnquiryType.BUY_ORDER, UUID.fromString(p.getName()));
				buy_order.forEach(this::insert);
				count = count + buy_order.size();
				
				ArrayList<Enquiry> sell_offer = getEnquiriesFromFile(yamlSellOffer, EnquiryType.SELL_OFFER, UUID.fromString(p.getName()));
				sell_offer.forEach(this::insert);
				count = count + sell_offer.size();
				
				alreadyLoaded.put(sellOfferFile, yamlSellOffer);
				alreadyLoaded.put(buyOrderFile, yamlBuyOrder);
			}
			System.out.println("Loaded total of " + count + " Enquiries");
		}
	}
	
	public int countEnquiries(UUID uuid) {
		File bo = getBuyOrderFile(uuid);
		File so = getSellOfferFile(uuid);
		if(!isYamlFileLoaded(bo)) return 0;
		return getYamlFile(so).getKeys(false).size() + getYamlFile(bo).getKeys(false).size();
	}
	
	public double getClaimableCoins(UUID uuid) {
		File so = getSellOfferFile(uuid);
		if(!isYamlFileLoaded(so)) return 0;
		YamlFile file = getYamlFile(so);
		double coins = 0;
		
		for(String key : file.getKeys(false)) 
			coins += file.getDouble(key + ".price") * file.getInt(key + ".claimable");
		
		return coins;
	}
	
	public double getClaimableItems(UUID uuid) {
		File bo = getBuyOrderFile(uuid);
		if(!isYamlFileLoaded(bo)) return 0;
		YamlFile file = getYamlFile(bo);
		int items = 0;
		
		for(String key : file.getKeys(false)) 
			items += file.getInt(key + ".claimable");
		
		return items;
	}
	
	public void save() {
		synchronized (LOCK) {
			System.out.println("Saving enquiries!");
			alreadyLoaded.forEach((file, fc) ->{
				try {
					fc.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			System.out.println("Saved!");
		}
	}
	
	private ArrayList<Enquiry> getEnquiriesFromFile(YamlFile fc, EnquiryType type, UUID owner) {
		ArrayList<Enquiry> e = new ArrayList<Enquiry>();
		if(type.equals(EnquiryType.BUY_ORDER)) {
			Iterator<String> ids = fc.getKeys(false).iterator();
			String id = null;
			while(ids.hasNext()) {
				id = ids.next();
				if(fc.getString(id) == null) continue;
				int category = Integer.valueOf(fc.getInt(id + ".category"));
				int show = fc.getInt(id + ".sub");
				int sub = fc.getInt(id + ".subsub");
				if(!check(category, show, sub)) continue;
				if(fc.getInt(id + ".left") == 0) continue;
				e.add(new BuyOrder(fc.getInt(id + ".amount"), fc.getDouble(id + ".price"), owner,category, show,
						sub, fc.getInt(id + ".left"), UUID.fromString(id), fc.getInt(id + ".claimable"), fc.getLong(id + ".timestamp")));
			}
		}
		if(type.equals(EnquiryType.SELL_OFFER)) {
			Iterator<String> ids = fc.getKeys(false).iterator();
			String id = null;
			while(ids.hasNext()) {
				id = ids.next();
				if(fc.getString(id) == null) continue;
				int category = Integer.valueOf(fc.getInt(id + ".category"));
				int show = fc.getInt(id + ".sub");
				int sub = fc.getInt(id + ".subsub");
				if(!check(category, show, sub)) continue;
				if(fc.getInt(id + ".left") == 0) continue;
				e.add(new SellOffer(fc.getInt(id + ".amount"), fc.getDouble(id + ".price"), owner,category, show,
						sub, fc.getInt(id + ".left"), UUID.fromString(id), fc.getInt(id + ".claimable"), fc.getLong(id + ".timestamp")));
			}
			
		}
		return e;
	}
	
	public void saveEnquiry(Enquiry e) {
		synchronized (LOCK) {
			File yamlFile = null;
			if(e instanceof SellOffer) {
				yamlFile = getSellOfferFile(e.getOwner());
			} else if(e instanceof BuyOrder) {
				yamlFile = getBuyOrderFile(e.getOwner());
			} else {
				return;
			}
			YamlFile fc = getYamlFile(yamlFile);
			fc.set(e.getID().toString() + ".left", e.getLeft());
			fc.set(e.getID().toString() + ".claimable", e.getClaimable());
		}
	}
	
	public void saveEnquiryAll(Enquiry e) {
		synchronized (LOCK) {
			File yamlFile = null;
			if(e instanceof SellOffer) {
				yamlFile = getSellOfferFile(e.getOwner());
			}
			if(e instanceof BuyOrder) {
				yamlFile = getBuyOrderFile(e.getOwner());
			}
			YamlFile fc = getYamlFile(yamlFile);
			if(fc == null) return;
			if(fc.getInt(e.getID().toString() + ".left") == e.getLeft() && e.getLeft() != 0) {
				return;
			}
			fc.set(e.getID().toString() + ".amount", e.getAmount());
			fc.set(e.getID().toString() + ".price", e.getPrice());
			fc.set(e.getID().toString() + ".category", e.getCategory());
			fc.set(e.getID().toString() + ".sub", e.getSubCategory());
			fc.set(e.getID().toString() + ".subsub", e.getSubSubCategory());
			fc.set(e.getID().toString() + ".amount", e.getAmount());
			fc.set(e.getID().toString() + ".timestamp", e.getTimeStamp());
			fc.set(e.getID().toString() + ".claimable", e.getClaimable());
			fc.set(e.getID().toString() + ".left", e.getLeft());
		}
	}
	
	public void checkEnquiry(Enquiry e) {
		synchronized (LOCK) {
			if(e.getLeft() != 0) return;
			Category cat = Category.getCategory(e.getCategory());
			SelfBalancingBST root = null;
			SelfBalancingBSTNode node = null;
			
			if(e instanceof SellOffer) {
				root = cat.getSellOffers(e.getSubCategory(), e.getSubSubCategory());
				node = root.get(e.getPrice(), root.getRoot());
			}
			
			if(e instanceof BuyOrder) {
				root = cat.getBuyOrders(e.getSubCategory(), e.getSubSubCategory());
				node = root.get(e.getPrice(), root.getRoot());
			}
			
			
			
			if(node != null && e.getLeft() == 0) node.remove(e.getID()); 
			if(node != null && node.get().size() == 0) root.deleteKey(e.getPrice());
			if(e.getLeft() == 0) {
				EnquiryFillEvent filledEvent = new EnquiryFillEvent(e);
				EventManager.callEvent(filledEvent);
			}
			
			if(e.getLeft() == 0 && !e.hasClaimable()) {
				deleteEnquiryFromFile(e);
			}
		}
	}
	
	public void deleteEnquiryFromFile(Enquiry e) {
		synchronized (LOCK) {
			if(!(e instanceof SellOffer) && !(e instanceof BuyOrder)) return;
			File yamlFile = new File(BazaarSocketPlugin.FILE_PATH + "/player/" + e.getOwner().toString() + "/" + (e instanceof SellOffer ? "sell_offer.yml" : e instanceof BuyOrder ? "buy_order.yml" : ""));
			YamlFile fc = getYamlFile(yamlFile);
			fc.set(e.getID().toString(), null);
		}
	}
	
	public void deleteEnquiryFromFile(UUID owner, String uuid, EnquiryType type) {
		synchronized (LOCK) {
			File yamlFile = new File(BazaarSocketPlugin.FILE_PATH + "/player/" + owner.toString() + "/" + (type == EnquiryType.SELL_OFFER ? "sell_offer.yml" : "buy_order.yml"));
			YamlFile fc = getYamlFile(yamlFile);
			fc.set(uuid, null);
		}
	}
	
	public Enquiry getEnquiry(UUID id, int cat, int show, int sub, double price, EnquiryType type) {
		synchronized (LOCK) {
			Category category = Category.getCategory(cat);
			SelfBalancingBST tree = category.getTree(type, show, cat);
			if(tree.get(price, tree.getRoot()) == null) return null;
			return tree.get(price, tree.getRoot()).get(id);
		}
	}
	
	public boolean check(int cat, int a, int b) {
		return cat > 0 && cat < Category.MAX_CATEGORIES && a <= Category.MAX_SUB && a > 0 && b > 0 && b <= Category.MAX_SUB_SUB;
	}
	
	public void insert(Enquiry e) {
		synchronized (LOCK) {
			checkUUID(e.getOwner());
			if(e.getLeft() == 0) return;
			saveEnquiryAll(e);
			if(e instanceof SellOffer) {
				SellOffer so = (SellOffer) e;
				SelfBalancingBST tree = null;
				SelfBalancingBSTNode node_bo = null;
				tree = Category.getCategory(e.getCategory()).getBuyOrders(e.getSubCategory(), e.getSubSubCategory());
				node_bo = tree.get(e.getPrice(), tree.getRoot());
				if(node_bo != null && node_bo.getKey() == e.getPrice()) {
					while(node_bo.get().size() != 0 && so.getLeft() > 0) {
						process(so, (BuyOrder) node_bo.getByTimeStamp());
					}
				}
				if(e.getLeft() > 0) {
					Category.getCategory(e.getCategory()).getSellOffers(e.getSubCategory(), e.getSubSubCategory()).insert(e.getPrice(), e);
				}
			}
			if(e instanceof BuyOrder) {
				BuyOrder bo = (BuyOrder) e;
				SelfBalancingBST tree = null;
				SelfBalancingBSTNode node_so = null;
				tree = Category.getCategory(e.getCategory()).getSellOffers(e.getSubCategory(), e.getSubSubCategory());
				node_so = tree.get(e.getPrice(), tree.getRoot());
				if(node_so != null && node_so.getKey() == e.getPrice()) {
					while(node_so.get().size() != 0 && bo.getLeft() > 0) {
						process((SellOffer) node_so.getByTimeStamp(), bo);
					}
				}
				if(e.getLeft() > 0) {
					Category.getCategory(e.getCategory()).getBuyOrders(e.getSubCategory(), e.getSubSubCategory()).insert(e.getPrice(), e);
				}
			}
		}
	}
	
	private void process(final SellOffer so, final BuyOrder bo) {
		int reducable = bo.getLeft() > so.getLeft() ? so.getLeft() : bo.getLeft();
		reduce(bo, reducable);
		reduce(so, reducable);
		bo.addClaimable(reducable);
		so.addClaimable(reducable);
		saveEnquiry(bo);
		saveEnquiry(so);
		checkEnquiry(bo);
		checkEnquiry(so);
	}
	
	
	
	public boolean reduce(Enquiry e, int subtract) {
		e.left = e.left - subtract;
		return e.left >= 0;
	}
}
