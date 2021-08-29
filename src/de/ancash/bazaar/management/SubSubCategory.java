package de.ancash.bazaar.management;

import org.bukkit.inventory.ItemStack;

import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.SerializableItemStack;

public class SubSubCategory {

	private final SelfBalancingBST sellOfferTree = new SelfBalancingBST();
	private final SelfBalancingBST buyOrderTree = new SelfBalancingBST();
	private final Duplet<ItemStack, SerializableItemStack> original;
	private final ItemStack show;
	private final double emptyPrice;
	
	public SubSubCategory(ItemStack show, ItemStack original, double emptyPrice) {
		this.emptyPrice = emptyPrice;
		this.show = show;
		this.original = Tuple.of(original, new SerializableItemStack(original));
	}
	
	public SelfBalancingBSTNode getMaxSellOffer() {
		return sellOfferTree.getMax();
	}
	
	public SelfBalancingBSTNode getMinSellOffer() {
		return sellOfferTree.getMin();
	}
	
	public SelfBalancingBST getSellOfferTree() {
		return sellOfferTree;
	}

	public SelfBalancingBSTNode getMaxBuyOrder() {
		return buyOrderTree.getMax();
	}
	
	public SelfBalancingBSTNode getMinBuyOrder() {
		return buyOrderTree.getMin();
	}
	
	public SelfBalancingBST getBuyOrderTree() {
		return buyOrderTree;
	}

	public ItemStack getOriginal() {
		return original.getFirst().clone();
	}
	
	public SerializableItemStack getOriginalAsSerializableItemStack(int id) {
		return original.getSecond();
	}

	public double getEmptyPrice() {
		return emptyPrice;
	}

	public ItemStack getShow() {
		return show;
	}
}