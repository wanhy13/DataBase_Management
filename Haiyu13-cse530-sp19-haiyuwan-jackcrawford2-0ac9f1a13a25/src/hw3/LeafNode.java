package hw3;

import java.util.ArrayList;
import java.util.Collections;
import hw1.Field;
import hw1.RelationalOperator;

public class LeafNode implements Node {
	
	private ArrayList<Entry> entries;
	private int degree;
	private InnerNode parent=null;
	
	public LeafNode(int degree) {
		this.degree = degree;
		this.entries=new ArrayList<Entry>();
	}
	
	/** if the entries number is less than the half
	 * return true
	 * else return false
	 */
	public boolean lessHalfFull() {
		int half=(degree+1)/2;
		if(entries.size()<half) {
			return true;
		}
		return false;
	}
	public boolean moreHalfFull() {
		int half = (degree+1)/2;
		if(entries.size()>half) {
			return true;
		}
		return false;
	}
	public boolean isfull() {
		if(entries.size()==this.degree) {
			return true;
		}
		return false;
	}
	
	public void addEntry(Entry entry) {
		entries.add(entry);
		ArrayList<Entry> res= new ArrayList<Entry>();
		while(!entries.isEmpty()) {
			Entry bigEntry = entries.get(0);
			for(Entry e : entries) {
				Field bigField = bigEntry.getField();
				Field f=e.getField();
				if(bigField.compare(hw1.RelationalOperator.LT, f)) {
					bigEntry=e;
				}
			}
			res.add(0,bigEntry);
			entries.remove(bigEntry);
		}
		entries = res;
	}
	
	public ArrayList<Entry> getEntries() {
		//your code here
		return entries;
	}
	
	public void setEntries(ArrayList<Entry> newEntries) {
		entries = newEntries;
	}

	public int getDegree() {
		return degree;
	}
	
	public boolean isLeafNode() {
		return true;
	}
	
	public InnerNode getParent(int pInner) {
					return this.parent;
		

	}
	public Field lastField() {
		int size = this.entries.size()-1;
		return this.entries.get(size).getField();
	}
	
	public void setParent(InnerNode i) {
		this.parent=i;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + degree;
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
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
		LeafNode other = (LeafNode) obj;
		if (degree != other.degree)
			return false;
		if (entries == null) {
			if (other.entries != null)
				return false;
		} else if (!entries.equals(other.entries))
			return false;
		return true;
	}
	
	

}