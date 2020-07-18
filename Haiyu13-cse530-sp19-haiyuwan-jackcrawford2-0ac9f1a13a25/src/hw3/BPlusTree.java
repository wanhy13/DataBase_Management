package hw3;


import java.util.ArrayList;


import hw1.Field;
import hw1.RelationalOperator;
import hw3.LeafNode;
import hw3.InnerNode;

public class BPlusTree {
    
    private int pInner;
    private int pLeaf;
    
    private Node root;
    
	public BPlusTree(int pInner, int pLeaf) {
    	//your code here
		this.pInner=pInner;
		this.pLeaf=pLeaf;
    }
	public void setRootNode(Node node) {
		this.root=node;
	}
    
	public LeafNode search(Field f) {
		//your code here
		LeafNode res = searchLeafNode(f);
		ArrayList<Entry> list = res.getEntries();
		for(Entry i:list) {
			if(i.getField().compare(hw1.RelationalOperator.EQ, f)) {
				return (LeafNode) res;
			};
		}
		return null;
	}
	
	public LeafNode searchLeafNode(Field f) {
		Node newroot = this.root;
		if(newroot.isLeafNode()) {
			return (LeafNode) newroot;
		}
		InnerNode r =(InnerNode) this.root;
		ArrayList<Field> keys =r.getKeys();
		for(int i=0; i<keys.size();i++) {
			if(f.compare(hw1.RelationalOperator.LTE, keys.get(i))){
				newroot = r.getChildren().get(i);
				BPlusTree childTree = new BPlusTree(this.pInner,this.pLeaf);
				childTree.setRootNode(newroot);
				return childTree.searchLeafNode(f);
			}
			else if(i==keys.size()-1){
				newroot = r.getChildren().get(i+1);
				BPlusTree childTree = new BPlusTree(this.pInner,this.pLeaf);
				childTree.setRootNode(newroot);
				return childTree.searchLeafNode(f);
			}
		}
		
		return null;
	}
	

	public void insert(Entry e) {
		if(this.root == null) {
			this.root =new LeafNode(pLeaf);
			LeafNode newRoot = (LeafNode) this.root;
			newRoot.addEntry(e);
			return;
		}
		
		//Search for the node where the new record should go
		LeafNode targetNode = searchLeafNode(e.getField());
		//If the target node is not full, add the record
		if(!targetNode.isfull()) {
			targetNode.addEntry(e);
		}
		else {
			//Make a new node that contains half the values of the old one
			LeafNode newNode = new LeafNode(pLeaf);
			ArrayList<Entry> entries = targetNode.getEntries();
			int loop=entries.size();
			for(int i = 0;i<loop;i++) {
				if(e.getField().compare(hw1.RelationalOperator.LTE, entries.get(i).getField())) {
					entries.add(i,e);
					break;
				}
				if(i==loop-1) entries.add(e);
			}
			int cut = (entries.size()-1)/2;
			ArrayList<Entry> entries1 = new ArrayList<Entry>();
			ArrayList<Entry> entries2 = new ArrayList<Entry>();
			for(int i=0; i<entries.size();i++) {
				if(i<=cut) {
					entries1.add(entries.get(i));
				}else {
					entries2.add(entries.get(i));
				}
			}
			Field newkey = entries1.get(entries1.size()-1).getField();
			targetNode.setEntries(entries2);
			newNode.setEntries(entries1);
			newNode.setParent(targetNode.getParent(pInner));
			//Insert the largest key of the new node into the parent
			InnerNode parentNode= targetNode.getParent(pInner);
			if(parentNode==null) {
				parentNode= new InnerNode(pInner);
				this.root=parentNode;
				parentNode.addChild(targetNode);
				targetNode.setParent(parentNode);
			}
			newNode.setParent(parentNode);
			parentNode.addChild(newNode);
			parentNode.addKey(newkey);
			if(parentNode.isOverFull()) {
				//If the parent is full:
				//Split the parent and add the middle key to its parent
				Split(parentNode);
			}
			
			
		}
		
	}

