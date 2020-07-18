package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.TupleDesc;
import hw4.BufferPool;
import hw4.Permissions;

public class YourHW4Tests {
	private Catalog c;
	private BufferPool bp;
	private HeapFile hf;
	private TupleDesc td;
	private int tid;
	
	@Before
	public void setup() {
		
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
		
		bp = Database.getBufferPool();
		
		tid = c.getTableId("test");
	}
	//deadlock
	@Test
	public void testDeadLock() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
	    bp.getPage(1, tid, 1, Permissions.READ_ONLY);
	    
	    bp.getPage(0, tid, 1, Permissions.READ_WRITE);
	    bp.getPage(1, tid, 0, Permissions.READ_WRITE);
	    
	    bp.transactionComplete(0, true);
	    bp.transactionComplete(1, true);
		
	    assertTrue(true);
		
	}
	@Test
	public void testFullBuffer() throws Exception {
		for(int i=0; i<50;i++) {
			//make the bp full
			bp.getPage(i, tid, i, Permissions.READ_ONLY);
			bp.getPage(i, tid, i, Permissions.READ_WRITE);
		}
		// undirty page
		bp.transactionComplete(0, true);
		bp.getPage(51, tid, 51, Permissions.READ_ONLY);
		bp.getPage(51, tid, 51, Permissions.READ_WRITE);
		
		assertTrue(true);
		
	}
	@Test
	public void test() {
		fail("Not yet implemented");
	}
	//multiple write locks
	//evict with full buffer
	//properly evict
	//upgrade read->write
	//various lock combos
	//down grade lock
	//deadlock
	//insert/delete
}
