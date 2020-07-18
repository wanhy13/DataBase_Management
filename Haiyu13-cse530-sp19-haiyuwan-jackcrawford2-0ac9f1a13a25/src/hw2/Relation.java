package hw2;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

/**
 * This class provides methods to perform relational algebra operations. It will be used
 * to implement SQL queries.
 * @author Doug Shook
 *
 */
public class Relation{

	private ArrayList<Tuple> tuples;
	private TupleDesc td;
	
	public Relation(ArrayList<Tuple> l, TupleDesc td) {
		//your code here
		this.tuples=l;
		this.td=td;
	}
	
	/**
	 * This method performs a select operation on a relation
	 * @param field number (refer to TupleDesc) of the field to be compared, left side of comparison
	 * @param op the comparison operator
	 * @param operand a constant to be compared against the given column
	 * @return
	 */
	public Relation select(int field, RelationalOperator op, Field operand) {
		
		ArrayList<Tuple> new_tuples = new ArrayList<Tuple>();
		
		for(int i = 0; i<this.tuples.size(); i++) {
			
			if (this.tuples.get(i).getField(field).compare(op, operand)){
				
				new_tuples.add(this.tuples.get(i));
			}
			
		}
		
		Relation new_relation = new Relation(new_tuples, this.td);
		
		//your code here
		return new_relation;
	}
	
	/**
	 * This method performs a rename operation on a relation
	 * @param fields the field numbers (refer to TupleDesc) of the fields to be renamed
	 * @param names a list of new names. The order of these names is the same as the order of field numbers in the field list
	 * @return
	 */
	public Relation rename(ArrayList<Integer> fields, ArrayList<String> names) {
		//your code here
		
		String[] new_names = new String[td.getFieldLength()];
		for (int i=0; i<td.getFieldLength();i++) {
			new_names[i]= td.getFieldName(i);
		}
		for(int n:fields) {
			int a = fields.indexOf(n);
			new_names[n]= names.get(a);
		}
		
		Type[] new_types = new Type[td.getFieldLength()];
		
		for (int i=0; i<td.getFieldLength();i++) {
			new_types[i] = td.getType(i);
		}
		
		TupleDesc new_td = new TupleDesc(new_types, new_names);
		Relation new_relation = new Relation(this.tuples, new_td);
		return new_relation;
	}
	
	/**
	 * This method performs a project operation on a relation
	 * @param fields a list of field numbers (refer to TupleDesc) that should be in the result
	 * @return
	 */
	public Relation project(ArrayList<Integer> fields) {
		
		String[] new_names = new String[fields.size()];
		Type[] new_types = new Type[fields.size()];
		ArrayList<Tuple> new_tuples = new ArrayList<Tuple>();
		
		for(int i = 0; i < fields.size(); i++) {
			new_names[i] = this.td.getFieldName(fields.get(i));
			new_types[i] = this.td.getType(fields.get(i));
		}
		
		
		TupleDesc new_td = new TupleDesc(new_types, new_names);
		
		for(int i = 0; i < this.tuples.size(); i++) {
			Tuple new_tuple = new Tuple(new_td);
			new_tuple.setId(this.tuples.get(i).getId());
			new_tuple.setPid(this.tuples.get(i).getPid());
			for(int k = 0; k < new_td.getFieldLength(); k++) {
				new_tuple.setField(k, this.tuples.get(i).getField(fields.get(k)));
			}
			new_tuples.add(new_tuple);
		}
		
		
		Relation new_relation = new Relation(new_tuples, new_td);
		return new_relation;
	}
	
	/**
	 * This method performs a join between this relation and a second relation.
	 * The resulting relation will contain all of the columns from both of the given relations,
	 * joined using the equality operator (=)
	 * @param other the relation to be joined
	 * @param field1 the field number (refer to TupleDesc) from this relation to be used in the join condition
	 * @param field2 the field number (refer to TupleDesc) from other to be used in the join condition
	 * @return
	 */
	public Relation join(Relation other, int field1, int field2) {
		Type[] td1=new Type[this.td.getFieldLength()+other.td.getFieldLength()];
		String[] new_fields = new String[this.td.getFieldLength()+other.td.getFieldLength()];
		for(int i=0; i<this.td.getFieldLength();i++) {
			td1[i]=this.td.getType(i);
			new_fields[i] = this.td.getFieldName(i);
		}
		for(int k=0; k<other.td.getFieldLength();k++) {
			td1[this.td.getFieldLength() + k]=other.td.getType(k);
			new_fields[this.td.getFieldLength() + k] = other.td.getFieldName(k);
		}
		
		TupleDesc new_td = new TupleDesc(td1, new_fields);
		
		ArrayList<Tuple> new_tuples = new ArrayList<Tuple>();

		//your code here
		for(int i = 0; i<this.tuples.size(); i++) {
			for(int k = 0; k<other.tuples.size(); k++) {
				if((this.tuples.get(i).getField(field1).getType()==other.tuples.get(k).getField(field2).getType()) && (this.tuples.get(i).getField(field1).equals(other.tuples.get(k).getField(field2))) ){
					Tuple new_tuple = new Tuple(new_td);
					new_tuple.setId(this.tuples.get(i).getId());
					new_tuple.setPid(this.tuples.get(i).getPid());
					for(int l = 0; l < this.td.getFieldLength(); l++) {
						new_tuple.setField(l, this.tuples.get(i).getField(l));
					}
					for(int m = 0; m < other.td.getFieldLength(); m++) {
						new_tuple.setField(this.td.getFieldLength() + m, other.tuples.get(k).getField(m));
					}
					
					new_tuples.add(new_tuple);
				}
			}
			
		}
		
		Relation new_relation = new Relation(new_tuples, new_td);
		return new_relation;
	}
	
	/**
	 * Performs an aggregation operation on a relation. See the lab write up for details.
	 * @param op the aggregation operation to be performed
	 * @param groupBy whether or not a grouping should be performed
	 * @return
	 */
	public Relation aggregate(AggregateOperator op, boolean groupBy) {
		ArrayList<Tuple> new_tuples = new ArrayList<Tuple>();
		
		Aggregator agg = new Aggregator(op, groupBy, this.td);
		for(int i = 0; i < this.tuples.size(); i++) {
			agg.merge(this.tuples.get(i));
		}
		
		new_tuples = agg.getResults();
		
		Relation new_relation = new Relation(new_tuples, this.td);
		
		//your code here
		return new_relation;
	}
	
	public TupleDesc getDesc() {
		return this.td;
	}
	
	public ArrayList<Tuple> getTuples() {
		return this.tuples;
	}
	
	/**
	 * Returns a string representation of this relation. The string representation should
	 * first contain the TupleDesc, followed by each of the tuples in this relation
	 */
	public String toString() {
		
		String string_val = "";
		string_val += this.td.toString();
		//your code here
		for(int i = 0; i < this.tuples.size(); i++) {
			string_val += this.tuples.get(i).toString();
			if (i != this.tuples.size() - 1){
				string_val += ",";
			}
		}
		return string_val;
	}
}
