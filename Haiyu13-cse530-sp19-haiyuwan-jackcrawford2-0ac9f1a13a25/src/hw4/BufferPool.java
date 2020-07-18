package hw4;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.Tuple;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool which check that the transaction has the appropriate
 * locks to read/write the page.
 */
public class BufferPool {
    /** Bytes per page, including header. */
    public static final int PAGE_SIZE = 4096;

    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;
    
    
    //private page list to store the pages in the pool
    private ArrayList<HeapPage> pages;
    
    private Manager manager;
   

	private static int numPages;

    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        // your code here
    		this.numPages=numPages;
    		this.pages = new ArrayList<HeapPage>();
    		manager = new Manager();
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param tableId the ID of the table with the requested page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public HeapPage getPage(int tid, int tableId, int pid, Permissions perm)
        throws Exception {
    		
        //Retrieve the specified page with the associated permissions.
    		try{
    			manager.acquireLock(tid, tableId,pid, perm);
    		}
    		catch(Exception e){
    			transactionComplete(tid, false);

    		}
    		// if it is present, it should be return
    		for(HeapPage i : pages) {
    			if(i.getId()==pid) {
    				return i;
    			}
    		}
    		// If it is not present, it should be added to the buffer pool and returned.
    		if(pages.size()==this.numPages) {
    			evictPage();
    		}
    		
    		HeapFile file = Database.getCatalog().getDbFile(tableId);
    		HeapPage page = file.readPage(pid);
    		pages.add(page);
        return page;
    }
   
    
    
    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param tableID the ID of the table containing the page to unlock
     * @param pid the ID of the page to unlock
     */
    public  void releasePage(int tid, int tableId, int pid) {
        // your code here
    		manager.releasePageLock(tid, tableId , pid);
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public   boolean holdsLock(int tid, int tableId, int pid) {
        // your code here
    		
        return manager.holdLock(tid,tableId,pid);
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction. If the transaction wishes to commit, write
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public void transactionComplete(int tid, boolean commit)
        throws IOException {
        // your code here
    		HashMap<Integer, Integer> pagelist = manager.releaseLocks(tid);
    		manager.releaseLocks(tid);
    		if(commit) {
    			for(int i: pagelist.keySet()) {
    				flushPage(i,pagelist.get(i));
    			}
    		}else {
    			for(int i: pagelist.keySet()) {
    				discardPage(i, pagelist.get(i));
    			}
    		}
    		
    }

    /**
     * Add a tuple to the specified table behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to. May block if the lock cannot 
     * be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public  void insertTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
     	HeapPage p = getPage(tid,tableId,t.getPid(),Permissions.READ_WRITE);
    		p.addTuple(t);
    		p.setDirty();
    		
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from. May block if
     * the lock cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty.
     *
     * @param tid the transaction adding the tuple.
     * @param tableId the ID of the table that contains the tuple to be deleted
     * @param t the tuple to add
     */
    public  void deleteTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
    		HeapPage p = getPage(tid, tableId,t.getPid(),Permissions.READ_WRITE);
    		p.deleteTuple(t);
    		p.setDirty();
    }
    /**
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized  void flushPage(int tableId, int pid) throws IOException {
        // your code here
    		for(HeapPage p:pages) {
    			if(p.getId()==pid&& tableId==p.getTableId()) {
    				HeapFile file = Database.getCatalog().getDbFile(tableId);
    				file.writePage(p);
    				p.setClean();
    				return;
    			}
    		}
    }
    /**
     * Discards a page from the buffer pool.
     * @param tableId
     * @param pid
     */
    private synchronized void discardPage(int tableId, int pid) throws IOException {
	    	byte[] b = new byte[0];
	    	boolean work = false;
//	    	HeapPage tp = new HeapPage(1,b,0);
//	    	for(HeapPage p:pages) {
//    			if(p.getId()==pid&& tableId==p.getTableId()) {
//    				tp = p;
//    				work=true;
//    				break;
//    			}
//    		}
    		for (Iterator<HeapPage> iterator = pages.iterator(); iterator.hasNext();) {
	    		HeapPage p = iterator.next();
	    		if(p.getId()==pid&& tableId==p.getTableId()) {
	    			iterator.remove();
	    		}
		}
    	}
//    	if(work) {
//    	pages.remove(tp);}}
    /**
     * if the bufferpool is full, evict the first clean page
     * @throws Exception
     */
    private synchronized  void evictPage() throws Exception {
        // your code here
    		for(HeapPage p :pages) {
    			if(!p.isDirty()) {
    				pages.remove(p);
    				return;
    			}
    		}
    		throw new Exception();
    }
    /**
     * trasactionManager: Manage all the transactions
     */
    private class Manager{
    		private HashMap<Integer, ArrayList<Lock>> transaction;
    		
    		private Manager() {
    			this.transaction = new HashMap<Integer, ArrayList<Lock>>();
    		}
    		/**
    		 * transaction remove all the locks
    		 * return the hashmap <tableId, pageId> to write the page into disk
    		 * @param tid
    		 */
    		public HashMap<Integer,Integer> releaseLocks(int tid) {
    			HashMap<Integer,Integer> res = new HashMap<Integer,Integer>();
    			for(int i : transaction.keySet()) {
    				for(Lock l:transaction.get(i)) {
    					res.put(l.tableId, l.pid);
    				}
    			}
    			transaction.remove(tid);
    			return res;
    		}
			/**
    		 *  check if the list of transaction has the lock
    		 * @param tid
    		 * @param tableId
    		 * @param pid
    		 * @return
    		 */
    		public boolean holdLock(int tid, int tableId, int pid) {
    			for(int i: transaction.keySet()) {
    				if(i==tid) {
    					ArrayList<Lock> locks= transaction.get(i);
    					for (Lock l: locks) {
    						if(l.pid==pid&&l.tableId==tableId) {
    							return true;
    						}
    					}
    				}
    			}
    			return false;
    		}
			/**
    		 * release all the locks of the transaction on a page
    		 * @param tid
    		 * @param tableId
    		 * @param pid
    		 */
    		public void releasePageLock(int tid, int tableId, int pid) {
    			for(int i:transaction.keySet()) {
    				if(i==tid) {
    					boolean findit = false;
    					Lock lo = new Lock(0,0,Permissions.READ_ONLY);
    					for(Lock l:transaction.get(i)) {
    						if(l.pid==pid&&l.tableId==tableId) {
    							findit=true;
    							lo = l;
    							break;
    						}
    					}
    					if(findit) {
							transaction.get(i).remove(lo);
					}
    				}
    			}

    		}

			/**
    		 * give the tid lock
    		 * @param tid tranactionId 
    		 * @param pid pageid
    		 * @param perm permission of the lock
    		 */
    		private boolean acquireLock(int tid, int tableId,int pid, Permissions perm) throws Exception{
    			// check the perm of the page
    			int require = 0;
    			while(blocked(tid,pid,tableId, perm)) { // keep trying to get the lock
    				
    				synchronized(this) {
    					require++;
    					// If tid has tried to acquire a lock too many times, abort
    					if (require >= 10) {
    						throw new Exception("DeadLock");
    					}

    				}

    				try {
    					Thread.sleep(10); // couldn't get lock, wait for some time, then try again
    				} catch (InterruptedException e) {
    				}

    			}
    			
    				//check if the transaction is in the map
    				if(transaction.containsKey(tid)) {
    					transaction.get(tid).add(new Lock(pid,tableId, perm));

    				}else {
    					 ArrayList<Lock> locks = new ArrayList<Lock>();
    					 locks.add(new Lock(pid,tableId,perm));
    					 transaction.put(tid, locks);
    				}
    				return true;
    			
    		}

    		/**
    		 * help method to check if the page has been blocked
    		 * @param pid the pageid
    		 * @param perm the permissions of the lock
    		 * @return
    		 */
    		private boolean blocked(int tid, int pid, int tableId, Permissions perm) {
    			//if the permission is read only. then no one can hold read_write
    			//else no one can hold the read lock
    			if(perm.equals(Permissions.READ_ONLY)) {
    				for(int i : transaction.keySet()) {
    					ArrayList<Lock> locks = transaction.get(i);
    					for(Lock lock:locks) {
    						if(lock.getPid()==pid&&lock.tableId==tableId) {
    							if(lock.getPermission().equals(Permissions.READ_WRITE)) {
    								if(i!=tid) {
        								return true;
        							}
    							}
    						}
    					}
    				}
    			}else {
    				for(int i : transaction.keySet()) {
    					ArrayList<Lock> locks = transaction.get(i);
    					for(Lock lock:locks) {
    						if(lock.getPid()==pid&&lock.tableId==tableId) {
    							if(i!=tid) {
    								return true;
    							}

    						}
    					}
    				}
    			}
    			return false;
    		}
    		
    		
    		
    }
   
 
    /**
     * Record the permission of the page
     * @author wanhaiyu
     *
     */
    private class Lock{
    		private Permissions permission;
    		private int pid;
    		private int tableId;
    		
    		public Lock(int pid,int tableId, Permissions permission) {
    			this.pid=pid;
    			this.tableId=tableId;
    			this.permission= permission;
    		}
    		

			public Permissions getPermission() {
				return permission;
			}
			

			public void setPermission(Permissions permission) {
				this.permission = permission;
			}

			public int getPid() {
				return pid;
			}

			public void setPid(int pid) {
				this.pid = pid;
			}
			
		    		
    }
    
}
