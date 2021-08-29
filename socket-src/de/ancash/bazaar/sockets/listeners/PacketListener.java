package de.ancash.bazaar.sockets.listeners;

import de.ancash.sockets.events.ServerPacketReceiveEvent;
import de.ancash.libs.org.bukkit.event.EventHandler;
import de.ancash.libs.org.bukkit.event.Listener;
import de.ancash.sockets.packet.Packet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.simpleyaml.configuration.file.YamlFile;

import de.ancash.misc.MathsUtils;
import de.ancash.Sockets;
import de.ancash.bazaar.sockets.BazaarSocketPlugin;
import de.ancash.bazaar.sockets.eqnuiry.BuyOrder;
import de.ancash.bazaar.sockets.management.Category;
import de.ancash.bazaar.sockets.management.SelfBalancingBST;
import de.ancash.bazaar.sockets.management.SelfBalancingBSTNode;
import de.ancash.bazaar.sockets.management.SellOffer;
import de.ancash.bazaar.sockets.packets.BazaarHeader;
import de.ancash.bazaar.sockets.packets.BazaarRequestPacket;
import de.ancash.bazaar.sockets.packets.BazaarResponsePacket;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.datastructures.tuples.Tuple;

public class PacketListener implements Listener{
		
	private final BazaarSocketPlugin pl;
	
	public PacketListener(BazaarSocketPlugin pl) {
		this.pl = pl;
	}
	
