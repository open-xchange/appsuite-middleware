package org.quickcached.cache.impl.softreferencemap;

import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickcached.QuickCached;
import org.quickcached.cache.impl.BaseCacheImpl;


/**
 * A SoftReference based ConcurrentHashMap cache.
 * 
 * Code based on example from http://www.javaspecialists.eu/archive/Issue015.html
 *
 * @author Akshathkumar Shetty
 */
public class SoftReferenceMapImpl extends BaseCacheImpl {
	private static final Logger logger = Logger.getLogger(SoftReferenceMapImpl.class.getName());
	
	/**
	 * The number of "hard" references to hold internally.
	 */
	private static int hardSize = 1000;
	
	/**
	 * The internal HashMap that will hold the SoftReference.
	 */
	private Map map = new ConcurrentHashMap();
	private Map mapTtl = new ConcurrentHashMap();
	
	/**
	 * The FIFO list of hard references, order of last access.
	 */
	private List hardCache = Collections.synchronizedList(new LinkedList());
	/**
	 * Reference queue for cleared SoftReference objects.
	 */
	private final ReferenceQueue queue = new ReferenceQueue();
	
	private int tunerSleeptime = 120;//in sec
	
	private final Object lock = new Object();
	
	protected volatile long evicted;
	protected volatile long expired;
	
	private Thread purgeThread = null;
	
	public SoftReferenceMapImpl() {
		startPurgeThread();
	}

	private void startPurgeThread()  {
		purgeThread = new Thread("SoftReferenceMapImpl-PurgeThread") {
			public void run() {
				long timespent = 0;
				long timeToSleep = 0;
				long stime = 0;
				long etime = 0;
				while (true) {
					timeToSleep = tunerSleeptime * 1000 - timespent;
					if (timeToSleep > 0) {
						try {
							Thread.sleep(timeToSleep);
						} catch (InterruptedException ex) {
							Logger.getLogger(SoftReferenceMapImpl.class.getName()).log(
								Level.FINE, "Interrupted "+ex);
							break;
						}
					}

					stime = System.currentTimeMillis();
					try {
						purgeOperation();
					} catch (Exception ex) {
						Logger.getLogger(SoftReferenceMapImpl.class.getName()).log(
							Level.SEVERE, null, ex);
					}
					
					processQueue();
					
					etime = System.currentTimeMillis();
					timespent = etime - stime;
				}
			}
		};
		purgeThread.setDaemon(true);
		purgeThread.start();
	}

