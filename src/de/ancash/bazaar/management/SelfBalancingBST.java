package de.ancash.bazaar.management;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;

import static de.ancash.bazaar.management.EnquiryUtils.instance;
/* Class SelfBalancingBinarySearchTree */

//use 2,4 tree?
public class SelfBalancingBST {

    private SelfBalancingBSTNode root;     
    
    public SelfBalancingBSTNode getRoot() {
    	return root;
    }
    
    public int getAllContents() {
    	int all = 0;
    	for(SelfBalancingBSTNode node : getAllNodes(root)) {
    		for(UUID id : node.getEnquiries().keySet()) {
    			all = all + node.getEnquiries().get(id).getLeft();
    		}
    	}
    	return all;
    }
    
    public int getEnquiryCount() {
    	int enquiries = 0;
    	for(SelfBalancingBSTNode node : getAllNodes(root)) enquiries += node.get().size();
    	return enquiries;
    }
    
    /* Constructor */
    
    public SelfBalancingBST() {
        root = null;
    }
    
    public List<SelfBalancingBSTNode> getAllNodes(SelfBalancingBSTNode node){
        List<SelfBalancingBSTNode> listOfNodes = new ArrayList<SelfBalancingBSTNode>();
        addAllNodes(node, listOfNodes);
        return listOfNodes;
    }

    private static void addAllNodes(SelfBalancingBSTNode node, List<SelfBalancingBSTNode> listOfNodes) {
        if (node != null) {
            listOfNodes.add(node);
            List<SelfBalancingBSTNode> children = node.getChildren();
            if (children != null) {
                for (SelfBalancingBSTNode child: children) {
                    addAllNodes(child, listOfNodes);
                }
            }
        }
    }
    
 // This method mainly calls deleteRec()
   public void deleteKey(double key) {
	   synchronized (this) {
		   root = deleteRec(root, key);
	   }
   }
 
    /* A recursive function to 
      delete an existing key in BST
     */
    SelfBalancingBSTNode deleteRec(SelfBalancingBSTNode root, double key) {
        /* Base Case: If the tree is empty */
        if (root == null)
            return root;
 
        /* Otherwise, recur down the tree */
        if (key < root.getKey())
            root.left = deleteRec(root.left, key);
        else if (key > root.getKey())
            root.right = deleteRec(root.right, key);
 
        // if key is same as root's 
        // key, then This is the
        // node to be deleted
        else {
            // node with only one child or no child
            if (root.left == null)
                return root.right;
            else if (root.right == null)
                return root.left;
 
            // node with two children: Get the inorder
            // successor (smallest in the right subtree)
            root.setKey(minValue(root.right));
 
            // Delete the inorder successor
            root.right = deleteRec(root.right, root.getKey());
        }
 
        return root;
    }
 
    public static double minValue(SelfBalancingBSTNode root) {
        double minv = root.getKey();
        while (root.left != null) 
        {
            minv = root.left.getKey();
            root = root.left;
        }
        return minv;
    }
    
    public static double maxValue(SelfBalancingBSTNode root) {
        double minv = root.getKey();
        while (root.right != null) 
        {
            minv = root.right.getKey();
            root = root.right;
        }
        return minv;
    }
    
    /* Function to get specific Node */
    
    public SelfBalancingBSTNode getMin() {
    	SelfBalancingBSTNode temp = null;
    	if(root == null) return null;
    	if(root.left == null) return root;
		temp = root.left;
		while(temp.left != null) {
			temp = temp.left;
		}
		return temp;
	}
    
    public SelfBalancingBSTNode getMax() {
    	if(root == null) return null;
    	SelfBalancingBSTNode temp = null;
    	if(root.right == null) return root;
		temp = root.right;
		while(temp.right != null) {
			temp = temp.right;
		}
		return temp;
	}
    
    /* Function to get specific Node */
    
    public SelfBalancingBSTNode get(double value, SelfBalancingBSTNode node) {
    	
    	if(node == null) return null;
    	
    	if(node.getKey() > value) {
    		if(node.left == null) return node;
    		node = get(value, node.left);
    	}
    	if(node.getKey() < value) {
    		if(node.right == null) return node;
    		node = get(value, node.right);
    	}
    	
    	
    	return node;
    }
    
    /* Function to check if tree is empty */