	@EventHandler
	public void onPacket(ServerPacketReceiveEvent event) {
		Packet packet = event.getPacket();
		if(packet.getSerializable() instanceof BazaarRequestPacket) {
			
			
			BazaarRequestPacket brp = (BazaarRequestPacket) packet.getSerializable();
			BazaarResponsePacket resp = null;
			Map<String, Serializable> map = new HashMap<>();
			SellOffer sellOffer = null;
			BuyOrder buyOrder = null;
			
			
			switch (packet.getHeader()) {
			case BazaarHeader.GET_PLACEHOLDERS:
				
				Map<String, String> placeholder = getPlaceholders(brp.getCategory(), brp.getSub(), brp.getSubSub(), 
						brp.getTopEnquiries(), brp.getTopSellOffer(), brp.getTopBuyOrder(), brp.getEmptyPrice(), brp.getPlayer());
				resp = new BazaarResponsePacket((Serializable) placeholder, packet.getTimeStamp());
				Sockets.write(resp.getPacket(), event.getKey());
				break;
			case BazaarHeader.CREATE_SELL_OFFER:
				Sockets.write(new BazaarResponsePacket(new HashMap<>(), packet.getTimeStamp()).getPacket(), event.getKey());
				sellOffer = new SellOffer(brp.getAmount(), brp.getPrice(), brp.getPlayer(), brp.getCategory(), brp.getSub(), brp.getSubSub());
				pl.getEnquiryUtils().insert(sellOffer);
				break;
			case BazaarHeader.CREATE_BUY_ORDER:	
				Sockets.write(new BazaarResponsePacket(new HashMap<>(), packet.getTimeStamp()).getPacket(), event.getKey());
				buyOrder= new BuyOrder(brp.getAmount(), brp.getPrice(), brp.getPlayer(), brp.getCategory(), brp.getSub(), brp.getSubSub());
				pl.getEnquiryUtils().insert(buyOrder);
				break;
			case BazaarHeader.GET_LOWEST_SELL_OFFER:
				if(Category.getCategory(brp.getCategory()).getSellOffers(brp.getSub(), brp.getSubSub()).isEmpty()) {
					resp = new BazaarResponsePacket(-1D, packet.getTimeStamp());
				} else {
					resp = new BazaarResponsePacket(Category.getCategory(brp.getCategory()).
							getSellOffers(brp.getSub(), brp.getSubSub()).getMin().getKey(), packet.getTimeStamp());
				}
				Sockets.write(resp.getPacket(), event.getKey());
				break;
			case BazaarHeader.GET_HIGHEST_SELL_OFFER:
				if(Category.getCategory(brp.getCategory()).getSellOffers(brp.getSub(), brp.getSubSub()).isEmpty()) {
					resp = new BazaarResponsePacket(-1D, packet.getTimeStamp());
				} else {
					resp = new BazaarResponsePacket(Category.getCategory(brp.getCategory()).
							getSellOffers(brp.getSub(), brp.getSubSub()).getMax().getKey(), packet.getTimeStamp());
				}
				Sockets.write(resp.getPacket(), event.getKey());
				break;
			case BazaarHeader.GET_HIGHEST_BUY_ORDER:
				if(Category.getCategory(brp.getCategory()).getBuyOrders(brp.getSub(), brp.getSubSub()).isEmpty()) {
					resp = new BazaarResponsePacket(-1D, packet.getTimeStamp());
				} else {
					resp = new BazaarResponsePacket(Category.getCategory(brp.getCategory()).
							getBuyOrders(brp.getSub(), brp.getSubSub()).getMax().getKey(), packet.getTimeStamp());
				}
				Sockets.write(resp.getPacket(), event.getKey());
				break;
			case BazaarHeader.GET_LOWEST_BUY_ORDER:
				if(Category.getCategory(brp.getCategory()).getBuyOrders(brp.getSub(), brp.getSubSub()).isEmpty()) {
					resp = new BazaarResponsePacket(-1D, packet.getTimeStamp());
				} else {
					resp = new BazaarResponsePacket(Category.getCategory(brp.getCategory()).
							getBuyOrders(brp.getSub(), brp.getSubSub()).getMin().getKey(), packet.getTimeStamp());
				}
				Sockets.write(resp.getPacket(), event.getKey());
				break;
			case BazaarHeader.GET_CLAIMABLE:
				resp = new BazaarResponsePacket(Tuple.of(getSellOffer(brp.getPlayer()), getBuyOrder(brp.getPlayer())), packet.getTimeStamp());
				Sockets.write(resp.getPacket(), event.getKey());
				break;
			case BazaarHeader.CLAIM_ENQUIRY:
				YamlFile file = null;
				int claim = 0;
				synchronized (pl.getEnquiryUtils().getLock()) {
					if(brp.isBuyOrder()) {
						file = pl.getEnquiryUtils().getYamlFile(pl.getEnquiryUtils().getBuyOrderFile(brp.getPlayer()));
						claim = file.getInt(brp.getUUID() + ".claimable");
						if(claim > brp.getAmount()) claim = brp.getAmount();
					} else {
						file = pl.getEnquiryUtils().getYamlFile(pl.getEnquiryUtils().getSellOfferFile(brp.getPlayer()));
						claim = file.getInt(brp.getUUID() + ".claimable");
					}
					file.set(brp.getUUID() + ".claimable", file.getInt(brp.getUUID() + ".claimable") - claim);
					map.put("amount", claim);
					if(file.getInt(brp.getUUID() + ".claimable") == 0) file.set(brp.getUUID().toString(), null);
				}
				Sockets.write(new BazaarResponsePacket((Serializable) map, packet.getTimeStamp()).getPacket(), event.getKey());
				break;
			case BazaarHeader.BUY_INSTANTLY:
				int buying = brp.getAmount();
				double buyPrice = brp.getPrice();
				SelfBalancingBST sellOfferTree = Category.getCategory(brp.getCategory()).getSellOffers(brp.getSub(), brp.getSubSub());
				SelfBalancingBSTNode sellOfferNode = sellOfferTree.get(buyPrice, sellOfferTree.getRoot());
				if(sellOfferNode == null || sellOfferNode.getKey() != buyPrice) {
					map.put("message", "§cToo much fluctuation!");
				} else {
					synchronized (pl.getEnquiryUtils().getLock()) {
						
						sellOffer = (SellOffer) sellOfferNode.getByTimeStamp();
						int left = buying;
						
						while(sellOffer != null && left > 0) {
							int reducable = sellOffer.getLeft();
							if(reducable > left) reducable = left;
							left -= reducable;
							sellOffer.addClaimable(reducable);
							sellOffer.setLeft(sellOffer.getLeft() - reducable);
							pl.getEnquiryUtils().saveEnquiry(sellOffer);
							if(sellOffer.getLeft() == 0) {
								pl.getEnquiryUtils().checkEnquiry(sellOffer);
								if(sellOfferNode.get().isEmpty()) break;
								sellOffer = (SellOffer) sellOfferNode.getByTimeStamp();
							}
						}
						map.put("amount", buying - left);
					}
				}
				Sockets.write(new BazaarResponsePacket((Serializable) map, packet.getTimeStamp()).getPacket(), event.getKey());
				break;
			case BazaarHeader.SELL_INSTANTLY:
				int selling = brp.getAmount();
				double sellPrice = brp.getPrice();
				SelfBalancingBST buyOrderTree = Category.getCategory(brp.getCategory()).getBuyOrders(brp.getSub(), brp.getSubSub());
				SelfBalancingBSTNode buyOrderNode = buyOrderTree.get(sellPrice, buyOrderTree.getRoot());
				if(buyOrderNode == null || buyOrderNode.getKey() != sellPrice) {
					map.put("message", "§cToo much fluctuation!");
				} else {
					synchronized (pl.getEnquiryUtils().getLock()) {
						
						buyOrder = (BuyOrder) buyOrderNode.getByTimeStamp();
						int left = selling;
						
						while(buyOrder != null && left > 0) {
							int reducable = buyOrder.getLeft();
							if(reducable > left) reducable = left;
							left -= reducable;
							buyOrder.addClaimable(reducable);
							buyOrder.setLeft(buyOrder.getLeft() - reducable);
							pl.getEnquiryUtils().saveEnquiry(buyOrder);
							if(buyOrder.getLeft() == 0) {
								pl.getEnquiryUtils().checkEnquiry(buyOrder);
								if(buyOrderNode.get().isEmpty()) break;
								buyOrder = (BuyOrder) buyOrderNode.getByTimeStamp();
							}
						}
						map.put("amount", selling - left);
					}
				}
				Sockets.write(new BazaarResponsePacket((Serializable) map, packet.getTimeStamp()).getPacket(), event.getKey());
				break;
			default:
				break;
			}
		}
	}	
	