	private void Split(InnerNode node) {
		//If the root needs to split:
		if(node==this.root) {
			//Create a new root with one key and two pointers
			InnerNode newRoot=new InnerNode(pInner);
			this.root=newRoot;
			newRoot.addChild(node);
			node.setParent(newRoot);
		}
		InnerNode parent = node.getParent(pInner);
		// Repeat until a split is not needed
		ArrayList<Node> children = node.getChildren();
		ArrayList<Field> keys = node.getKeys();
		//create a new innernode
		InnerNode newInnerNode = new InnerNode(pInner);
		//contains half the values of the old one
		ArrayList<Node> children1 =  new ArrayList<Node>();
		ArrayList<Field> keys1 = new ArrayList<Field>();
		ArrayList<Node> children2 =  new ArrayList<Node>();
		ArrayList<Field> keys2 = new ArrayList<Field>();
	
		int cutPoint = (keys.size()-1)/2;

		for(int i=0; i<keys.size();i++) {
			if(i<cutPoint) {
				keys1.add(keys.get(i));
				children1.add(children.get(i));
				if(children.get(i).isLeafNode()) {
					LeafNode childreni=(LeafNode) children.get(i);
					childreni.setParent(newInnerNode);
				}else {
					InnerNode childreni=(InnerNode) children.get(i);
					childreni.setParent(newInnerNode);
				}
			}else if(i==cutPoint) {
				children1.add(children.get(i));
				parent.addKey(keys.get(i));
			}else if(i>cutPoint) {
				
				keys2.add(keys.get(i));
				
				children2.add(children.get(i));
			}
		}
		children2.add(children.get(children.size()-1));
		node.setChildren(children2);
		node.setKeys(keys2);
		newInnerNode.setChildren(children1);
		newInnerNode.setKeys(keys1);
		
		newInnerNode.setParent(parent);
		parent.addChild(newInnerNode);
		//loop
		if(parent.isOverFull()) {
			Split(parent);
		}	
	}
	public void delete(Entry e) {
		//your code here
		//Search for the node where the new record should go
		LeafNode target = search(e.getField());
		if(target==null) {
			return;
		}
		//If the node is more than half full, remove entry and done
		for(Entry en:target.getEntries()) {
			if(en.getField().compare(hw1.RelationalOperator.EQ, e.getField())) {
				target.getEntries().remove(en);
				break;
			}
		}
		
		InnerNode parent= target.getParent(pInner);
		if(target.lessHalfFull()) {
			//If sibling (with same parent) is more than half full,
			//take an entry from it, update parent
			ArrayList<Node> listofchildren = parent.getChildren();
			int pos=0;
			for(int i=0;i<listofchildren.size();i++) {
				if(target.equals(listofchildren.get(i))) {
					pos=i;
					break;
				}
			}
			if(pos!=0) {
				LeafNode lNode=(LeafNode)listofchildren.get(pos-1);
				
				if(lNode.moreHalfFull()) {
					ArrayList<Entry> LNodeEntries = lNode.getEntries();
					Entry moveEntry = LNodeEntries.get(LNodeEntries.size()-1);
					LNodeEntries.remove(moveEntry);
					target.addEntry(moveEntry);
					//change the key 
					ArrayList <Field> parentKeys = parent.getKeys();
					parentKeys.remove(pos-1);
					parentKeys.add(pos-1, LNodeEntries.get(LNodeEntries.size()-1).getField());
					return;
				}
				else if(pos!=listofchildren.size()-1){
					LeafNode rNode=(LeafNode)listofchildren.get(pos+1);
					if(rNode.moreHalfFull()){
						ArrayList<Entry> rNodeEntries = rNode.getEntries();
						Entry moveEntry = rNodeEntries.get(rNodeEntries.size());
						rNodeEntries.remove(moveEntry);
						target.addEntry(moveEntry);
						//change the key
						ArrayList <Field> parentKeys = parent.getKeys();
						parentKeys.remove(pos);
						parentKeys.add(pos, moveEntry.getField());
						return;
					}
				}
				//merge: cause pos!=0 and the left side is unhalf-full 
				// we need to always merge it with the left node
				ArrayList<Entry> LNodeEntries = lNode.getEntries();
				for(Entry i : LNodeEntries) {
					target.addEntry(i);
				}
				parent.getChildren().remove(lNode);
				if(pos==parent.getKeys().size()) {
					parent.getKeys().remove(pos-1);
				}
				else {
					parent.getKeys().remove(pos);
				}
				//May cause parents to merge
				if(parent.lessHalfFull()) {
					if(parent==this.root) {
						if(parent.getKeys().size()==0) {
							if(parent.getChildren().get(0).isLeafNode()) {
								LeafNode newRoot = (LeafNode) parent.getChildren().get(0);
								newRoot.setParent(null);
								this.root=newRoot;
							}else {
								InnerNode newRoot = (InnerNode) parent.getChildren().get(0);
								newRoot.setParent(null);
								this.root=newRoot;
							}
						}
					}else {
						modifyParent(parent);
					}
				}
				
			}else {
				LeafNode rNode=(LeafNode)listofchildren.get(pos+1);
				if(rNode.moreHalfFull()){
					ArrayList<Entry> rNodeEntries = rNode.getEntries();
					Entry moveEntry = rNodeEntries.get(rNodeEntries.size());
					rNodeEntries.remove(moveEntry);
					target.addEntry(moveEntry);
					//change the key
					ArrayList <Field> parentKeys = parent.getKeys();
					parentKeys.remove(pos);
					parentKeys.add(pos, moveEntry.getField());
					return;
				}else {
					//merge right node
					ArrayList<Entry> rNodeEntries = rNode.getEntries();
					for(Entry i : rNodeEntries) {
						target.addEntry(i);
					}
					parent.getChildren().remove(rNode);
					parent.getKeys().remove(pos);
					//May cause parents to merge
					if(parent.lessHalfFull()) {
						if(parent==this.root) {
							if(parent.getKeys().size()==0) {
								if(parent.getChildren().get(0).isLeafNode()) {
									LeafNode newRoot = (LeafNode) parent.getChildren().get(0);
									newRoot.setParent(null);
									this.root=newRoot;
								}else {
									InnerNode newRoot = (InnerNode) parent.getChildren().get(0);
									newRoot.setParent(null);
									this.root=newRoot;
								}
							}
						}else {
							modifyParent(parent);
						}
					}
				}
			}

		}
	}

