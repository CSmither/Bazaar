package de.ancash.bazaar.sockets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.management.BuyOrder;
import de.ancash.bazaar.management.Enquiry;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.management.SellOffer;
import de.ancash.bazaar.management.Enquiry.EnquiryType;
import de.ancash.bazaar.sockets.packets.BazaarMessagePacket;
import de.ancash.bazaar.sockets.packets.BazaarPacket;
import de.ancash.bazaar.utils.Chat;
import de.ancash.bazaar.utils.Chat.ChatLevel;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.misc.MathsUtils;

import static de.ancash.bazaar.utils.Chat.sendMessage;

public class EnquiryUtils {

private final CompactMap<String, YamlFile> loadedFiles = new CompactMap<>();
	
	private SelfBalancingBST[][][] BUY_ORDERS = new SelfBalancingBST[5][18][9];
	private SelfBalancingBST[][][] SELL_OFFERS = new SelfBalancingBST[5][18][9];
	
	private static final String DIR = "storage/Bazaar/player/";
	private static final String SELL_OFFER_FILE_NAME = "sell_offer.yml";
	private static final String BUY_ORDER_FILE_NAME = "buy_order.yml";
	
	private final Bazaar pl;
	
	EnquiryUtils(Bazaar pl) {
		this.pl = pl;
		load();
	}
	
	void reload() {
		save();
		load();
	}
	
	private void load() {
		sendMessage("Loading Files from storage!", ChatLevel.INFO);
		for(int c = 0; c<5; c++) {
			for(int s = 0; s<18; s++) {
				for(int ss = 0; ss<9; ss++) {
					BUY_ORDERS[c][s][ss] = new SelfBalancingBST();
					SELL_OFFERS[c][s][ss] = new SelfBalancingBST();
				}
			}
		}
		File playerDir = new File(DIR);
		if(!playerDir.exists()) playerDir.mkdirs();
		if(playerDir.listFiles().length == 0) {
			Chat.sendMessage("No Files found!", ChatLevel.INFO);
			return;
		}
		int count = 0;
		
		for(File p : playerDir.listFiles()) {
			File sellOfferFile = new File(DIR + p.getName() + "/" + SELL_OFFER_FILE_NAME);
			File buyOrderFile = new File(DIR + p.getName() + "/" + BUY_ORDER_FILE_NAME);
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
			
			loadedFiles.put(p.getName() + EnquiryType.SELL_OFFER, yamlSellOffer);
			loadedFiles.put(p.getName() + EnquiryType.BUY_ORDER, yamlBuyOrder);
		}
		sendMessage("Loaded total of " + count + " Enquiries", ChatLevel.INFO);
	}

	void checkFiles(UUID player) throws IOException, InvalidConfigurationException {
		YamlFile sellOfferFile = new YamlFile(DIR + player + "/" + SELL_OFFER_FILE_NAME);
		sellOfferFile.createOrLoad();
		YamlFile buyOrderFile = new YamlFile(DIR + player + "/" + BUY_ORDER_FILE_NAME);
		buyOrderFile.createOrLoad();
		loadedFiles.put(player.toString() + EnquiryType.BUY_ORDER, buyOrderFile);
		loadedFiles.put(player.toString() + EnquiryType.SELL_OFFER, sellOfferFile);
	}
	
	void save() {
		loadedFiles.forEach((file, fc) ->{try {fc.save();} catch (IOException e) {e.printStackTrace();}});
		loadedFiles.clear();
	}
	
	private ArrayList<Enquiry> get(YamlFile fc, EnquiryType type, UUID owner) {
		ArrayList<Enquiry> e = new ArrayList<Enquiry>();
		if(type == EnquiryType.BUY_ORDER) {
			Iterator<String> ids = fc.getKeys(false).iterator();
			String id = null;
			while(ids.hasNext()) {
				id = ids.next();
				if(fc.getString(id) == null) continue;
				int category = Integer.valueOf(fc.getInt(id + ".category"));
				int show = fc.getInt(id + ".sub");
				int sub = fc.getInt(id + ".subsub");
				if(fc.getInt(id + ".left") == 0) continue;
				e.add(new BuyOrder(fc.getInt(id + ".amount"), fc.getDouble(id + ".price"), owner,category, show,
						sub, fc.getInt(id + ".left"), fc.getLong(id + ".timestamp"),UUID.fromString(id), fc.getInt(id + ".claimable"),
						fc.getLong(id + ".lastEdit")));
			}
		}
		if(type == EnquiryType.SELL_OFFER) {
			Iterator<String> ids = fc.getKeys(false).iterator();
			String id = null;
			while(ids.hasNext()) {
				id = ids.next();
				if(fc.getString(id) == null) continue;
				int category = Integer.valueOf(fc.getInt(id + ".category"));
				int show = fc.getInt(id + ".sub");
				int sub = fc.getInt(id + ".subsub");
				if(fc.getInt(id + ".left") == 0) continue;
				e.add(new SellOffer(fc.getInt(id + ".amount"), fc.getDouble(id + ".price"), owner,category, show,
						sub, fc.getInt(id + ".left"), fc.getLong(id + ".timestamp"),UUID.fromString(id), fc.getInt(id + ".claimable"),
						fc.getLong(id + ".lastEdit")));
			}
			
		}
		return e;
	}
	
