package de.ancash.bazaar.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import de.ancash.bazaar.utils.Enquiry;

/* Class SBBSTNode */

public class SelfBalancingBSTNode {    

	SelfBalancingBSTNode left, right;

    private double key;
    
    private HashMap<UUID, Enquiry> enquiries = new HashMap<UUID, Enquiry>();
    double height;
    
    public ArrayList<SelfBalancingBSTNode> getChildren() {
    	ArrayList<SelfBalancingBSTNode> children = new ArrayList<SelfBalancingBSTNode>();
    	if(left != null) children.add(left);
    	if(right != null) children.add(right);
    	return children.size() == 0 ? null : children;
    }
    
    /* Constructor */

    public SelfBalancingBSTNode() {
        left = null;
        right = null;
        key = 0;
        height = 0;
    }

    public void remove(UUID id) {getEnquiries().remove(id);}
    public boolean contains(UUID id) {return getEnquiries().containsKey(id);}
    public Enquiry get(UUID id) {return getEnquiries().get(id);}
    public double getKey() {return key;}
	public HashMap<UUID, Enquiry> getEnquiries() {return enquiries;}
    public SelfBalancingBSTNode getLeft() {return left;}
    public SelfBalancingBSTNode getRight() {return right;}
    public void setKey(double key) {this.key = key;}
	
    public HashMap<UUID, Enquiry> get() {
    	return getEnquiries();
    }
    
    public void add(Enquiry e) {
    	if(e == null) return;
    	getEnquiries().put(e.getID(), e);
    }
    
    /* Constructor */

    public SelfBalancingBSTNode(double n) {
        left = null;
        right = null;
        key = n;
        height = 0;
    }

	public Enquiry random() {
		for(UUID id : enquiries.keySet()) {
			return enquiries.get(id);
		}
		return null;
	}

	public int sum() {
		int total = 0;
		for(UUID uuid : enquiries.keySet()) {
			total = total + enquiries.get(uuid).getLeft();
		}
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

}