	private void modifyParent(InnerNode node) {
		// TODO Auto-generated method stub
		InnerNode parent = node.getParent(pInner);
		ArrayList<Node> children = parent.getChildren();
		ArrayList<Field> parentKey=parent.getKeys();
		int pos=0;
		for(int i=0; i<children.size();i++) {
			if(children.get(i)==node) {
				pos=i;
				break;
			}
		}
		if(pos!=0) {
			InnerNode lNode = (InnerNode) children.get(pos-1);
			
			if(lNode.moreHalfFull()) {
				ArrayList<Field> lKey=lNode.getKeys();
				ArrayList<Node> lChildren=lNode.getChildren();
				Node moveNode=lChildren.get(lChildren.size()-1);
				Field moveKey = lKey.get(lKey.size()-1);
				
				node.addChild(moveNode);
				if(moveNode.isLeafNode()) {
					LeafNode lmoveNode=(LeafNode) moveNode;
					lmoveNode.setParent(node);
					}
				else {
					InnerNode imoveNode=(InnerNode) moveNode;
					imoveNode.setParent(node);
				}
				node.addKey(parentKey.get(pos-1));
				parent.addKey(moveKey);
				
				lKey.remove(moveKey);
				lChildren.remove(moveNode);
				return;
			}
			if(pos!=children.size()-1) {
				InnerNode rNode = (InnerNode) children.get(pos+1);

				if(rNode.moreHalfFull()) {
					ArrayList<Field> rKey=rNode.getKeys();
					ArrayList<Node> rChildren=rNode.getChildren();
					Node moveNode=rChildren.get(0);
					Field moveKey = rKey.get(0);

					node.addChild(moveNode);
					if(moveNode.isLeafNode()) {
						LeafNode lmoveNode=(LeafNode) moveNode;
						lmoveNode.setParent(node);
					}
					else {
						InnerNode imoveNode=(InnerNode) moveNode;
						imoveNode.setParent(node);
					}
					node.addKey(parentKey.get(pos));
					parent.addKey(moveKey);

					rKey.remove(moveKey);
					rChildren.remove(moveNode);
					return;
				}}	
			//merge
			ArrayList<Field> lKey=lNode.getKeys();
			ArrayList<Node> lChildren=lNode.getChildren();
			for(Field f:lKey) {
				node.addKey(f);
			}
			for(Node n:lChildren) {
				node.addChild(n);
				if(n.isLeafNode()) {
					LeafNode leafn= (LeafNode) n;
					leafn.setParent(node);
				}else {
					InnerNode leafn= (InnerNode) n;
					leafn.setParent(node);
				}
			}
			children.remove(lNode);
			node.addKey(parentKey.get(pos-1));
			parentKey.remove(pos-1);
			ArrayList<Field> nodeKey = node.getKeys();
			if(parent.lessHalfFull()) {
				if(parent==this.root) {
					if(parent.getKeys().size()==0) {
						if(parent.getChildren().get(0).isLeafNode()) {
							LeafNode newRoot = (LeafNode) parent.getChildren().get(0);
							newRoot.setParent(null);
							this.root=newRoot;
						}else {
							InnerNode newRoot = (InnerNode) parent.getChildren().get(0);
							newRoot.setParent(null);
							this.root=newRoot;
						}
					}
				}else {
					modifyParent(parent);
				}
			}



		}
		if(pos==0) {
			InnerNode rNode = (InnerNode) children.get(pos+1);
			if(rNode.moreHalfFull()) {
				ArrayList<Field> rKey=rNode.getKeys();
				ArrayList<Node> rChildren=rNode.getChildren();
				Node moveNode=rChildren.get(0);
				Field moveKey = rKey.get(0);

				node.addChild(moveNode);
				if(moveNode.isLeafNode()) {
					LeafNode lmoveNode=(LeafNode) moveNode;
					lmoveNode.setParent(node);
					}
				else {
					InnerNode imoveNode=(InnerNode) moveNode;
					imoveNode.setParent(node);
				}
				node.addKey(parentKey.get(pos));
				parent.addKey(moveKey);
				
				rKey.remove(moveKey);
				rChildren.remove(moveNode);
				return;
			}
			//merge
			ArrayList<Field> rKey=rNode.getKeys();
			ArrayList<Node> rChildren=rNode.getChildren();
			for(Field f:rKey) {
				node.addKey(f);
			}
			for(Node n:rChildren) {
				node.addChild(n);
				if(n.isLeafNode()) {
					LeafNode leafn= (LeafNode) n;
					leafn.setParent(node);
				}else {
					InnerNode leafn= (InnerNode) n;
					leafn.setParent(node);
				}
			}
			children.remove(rNode);
			node.addKey(parentKey.get(pos));
			parentKey.remove(pos);
			ArrayList<Field> nodeKey = node.getKeys();
			if(parent.lessHalfFull()) {
				if(parent==this.root) {
					if(parent.getKeys().size()==0) {
						if(parent.getChildren().get(0).isLeafNode()) {
							LeafNode newRoot = (LeafNode) parent.getChildren().get(0);
							newRoot.setParent(null);
							this.root=newRoot;
						}else {
							InnerNode newRoot = (InnerNode) parent.getChildren().get(0);
							newRoot.setParent(null);
							this.root=newRoot;
						}
					}
				}else {
					modifyParent(parent);
				}
			}
			
		}
				
		
	}
	public Node getRoot() {
		//your code here
		return root;
	}



	
}
