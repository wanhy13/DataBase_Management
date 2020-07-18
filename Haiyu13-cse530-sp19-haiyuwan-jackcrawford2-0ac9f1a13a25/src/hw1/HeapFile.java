package hw1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

import hw2.Relation;

/**
 * A heap file stores a collection of tuples. It is also responsible for managing pages.
 * It needs to be able to manage page creation as well as correctly manipulating pages
 * when tuples are added or deleted.
 * @author Sam Madden modified by Doug Shook
 *
 */
public class HeapFile {
	
	public static final int PAGE_SIZE = 4096;
	private File file;
	private TupleDesc td;
	
	/**
	 * Creates a new heap file in the given location that can accept tuples of the given type
	 * @param f location of the heap file
	 * @param types type of tuples contained in the file
	 */
	public HeapFile(File f, TupleDesc type) {
		//your code here
		this.file=f;
		this.td=type;
	}
	
	public File getFile() {
		//your code here
		return file;
	}
	
	public TupleDesc getTupleDesc() {
		//your code here
		return td;
	}
	
	/**
	 * Creates a HeapPage object representing the page at the given page number.
	 * Because it will be necessary to arbitrarily move around the file, a RandomAccessFile object
	 * should be used here.
	 * @param id the page number to be retrieved
	 * @return a HeapPage at the given page number
	 * @throws IOException 
	 */
	public HeapPage readPage(int id)  {
		//your code here
		try {
			RandomAccessFile newfile= new RandomAccessFile(this.file, "r");
			byte[] data= new byte[PAGE_SIZE];
			
			//cannot use readfully for the testhfMultiPage and testHpRemove
			try {
				newfile.seek(id*PAGE_SIZE);
				newfile.read(data);
				newfile.close();
				
				HeapPage res= new HeapPage(id, data, getId());
				return res;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	/**
	 * Returns a unique id number for this heap file. Consider using
	 * the hash of the File itself.
	 * @return
	 */
	public int getId() {
		//your code here
		return file.hashCode();
	}
	
	/**
	 * Writes the given HeapPage to disk. Because of the need to seek through the file,
	 * a RandomAccessFile object should be used in this method.
	 * @param p the page to write to disk
	 * @throws IOException 
	 */
	public void writePage(HeapPage p) throws IOException {
		//your code here
		RandomAccessFile newfile= new RandomAccessFile(getFile(), "rw");
		newfile.seek(p.getId()*PAGE_SIZE);
		byte[] data= new byte[PAGE_SIZE];
		data=p.getPageData();		
		newfile.write(data);
		newfile.close();
	}
	
	/**
	 * Adds a tuple. This method must first find a page with an open slot, creating a new page
	 * if all others are full. It then passes the tuple to this page to be stored. It then writes
	 * the page to disk (see writePage)
	 * @param t The tuple to be stored
	 * @return The HeapPage that contains the tuple
	 */
	public boolean isfull(HeapPage p) {
		for (int i=0; i<p.getNumSlots(); i++) {
			if(!p.slotOccupied(i)) {
				return false;
			}
		}
		return true;
	}
	public HeapPage addTuple(Tuple t) throws Exception {
		//your code here
		for(int i=0; i<getNumPages(); i++) {
			HeapPage p= readPage(i);
			if(!isfull(p)) {
				p.addTuple(t);
				writePage(p);
				return p;
			}
		}
		
		//create new page
		HeapPage p= new HeapPage(getNumPages(), new byte[PAGE_SIZE], getId());
		p.addTuple(t);
		writePage(p);
		return p;
		
	}
	
	/**
	 * This method will examine the tuple to find out where it is stored, then delete it
	 * from the proper HeapPage. It then writes the modified page to disk.
	 * @param t the Tuple to be deleted
	 * @throws Exception 
	 */
	public void deleteTuple(Tuple t) throws Exception{
		//your code here
		HeapPage p= readPage(t.getPid());
		p.deleteTuple(t);
		writePage(p);
	}
	
	
	/**
	 * Returns an ArrayList containing all of the tuples in this HeapFile. It must
	 * access each HeapPage to do this (see iterator() in HeapPage)
	 * @return
	 * @throws IOException 
	 */
	public ArrayList<Tuple> getAllTuples() {
		//your code here
		ArrayList<Tuple> res = new ArrayList<Tuple>();
		for (int i=0; i<getNumPages();i++) {
			Iterator<Tuple> itr= readPage(i).iterator();
			//while(readPage(i).iterator().hasNext()) {
				//res.add(readPage(i).iterator().next());
			//}
			while(itr.hasNext()) {
				res.add(itr.next());
			}
		}
		return res;
	}
	
	/**
	 * Computes and returns the total number of pages contained in this HeapFile
	 * @return the number of pages
	 */
	public int getNumPages() {
		//your code here
		
		//using ceil or floor seems no differences
		return (int)Math.floor((double)getFile().length() / HeapFile.PAGE_SIZE);
	}
}
