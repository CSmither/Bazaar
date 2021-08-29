package de.ancash.bazaar.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.bazaar.gui.base.AbstractBazaarCreateBuyOrderInv;
import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.BuyOrder;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Triplet;
import de.ancash.datastructures.tuples.Tuple;

import static de.ancash.misc.MathsUtils.round;

import java.util.HashMap;
import java.util.Map;

public class BazaarCreateBuyOrderImpl extends AbstractBazaarCreateBuyOrderInv{

	public BazaarCreateBuyOrderImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public void onCreationConfirm(AbstractBazaarIGUI igui) {
		Player player = Bukkit.getPlayer(igui.getId());
		if(PlayerManager.get(player.getUniqueId()).getEnquiries() >= 27) {
			player.sendMessage(igui.getPlugin().getResponse().CANNOT_CREATE_ENQUIRY);
			return;
		}
		if(igui.getEnquiryPrice() * igui.getEnquiryAmount() > igui.getPlugin().getEconomy().getBalance(player)) {
			player.sendMessage(igui.getPlugin().getResponse().NO_MONEY);
		} else {
			BuyOrder buyOrder = new BuyOrder(igui.getEnquiryAmount(), igui.getEnquiryPrice(), igui.getId(), igui.getCurrentCategory(), igui.getCurrentSub(), igui.getCurrentSubSub());
			igui.getPlugin().getEconomy().withdrawPlayer(player, igui.getEnquiryPrice() * igui.getEnquiryAmount());
			igui.getPlugin().getEnquiryUtils().insert(buyOrder);
			player.sendMessage(igui.getPlugin().getResponse().BUY_ORDER_SETUP
					.replace(BazaarPlaceholder.AMOUNT, igui.getEnquiryAmount() + "")
					.replace(BazaarPlaceholder.PRICE, round(igui.getEnquiryAmount() * igui.getEnquiryPrice(), 1) + "")
					.replace(BazaarPlaceholder.DISPLAY_NAME, Category.getCategory(igui.getCurrentCategory()).getSubSubShow(igui.getCurrentSub(), igui.getCurrentSubSub()).getItemMeta().getDisplayName()));
		}
	}

	@Override
	public Triplet<Map<String, String>, Map<String, String>, Map<String, String>> getPlaceholders(AbstractBazaarIGUI igui) {
		Map<String, String> placeholderA = new HashMap<>();
		final double highest_buy_order = igui.getHighestBuyOrderPrice();
		final double lowest_sell_offer = igui.getLowestSellOfferPrice();
		placeholderA.put(BazaarPlaceholder.ORDERING, igui.getEnquiryAmount() + "");
		placeholderA.put(BazaarPlaceholder.UNIT_PRICE, highest_buy_order + "");
		placeholderA.put(BazaarPlaceholder.PRICE_TOTAL, round(highest_buy_order, 1) * igui.getEnquiryAmount() + "");
		
		
		Map<String, String> placeholderB = new HashMap<>();
		placeholderB.put(BazaarPlaceholder.ORDERING, igui.getEnquiryAmount() + "");
		placeholderB.put(BazaarPlaceholder.UNIT_PRICE, round((highest_buy_order + 0.1), 1) + "");
		placeholderB.put(BazaarPlaceholder.PRICE_TOTAL, round((highest_buy_order + 0.1) * igui.getEnquiryAmount(), 1) + "");
		
		Map<String, String> placeholderC = new HashMap<>();
		placeholderC.put(BazaarPlaceholder.ORDERING, igui.getEnquiryAmount() + "");
		Duplet<Double, String> spread = AbstractBazaarIGUI.getSpread(lowest_sell_offer, highest_buy_order, 5);
		placeholderC.put(BazaarPlaceholder.UNIT_PRICE, round((highest_buy_order + spread.getFirst()), 1) + "");
		placeholderC.put(BazaarPlaceholder.PRICE_TOTAL, round((highest_buy_order + spread.getFirst()) * igui.getEnquiryAmount(), 1) + "");
		placeholderC.put(BazaarPlaceholder.OFFERS_PRICE_LOWEST, round(lowest_sell_offer, 1) + "");
		placeholderC.put(BazaarPlaceholder.ORDERS_PRICE_HIGHEST, round(highest_buy_order, 1) + "");
		placeholderC.put(BazaarPlaceholder.SPREAD, spread.getSecond());

		return Tuple.of(placeholderA, placeholderB, placeholderC);
	}
}