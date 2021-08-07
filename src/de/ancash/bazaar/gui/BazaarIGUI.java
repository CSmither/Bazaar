package de.ancash.bazaar.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ancash.bazaar.gui.inventory.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.inventory.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.SelfBalancingBST;
import de.ancash.bazaar.management.SelfBalancingBSTNode;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.misc.MathsUtils;

public class BazaarIGUI extends AbstractBazaarIGUI{

	
	
	public BazaarIGUI(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	/**
	 * Get all placeholders for enquires
	 * 
	 * @param cat
	 * @param sub
	 * @param subsub
	 * @return
	 */
	public Map<String, String> getPlaceholders(int cat, int sub, int subsub, boolean topSellOffers, int tSOcnt, boolean topBuyOrders, int tBOcnt) {
		Category category = Category.getCategory(cat);
		
		List<SelfBalancingBST> allBuyOrders = subsub != -1 ? Arrays.asList(category.getBuyOrders(sub, subsub)): category.getSubBuyOrders(sub);
		List<SelfBalancingBST> allSellOffers = subsub != -1 ? Arrays.asList(category.getSellOffers(sub, subsub)) : category.getSubSellOffers(sub);
		Map<String, String> placeholder = new HashMap<>();
		
		for(SelfBalancingBST tree : allBuyOrders) 
			getTreeInfo(tree, placeholder, "orders");
		
		for(SelfBalancingBST tree : allSellOffers) 
			getTreeInfo(tree, placeholder, "offers");
		
		if(subsub != -1) {
			SelfBalancingBST sellOffer = category.getSellOffers(sub, subsub);
			SelfBalancingBST buyOrder = category.getBuyOrders(sub, subsub);
			double sellOfferLowest = sellOffer.isEmpty() ? 0 : sellOffer.getMin().getKey();
			double sellOfferHighest= sellOffer.isEmpty() ? 0 : sellOffer.getMax().getKey();
			double buyOrderLowest = buyOrder.isEmpty() ? 0 : buyOrder.getMin().getKey();
			double buyOrderHighest= buyOrder.isEmpty() ? 0 : buyOrder.getMax().getKey();
			placeholder.put(BazaarPlaceholder.OFFERS_PRICE_LOWEST, sellOfferHighest == 0 ? "§cN/A" : "" + sellOfferLowest);
			placeholder.put(BazaarPlaceholder.OFFERS_PRICE_HIGHEST, sellOfferHighest == 0 ? "§cN/A" : "" + sellOfferHighest);
			placeholder.put(BazaarPlaceholder.ORDERS_PRICE_LOWEST, buyOrderLowest == 0 ? "§cN/A" : "" + buyOrderLowest);
			placeholder.put(BazaarPlaceholder.ORDERS_PRICE_HIGHEST, buyOrderLowest == 0 ? "§cN/A" : "" + buyOrderHighest);
			placeholder.put(BazaarPlaceholder.OFFERS_PRICE_LOWEST_STACK, sellOfferHighest == 0 ? "§cN/A" : "" + MathsUtils.round(sellOfferLowest * 64, 2));
			placeholder.put(BazaarPlaceholder.OFFERS_PRICE_HIGHEST_STACK, sellOfferHighest == 0 ? "§cN/A" : "" + MathsUtils.round(sellOfferHighest * 64, 2));
			placeholder.put(BazaarPlaceholder.ORDERS_PRICE_LOWEST_STACK, buyOrderLowest == 0 ? "§cN/A" : "" + MathsUtils.round(buyOrderLowest * 64, 2));
			placeholder.put(BazaarPlaceholder.ORDERS_PRICE_HIGHEST_STACK, buyOrderLowest == 0 ? "§cN/A" : "" + MathsUtils.round(buyOrderHighest * 64, 2));
			
			if(topBuyOrders)
				getTopBuyOrders(placeholder, tBOcnt);
			if(topSellOffers)
				getTopSellOffers(placeholder, tSOcnt);
		}
		return placeholder;
	}
	
	private Map<String, String> getTopBuyOrders(Map<String, String> placeholder, int cnt) {
		SelfBalancingBST rootBuyOrder = Category.getCategory(getCurrentCategory()).getBuyOrders(getCurrentSub(), getCurrentSubSub());
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
	
	private Map<String, String> getTopSellOffers(Map<String, String> placeholder, int cnt) {
		SelfBalancingBST rootSellOffer = Category.getCategory(getCurrentCategory()).getSellOffers(getCurrentSub(), getCurrentSubSub());
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

	public double getHighestBuyOrderPrice() {
		SelfBalancingBSTNode max = Category.getCategory(getCurrentCategory()).getBuyOrders(getCurrentSub(), getCurrentSubSub()).getMax();
		return max == null ? Category.getCategory(getCurrentCategory()).getEmptyPrices()[getCurrentSub() - 1][getCurrentSubSub() - 1] : max.getKey();
	}
	
	public double getHighestSellOfferPrice() {
		SelfBalancingBSTNode max = Category.getCategory(getCurrentCategory()).getSellOffers(getCurrentSub(), getCurrentSubSub()).getMax();
		return max == null ? Category.getCategory(getCurrentCategory()).getEmptyPrices()[getCurrentSub() - 1][getCurrentSubSub() - 1] : max.getKey();
	}
	
	public double getLowestBuyOrderPrice() {
		SelfBalancingBSTNode min = Category.getCategory(getCurrentCategory()).getBuyOrders(getCurrentSub(), getCurrentSubSub()).getMin();
		return min == null ? Category.getCategory(getCurrentCategory()).getEmptyPrices()[getCurrentSub() - 1][getCurrentSubSub() - 1] : min.getKey();
	}
	
	public double getLowestSellOfferPrice() {
		SelfBalancingBSTNode min = Category.getCategory(getCurrentCategory()).getSellOffers(getCurrentSub(), getCurrentSubSub()).getMin();
		return min == null ? Category.getCategory(getCurrentCategory()).getEmptyPrices()[getCurrentSub() - 1][getCurrentSubSub() - 1] : min.getKey();
	}
}
