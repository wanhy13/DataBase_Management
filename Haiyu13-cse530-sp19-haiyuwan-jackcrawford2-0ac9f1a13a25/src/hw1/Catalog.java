package hw1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * The Catalog keeps track of all available tables in the database and their
 * associated schemas.
 * For now, this is a stub catalog that must be populated with tables by a
 * user program before it can be used -- eventually, this should be converted
 * to a catalog that reads a catalog table from disk.
 */

public class Catalog {
	
    /**
     * Constructor.
     * Creates a new, empty catalog.
     */
	private ArrayList<Table> tables;
    public Catalog() {
    	//your code here
    	this.tables=new ArrayList<Table>();
    }
    
    
    
    public class Table {
    	
    		private String tableName;
    		
    		private String primaryKey;
    		private int tableID;
    		private TupleDesc td;
    		private HeapFile the_heap;
    		
    		
    		private Table(String name, HeapFile file, String key) {
    			tableName = name;
    			the_heap = file;
    			primaryKey = key;
    			tableID = file.getId();
    		}
    		
    		public void setTableID(int id) {
    			tableID = id;
    		}
    		
    		public int getTableID() {
    			return tableID;
    		}
    		
    		public String getTableName() {
    			return tableName;
    		}
    		
    		public void setTD(TupleDesc tupleDesc) {
    			td = tupleDesc;
    		}
    		
    		public TupleDesc getTD() {
    			return td;
    		}
    		
    		
    		public HeapFile getHeap() {
    			return the_heap;
    		}
    		
    		public String getPrimaryKey() {
    			return primaryKey;
    		}
    	
    }

    /**
     * Add a new table to the catalog.
     * This table's contents are stored in the specified HeapFile.
     * @param file the contents of the table to add;  f ile.getId() is the identfier of
     *    this file/tupledesc param for the calls getTupleDesc and getFile
     * @param name the name of the table -- may be an empty string.  May not be null.  If a name conflict exists, use the last table to be added as the table for a given name.
     * @param pkeyField the name of the primary key field
     */
    public void addTable(HeapFile file, String name, String pkeyField) {
    	//your code here
    		Table newTable = new Table(name, file, pkeyField);
    		newTable.setTD(file.getTupleDesc());
    		newTable.setTableID(file.getId());
    		tables.add(newTable);
    }

    public void addTable(HeapFile file, String name) {
        addTable(file,name,"");
    }
    
    public Table getTableByName(String name){
    		for(int i = 0; i< tables.size(); i++) {
    			if (tables.get(i).getTableName() == name){
    				return tables.get(i);
    			}
    		}
    		return null;
    }

    /**
     * Return the id of the table with a specified name,
     * @throws NoSuchElementException if the table doesn't exist
     */
    public int getTableId(String name) throws NoSuchElementException{
	    	for (int i = 0; i<tables.size(); i++) {	
	    		//System.out.println(tables.get(i).getTableID());
	    		if (tables.get(i).getTableName().equals(name)) {
	    			
	    			return tables.get(i).getTableID();
	    		}
		}
	    	throw new NoSuchElementException("no such table");
    }

    /**
     * Returns the tuple descriptor (schema) of the specified table
     * @param tableid The id of the table, as specified by the DbFile.getId()
     *     function passed to addTable
     */
    public TupleDesc getTupleDesc(int tableid) throws NoSuchElementException {
    	//your code here
	    	for (int i = 0; i<tables.size(); i++) {	
	    		//System.out.println(tables.get(i).getTableID());
	    		if (tables.get(i).getTableID() == tableid) {
	    			return tables.get(i).getTD();
	    		}
		}
	   
	    	throw new NoSuchElementException("no such schema");
    }

    /**
     * Returns the HeapFile that can be used to read the contents of the
     * specified table.
     * @param tableid The id of the table, as specified by the HeapFile.getId()
     *     function passed to addTable
     */
    public HeapFile getDbFile(int tableid) throws NoSuchElementException {
	    	for (int i = 0; i<tables.size(); i++) {	
	    		if (tables.get(i).getTableID() == tableid) {
	    			return tables.get(i).getHeap();
	    		}
		}
	    	throw new NoSuchElementException("no such file");
    }

    /** Delete all tables from the catalog */
    public void clear() {
    		ArrayList<Table> new_table = new ArrayList<Table>();
    		tables = new_table;
    }

    public String getPrimaryKey(int tableid) {
	    	for (int i = 0; i<tables.size(); i++) {	
	    		if (tables.get(i).getTableID() == tableid) {
	    			return tables.get(i).getPrimaryKey();
	    		}
		}
	    	return "";
    }

    public Iterator<Integer> tableIdIterator() {
    	//your code here
    	ArrayList<Integer> res= new ArrayList<Integer>();
    	for(Table t: tables) {
    		res.add(t.getTableID());
    	}
    	return res.iterator();
    }

    public String getTableName(int id) {
	    	for (int i = 0; i<tables.size(); i++) {	
	    		if (tables.get(i).getTableID() == id) {
	    			return tables.get(i).getTableName();
	    		}
		}
	    	return "";
    }
    
    /**
     * Reads the schema from a file and creates the appropriate tables in the database.
     * @param catalogFile
     */
    public void loadSchema(String catalogFile) {
        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(catalogFile)));

            while ((line = br.readLine()) != null) {
                //assume line is of the format name (field type, field type, ...)
                String name = line.substring(0, line.indexOf("(")).trim();
                //System.out.println("TABLE NAME: " + name);
                String fields = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                String[] els = fields.split(",");
                ArrayList<String> names = new ArrayList<String>();
                ArrayList<Type> types = new ArrayList<Type>();
                String primaryKey = "";
                for (String e : els) {
                    String[] els2 = e.trim().split(" ");
                    names.add(els2[0].trim());
                    if (els2[1].trim().toLowerCase().equals("int"))
                        types.add(Type.INT);
                    else if (els2[1].trim().toLowerCase().equals("string"))
                        types.add(Type.STRING);
                    else {
                        System.out.println("Unknown type " + els2[1]);
                        System.exit(0);
                    }
                    if (els2.length == 3) {
                        if (els2[2].trim().equals("pk"))
                            primaryKey = els2[0].trim();
                        else {
                            System.out.println("Unknown annotation " + els2[2]);
                            System.exit(0);
                        }
                    }
                }
                Type[] typeAr = types.toArray(new Type[0]);
                String[] namesAr = names.toArray(new String[0]);
                TupleDesc t = new TupleDesc(typeAr, namesAr);
                HeapFile tabHf = new HeapFile(new File("testfiles/" + name + ".dat"), t);
                addTable(tabHf,name,primaryKey);
                System.out.println("Added table : " + name + " with schema " + t);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IndexOutOfBoundsException e) {
            System.out.println ("Invalid catalog entry : " + line);
            System.exit(0);
        }
    }
}