	List<BazaarPacket> insert(Enquiry e) {
		List<BazaarPacket> resps = new ArrayList<>();
		
		resps.add(new BazaarMessagePacket("§c§lInvalid Enquiry!", e.getOwner(), true, false));
		
		if(e.getLeft() <= 0) {
			return resps;
		}
		try {
			checkFiles(e.getOwner());
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
			return resps;
		}
		if(!canInsert(e)) {
			resps.clear();
			resps.add(new BazaarMessagePacket(pl.getResponse().CANNOT_CREATE_ENQUIRY, e.getOwner(), true, false));
			return resps;
		}
		saveAll(e);
		if(e instanceof SellOffer) {
			resps.clear();
			resps.add(new BazaarMessagePacket(pl.getResponse().SELL_OFFER_SETUP
					.replace("%amount%", e.getAmount() + "")
					.replace("%price%", e.getPrice() + ""), e.getOwner(), true, false, e.getCategory(), e.getSub(), e.getSubSub()));
			SellOffer so = (SellOffer) e;
			SelfBalancingBST tree = getBuyOrderSubSub(e.getCategory(), e.getSub(), e.getSubSub());
			SelfBalancingBSTNode node_bo = null;
			node_bo = tree.get(e.getPrice(), tree.getRoot());
			if(node_bo != null && node_bo.getKey() == e.getPrice()) {
				while(node_bo.get().size() != 0 && so.getLeft() > 0) {
					resps.addAll(process(so, (BuyOrder) node_bo.getByTimeStamp()));
				}
			}
			if(e.getLeft() > 0) {
				getSellOfferSubSub(e.getCategory(), e.getSub(), e.getSubSub()).insert(e.getPrice(), e);
			}
		}
		if(e instanceof BuyOrder) {
			resps.clear();
			resps.add(new BazaarMessagePacket(pl.getResponse().BUY_ORDER_SETUP
					.replace("%amount%", e.getAmount() + "")
					.replace("%price%", e.getPrice() + ""), e.getOwner(), true, false, e.getCategory(), e.getSub(), e.getSubSub()));
			BuyOrder bo = (BuyOrder) e;
			SelfBalancingBST tree = getSellOfferSubSub(e.getCategory(), e.getSub(), e.getSubSub());
			SelfBalancingBSTNode node_so = null;
			node_so = tree.get(e.getPrice(), tree.getRoot());
			if(node_so != null && node_so.getKey() == e.getPrice()) {
				while(node_so.get().size() != 0 && bo.getLeft() > 0) {
					resps.addAll(process((SellOffer) node_so.getByTimeStamp(), bo));
				}
			}
			if(e.getLeft() > 0) {
				getBuyOrderSubSub(e.getCategory(), e.getSub(), e.getSubSub()).insert(e.getPrice(), e);
			}
		}
		return resps;
	}
	
	private boolean canInsert(Enquiry e) {
		return loadedFiles.get(e.getOwner().toString() + e.getType()).getKeys(false).size() < 27;
	}

	private List<BazaarPacket> process(final SellOffer so, final BuyOrder bo) {
		List<BazaarPacket> bps = new ArrayList<>();
		int reducable = bo.getLeft() > so.getLeft() ? so.getLeft() : bo.getLeft();
		reduce(bo, reducable);
		reduce(so, reducable);
		bo.addClaimable(reducable);
		so.addClaimable(reducable);
		bo.setLastEdit(System.nanoTime());
		so.setLastEdit(System.nanoTime());
		saveEnquiry(bo);
		saveEnquiry(so);
		Optional.ofNullable(checkEnquiry(bo)).ifPresent(bps::add);
		Optional.ofNullable(checkEnquiry(so)).ifPresent(bps::add);
		return bps;
	}
	