	public Map<String, Map<String, Number>> getSellOffer(UUID player) {
		Map<String, Map<String, Number>> allSellOffer = new HashMap<String, Map<String, Number>>();
		YamlFile fc = pl.getEnquiryUtils().getYamlFile(pl.getEnquiryUtils().getSellOfferFile(player));
		for(String id : fc.getKeys(false)) {
			if(fc.getString(id) != null && fc.getString(id).equals("null")) continue;
			int total = fc.getInt(id + ".amount");
			double price = fc.getDouble(id + ".price");
			int category = fc.getInt(id + ".category");
			int item_id = fc.getInt(id + ".item_id");
			int claimable = fc.getInt(id + ".claimable");
			int show = fc.getInt(id + ".sub");
			int sub = fc.getInt(id + ".subsub");
			
			Map<String, Number> datas = new HashMap<String, Number>();
			datas.put("total", total);
			datas.put("price", price);
			datas.put("category", category);
			datas.put("item_id", item_id);
			datas.put("show", show);
			datas.put("sub", sub);
			
			if(fc.getInt(id + ".left") == 0) {
				datas.put("left", 0);
				datas.put("claimable", claimable);
			} else {
				SelfBalancingBST root = Category.getCategory(category).getSellOffers(show, sub);
				SelfBalancingBSTNode sellOffer = root.get(fc.getDouble(id + ".price"), root.getRoot());
				SellOffer sO = (SellOffer) sellOffer.get(UUID.fromString(id));
				if(sO == null) {
					System.err.println("NPE While Getting All Sell OffersFor A Player! (" + category + ", " + show + ", " + sub + ". Deleting it...");
					fc.set(id, null);
					continue;
				}
				datas.put("left", sO.getLeft());
				datas.put("claimable", sO.getClaimable());
			}
			allSellOffer.put(id, datas);
		}
		return allSellOffer;
	}
	
	public Map<String, Map<String, Number>> getBuyOrder(UUID player) {
		Map<String, Map<String, Number>> allBuyOrder = new HashMap<String, Map<String, Number>>();
		YamlFile fc = pl.getEnquiryUtils().getYamlFile(pl.getEnquiryUtils().getBuyOrderFile(player));
		for(String id : fc.getKeys(false)) {
			if(fc.getString(id) != null && fc.getString(id).equals("null")) continue;
			int total = fc.getInt(id + ".amount");
			double price = fc.getDouble(id + ".price");
			int category = fc.getInt(id + ".category");
			int item_id = fc.getInt(id + ".item_id");
			int claimable = fc.getInt(id + ".claimable");
			int sub = fc.getInt(id + ".sub");
			int subsub = fc.getInt(id + ".subsub");
			
			Map<String, Number> datas = new HashMap<String, Number>();
			datas.put("total", total);
			datas.put("price", price);
			datas.put("category", category);
			datas.put("item_id", item_id);
			datas.put("show", sub);
			datas.put("sub", subsub);
			
			if(fc.getInt(id + ".left") == 0) {
				datas.put("left", 0);
				datas.put("claimable", claimable);
			} else {
				SelfBalancingBST root = Category.getCategory(category).getBuyOrders(sub, subsub);
				SelfBalancingBSTNode buyOrder = root.get(fc.getDouble(id + ".price"), root.getRoot());
				if(buyOrder == null || buyOrder.get(UUID.fromString(id)) == null) {
					System.err.println("NPE While Getting All Buy Orders For A Player! (" + category + ", " + sub + ", " + subsub + ". Deleting it...");
					fc.set(id, null);
					continue;
				}
				BuyOrder sO = (BuyOrder) buyOrder.get(UUID.fromString(id));
				datas.put("left", sO.getLeft());
				datas.put("claimable", sO.getClaimable());
			}
			allBuyOrder.put(id, datas);
		}
		return allBuyOrder;
	}
	
