package hw1;

import java.sql.Types;
import java.util.HashMap;

/**
 * This class represents a tuple that will contain a single row's worth of information
 * from a table. It also includes information about where it is stored
 * @author Sam Madden modified by Doug Shook
 *
 */
public class Tuple {
	
	/**
	 * Creates a new tuple with the given description
	 * @param t the schema for this tuple
	 */
	private int pageID;
	private int tupleID;
	private TupleDesc tupleDescription;
	private Field[] fields;
	
	public Tuple(TupleDesc t) {
		tupleDescription = t;
		this.fields = new Field[t.getFieldLength()];
	}
	
	public TupleDesc getDesc() {
		return tupleDescription;
	}
	
	/**
	 * retrieves the page id where this tuple is stored
	 * @return the page id of this tuple
	 */
	public int getPid() {
		
		return pageID;
	}

	public void setPid(int pid) {
		pageID = pid;
	}

	/**
	 * retrieves the tuple (slot) id of this tuple
	 * @return the slot where this tuple is stored
	 */
	public int getId() {
		//your code here
		return tupleID;
	}

	public void setId(int id) {
		tupleID = id;
	}
	
	public void setDesc(TupleDesc td) {
		tupleDescription = td;
	}
	
	/**
	 * Stores the given data at the i-th field
	 * @param i the field number to store the data
	 * @param v the data
	 */
	public void setField(int i, Field v) {
		fields[i] = v;
	}
	
	public Field getField(int i) {
		return fields[i];
	}
	
	/**
	 * Creates a string representation of this tuple that displays its contents.
	 * You should convert the binary data into a readable format (i.e. display the ints in base-10 and convert
	 * the String columns to readable text).
	 */
	public String toString() {
		
		String finalString = "";
		
		for (int i = 0; i < fields.length; i++) {
			if (i == 0){
				finalString += (fields[i].toString());
			}
			else {
				finalString += (", " + fields[i].toString());
			}
		}
		
		return finalString;
	}
}
	