    public boolean isEmpty() {
        return root == null;
    }



    /* Make the tree logically empty */

    public void clear() {

        root = null;
    }

    /* Function to insert getKey() */

    public void insert(double key, Enquiry e) {
    	synchronized (this) {
    		root = insert(key, root, e);
		}        
    }

    /* Function to get height of node */

    private double height(SelfBalancingBSTNode t ) {
        return t == null ? -1 : t.height;
    }

    /* Function to max of left/right node */

    private double max(double lhs, double rhs) {
        return lhs > rhs ? lhs : rhs;
    }

    /* Function to insert getKey() recursively */

    private SelfBalancingBSTNode insert(double x, SelfBalancingBSTNode t, Enquiry e) {
        if (t == null) {
        	t = new SelfBalancingBSTNode(x);
        	t.add(e);
        } else if (x < t.getKey()) {
            t.left = insert( x, t.left, e);
            if (height( t.left ) - height( t.right ) == 2)
                if (x < t.left.getKey())
                    t = rotateWithLeftChild( t );
                else
                    t = doubleWithLeftChild( t );
        } else if (x > t.getKey()) {
            t.right = insert( x, t.right , e);
            if (height( t.right ) - height( t.left ) == 2)
                if (x > t.right.getKey())
                    t = rotateWithRightChild( t );
                else
                    t = doubleWithRightChild( t );
        } else {
        	t.add(e);
        }   
        t.height = max( height( t.left ), height( t.right ) ) + 1;
        return t;

    }

    /* Rotate binary tree node with left child */     

    private SelfBalancingBSTNode rotateWithLeftChild(SelfBalancingBSTNode k2) {
        SelfBalancingBSTNode k1 = k2.left;
        k2.left = k1.right;
        k1.right = k2;
        k2.height = max( height( k2.left ), height( k2.right ) ) + 1;
        k1.height = max( height( k1.left ), k2.height ) + 1;
        return k1;
    }



    /* Rotate binary tree node with right child */

    private SelfBalancingBSTNode rotateWithRightChild(SelfBalancingBSTNode k1) {
        SelfBalancingBSTNode k2 = k1.right;
        k1.right = k2.left;
        k2.left = k1;
        k1.height = max( height( k1.left ), height( k1.right ) ) + 1;
        k2.height = max( height( k2.right ), k1.height ) + 1;
        return k2;
    }

    /**

     * Double rotate binary tree node: first left child

     * with its right child; then node k3 with new left child */

    private SelfBalancingBSTNode doubleWithLeftChild(SelfBalancingBSTNode k3) {
        k3.left = rotateWithRightChild( k3.left );
        return rotateWithLeftChild( k3 );
    }

    /**

     * Double rotate binary tree node: first right child

     * with its left child; then node k1 with new right child */      

    private SelfBalancingBSTNode doubleWithRightChild(SelfBalancingBSTNode k1) {
        k1.right = rotateWithLeftChild( k1.right );
        return rotateWithRightChild( k1 );
    }    

    /* Functions to count number of nodes */

    public double countNodes() {
        return countNodes(root);
    }

    private double countNodes(SelfBalancingBSTNode r) {
        if (r == null) return 0;
        double l = 1;
        l += countNodes(r.left);
        l += countNodes(r.right);
        return l;
    }

    /* Functions to search for an element */

    public boolean search(double val) {
        return search(root, val);
    }

    private boolean search(SelfBalancingBSTNode r, double val) {
        boolean found = false;
        while ((r != null) && !found) {
            double rval = r.getKey();
            if (val < rval)
                r = r.left;
            else if (val > rval)
                r = r.right;
            else
            {
                found = true;
                break;
            }
            found = search(r, val);
        }
        return found;
    }

    /* Function for inorder traversal */

    public void inorder() {
        inorder(root);
    }

    private void inorder(SelfBalancingBSTNode r) {
        if (r != null) {
            inorder(r.left);
            System.out.print(r.getKey() +"_");
            inorder(r.right);
        }
    }

    /* Function for preorder traversal */

    public void preorder() {
    	preorder(root);
    }

    private void preorder(SelfBalancingBSTNode r) {
    	if (r != null) {
            System.out.print(r.getKey() +"_");
            preorder(r.left);             
            preorder(r.right);
        }
    }

