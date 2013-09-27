package org.quickcached.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Akshathkumar Shetty
 */
public abstract class MemcachedClient {
	public static final String SpyMemcachedImpl = "SpyMemcached";
	public static final String XMemcachedImpl = "XMemcached";
	
	private static String defaultImpl = XMemcachedImpl;	
	
	private static Map implMap = new HashMap();
	public static void registerImpl(String implName, String fullClassName) {
		implMap.put(implName, fullClassName);
	}
	
	static {
		registerImpl(SpyMemcachedImpl, "org.quickcached.client.impl.SpyMemcachedImpl");
		registerImpl(XMemcachedImpl, "org.quickcached.client.impl.XMemcachedImpl");
		
		String impl = System.getProperty("org.quickcached.client.defaultImpl");
		if(impl!=null) {
			defaultImpl = impl;
		}
	}
	
	public static MemcachedClient getInstance() 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return getInstance(null);
	}
	
	public static MemcachedClient getInstance(String implName) 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String fullClassName = (String) implMap.get(implName);
		if(fullClassName==null) fullClassName = (String) implMap.get(defaultImpl);
		
		MemcachedClient client = (MemcachedClient) Class.forName(fullClassName).newInstance();
		
		String binaryConnection = System.getProperty("org.quickcached.client.binaryConnection");
		if(binaryConnection==null || binaryConnection.equalsIgnoreCase("true")) {
			client.setUseBinaryConnection(true);
		} else {
			client.setUseBinaryConnection(false);
		}
		return client;
	}

	private long defaultTimeoutMiliSec = 1000;//1sec
	public long getDefaultTimeoutMiliSec() {
		return defaultTimeoutMiliSec;
	}

	public void setDefaultTimeoutMiliSec(int aDefaultTimeoutMiliSec) {
		defaultTimeoutMiliSec = aDefaultTimeoutMiliSec;
	}
	
	public abstract void setUseBinaryConnection(boolean flag);
	public abstract void setConnectionPoolSize(int size);

	public abstract void setAddresses(String list);
	public abstract void init() throws IOException;
	public abstract void stop() throws IOException ;
	
	public abstract void addServer(String list) throws IOException;
	public abstract void removeServer(String list);
	
	public abstract void set(String key, int ttlSec, Object value, long timeoutMiliSec) 
			throws TimeoutException;
	public abstract Object get(String key, long timeoutMiliSec) throws TimeoutException;
	public abstract boolean delete(String key, long timeoutMiliSec) throws TimeoutException;
	public abstract void flushAll() throws TimeoutException;
	
	public abstract boolean touch(String key, int ttlSec, long timeoutMiliSec) throws TimeoutException;
	public abstract Object gat(String key, int ttlSec, long timeoutMiliSec) throws TimeoutException;
        
	public abstract Map getStats() throws Exception;
	public abstract Object getBaseClient();
	
	public abstract boolean add(String key, int ttlSec, Object value, long timeoutMiliSec) 
			throws TimeoutException;
	public abstract boolean replace(String key, int ttlSec, Object value, long timeoutMiliSec) 
			throws TimeoutException;
	public abstract boolean append(String key, Object value, long timeoutMiliSec) 
			throws TimeoutException;
	public abstract boolean prepend(String key, Object value, long timeoutMiliSec) 
			throws TimeoutException;
	
	public abstract void increment(String key, int value, long timeoutMiliSec) 
			throws TimeoutException;
	public abstract void decrement(String key, int value, long timeoutMiliSec) 
			throws TimeoutException;
	
	public abstract Map getVersions() throws TimeoutException;	
	
	public void set(String key, int ttlSec, Object value) 
			throws TimeoutException {
		set(key, ttlSec, value, defaultTimeoutMiliSec);
	}
	public Object get(String key) throws TimeoutException {
		return get(key, defaultTimeoutMiliSec);
	}
	public boolean delete(String key) throws TimeoutException {
		return delete(key, defaultTimeoutMiliSec);
	}
	
	public boolean add(String key, int ttlSec, Object value) throws TimeoutException {
		return add(key, ttlSec, value, defaultTimeoutMiliSec);
	}
	public boolean replace(String key, int ttlSec, Object value) 
			throws TimeoutException {
		return replace(key, ttlSec, value, defaultTimeoutMiliSec);
	}
	public boolean append(String key, Object value) 
			throws TimeoutException {
		return append(key, value, defaultTimeoutMiliSec);
	}
	public boolean prepend(String key, Object value) 
			throws TimeoutException {
		return prepend(key, value, defaultTimeoutMiliSec);
	}
	
	public void increment(String key, int value) throws TimeoutException {
		increment(key, value, defaultTimeoutMiliSec);
	}
	public void decrement(String key, int value) throws TimeoutException {
		decrement(key, value, defaultTimeoutMiliSec);
	}
	
	public boolean touch(String key, int ttlSec) throws TimeoutException {
		return touch(key, ttlSec, defaultTimeoutMiliSec);
	}
	
	public Object gat(String key, int ttlSec) throws TimeoutException {
		return gat(key, ttlSec, defaultTimeoutMiliSec);
	}
}
