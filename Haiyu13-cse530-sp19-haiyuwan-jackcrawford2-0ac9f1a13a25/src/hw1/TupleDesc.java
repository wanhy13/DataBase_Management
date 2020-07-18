package hw1;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc {

	private Type[] types;
	private String[] fields;
	

	
    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     *
     * @param typeAr array specifying the number of and types of fields in
     *        this TupleDesc. It must contain at least one entry.
     * @param fieldAr array specifying the names of the fields. Note that names may be null.
     */
	public int getFieldLength() {
		return fields.length;
	}

    public TupleDesc(Type[] typeAr, String[] fieldAr) {
    	//your code here
    	this.types=typeAr;
    	this.fields=fieldAr;
    	
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        //your code here
    	return this.fields.length;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     *
     * @param i index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        //your code here
    	if (i>=this.fields.length||this.fields==null) {
			throw new NoSuchElementException("no such field");
		}
		
		return this.fields[i];

    	
    }

   

    /**
     * Find the index of the field with a given name.
     *
     * @param name name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException if no field with a matching name is found.
     */
    public int nameToId(String name) throws NoSuchElementException {
        //your code here
    		for (int i=0; i<this.fields.length;i++) {
    			if(fields[i].equals(name)) {
    				return i;
    			}
    		}
    		throw new NoSuchElementException("no such field");
    	
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     *
     * @param i The index of the field to get the type of. It must be a valid index.
     * @return the type of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public Type getType(int i) throws NoSuchElementException {
        //your code here
    		if(i>=this.fields.length || i < 0) {
    			throw new NoSuchElementException("i is not a valid field reference");
    		}
    	return this.types[i];
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     * Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
    	//your code here
    		int result=0;
    		for (int i=0;i<this.types.length;i++) {
    			if(this.types[i]==Type.INT) {
    				result=result+4;
    			}
    			else {
    				result=result+129;
    			}
    		}
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
		TupleDesc other = (TupleDesc) obj;
		if (!Arrays.equals(fields, other.fields))
			return false;
		if (!Arrays.equals(types, other.types))
			return false;
		return true;
	}
    

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(fields);
		result = prime * result + Arrays.hashCode(types);
		return result;
	}

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * @return String describing this descriptor.
     */
    public String toString() {
    	
    		String outString = "";
    		
    		for (int i = 0; i < fields.length; i++){
    			if (this.types[i]==Type.INT) {
    				outString += (", int[" + i + "](" + fields[i] + "[" + i + ")");
    			}
    			else {
    				outString += (", string[" + i + "](" + fields[i] + "[" + i + ")");
    			}
    			
    		}
    		return outString;
    }
     
}

