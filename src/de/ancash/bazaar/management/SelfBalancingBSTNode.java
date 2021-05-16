package de.ancash.bazaar.management;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import de.ancash.datastructures.maps.CompactMap;

/* Class SBBSTNode */

public class SelfBalancingBSTNode {    

	SelfBalancingBSTNode left, right;
	
    private double key;
    
    private CompactMap<UUID, Enquiry> enquiries = new CompactMap<UUID, Enquiry>();
    double height;
    
    
    /* Constructor */

    public SelfBalancingBSTNode(double n) {
        left = null;
        right = null;
        key = n;
        height = 0;
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[");
    	enquiries.entrySet().forEach(entry-> sb.append(entry.getValue().toString() + ", "));
    	return sb.toString() + "]".replace(", ]", "]");
    }
    
    public ArrayList<SelfBalancingBSTNode> getChildren() {
    	ArrayList<SelfBalancingBSTNode> children = new ArrayList<SelfBalancingBSTNode>();
    	if(left != null) children.add(left);
    	if(right != null) children.add(right);
    	return children.size() == 0 ? null : children;
    }
    
    public CompactMap<UUID, Enquiry> get() {
    	return getEnquiries();
    }
    
    public void add(Enquiry e) {
    	if(e == null) return;
    	getEnquiries().put(e.getID(), e);
    }

	public Enquiry random() {
		return enquiries.entrySet().iterator().next().getValue();
	}

	public int sum() {
		int total = 0;
	    for(Map.Entry<UUID, Enquiry> entry : enquiries.entrySet()) 
	    	total += entry.getValue().getLeft();
		return total;
	}
	
	public Enquiry getByTimeStamp() {
		UUID temp = null;
		long time = Long.MAX_VALUE;
		for(UUID id : enquiries.keySet()) {
			if(enquiries.get(id).getTimeStamp() < time) {
				time = enquiries.get(id).getTimeStamp();
				temp = id;
			}
		}
		return enquiries.get(temp);
	}   
    
    public void remove(UUID id) {getEnquiries().remove(id);}
    public boolean contains(UUID id) {return getEnquiries().containsKey(id);}
    public Enquiry get(UUID id) {return getEnquiries().get(id);}
    public double getKey() {return key;}
	public CompactMap<UUID, Enquiry> getEnquiries() {return enquiries;}
    public SelfBalancingBSTNode getLeft() {return left;}
    public SelfBalancingBSTNode getRight() {return right;}
    public void setKey(double key) {this.key = key;}
    
    public static void main(String[] args) {

    }
}