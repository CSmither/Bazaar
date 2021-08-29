package de.ancash.bazaar.sockets.management;

public class SubSubCategory {

	private final SelfBalancingBST sellOfferTree = new SelfBalancingBST();
	private final SelfBalancingBST buyOrderTree = new SelfBalancingBST();
	
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
}