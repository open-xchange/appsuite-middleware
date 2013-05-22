package org.quickcached.cache.impl.directbytebuffer;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickcached.QuickCached;
import org.quickcached.cache.impl.BaseCacheImpl;

/**
 *
 * @author Akshathkumar Shetty
 */
public class DirectByteBufferImpl extends BaseCacheImpl {
	private static final Logger logger = Logger.getLogger(DirectByteBufferImpl.class.getName());
	private static int tunerSleeptime = 130;//in sec
	
	private Map map = new ConcurrentHashMap();
	private Map mapTtl = new ConcurrentHashMap();
	
	protected volatile long expired;
	
	private Thread purgeThread = null;
	
	public DirectByteBufferImpl() {
		startPurgeThread();
	}
	
	private void startPurgeThread()  {
		purgeThread = new Thread("DirectByteBuffer-PurgeThread") {
			public void run() {
				long timespent = 0;
				long timeToSleep = 0;
				long stime = 0;
				long etime = 0;
				
				while(true) {
					timeToSleep = tunerSleeptime*1000 - timespent;
					if(timeToSleep>0) {
						try {
							Thread.sleep(timeToSleep);
						} catch (InterruptedException ex) {
							logger.log(Level.FINE, "Interrupted "+ex);
							break;
						}
					}
					
					stime = System.currentTimeMillis();
					try {
						purgeOperation();
					} catch (Exception ex) {
						logger.log(Level.WARNING, "Error: "+ex, ex);
					}
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
			while(iterator.hasNext()) {
				key = (String) iterator.next();
				expTime = (Date) mapTtl.get(key);
				
				if(expTime.before(currentTime)) {
					mapTtl.remove(key);
					map.remove(key);
					expired++;
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error: " + e, e);
		}
	}
	
	public void saveStats(Map stats) {
		if(stats==null) stats = new LinkedHashMap();
		super.saveStats(stats);
		
		//expired - Number of items that expired
		stats.put("expired", ""+expired);
	}
	
	public String getName() {
		return "DirectByteBufferImpl";
	}
	
	public long getSize() {
		return map.size();
	}
	
	public void setToCache(String key, Object value, int objectSize, 
			int expInSec) throws Exception {
		ByteBuffer buffer = getByteBuffer(value);
		map.put(key, buffer);
		if (expInSec != 0) {
			mapTtl.put(key, new Date(System.currentTimeMillis()+expInSec*1000));
		} else {
			mapTtl.remove(key);//just in case
		}
	}
	
	public void updateToCache(String key, Object value, int objectSize) throws Exception {
		ByteBuffer buffer = getByteBuffer(value);
		map.put(key, buffer);
	}
	
	public void updateToCache(String key, Object value, int objectSize, int expInSec) throws Exception {
		updateToCache(key, value, objectSize);
		if (expInSec != 0) {
			mapTtl.put(key, new Date(System.currentTimeMillis()+expInSec*1000));
		} else {
			mapTtl.remove(key);//just in case
		}
	}	
	
	public Object getFromCache(String key) throws IOException, ClassNotFoundException {
		ByteBuffer buffer = (ByteBuffer) map.get(key);
		if (buffer != null) {
			return getByteBuffer(buffer);
		} else {
			if(QuickCached.DEBUG) logger.log(Level.FINE, "no value for key: {0}", key);
			return null;
		}
	}
	
	public boolean deleteFromCache(String key) throws Exception {
		mapTtl.remove(key);
		Object obj = map.remove(key);
		return obj!=null;
	}
	
	public void flushCache() throws Exception {
		mapTtl.clear();
		map.clear();
	}
	
	private ByteBuffer getByteBuffer(Object value) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream stream = new ObjectOutputStream(baos);
		stream.writeObject(value);
		stream.close();
		byte b[] = baos.toByteArray();
		ByteBuffer buffer = ByteBuffer.allocateDirect(b.length);
		buffer.put(b);
		return buffer;
	}
	
	private Object getByteBuffer(ByteBuffer buffer) throws IOException, ClassNotFoundException {
		byte buf[] = new byte[buffer.capacity()];
		buffer.rewind();
		buffer.get(buf);
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInputStream oois = new ObjectInputStream(bais);
		Object object = oois.readObject();
		oois.close();
		return object;
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
		
		HashMap<Object, Object> wcache = new HashMap<Object, Object>();

		Iterator<String> it = map.keySet().iterator();
		String key = null;
		Object value = null;
		while(it.hasNext()) {
				key = (String) it.next();
				try {
					value = getFromCache(key);
				} catch (ClassNotFoundException ex) {
					logger.log(Level.FINE, "Error: "+ex, ex);
					continue;
				}
				
				if(value!=null) {
					wcache.put(key, value);
				} else {
					mapTtl.remove(key);
				}
		}
		out.writeObject(wcache);
		out.writeObject(mapTtl);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		HashMap<Object, Object> wcache = (HashMap<Object, Object>) in.readObject();
		
		Iterator it = wcache.keySet().iterator();
		Object key = null;
		Object value = null;
		while(it.hasNext()) {
			key = it.next();
			value = wcache.get(key);
			
			ByteBuffer buffer = getByteBuffer(value);
			map.put(key, buffer);
		}
		
		mapTtl = (Map) in.readObject();
	} 
}
