package hw2;

import java.awt.List;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import hw1.IntField;
import hw1.RelationalOperator;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

/**
 * A class to perform various aggregations, by accepting one tuple at a time
 * @author Doug Shook
 *
 */
public class Aggregator {
	
	private AggregateOperator ao;
	private boolean gb;
	private TupleDesc t;
	private ArrayList<ArrayList<Tuple>> groups;
	
	
	

	public Aggregator(AggregateOperator o, boolean groupBy, TupleDesc td) {
		//your code here
		this.ao=o;
		this.gb= groupBy;
		this.t=td;
		this.groups=new ArrayList<ArrayList<Tuple>>();
		
	}
	
	/**
	 * Merges the given tuple into the current aggregation
	 * @param t the tuple to be aggregated
	 */
	// merge just create a arraylist of arraylist
	public void merge(Tuple t) {
		if(this.gb) {
			for(int i=0; i<groups.size();i++) {
				// get i list of tuple
				// get i list first tuple
				// get tuple's first column
				hw1.Field key= groups.get(i).get(0).getField(0);
				if(key.equals(t.getField(0))){
					//t is in this group add it into the list
					groups.get(i).add(t);
					return;
				}
			}
			// create new group 
			ArrayList<Tuple> new_list= new ArrayList<Tuple>();
			new_list.add(t);
			groups.add(new_list);
		}
		else {
			if(groups.isEmpty()) {
				// create tuple list
				ArrayList<Tuple> first= new ArrayList<Tuple>();
				first.add(t);
				groups.add(first);
			}else {
				groups.get(0).add(t);
			}
		}
	}
	
	/** 
	 * different os
	 */
	//max:
	private Tuple max(ArrayList<Tuple> list) {
		if(t.getType(0)!=hw1.Type.INT) {
			ArrayList<String> s_list = new ArrayList<String> ();
			for(Tuple t: list) {
				s_list.add(((StringField) t.getField(0)).getValue());
			}
			Collections.sort(s_list);
			Tuple result= new Tuple(t);
			StringField res2 = new StringField(s_list.get(s_list.size()-1));
			result.setField(t.getFieldLength()-1, res2);
			return result;
		}
		
		hw1.IntField res= (IntField)list.get(0).getField(t.getFieldLength()-1);
		int val =res.getValue();
		for(int i=1; i< list.size();i++) {
			hw1.IntField n = (IntField) list.get(i).getField(t.getFieldLength()-1);
			
			if(val<n.getValue()) {
				val=n.getValue();
			}
		}
		Tuple result = new Tuple(t);
		hw1.IntField res2 = new IntField(val);
		result.setField(t.getFieldLength()-1, res2);
		return result;
	}
	// min
	private Tuple min(ArrayList<Tuple> list) {
		if(t.getType(0)!=hw1.Type.INT) {
			ArrayList<String> s_list = new ArrayList<String> ();
			for(Tuple t: list) {
				s_list.add(((StringField) t.getField(0)).getValue());
			}
			Collections.sort(s_list);
			Tuple result= new Tuple(t);
			StringField res2 = new StringField(s_list.get(0));
			result.setField(t.getFieldLength()-1, res2);
			return result;
		}
		hw1.IntField res= (IntField)list.get(0).getField(t.getFieldLength()-1);
		int val =res.getValue();
		for(int i=1; i< list.size();i++) {
			hw1.IntField n = (IntField) list.get(i).getField(t.getFieldLength()-1);
			
			if(val>n.getValue()) {
				val=n.getValue();
			}
		}
		Tuple result = new Tuple(t);
		hw1.IntField res2 = new IntField(val);
		result.setField(t.getFieldLength()-1, res2);
		return result;
	}
	//avg
	private Tuple avg(ArrayList<Tuple> list) {
		int size = list.size();
		int val = 0;
		
		
		for(Tuple i: list) {
			hw1.IntField n= (hw1.IntField) i.getField(t.getFieldLength()-1);
			val = val +n.getValue();
			}
		hw1.IntField res = new IntField(val/size);
		
		Tuple result = new Tuple(t);
		result.setField(t.getFieldLength()-1, res);
		
		return result;
	}
	