	public void purgeOperation() {
		try {
			Iterator iterator = mapTtl.keySet().iterator();
			String key = null;
			Date expTime;
			Date currentTime = new Date();
			while (iterator.hasNext()) {
				key = (String) iterator.next();
				expTime = (Date) mapTtl.get(key);

				if (expTime.before(currentTime)) {
					mapTtl.remove(key);
					map.remove(key);
					while(true) {
						if(hardCache.remove(key)==false) {
							break;
						}
					}
					expired++;
				}
				Thread.yield();
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error: " + e, e);
		}
	}
	
	public void saveStats(Map stats) {
		if(stats==null) stats = new LinkedHashMap();
		super.saveStats(stats);
		
		//evicted - Number of valid items removed from cache to free memory for new items
		stats.put("evicted", "" + evicted);
		
		//expired - Number of items that expired
		stats.put("expired", ""+expired);
	}

	public String getName() {
		return "SoftReferenceMapImpl";
	}

	public long getSize() {
		processQueue(); // throw out garbage collected values first
		return map.size();
	}

	public void setToCache(String key, Object value, int objectSize,
			int expInSec) throws Exception {
		map.put(key, new SoftValue(value, key, queue));
		if (expInSec != 0) {
			mapTtl.put(key, new Date(System.currentTimeMillis() + expInSec * 1000));
		} else {
			mapTtl.remove(key);//just in case
		}
	}

	public void updateToCache(String key, Object value, int objectSize) throws Exception {
		//no action required here for ref based cache
	}
	
	public void updateToCache(String key, Object value, int objectSize, int expInSec) throws Exception {
		//no action required here for ref based cache
		if (expInSec != 0) {
			mapTtl.put(key, new Date(System.currentTimeMillis() + expInSec * 1000));
		} else {
			mapTtl.remove(key);//just in case
		}
	}

	public Object getFromCache(String key) throws Exception {
		Object result = null;
		// We get the SoftReference represented by that key
		SoftReference softRef = (SoftReference) map.get(key);
		if (softRef != null) {
			// From the SoftReference we get the value, which can be
			// null if it was not in the map, or it was removed in
			// the processQueue() method defined below
			result = softRef.get();
			if (result == null) {
				// If the value has been garbage collected, remove the
				// entry from the HashMap.
				map.remove(key);
				mapTtl.remove(key);
			} else {
				// We now add this object to the beginning of the hard
				// reference queue.  One reference can occur more than
				// once, because lookups of the FIFO queue are slow, so
				// we don't want to search through it each time to remove
				// duplicates.
				hardCache.add(0, result);//addFirst
				
				try {
					synchronized(lock) {
						int size = hardCache.size();
						if (size > getHardSize()) {
							// Remove the last entry if list longer than HARD_SIZE
							hardCache.remove(size-1);//removeLast();
						}
					}
				} catch(IndexOutOfBoundsException e) {
					logger.log(Level.SEVERE, "IndexOutOfBoundsException: "+e);
				}
			}
		}
		if(result==null && QuickCached.DEBUG) {
			logger.log(Level.FINE, "no value in db for key: {0}", key);
		}
		return result;
	}

	public boolean deleteFromCache(String key) throws Exception {		
		mapTtl.remove(key);
		Object obj = map.remove(key);
		
		return obj != null;
	}

	public void flushCache() throws Exception {
		hardCache.clear();	
		mapTtl.clear();
		map.clear();
	}

	

	public static int getHardSize() {
		return hardSize;
	}

	public static void setHardSize(int aHardSize) {
		hardSize = aHardSize;
	}

	/**
	 * We define our own subclass of SoftReference which contains not only the
	 * value but also the key to make it easier to find the entry in the HashMap
	 * after it's been garbage collected.
	 */
	private static class SoftValue extends SoftReference {
		private final Object key; // always make data member final

		/**
		 * Did you know that an outer class can access private data members and
		 * methods of an inner class? I didn't know that! I thought it was only
		 * the inner class who could access the outer class's private
		 * information. An outer class can also access private members of an
		 * inner class inside its inner class.
		 */
		private SoftValue(Object v, Object key, ReferenceQueue q) {
			super(v, q);
			this.key = key;
		}		
	}

	/**
	 * Here we go through the ReferenceQueue and remove garbage collected
	 * SoftValue objects from the HashMap by looking them up using the
	 * SoftValue.key data member.
	 */
	private void processQueue() {
		SoftValue sv = null;
		try {
			while ((sv = (SoftValue) queue.poll()) != null) {
				map.remove(sv.key); // we can access private data!
				mapTtl.remove(sv.key); // we can access private data!				
				while(true) {
					if(hardCache.remove(sv.key)==false) {
						break;
					}
				}
				evicted++;
				Thread.yield();
			}
		} catch (Throwable e) {
			logger.log(Level.WARNING, "Error: "+e, e);
		}
	}
	
	private String fileName = "./"+getName()+"_"+QuickCached.getPort()+".dat";
	public boolean saveToDisk() {
		System.out.print("Saving state to disk.. ");
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(fileName));
			writeObject(oos);
			oos.flush();
			return true;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error: {0}", e);
		} finally {
			if(oos!=null) {
				try {
					oos.close();
				} catch (IOException ex) {
					logger.log(Level.WARNING, "Error: "+ex, ex);
				}
			}
			System.out.println("Done");
		}		
		return false;
	}

	public boolean readFromDisk() {
		File file = new File(fileName);
		if(file.canRead()==false) return false;
		System.out.print("Reading state from disk.. ");
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(fileName));
			readObject(ois);
			return true;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error: {0}", e);
		} finally {
			if(ois!=null) {
				try {
					ois.close();
				} catch (IOException ex) {
					logger.log(Level.WARNING, "Error: "+ex, ex);
				}
			}
			file.delete();
			System.out.println("Done");
		}
		return false;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		purgeThread.interrupt();
		processQueue();
		
		HashMap<Object, Object> wcache = new HashMap<Object, Object>();

		Iterator<String> it = map.keySet().iterator();
		Object key = null;
		Object value = null;
		while(it.hasNext()) {
				key = it.next();
				SoftReference ref = (SoftReference) map.get(key);
				if(ref == null) continue;
				value = ref.get();
				if(value!=null) {
					wcache.put(key, value);
				} else {
					mapTtl.remove(key);
					while(true) {
						if(hardCache.remove(key)==false) {
							break;
						}
					}
				}
		}
		out.writeObject(wcache);
		out.writeObject(mapTtl);
		out.writeObject(hardCache);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		HashMap<Object, Object> wcache = (HashMap<Object, Object>) in.readObject();
		
		Iterator it = wcache.keySet().iterator();
		Object key = null;
		Object value = null;
		while(it.hasNext()) {
			key = it.next();
			value = wcache.get(key);
			map.put(key, new SoftValue(value, key, queue));
		}
		
		mapTtl = (Map) in.readObject();
		hardCache = (List) in.readObject();
	} 
}
