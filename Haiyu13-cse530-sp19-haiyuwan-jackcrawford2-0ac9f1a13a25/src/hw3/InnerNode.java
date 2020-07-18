package hw3;

import java.util.ArrayList;

import hw1.Field;

public class InnerNode implements Node {
	
	private ArrayList<Node> childrenNodes;
	private ArrayList<Field> keys;
	private int degree;
	private InnerNode parent=null;
	
	
	//degree is the size of the childrenNodes
	public InnerNode(int degree) {
		this.degree = degree;
		this.childrenNodes=new ArrayList<Node>();
		this.keys=new ArrayList<Field>();
	}
	
	public boolean isOverFull() {
		if(degree<=keys.size()) {
			return true;
		}
		return false;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((childrenNodes == null) ? 0 : childrenNodes.hashCode());
		result = prime * result + degree;
		result = prime * result + ((keys == null) ? 0 : keys.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InnerNode other = (InnerNode) obj;
		if (childrenNodes == null) {
			if (other.childrenNodes != null)
				return false;
		} else if (!childrenNodes.equals(other.childrenNodes))
			return false;
		if (degree != other.degree)
			return false;
		if (keys == null) {
			if (other.keys != null)
				return false;
		} else if (!keys.equals(other.keys))
			return false;
		return true;
	}

	public boolean lessHalfFull() {
		int half = degree/2;
		if(keys.size()<half) {
			return true;
		}
		return false;
	}
	public boolean moreHalfFull() {
		int half = degree/2;
		if(keys.size()>half) {
			return true;
		}
		return false;
	}
	
	
	public ArrayList<Field> getKeys() {
		//your code here
		return keys;
	}
	public void addKey(Field f) {
		keys.add(f);
		//sort the keys
		ArrayList<Field> res= new ArrayList<Field>();
		while(!keys.isEmpty()) {
			Field bigEntry = keys.get(0);
			for(Field e : keys) {
				if(bigEntry.compare(hw1.RelationalOperator.LT, e)) {
					bigEntry=e;
				}
			}
			res.add(0,bigEntry);
			keys.remove(bigEntry);
		}
		keys = res;
	}
	
	public ArrayList<Node> getChildren() {
		return childrenNodes;
	}
	public void addChild(Node child) {
//		for(int i = 0; i<this.keys.size();i++) {
//			if(child.isLeafNode()) {
//				LeafNode childleaf=(LeafNode) child;
//				ArrayList<Entry> entries = childleaf.getEntries();
//				Field bigestField = entries.get(entries.size()-1).getField();
//				if(bigestField.compare(hw1.RelationalOperator.LTE, keys.get(i))) {
//					this.childrenNodes.add(i,child);
//				}
//				else if(i==keys.size()) {
//					this.childrenNodes.add(child);
//					
//				}
//				
//			}else {
//				InnerNode childinner =(InnerNode)child;
//				ArrayList<Field> childkeys = childinner.getKeys();
//				Field bigestField = childkeys.get(childkeys.size()-1);
//				if(bigestField.compare(hw1.RelationalOperator.LTE, keys.get(i))) {
//					this.childrenNodes.add(i,child);
//				}
//				else if(i==keys.size()) {
//					this.childrenNodes.add(child);
//				}
//				
//			}
//		}
		if(childrenNodes.isEmpty()) {
			childrenNodes.add(child);
			return;
		}
		for(int i=0;i<childrenNodes.size();i++) {
			if(child.isLeafNode()) {
				LeafNode childadd = (LeafNode) child;
				LeafNode childinlist = (LeafNode) childrenNodes.get(i);
				if(childadd.lastField().compare(hw1.RelationalOperator.LT, childinlist.lastField())) {
					childrenNodes.add(i, child);
					return;
				}
			}else {
				InnerNode childadd = (InnerNode) child;
				InnerNode childinlist = (InnerNode) childrenNodes.get(i);
				if(childadd.lastKey().compare(hw1.RelationalOperator.LT, childinlist.lastKey())) {
					childrenNodes.add(i, child);
					return;
				}
			}
		}
	}
	public void setChildren(ArrayList<Node> newChildren) {
		this.childrenNodes=newChildren;
	}
	public void setKeys(ArrayList<Field> newKeys) {
		this.keys=newKeys;
	}

	public int getDegree() {
		return degree;
	}
	
	public boolean isLeafNode() {
		return false;
	}
	
	public InnerNode getParent(int pInner) {
		if(this.parent!=null) {
			return this.parent;
		}
		InnerNode p=  new InnerNode(pInner);
		p.addChild(this);
		setParent(p);
		return this.parent;
	}
	public void setParent(InnerNode i) {
		this.parent = i;
	}
	public Field lastKey() {
		int size = keys.size()-1;
		return this.keys.get(size);
	}

}