	//count
	private Tuple count(ArrayList<Tuple> list) {
		int val = list.size();
		IntField value= new IntField(val);
		if(t.getFieldLength()>1) {
			Type[] type= new Type[] {t.getType(0),Type.INT};
			String[] field= new String[] {t.getFieldName(0), t.getFieldName(1)};
			TupleDesc new_t= new TupleDesc(type,field);
			Tuple res= new Tuple (new_t);
			res.setField(0, list.get(0).getField(0));
			res.setField(1, value);
			return res;
		}else {
			Type[] type= new Type[] {Type.INT};
			String[] field= new String[] {t.getFieldName(0)};
			TupleDesc new_t= new TupleDesc(type,field);
			Tuple res= new Tuple (new_t);
			res.setField(0, value);
			return res;
		}
		
	}
	//sum
	private Tuple sum(ArrayList<Tuple> list) {
		
		int val = 0;
		
		
		for(Tuple i: list) {
			hw1.IntField n= (hw1.IntField) i.getField(t.getFieldLength()-1);
			val = val +n.getValue();
			}
		hw1.IntField res = new IntField(val);
		
		Tuple result = new Tuple(t);
		result.setField(t.getFieldLength()-1, res);
		
		return result;
	}

	
	/**
	 * Returns the result of the aggregation
	 * @return a list containing the tuples after aggregation
	 * @throws Exception 
	 */
	public ArrayList<Tuple> getResults()  {
		//your code here
        //return this.tuples;
		
		if(this.gb) {
			ArrayList<Tuple> result= new ArrayList<Tuple>();
			for(int i=0; i<groups.size();i++) {
				switch (this.ao) {
				case MAX:
//					if(t.getType(1)!=hw1.Type.INT) {
//						Type[] type= new Type[] {t.getType(0),Type.INT};
//						String[] field= new String[] {t.getFieldName(0), t.getFieldName(1)};
//						TupleDesc new_t= new TupleDesc(type,field);
//						Tuple res= new Tuple (new_t);
//						res.setField(0, groups.get(i).get(0).getField(0));
//						res.setField(1, new IntField(0));
//						result.add(res);
//						break;
//					}else {
						Tuple resmax=max(groups.get(i));
						resmax.setField(0, groups.get(i).get(0).getField(0));
						result.add(resmax);
						break;
					
//					}
				case MIN:
//					if(t.getType(1)!=hw1.Type.INT) {
//						Type[] type= new Type[] {t.getType(0),Type.INT};
//						String[] field= new String[] {t.getFieldName(0), t.getFieldName(1)};
//						TupleDesc new_t= new TupleDesc(type,field);
//						Tuple res= new Tuple (new_t);
//						res.setField(0, groups.get(i).get(0).getField(0));
//						res.setField(1, new IntField(0));
//						result.add(res);
//						break;
//					}else {
						Tuple resmin=min(groups.get(i));
						resmin.setField(0, groups.get(i).get(0).getField(0));
						result.add(resmin);
						break;

//					}
				case AVG:
					if(t.getType(1)!=hw1.Type.INT) {
						Type[] type= new Type[] {t.getType(0),Type.INT};
						String[] field= new String[] {t.getFieldName(0), t.getFieldName(1)};
						TupleDesc new_t= new TupleDesc(type,field);
						Tuple res= new Tuple (new_t);
						res.setField(0, groups.get(i).get(0).getField(0));
						res.setField(1, new IntField(0));
						result.add(res);
						break;
					}else {
						Tuple res=avg(groups.get(i));
						res.setField(0, groups.get(i).get(0).getField(0));
						result.add(res);
						break;
						
					}
				case SUM:
					if(t.getType(1)!=hw1.Type.INT) {
						Type[] type= new Type[] {t.getType(0),Type.INT};
						String[] field= new String[] {t.getFieldName(0), t.getFieldName(1)};
						TupleDesc new_t= new TupleDesc(type,field);
						Tuple res= new Tuple (new_t);
						res.setField(0, groups.get(i).get(0).getField(0));
						res.setField(1, new IntField(0));
						result.add(res);
						break;
					}else {
						Tuple res=sum(groups.get(i));
						res.setField(0, groups.get(i).get(0).getField(0));
						result.add(res);
						break;
					}
				case COUNT:
					
					Tuple res=count(groups.get(i));
					res.setField(0, groups.get(i).get(0).getField(0));
					result.add(res);
					break;
					
					
				}
			}
			return result;
		}
		
		else {
			ArrayList<Tuple> result= new ArrayList<Tuple>();
			switch (this.ao) {
			case MAX:
//				if(t.getType(0)!=hw1.Type.INT) {
//					
//						
//					Type[] type= new Type[] {Type.INT};
//					String[] field= new String[] {t.getFieldName(0)};
//					TupleDesc new_t= new TupleDesc(type,field);
//					Tuple res= new Tuple (new_t);
//					
//					res.setField(0, new IntField(0));
//					result.add(res);
//					return result;
//				}else {
					ArrayList<Tuple> resmax= new ArrayList<Tuple>();
					resmax.add(max(groups.get(0)));
					return resmax;
//				}
			case MIN:
//				if(t.getType(0)!=hw1.Type.INT) {
//					ArrayList<Tuple> res= new ArrayList<Tuple>();
//					res.add(min(groups.get(0)));
//					return res;
//					Type[] type= new Type[] {Type.INT};
//					String[] field= new String[] {t.getFieldName(0)};
//					TupleDesc new_t= new TupleDesc(type,field);
//					Tuple res= new Tuple (new_t);
//					
//					res.setField(0, new IntField(0));
//					result.add(res);
//					return result;
//				}else {
					ArrayList<Tuple> resmin= new ArrayList<Tuple>();
					resmin.add(min(groups.get(0)));
					return resmin;
				//}
			case AVG:
				if(t.getType(0)!=hw1.Type.INT) {
					Type[] type= new Type[] {Type.INT};
					String[] field= new String[] {t.getFieldName(0)};
					TupleDesc new_t= new TupleDesc(type,field);
					Tuple res= new Tuple (new_t);
					
					res.setField(0, new IntField(0));
					result.add(res);
					return result;
				}else {
					ArrayList<Tuple> res= new ArrayList<Tuple>();
					res.add(avg(groups.get(0)));
					return res;
				}
			case SUM:
				if(t.getType(0)!=hw1.Type.INT) {
					Type[] type= new Type[] {Type.INT};
					String[] field= new String[] {t.getFieldName(0)};
					TupleDesc new_t= new TupleDesc(type,field);
					Tuple res= new Tuple (new_t);
					
					res.setField(0, new IntField(0));
					result.add(res);
					return result;
				}else {
					ArrayList<Tuple> res= new ArrayList<Tuple>();
					res.add(sum(groups.get(0)));
					return res;
				}
			case COUNT:
				ArrayList<Tuple> res= new ArrayList<Tuple>();
				res.add(count(groups.get(0)));
				return res;
				
			}
		}
		return null;
		
	}

}
