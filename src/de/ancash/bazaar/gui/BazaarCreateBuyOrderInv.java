package de.ancash.bazaar.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.bazaar.gui.inventory.AbstractBazaarCreateBuyOrderInv;
import de.ancash.bazaar.gui.inventory.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.inventory.BazaarInventoryClassManager;
import de.ancash.bazaar.management.BuyOrder;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Triplet;
import de.ancash.datastructures.tuples.Tuple;

import static de.ancash.misc.MathsUtils.round;

import java.util.HashMap;
import java.util.Map;

public class BazaarCreateBuyOrderInv extends AbstractBazaarCreateBuyOrderInv{

	public BazaarCreateBuyOrderInv(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public void onCreationConfirm(AbstractBazaarIGUI igui) {
		Player player = Bukkit.getPlayer(igui.getId());
		if(PlayerManager.get(player.getUniqueId()).getEnquiries() >= 27) {
			player.sendMessage(igui.getPlugin().getResponse().CANNOT_CREATE_ENQUIRY);
			igui.closeAll();
			return;
		}
		if(igui.getEnquiryPrice() * igui.getEnquiryAmount() > igui.getPlugin().getEconomy().getBalance(player)) {
			player.sendMessage(igui.getPlugin().getResponse().NO_MONEY);
			igui.closeAll();
		} else {
			BuyOrder buyOrder = new BuyOrder(igui.getEnquiryAmount(), igui.getEnquiryPrice(), igui.getId(), igui.getCurrentCategory(), igui.getCurrentSub(), igui.getCurrentSubSub());
			igui.getPlugin().getEnquiryUtils().insert(buyOrder);
			igui.getInventory().getViewers().forEach(p -> p.sendMessage(igui.getPlugin().getResponse().BUY_ORDER_SETUP
					.replace("%amount%", igui.getEnquiryAmount() + "")
					.replace("%price%", round(igui.getEnquiryAmount() * igui.getEnquiryPrice(), 1) + "")
					.replace("%displayname%", Category.getCategory(igui.getCurrentCategory()).getSubSub()[igui.getCurrentSub() - 1][igui.getCurrentSubSub() - 1].getItemMeta().getDisplayName())));
			igui.closeAll();
		}
	}

	@Override
	public Triplet<Map<String, String>, Map<String, String>, Map<String, String>> getPlaceholders(AbstractBazaarIGUI igui) {
		Map<String, String> placeholderA = new HashMap<>();
		placeholderA.put("%ordering%", igui.getEnquiryAmount() + "");
		placeholderA.put("%unit_price%", round(igui.getHighestBuyOrderPrice(), 1) + "");
		placeholderA.put("%price_total%", round(igui.getHighestBuyOrderPrice(), 1) * igui.getEnquiryAmount() + "");
		
		
		Map<String, String> placeholderB = new HashMap<>();
		placeholderB.put("%ordering%", igui.getEnquiryAmount() + "");
		placeholderB.put("%unit_price%", round((igui.getHighestBuyOrderPrice() + 0.1), 1) + "");
		placeholderB.put("%price_total%", round((igui.getHighestBuyOrderPrice() + 0.1) * igui.getEnquiryAmount(), 1) + "");
		
		Map<String, String> placeholderC = new HashMap<>();
		placeholderC.put("%ordering%", igui.getEnquiryAmount() + "");
		Duplet<Double, String> spread = AbstractBazaarIGUI.getSpread(igui.getLowestSellOfferPrice(), igui.getHighestBuyOrderPrice(), 5);
		placeholderC.put("%unit_price%", round((igui.getHighestBuyOrderPrice() + spread.getFirst()), 1) + "");
		placeholderC.put("%price_total%", round((igui.getHighestBuyOrderPrice() + spread.getFirst()) * igui.getEnquiryAmount(), 1) + "");
		placeholderC.put("%offers_price_lowest%", round(igui.getLowestSellOfferPrice(), 1) + "");
		placeholderC.put("%orders_price_highest%", round(igui.getHighestBuyOrderPrice(), 1) + "");
		placeholderC.put("%spread%", spread.getSecond());

		return Tuple.of(placeholderA, placeholderB, placeholderC);
	}
}