	/**
	 * Get all placeholders for enquires
	 * 
	 * @param cat
	 * @param sub
	 * @param subsub
	 * @return
	 */
	public Map<String, String> getPlaceholders(int cat, int sub, int subsub, boolean getTopEnquiries, int tSOcnt, int tBOcnt, double emptyprice, UUID uuid) {
		Map<String, String> placeholder = new HashMap<>();

		if(cat > 0) {
			Category category = Category.getCategory(cat);
			
			List<SelfBalancingBST> allBuyOrders = subsub != -1 ? Arrays.asList(category.getBuyOrders(sub, subsub)): category.getAllSubBuyOrders(sub);
			List<SelfBalancingBST> allSellOffers = subsub != -1 ? Arrays.asList(category.getSellOffers(sub, subsub)) : category.getAllSubSellOffers(sub);
			
			for(SelfBalancingBST tree : allBuyOrders) 
				getTreeInfo(tree, placeholder, "orders");
			
			for(SelfBalancingBST tree : allSellOffers) 
				getTreeInfo(tree, placeholder, "offers");
			
			if(subsub > 0) {
				SelfBalancingBST sellOffer = category.getSellOffers(sub, subsub);
				SelfBalancingBST buyOrder = category.getBuyOrders(sub, subsub);
				double sellOfferLowest = sellOffer.isEmpty() ? 0 : sellOffer.getMin().getKey();
				double sellOfferHighest= sellOffer.isEmpty() ? 0 : sellOffer.getMax().getKey();
				double buyOrderLowest = buyOrder.isEmpty() ? 0 : buyOrder.getMin().getKey();
				double buyOrderHighest= buyOrder.isEmpty() ? 0 : buyOrder.getMax().getKey();
				placeholder.put(BazaarPlaceholder.OFFERS_PRICE_LOWEST, sellOfferLowest == 0 ? "§cN/A" : "" + sellOfferLowest);
				placeholder.put(BazaarPlaceholder.OFFERS_PRICE_HIGHEST, sellOfferHighest == 0 ? "§cN/A" : "" + sellOfferHighest);
				placeholder.put(BazaarPlaceholder.ORDERS_PRICE_LOWEST, buyOrderLowest == 0 ? "§cN/A" : "" + buyOrderLowest);
				placeholder.put(BazaarPlaceholder.ORDERS_PRICE_HIGHEST, buyOrderHighest == 0 ? "§cN/A" : "" + buyOrderHighest);
				placeholder.put(BazaarPlaceholder.OFFERS_PRICE_LOWEST_STACK, sellOfferLowest == 0 ? "§cN/A" : "" + MathsUtils.round(sellOfferLowest * 64, 2));
				placeholder.put(BazaarPlaceholder.OFFERS_PRICE_HIGHEST_STACK, sellOfferHighest == 0 ? "§cN/A" : "" + MathsUtils.round(sellOfferHighest * 64, 2));
				placeholder.put(BazaarPlaceholder.ORDERS_PRICE_LOWEST_STACK, buyOrderLowest == 0 ? "§cN/A" : "" + MathsUtils.round(buyOrderLowest * 64, 2));
				placeholder.put(BazaarPlaceholder.ORDERS_PRICE_HIGHEST_STACK, buyOrderHighest == 0 ? "§cN/A" : "" + MathsUtils.round(buyOrderHighest * 64, 2));
				if(getTopEnquiries)
					getTopBuyOrders(placeholder, tBOcnt, cat, sub, subsub);
				if(getTopEnquiries)
					getTopSellOffers(placeholder, tSOcnt, cat, sub, subsub);
			}
		}
		
		placeholder.put(BazaarPlaceholder.ENQUIRIES, pl.getEnquiryUtils().countEnquiries(uuid) + "");
		placeholder.put(BazaarPlaceholder.CLAIMABLE_COINS, pl.getEnquiryUtils().getClaimableCoins(uuid) + "");
		placeholder.put(BazaarPlaceholder.CLAIMABLE_ITEMS, pl.getEnquiryUtils().getClaimableItems(uuid) +  "");
		
		return placeholder;
	}
	
