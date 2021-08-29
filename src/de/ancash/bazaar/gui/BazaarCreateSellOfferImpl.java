package de.ancash.bazaar.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.bazaar.gui.base.AbstractBazaarCreateSellOfferInv;
import de.ancash.bazaar.gui.base.AbstractBazaarIGUI;
import de.ancash.bazaar.gui.base.BazaarInventoryClassManager;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;
import de.ancash.bazaar.management.SellOffer;
import de.ancash.bazaar.utils.BazaarPlaceholder;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.InventoryUtils;

import static de.ancash.misc.MathsUtils.round;

public class BazaarCreateSellOfferImpl extends AbstractBazaarCreateSellOfferInv{
	
	public BazaarCreateSellOfferImpl(BazaarInventoryClassManager clazzManager) {
		super(clazzManager);
	}

	@Override
	public void onCreationConfirm(AbstractBazaarIGUI igui) {
		Player player = Bukkit.getPlayer(igui.getId());
		if(PlayerManager.get(player.getUniqueId()).getEnquiries() >= 27) {
			player.sendMessage(igui.getPlugin().getResponse().CANNOT_CREATE_ENQUIRY);
			return;
		}
		InventoryUtils.removeItemAmount(igui.getEnquiryAmount(), Category.getCategory(igui.getCurrentCategory()).getOriginal(igui.getCurrentSub(), igui.getCurrentSubSub()), player);
		SellOffer sellOffer = new SellOffer(igui.getEnquiryAmount(), igui.getEnquiryPrice(), igui.getId(), igui.getCurrentCategory(), igui.getCurrentSub(), igui.getCurrentSubSub());
		igui.getPlugin().getEnquiryUtils().insert(sellOffer);
		
		player.sendMessage(igui.getPlugin().getResponse().SELL_OFFER_SETUP
				.replace(BazaarPlaceholder.AMOUNT, igui.getEnquiryAmount() + "")
				.replace(BazaarPlaceholder.PRICE, round(igui.getEnquiryAmount() * igui.getEnquiryPrice(), 1) + "")
				.replace(BazaarPlaceholder.DISPLAY_NAME, Category.getCategory(igui.getCurrentCategory()).getSubSubShow(igui.getCurrentSub(), igui.getCurrentSubSub()).getItemMeta().getDisplayName()));
	}

	@Override
	public Duplet<Double, Double> getPrices(AbstractBazaarIGUI igui) {
		return Tuple.of(igui.getLowestSellOfferPrice(), igui.getHighestBuyOrderPrice());
	}
}