	private void saveAll(Enquiry e) {
		YamlFile fc = loadedFiles.get(e.getOwner() + "" + e.getType());
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
	}
	
	private BazaarMessagePacket checkEnquiry(Enquiry e) {
		BazaarMessagePacket bmp = null;
		if(e.getLeft() != 0) return bmp;
		SelfBalancingBST root = null;
		SelfBalancingBSTNode node = null;
		
		if(e instanceof SellOffer) {
			root = getSellOfferSubSub(e.getCategory(), e.getSub(), e.getSubSub());
			node = root.get(e.getPrice(), root.getRoot());
		}
		
		if(e instanceof BuyOrder) {
			root = getBuyOrderSubSub(e.getCategory(), e.getSub(), e.getSubSub());
			node = root.get(e.getPrice(), root.getRoot());
		}
		
		if(node != null && e.getLeft() == 0) node.remove(e.getID()); 
		if(node != null && node.get().size() == 0) root.deleteKey(e.getPrice());
		if(e.getLeft() == 0) {
			if(e instanceof SellOffer) {
				bmp = new BazaarMessagePacket("§6[Bazaar] §eYour §aSell Offer §efor §a" + e.getAmount() + "§7x %displayname% §ewas filled!",
						e.getOwner(), true, false, e.getCategory(), e.getSub(), e.getSubSub());
			} else {
				bmp = new BazaarMessagePacket("§6[Bazaar] §eYour §aBuy Order §efor §a" + e.getAmount() + "§7x %displayname% §ewas filled!", 
						e.getOwner(), true, false, e.getCategory(), e.getSub(), e.getSubSub());
			}
		}
		if(e.getLeft() == 0 && !e.hasClaimable()) {
			deleteEnquiry(e);
		}
		return bmp;
	}
	
	double getClaimableCoins(UUID player) {
		YamlFile file = loadedFiles.get(player.toString() + EnquiryType.SELL_OFFER);
		double coins = 0;
		for(String id : file.getKeys(false)) {
			coins += file.getDouble(id + ".claimable") * file.getDouble(id + ".price");
		}
		return MathsUtils.round(coins, 2);
	}
	
	int getClaimableItems(UUID player) {
		YamlFile file = loadedFiles.get(player.toString() + EnquiryType.BUY_ORDER);
		int items = 0;
		for(String id : file.getKeys(false)) {
			items += file.getInt(id + ".claimable");
		}
		return items;
	}
	
	int getSellOffers(UUID player) {
		return loadedFiles.get(player.toString() + EnquiryType.SELL_OFFER).getKeys(false).size();
	}
	
	int getBuyOrders(UUID player) {
		return loadedFiles.get(player.toString() + EnquiryType.BUY_ORDER).getKeys(false).size();
	}
	
	SelfBalancingBST[] getSellOfferSub(int category, int sub) {
		return SELL_OFFERS[category - 1][sub - 1];
	}
	
	SelfBalancingBST[] getBuyOrderSub(int category, int sub) {
		return BUY_ORDERS[category - 1][sub - 1];
	}
	
	SelfBalancingBST getSellOfferSubSub(int category, int sub, int subsub) {
		return SELL_OFFERS[category - 1][sub - 1][subsub - 1];
	}
	
	SelfBalancingBST getBuyOrderSubSub(int category, int sub, int subsub) {
		return BUY_ORDERS[category - 1][sub - 1][subsub - 1];
	}
	
	void deleteEnquiry(Enquiry e) {
		YamlFile fc = loadedFiles.get(e.getOwner().toString() + e.getType());
		fc.set(e.getID().toString(), null);
	}
	
	void deleteEnquiry(UUID owner, String uuid, EnquiryType type) {
		YamlFile fc = loadedFiles.get(owner + "" + type);
		fc.set(uuid, null);
	}
	
	private void reduce(Enquiry e, int subtract) {
		e.setLeft(e.getLeft() - subtract);
	}
	
	private void saveEnquiry(Enquiry e) {
		YamlFile fc = loadedFiles.get(e.getOwner().toString() + e.getType());
		fc.set(e.getID().toString() + ".left", e.getLeft());
		fc.set(e.getID().toString() + ".claimable", e.getClaimable());
		fc.set(e.getID().toString() + ".lastEdit", e.getLastEdit());
	}	
}