    /* Function for postorder traversal */

    public void postorder() {
        postorder(root);
    }

    private void postorder(SelfBalancingBSTNode r) {
        if (r != null) {
            postorder(r.left);             
            postorder(r.right);
            System.out.print(r.getKey() +"_");
        }
    }     

    public static SelfBalancingBSTNode KthLargestUsingMorrisTraversal(SelfBalancingBSTNode root, int k) {  
    	SelfBalancingBSTNode curr = root;  
    	SelfBalancingBSTNode Klargest = null;  
      
        // count variable to keep count of visited Nodes  
        int count = 0;  
      
        while (curr != null) {  
            // if right child is NULL  
            if (curr.right == null) {  
      
                // first increment count and check if count = k  
                if (++count == k)  
                    Klargest = curr;  
      
                // otherwise move to the left child  
                curr = curr.left;  
            } else {  
      
                // find inorder successor of current Node  
            	SelfBalancingBSTNode succ = curr.right;  
      
                while (succ.left != null && succ.left != curr)  
                    succ = succ.left;  
      
                if (succ.left == null) {  
      
                    // set left child of successor to the  
                    // current Node  
                    succ.left = curr;  
      
                    // move current to its right  
                    curr = curr.right;  
                } else {  
                	// restoring the tree back to original binary  
                    // search tree removing threaded links  
                    succ.left = null;  
      
                    if (++count == k)  
                        Klargest = curr;  
      
                    // move current to its left child  
                    curr = curr.left;  
                }  
            }  
        }  
        return Klargest;  
    }
    
 // Function to find k'th smallest element in BST
    // Here i denotes the number of nodes processed so far
    public static double kthSmallest(SelfBalancingBSTNode root, AtomicInteger i, double k) {
        // base case
        if (root == null) {
            return -1D;
        }
 
        if(root.left == null && root.right == null) root.getKey();
        
        // search in left subtree
        double left = kthSmallest(root.left, i, k);
 
        // if k'th smallest is found in left subtree, return it
        if (left != -1D) {
            return left;
        }
 
        // if current element is k'th smallest, return its value
        if (i.incrementAndGet() == k) {
            return root.getKey();
        }
 
        // else search in right subtree
        return kthSmallest(root.right, i, k);
    }
 
    // Function to find k'th smallest element in BST
    public static double kthSmallest(SelfBalancingBSTNode root, double k) {
        // maintain index to count number of nodes processed so far
        AtomicInteger i = new AtomicInteger(0);
 
        // traverse the tree in in-order fashion and return k'th element
        return kthSmallest(root, i, k);
    }
    
    public Duplet<Integer, Double> processInstaSell(int amount) {
    	synchronized (this) {
    		return processInstaSell(amount, (Duplet<Integer, Double>) Tuple.of(0, 0D));
		}
    }
    
    private Duplet<Integer, Double> processInstaSell(int amount, Duplet<Integer, Double> pair) {
    	if(isEmpty()) return pair;
    	SelfBalancingBSTNode node = getMax();
    	BuyOrder bo = (BuyOrder) node.getByTimeStamp();
    	if(bo == null) return pair;
    	
    	int reducable = bo.getLeft() > amount ? amount : bo.getLeft();
    	pair.setFirst(pair.getFirst() + reducable);
    	pair.setSecond(pair.getSecond() + (bo.getPrice() * reducable));
    	instance.reduce(bo, reducable);
    	bo.addClaimable(reducable);
    	if(bo.getLeft() == 0) {
    		instance.saveAll(bo);
    		instance.checkEnquiry(bo);
    		node.remove(bo.getID());
    	}
    	if(node.get().isEmpty()) {
    		deleteKey(bo.getPrice());
    		return pair;
    	}
    	//if(amount > 0 && getRoot().getChildren() != null && getRoot().getChildren().size() != 0) return processInstaSell(amount, pair);
    	return pair;
    }
    
    public int maxDepthRec(SelfBalancingBSTNode nodeTest) { 
	    if (nodeTest == null) return 0; 
	    int leftDepth = maxDepthRec(nodeTest.left); 
	    int rightDepth = maxDepthRec(nodeTest.right); 
	    if (leftDepth > rightDepth) 
	      return (leftDepth + 1); 
	    else 
	      return (rightDepth + 1);   
    }
}
