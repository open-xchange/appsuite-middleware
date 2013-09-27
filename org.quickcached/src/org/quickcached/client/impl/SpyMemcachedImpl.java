package org.quickcached.client.impl;

import net.spy.memcached.internal.OperationFuture;
import org.quickcached.client.TimeoutException;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.quickcached.client.MemcachedClient;
import java.util.Map;
import net.spy.memcached.CASValue;

/**
 *
 * @author Akshathkumar Shetty
 */
public class SpyMemcachedImpl extends MemcachedClient {
	private net.spy.memcached.MemcachedClient[] c = null;        
	
	private String hostList;
	private boolean binaryConnection = true;
	
	private int poolSize = 20;
	
	public SpyMemcachedImpl() {
		
	}
	
	public void setUseBinaryConnection(boolean flag) {
		binaryConnection = flag;
	}
	
	public void setConnectionPoolSize(int size) {
		poolSize = size;
	}
	
	public void setAddresses(String list) {
		hostList = list;
	}

	public void addServer(String list) throws IOException{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void removeServer(String list) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void init() throws IOException {
		if(c!=null) stop();
		
		c = new net.spy.memcached.MemcachedClient[poolSize];
		if(binaryConnection==false) {
			for(int i=0;i<poolSize;i++) {
				c[i] = new net.spy.memcached.MemcachedClient(
					net.spy.memcached.AddrUtil.getAddresses(hostList));
			}
		} else {
			for(int i=0;i<poolSize;i++) {
				c[i] = new net.spy.memcached.MemcachedClient(
					new net.spy.memcached.BinaryConnectionFactory(),
					net.spy.memcached.AddrUtil.getAddresses(hostList));
			}
		}
	}

	public void stop() throws IOException {
		for(int i=0;i<poolSize;i++) {
			c[i].shutdown();
		}
		c = null;
	}
	
	public net.spy.memcached.MemcachedClient getCache() {
		int i = (int) (Math.random()* poolSize);
		return c[i];
	}
	
	public boolean touch(String key, int ttlSec, long timeoutMiliSec) throws TimeoutException {
		Future <Boolean> f = getCache().touch(key, ttlSec);
		Boolean flag = false;
		try {
			flag = (Boolean) f.get(timeoutMiliSec, TimeUnit.MILLISECONDS);
		} catch(Exception e) {
			f.cancel(false);
			throw new TimeoutException("Timeout "+e);
		}
		return flag.booleanValue();
	}
			

	public void set(String key, int ttlSec, Object value, long timeoutMiliSec) 
			throws TimeoutException {
		Future <Boolean> f = getCache().set(key, ttlSec, value);
		Boolean flag = false;
		try {
			flag = (Boolean) f.get(timeoutMiliSec, TimeUnit.MILLISECONDS);
		} catch(Exception e) {
			f.cancel(false);
			throw new TimeoutException("Timeout "+e);
		}
	}
	
	public boolean add(String key, int ttlSec, Object value, long timeoutMiliSec) 
			throws TimeoutException {
		Future <Boolean> f = getCache().add(key, ttlSec, value);
		Boolean flag = false;
		try {
			flag = (Boolean) f.get(timeoutMiliSec, TimeUnit.MILLISECONDS);
		} catch(Exception e) {
			f.cancel(false);
			throw new TimeoutException("Timeout "+e);
		}
		return flag.booleanValue();
	}
	
	public boolean replace(String key, int ttlSec, Object value, long timeoutMiliSec) 
			throws TimeoutException {
		Future <Boolean> f = getCache().replace(key, ttlSec, value);
		Boolean flag = false;
		try {
			flag = (Boolean) f.get(timeoutMiliSec, TimeUnit.MILLISECONDS);
		} catch(Exception e) {
			f.cancel(false);
			throw new TimeoutException("Timeout "+e);
		}
		return flag.booleanValue();
	}
	
	public boolean append(String key, Object value, long timeoutMiliSec) 
			throws TimeoutException {
		CASValue casv = getCache().gets(key);		
		Future <Boolean> f = getCache().append(casv.getCas(), key, value);
		Boolean flag = false;
		try {
			flag = (Boolean) f.get(timeoutMiliSec, TimeUnit.MILLISECONDS);
		} catch(Exception e) {
			f.cancel(false);
			throw new TimeoutException("Timeout "+e);
		}
		return flag.booleanValue();
	}
	
	public boolean prepend(String key, Object value, long timeoutMiliSec) 
			throws TimeoutException {
		CASValue casv = getCache().gets(key);
		Future <Boolean> f = getCache().prepend(casv.getCas(),key, value);
		Boolean flag = false;
		try {
			flag = (Boolean) f.get(timeoutMiliSec, TimeUnit.MILLISECONDS);
		} catch(Exception e) {
			f.cancel(false);
			throw new TimeoutException("Timeout "+e);
		}
		return flag.booleanValue();
	}

	public Object get(String key, long timeoutMiliSec) throws TimeoutException {
		Object readObject = null;
		Future <Object> f = getCache().asyncGet(key);
		try {
			readObject = f.get(timeoutMiliSec, TimeUnit.MILLISECONDS);
		} catch(Exception e) {
			f.cancel(false);
			throw new TimeoutException("Timeout "+e);
		}
		return readObject;
	}
	
	public Object gat(String key, int ttlSec, long timeoutMiliSec) throws TimeoutException {
		OperationFuture <CASValue<Object>> f = getCache().asyncGetAndTouch(key, ttlSec);
		CASValue<Object> casv = null;
		try {
			casv = f.get(timeoutMiliSec, TimeUnit.MILLISECONDS);
		} catch(Exception e) {
			f.cancel(false);
			throw new TimeoutException("Timeout "+e);
		}		
		return casv.getValue();
	}

	public boolean delete(String key, long timeoutMiliSec) throws TimeoutException {
		Future <Boolean> f = getCache().delete(key);
		Boolean flag = false;
		try {
			flag = (Boolean) f.get(timeoutMiliSec, TimeUnit.MILLISECONDS);
		} catch(Exception e) {
			f.cancel(false);
			throw new TimeoutException("Timeout "+e);
		}
		return flag.booleanValue();
	}
	
	public void increment(String key, int value, long timeoutMiliSec) 
			throws TimeoutException {
		long newval = getCache().incr(key, value);
		if(newval==-1) {
			throw new TimeoutException("Timeout ");
		}
	}

	public void decrement(String key, int value, long timeoutMiliSec) 
			throws TimeoutException {
		long newval = getCache().decr(key, value);
		if(newval==-1) {
			throw new TimeoutException("Timeout ");
		}
	}

	public void flushAll() throws TimeoutException {
		for(int i=0;i<poolSize;i++) {
			c[i].flush();
		}
	}
	
	public Object getBaseClient() {
		return getCache();
	}
        
	public Map getStats() throws Exception {
		return getCache().getStats();
	}
	
	public Map getVersions() throws TimeoutException {
		return getCache().getVersions();
	}
	
}