	private Map<String, String> getTopBuyOrders(Map<String, String> placeholder, int cnt, int cat, int sub, int subsub) {
		SelfBalancingBST rootBuyOrder = Category.getCategory(cat).getBuyOrders(sub, subsub);
		StringBuilder builder = new StringBuilder();
		for(int i = 1; i<=cnt; i++) {
			SelfBalancingBSTNode kthLargest = SelfBalancingBST.KthLargestUsingMorrisTraversal(rootBuyOrder.getRoot(), i);
			double value = kthLargest == null ? -1D : MathsUtils.round(kthLargest.getKey(), 1);
			if(value == -1D) break; 
			builder.append("§8- §6");
			SelfBalancingBSTNode node = rootBuyOrder.get(value, rootBuyOrder.getRoot());
			builder.append(value + " coins §7each §f: §a" + node.sum() + "§7x from §f" + (node.get().size() != 1 ? node.get().size() + " §7orders": "1 §7order"));
			builder.append("\n");
		}
		if(builder.toString().endsWith("\n")) {
			placeholder.put("%top_orders_" + cnt + "%", builder.toString().substring(0, builder.toString().length() - 1));
		} else {
			placeholder.put("%top_orders_" + cnt + "%", builder.toString());
		}
		return placeholder;
	}
	
	private Map<String, String> getTopSellOffers(Map<String, String> placeholder, int cnt, int cat, int sub, int subsub) {
		SelfBalancingBST rootSellOffer = Category.getCategory(cat).getSellOffers(sub, subsub);
		StringBuilder builder = new StringBuilder();
		for(int i = 1; i<=cnt; i++) {
			double value = SelfBalancingBST.kthSmallest(rootSellOffer.getRoot(), i);
			if(value == -1D) break;
			StringBuilder temp = new StringBuilder();
			temp.append("§8- §6");
			SelfBalancingBSTNode node = rootSellOffer.get(value, rootSellOffer.getRoot());
			temp.append(value + " coins §7each §f: §a" + node.sum() + "§7x from §f" + (node.get().size() != 1 ? node.get().size() + " §7offers": "1 §7offer"));
			temp.append("\n");
			if(!builder.toString().contains(temp.toString())) {
				builder.append(temp.toString());
			}
		}
		if(builder.toString().endsWith("\n")) {
			placeholder.put("%top_offers_" + cnt + "%", builder.toString().substring(0, builder.toString().length() - 1));
		} else {
			placeholder.put("%top_offers_" + cnt + "%", builder.toString());
		}
		return placeholder;
	}
		
	/**
	 * 
	 * @param tree
	 * @param map
	 * @param type
	 */
	private void getTreeInfo(SelfBalancingBST tree, Map<String, String> map, String type) {
		add(map, "%" + type + "_content%", tree == null || tree.isEmpty()? 0 : tree.getAllContents());
		add(map, "%" + type + "_total%", tree == null || tree.isEmpty() ? 0 : tree.getEnquiryCount());
	}
	
	/**
	 * 
	 * @param map
	 * @param key
	 * @param toAdd
	 */
	private void add(Map<String, String> map, String key, int toAdd) {
		if(map.containsKey(key)) {
			map.put(key, (Integer.valueOf(map.get(key)) + toAdd) + "");
		} else {
			map.put(key, toAdd + "");
		}
	}

	public double getHighestBuyOrderPrice(int cat, int sub, int subsub, int emptyprice) {
		SelfBalancingBSTNode max = Category.getCategory(cat).getBuyOrders(sub, subsub).getMax();
		return max == null ? emptyprice : max.getKey();
	}
	
	public double getHighestSellOfferPrice(int cat, int sub, int subsub, int emptyprice) {
		SelfBalancingBSTNode max = Category.getCategory(cat).getSellOffers(sub, subsub).getMax();
		return max == null ? emptyprice : max.getKey();
	}
	
	public double getLowestBuyOrderPrice(int cat, int sub, int subsub, int emptyprice) {
		SelfBalancingBSTNode min = Category.getCategory(cat).getBuyOrders(sub, subsub).getMin();
		return min == null ? emptyprice : min.getKey();
	}
	
	public double getLowestSellOfferPrice(int cat, int sub, int subsub, int emptyprice) {
		SelfBalancingBSTNode min = Category.getCategory(cat).getSellOffers(subsub, subsub).getMin();
		return min == null ? emptyprice : min.getKey();
	}
}