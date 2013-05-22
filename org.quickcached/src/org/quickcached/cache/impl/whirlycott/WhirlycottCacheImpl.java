package org.quickcached.cache.impl.whirlycott;

import com.whirlycott.cache.*;
import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickcached.cache.impl.BaseCacheImpl;

/**
 * WhirlycottCache based implementation
 * @author akshath
 */
public class WhirlycottCacheImpl extends BaseCacheImpl {
	private static final Logger logger = Logger.getLogger(WhirlycottCacheImpl.class.getName());

	private Cache cache = null;	
	
	
	public WhirlycottCacheImpl() {
		FileInputStream myInputStream = null;
		Properties config = null;
		String fileCfg = "./conf/whirlycache/default.ini";
		try {
			config = new Properties();
			myInputStream = new FileInputStream(fileCfg);
			config.load(myInputStream);
		} catch (Exception e) {
			logger.severe("Could not load["+fileCfg+"] "+e);
		} finally {
			if(myInputStream!=null) {
				try {
					myInputStream.close();
				} catch (IOException ex) {
					Logger.getLogger(WhirlycottCacheImpl.class.getName()).log(
							Level.SEVERE, "Error", ex);
				}
			}
		}
		
		try {
			if(config!=null) {
				Map map = CacheManager.getConfiguration();			
				CacheConfiguration cc = (CacheConfiguration) map.get("default");
			
				cc.setTunerSleepTime(Integer.parseInt(config.getProperty("tuner-sleeptime").trim()));
				cc.setPolicy(config.getProperty("policy").trim());
				cc.setMaxSize(Integer.parseInt(config.getProperty("maxsize").trim()));
				cc.setBackend(config.getProperty("backend").trim());
				
				CacheManager.getInstance().destroy("default");
				cache = CacheManager.getInstance().createCache(cc);
			}			
		} catch(Exception e) {
			logger.severe("Error: "+e);
		}
	}
	
	public String getName() {
		return "WhirlycottCacheImpl";
	}
	
	public long getSize() {
		return cache.size();
	}
	public void setToCache(String key, Object value, int objectSize, 
			int expInSec) throws Exception {
		cache.store(key, value, expInSec*1000);
	}
	
	public void updateToCache(String key, Object value, int objectSize) throws Exception {
		//no action required here for ref based cache	
	}
	
	public void updateToCache(String key, Object value, int objectSize, int expInSec) throws Exception {
		cache.store(key, value, expInSec*1000);
	}
	
	
	public Object getFromCache(String key) throws Exception {
		return cache.retrieve(key);
	}
	public boolean deleteFromCache(String key) throws Exception {
		Object obj = cache.remove(key);
		return obj!=null;
	}
	
	public void flushCache() throws Exception {
		cache.clear();
	}
	
	public boolean saveToDisk() {
		return false;
	}

	public boolean readFromDisk() {		
		return false;
	}
}
