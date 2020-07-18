package hw2;

import java.util.ArrayList;

import java.util.List;

import hw1.Catalog;
import hw1.Database;
import hw1.Field;
import hw1.HeapFile;
import hw1.Tuple;
import hw1.TupleDesc;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;

public class Query {

	private String q;
	
	public Query(String q) {
		this.q = q;
	}
	
	public Relation execute()  {
		Statement statement = null;
		try {
			statement = CCJSqlParserUtil.parse(q);
		} catch (JSQLParserException e) {
			System.out.println("Unable to parse query");
			e.printStackTrace();
		}
		Select selectStatement = (Select) statement;
		PlainSelect sb = (PlainSelect)selectStatement.getSelectBody();
		
		//your code here
		// get the table from heapfile
		FromItem new_from = sb.getFromItem();
		Table theTable = (Table) new_from;
		String table_name= theTable.getName();
		
		Catalog the_catalog = Database.getCatalog();
		int id = the_catalog.getTableId(table_name);
		HeapFile file= the_catalog.getDbFile(id);
		TupleDesc td = file.getTupleDesc();
		
		// create new relation
		Relation new_relation = new Relation(file.getAllTuples() , td );
		
		// get all the join table
		List<Join> table_list = sb.getJoins();
		if(table_list!=null) {
			for(Join i: table_list) {
				// the join relation
				Table join_table = (Table) i.getRightItem();
				String join_table_name= join_table.getName();
				int join_id = the_catalog.getTableId(join_table_name);
				HeapFile join_file= the_catalog.getDbFile(join_id);
				TupleDesc join_td = join_file.getTupleDesc();
				Relation join_relation = new Relation(join_file.getAllTuples() , join_td );

				//find the fields

				WhereExpressionVisitor new_visitor = new WhereExpressionVisitor();
				i.getOnExpression().accept(new_visitor);
				String[] b = new_visitor.getRight().toString().split("\\.");
				// change the order
				if(b[0].toLowerCase().equals(join_table_name.toLowerCase())) {
					int field1 = td.nameToId(new_visitor.getLeft());
					int field2 = join_td.nameToId(b[1].toLowerCase());
					new_relation=new_relation.join(join_relation, field1, field2);
					td = new_relation.getDesc();
				}else {
					int field1 = join_td.nameToId(new_visitor.getLeft());
					int field2 = td.nameToId(b[1].toLowerCase());
					new_relation=new_relation.join(join_relation, field1, field2);
					td = new_relation.getDesc();
				}
				

			}
		}
		
		//where
		if(sb.getWhere()!=null) {
		Expression where = sb.getWhere();
		WhereExpressionVisitor new_visitor = new WhereExpressionVisitor();
		where.accept(new_visitor);
		TupleDesc after_from_td = new_relation.getDesc();
		int field_select = after_from_td.nameToId(new_visitor.getLeft());
		new_relation=new_relation.select(field_select, new_visitor.getOp(), new_visitor.getRight());
		}
		
		//project
		boolean ag = false;
		
		TupleDesc after_where_td = new_relation.getDesc();
		
		List<SelectItem> select = sb.getSelectItems();
	   
		ArrayList<Integer> field_list = new ArrayList<Integer>();
		boolean selectall= false;
		for(int i=0; i< select.size();i++) {
			ColumnVisitor cv = new ColumnVisitor();
			select.get(i).accept(cv);
			
			String name = cv.getColumn();
			if(!name.equals("*")) {
				field_list.add(after_where_td.nameToId(name));
			}else {
				selectall= true;
				break;
			}
		}
			if(!selectall) {
			new_relation=new_relation.project(field_list);
			}
		
		
		
		//group by and aggregate
		TupleDesc after_select_td = new_relation.getDesc();
		for(int i=0; i< select.size();i++) {
			ColumnVisitor cv = new ColumnVisitor();
			select.get(i).accept(cv);
			if(cv.isAggregate()) {
				ag = true;
				break;
			}
		}
		if(ag) {
			List<Expression> gb =  sb.getGroupByColumnReferences();
			boolean isgb = false;
			if(gb==null) {
				isgb = false;
			}else {
				isgb = true;
			}

			if(isgb) {
				ColumnVisitor cv_aggregate = new ColumnVisitor();
				select.get(1).accept(cv_aggregate);
				AggregateOperator op = cv_aggregate.getOp();
				new_relation=new_relation.aggregate(op, isgb);

			}
			else {
				ColumnVisitor cv_aggregate = new ColumnVisitor();
				select.get(0).accept(cv_aggregate);
				AggregateOperator op = cv_aggregate.getOp();
				if(op.equals(AggregateOperator.COUNT)) {
					if(selectall) {
						ArrayList<Integer> f = new ArrayList<Integer>();
						f.add(0);
						new_relation=new_relation.project(f);
						
						new_relation=new_relation.aggregate(op, isgb);
					}
				}else {
				new_relation=new_relation.aggregate(op, isgb);
				}
			}
		}
		
		
		return new_relation;
		
	}
}
