package de.ancash.bazaar.management;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.management.Enquiry.EnquiryType;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.minecraft.XMaterial;
import de.ancash.misc.Validate;
import de.ancash.yaml.configuration.file.YamlFile;
import de.ancash.yaml.exceptions.InvalidConfigurationException;

public class EnquiryUtils {

	static EnquiryUtils instance;
	private static CompactMap<File, YamlFile> alreadyLoaded = new CompactMap<File, YamlFile>();
	
	public YamlFile getYamlFile(File file) {return alreadyLoaded.get(file);}
	public boolean isYamlFileLoaded(File file) {return alreadyLoaded.containsKey(file);}
	public void addYamlFile(File file, YamlFile fc) {alreadyLoaded.put(file, fc);}
	private Bazaar pl;
	
	public EnquiryUtils(Bazaar pl) {
		Validate.isTrue(this.pl == null);
		Validate.isTrue(instance == null);
		Validate.notNull(pl);
		this.pl = pl;
		instance = this;
	}
	
	public void load() {
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
			YamlFile yamlBuyOrder = new YamlFile(buyOrderFile);
			
			try {
				yamlBuyOrder.createOrLoad();
				yamlSellOffer.createOrLoad();
			} catch (InvalidConfigurationException | IOException e1) {
				e1.printStackTrace();
			}
			
			ArrayList<Enquiry> buy_order = get(yamlBuyOrder, EnquiryType.BUY_ORDER, UUID.fromString(p.getName()));
			buy_order.forEach(e -> insert(e));
			count = count + buy_order.size();
			
			ArrayList<Enquiry> sell_offer = get(yamlSellOffer, EnquiryType.SELL_OFFER, UUID.fromString(p.getName()));
			sell_offer.forEach(e -> insert(e));
			count = count + sell_offer.size();
			
			alreadyLoaded.put(sellOfferFile, yamlSellOffer);
			alreadyLoaded.put(buyOrderFile, yamlBuyOrder);
		}
		Chat.sendMessage("Loaded total of " + count + " Enquiries", ChatLevel.INFO);
	}
	
	public void save() {
		alreadyLoaded.forEach((file, fc) ->{
			try {
				fc.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	private ArrayList<Enquiry> get(YamlFile fc, EnquiryType type, UUID owner) {
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
						sub, fc.getInt(id + ".left"), fc.getLong(id + ".timestamp"),UUID.fromString(id), fc.getInt(id + ".claimable"),
						fc.getLong(id + ".lastEdit")));
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
						sub, fc.getInt(id + ".left"), fc.getLong(id + ".timestamp"),UUID.fromString(id), fc.getInt(id + ".claimable"),
						fc.getLong(id + ".lastEdit")));
			}
			
		}
		return e;
	}
	
	//XXX
	public void save(Enquiry e) {
		File yamlFile = null;
		if(e instanceof SellOffer) {
			yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/sell_offer.yml");
		} else if(e instanceof BuyOrder) {
			yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/buy_order.yml");
		} else {
			return;
		}
		YamlFile fc = alreadyLoaded.get(yamlFile);
		fc.set(e.getID().toString() + ".left", e.getLeft());
		fc.set(e.getID().toString() + ".claimable", e.getClaimable());
		fc.set(e.getID().toString() + ".lastEdit", e.getLastEdit());
	}
	
	//XXX
	public void saveAll(Enquiry e) {
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
		fc.set(e.getID().toString() + ".sub", e.getSub());
		fc.set(e.getID().toString() + ".subsub", e.getSubSub());
		fc.set(e.getID().toString() + ".amount", e.getAmount());
		fc.set(e.getID().toString() + ".timestamp", e.getTimeStamp());
		fc.set(e.getID().toString() + ".claimable", e.getClaimable());
		fc.set(e.getID().toString() + ".left", e.getLeft());
		fc.set(e.getID() + ".lastEdit", e.getLastEdit());
	}
	
	public void checkEnquiry(Enquiry e) {
		if(e.getLeft() != 0) return;
		Category cat = Category.getCategory(e.getCategory());
		SelfBalancingBST root = null;
		SelfBalancingBSTNode node = null;
		
		if(e instanceof SellOffer) {
			root = cat.getSellOffers(e.getSub(), e.getSubSub());
			node = root.get(e.getPrice(), root.getRoot());
		}
		
		if(e instanceof BuyOrder) {
			root = cat.getBuyOrders(e.getSub(), e.getSubSub());
			node = root.get(e.getPrice(), root.getRoot());
		}
		
		
		
		if(node != null && e.getLeft() == 0) node.remove(e.getID()); 
		if(node != null && node.get().size() == 0) root.deleteKey(e.getPrice());
		Player p = Bukkit.getPlayer(e.getOwner());
		if(e.getLeft() == 0) {
			if(p != null) {
				if(e instanceof SellOffer) {
					String name = cat.getSubSub()[e.getSub() - 1][e.getSubSub() - 1].getItemMeta().getDisplayName();
					p.sendMessage("§6[Bazaar] §eYour §aSell Offer §efor §a" + e.getAmount() + "§7x " + name + " §ewas filled!");
				}
				
				if(e instanceof BuyOrder) {
					String name = cat.getSubSub()[e.getSub() - 1][e.getSubSub() - 1].getItemMeta().getDisplayName();
					p.sendMessage("§6[Bazaar] §eYour §aBuy Order §efor §a" + e.getAmount() + "§7x " + name + " §ewas filled!");
				}
			}
		}
		
		if(e.getLeft() == 0 && !e.hasClaimable()) {
			deleteEnquiry(e);
		}
	}
	
	//XXX
	public void deleteEnquiry(Enquiry e) {
		if(!(e instanceof SellOffer) && !(e instanceof BuyOrder)) return;
		File yamlFile = new File("plugins/Bazaar/player/" + e.getOwner().toString() + "/" + (e instanceof SellOffer ? "sell_offer.yml" : e instanceof BuyOrder ? "buy_order.yml" : ""));
		YamlFile fc = alreadyLoaded.get(yamlFile);
		fc.set(e.getID().toString(), null);
	}
	
	//XXX
	public void delete(UUID owner, String uuid, EnquiryType type) {
		File yamlFile = new File("plugins/Bazaar/player/" + owner.toString() + "/" + (type == EnquiryType.SELL_OFFER ? "sell_offer.yml" : "buy_order.yml"));
		YamlFile fc = alreadyLoaded.get(yamlFile);
		fc.set(uuid, null);
	}
	
	public Enquiry getEnquiry(UUID id, int cat, int show, int sub, double price, EnquiryType type) {
		Category category = Category.getCategory(cat);
		SelfBalancingBST tree = category.getTree(type, show, cat);
		if(tree.get(price, tree.getRoot()) == null) return null;
		return tree.get(price, tree.getRoot()).get(id);
	}
	
	public boolean check(int cat, int a, int b) {
		if(!Category.exists(cat)) {
			Chat.sendMessage("Cannot load Item(" + a + ", " + b + ") for non-existing Category(" + cat + ")!", ChatLevel.WARN);
			return false;
		}
		return a <= 18 && a > 0 && b >0 && b <= 9 && !Category.getCategory(cat).getSubSub()[a - 1][b - 1].getType().equals(XMaterial.AIR.parseMaterial());
	}
	
	public void insert(Enquiry e) {
		if(e.getLeft() == 0) return;
		saveAll(e);
		if(e instanceof SellOffer) {
			SellOffer so = (SellOffer) e;
			SelfBalancingBST tree = null;
			SelfBalancingBSTNode node_bo = null;
			tree = Category.getCategory(e.getCategory()).getBuyOrders(e.getSub(), e.getSubSub());
			node_bo = tree.get(e.getPrice(), tree.getRoot());
			if(node_bo != null && node_bo.getKey() == e.getPrice()) {
				while(node_bo.get().size() != 0 && so.getLeft() > 0) {
					process(so, (BuyOrder) node_bo.getByTimeStamp());
				}
			}
			if(e.getLeft() > 0) {
				Category.getCategory(e.getCategory()).getSellOffers(e.getSub(), e.getSubSub()).insert(e.getPrice(), e);
			}
		}
		if(e instanceof BuyOrder) {
			BuyOrder bo = (BuyOrder) e;
			SelfBalancingBST tree = null;
			SelfBalancingBSTNode node_so = null;
			tree = Category.getCategory(e.getCategory()).getSellOffers(e.getSub(), e.getSubSub());
			node_so = tree.get(e.getPrice(), tree.getRoot());
			if(node_so != null && node_so.getKey() == e.getPrice()) {
				while(node_so.get().size() != 0 && bo.getLeft() > 0) {
					process((SellOffer) node_so.getByTimeStamp(), bo);
				}
			}
			if(e.getLeft() > 0) {
				Category.getCategory(e.getCategory()).getBuyOrders(e.getSub(), e.getSubSub()).insert(e.getPrice(), e);
			}
		}
	}
	
	public void process(final SellOffer so, final BuyOrder bo) {
		int reducable = bo.getLeft() > so.getLeft() ? so.getLeft() : bo.getLeft();
		reduce(bo, reducable);
		reduce(so, reducable);
		bo.addClaimable(reducable);
		so.addClaimable(reducable);
		bo.setLastEdit(System.nanoTime());
		so.setLastEdit(System.nanoTime());
		save(bo);
		save(so);
		checkEnquiry(bo);
		checkEnquiry(so);
	}
	
	public boolean reduce(Enquiry e, int subtract) {
		e.left = e.left - subtract;
		return e.left >= 0;
	}
}
