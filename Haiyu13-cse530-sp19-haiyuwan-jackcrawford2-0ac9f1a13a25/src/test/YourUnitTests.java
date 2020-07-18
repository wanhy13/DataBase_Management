package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.Field;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.TupleDesc;
import hw3.BPlusTree;
import hw3.Entry;
import hw3.InnerNode;
import hw3.LeafNode;
import hw3.Node;

public class YourUnitTests {
	
	private HeapFile hf;
	private TupleDesc td;
	private Catalog c;
	private HeapPage hp;

	@Before
	public void setup() throws IOException {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		td = c.getTupleDesc(tableId);
		hf = c.getDbFile(tableId);
		hp = hf.readPage(0);
	}
	
	@Test
	public void test_search(){
		BPlusTree bp = new BPlusTree(7, 4);
		bp.insert(new Entry(new IntField(4), 0));
		bp.insert(new Entry(new IntField(5), 0));
		bp.insert(new Entry(new IntField(6), 0));
		bp.insert(new Entry(new IntField(14), 0));
		bp.insert(new Entry(new IntField(8), 0));
		bp.insert(new Entry(new IntField(9), 0));
		bp.insert(new Entry(new IntField(7), 0));
		
		Node test_leaf = bp.search(new IntField(7));
		assertTrue(test_leaf.isLeafNode() == true);
	}
	@Test
	public void test_insertduplicate() {
		BPlusTree bp = new BPlusTree(3, 2);
		bp.insert(new Entry(new IntField(1), 0));
		bp.insert(new Entry(new IntField(1), 0));
		bp.insert(new Entry(new IntField(1), 0));
	
		//verify root properties
				Node root = bp.getRoot();
				assertTrue(root.isLeafNode() == false);
				InnerNode in = (InnerNode)root;
				
				ArrayList<Field> k = in.getKeys();
				ArrayList<Node> c = in.getChildren();

			assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(1)));
				
				Node l = c.get(0);
				Node r = c.get(1);

				assertTrue(l.isLeafNode());
				assertTrue(l.isLeafNode());
				
				LeafNode ll = (LeafNode)l;
				LeafNode rr = (LeafNode)r;

				ArrayList<Entry> lll = ll.getEntries();
				ArrayList<Entry> rrr = rr.getEntries();
			


				assertTrue(lll.get(0).getField().equals(new IntField(1)));
				assertTrue(rrr.get(0).getField().equals(new IntField(1)));
	}

}
