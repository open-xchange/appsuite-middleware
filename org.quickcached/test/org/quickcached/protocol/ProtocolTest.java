package org.quickcached.protocol;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

import net.rubyeye.xmemcached.exception.MemcachedException;
import org.quickcached.client.*;

/**
 *
 * @author akshath
 */
public class ProtocolTest extends TestCase  {
	protected MemcachedClient c = null;

	public ProtocolTest(String name) {
        super(name);
    }

	public void testGet() throws TimeoutException {
		String readObject = null;
		String key = null;
		//1
		key = "testget1";
		c.set(key, 3600, "World");
		readObject = (String) c.get(key);
		
		assertNotNull(readObject);
		assertEquals("World",  readObject);
    
		//2
        Date value = new Date();
		key = "testget2";
		c.set(key, 3600, value);
		Date readObjectDate = (Date) c.get(key);

		assertNotNull(readObjectDate);
		assertEquals(value.getTime(),  readObjectDate.getTime());
		
		//3
		Object client = c.getBaseClient();
		key = "testget3";
		if(client instanceof net.rubyeye.xmemcached.MemcachedClient) {
			net.rubyeye.xmemcached.MemcachedClient xmc = (net.rubyeye.xmemcached.MemcachedClient) client;
			
			try {
				xmc.setWithNoReply(key, 3600, "World");			
			} catch (InterruptedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MemcachedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			readObject = (String) c.get(key);
			assertNotNull(readObject);
			assertEquals("World",  readObject);
		} else if (client instanceof net.spy.memcached.MemcachedClient) {
			net.spy.memcached.MemcachedClient smc = (net.spy.memcached.MemcachedClient) client;
			
			//does not support noreply
		}
		
	}

	public void testAppend() throws TimeoutException {
		String readObject = null;
		String key = null;
		String value = null;
		
		//1
		key = "testapp1";
		c.set(key, 3600, "ABCD");

		c.append(key, "EFGH");
		readObject = (String) c.get(key);

		assertNotNull(readObject);
		assertEquals("ABCDEFGH", readObject);
		
		//2
		Object client = c.getBaseClient();
		if(client instanceof net.rubyeye.xmemcached.MemcachedClient) {
			net.rubyeye.xmemcached.MemcachedClient xmc = (net.rubyeye.xmemcached.MemcachedClient) client;
			key = "testapp1";
			try {
				xmc.appendWithNoReply(key,"XYZ");				
			} catch (InterruptedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MemcachedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			readObject = (String) c.get(key);
			assertNotNull(readObject);
			assertEquals("ABCDEFGHXYZ", readObject);
		} else if (client instanceof net.spy.memcached.MemcachedClient) {			
			//does not support noreply			
		}		
	}

	public void testPrepend() throws TimeoutException {
		String readObject = null;
		String key = null;
		String value = null;
		
		key = "testpre1";

		//1
		c.set(key, 3600, "ABCD");
		c.prepend(key, "EFGH");
		readObject = (String) c.get(key);

		assertNotNull(readObject);
		assertEquals("EFGHABCD", readObject);
		
		//2
		Object client = c.getBaseClient();
		if(client instanceof net.rubyeye.xmemcached.MemcachedClient) {
			net.rubyeye.xmemcached.MemcachedClient xmc = (net.rubyeye.xmemcached.MemcachedClient) client;
			key = "testpre1";
			try {
				xmc.prependWithNoReply(key,"XYZ");				
			} catch (InterruptedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MemcachedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			readObject = (String) c.get(key);
			assertNotNull(readObject);
			assertEquals("XYZEFGHABCD", readObject);
		} else if (client instanceof net.spy.memcached.MemcachedClient) {			
			//does not support noreply			
		}
	}

	public void testAdd() throws TimeoutException {
		String readObject = null;
		String key = null;
		String value = null;
		
		//1
        value = "ABCD";
		key = "testadd1";

		c.delete(key);
		boolean flag = c.add(key, 3600, value);
		assertTrue(flag);
		
		readObject = (String) c.get(key);
		assertNotNull(readObject);
		assertEquals("ABCD", readObject);

		
		flag = c.add(key, 3600, "XYZ");
		assertFalse(flag);
		
		//read old value
		readObject = (String) c.get(key);
		assertNotNull(readObject);
		assertEquals("ABCD", readObject);
		
		//2
		key = "testadd2";
		c.delete(key);
		Object client = c.getBaseClient();
		if(client instanceof net.rubyeye.xmemcached.MemcachedClient) {
			net.rubyeye.xmemcached.MemcachedClient xmc = (net.rubyeye.xmemcached.MemcachedClient) client;
			try {
				xmc.addWithNoReply(key, 3600, value);
			} catch (InterruptedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MemcachedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			readObject = (String) c.get(key);
			assertNotNull(readObject);
			assertEquals("ABCD", readObject);

			flag = c.add(key, 3600, "XYZ");
			assertFalse(flag);

			//read old value
			readObject = (String) c.get(key);
			assertNotNull(readObject);
			assertEquals("ABCD", readObject);
		} else if (client instanceof net.spy.memcached.MemcachedClient) {			
			//does not support noreply			
		}
		
	}

	public void testReplace() throws TimeoutException {
		String readObject = null;
		String key = null;
		String value = null;
		
		//1
        value = "ABCD";
		key = "testrep1";

		c.set(key, 3600, "World");

		boolean flag = c.replace(key, 3600, value);
		assertTrue(flag);
		readObject = (String) c.get(key);
		assertNotNull(readObject);
		assertEquals("ABCD", readObject);

		c.delete(key);
		
		flag = c.replace(key, 3600, "XYZ");
		assertFalse(flag);
		
		//read old value i.e. no value
		readObject = (String) c.get(key);
		assertNull(readObject);
		
		//2
		key = "testrep2";
		c.set(key, 3600, "World");
		
		Object client = c.getBaseClient();
		if(client instanceof net.rubyeye.xmemcached.MemcachedClient) {
			net.rubyeye.xmemcached.MemcachedClient xmc = (net.rubyeye.xmemcached.MemcachedClient) client;
			try {
				xmc.replaceWithNoReply(key, 3600, value);
			} catch (InterruptedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MemcachedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			readObject = (String) c.get(key);
			assertNotNull(readObject);
			assertEquals("ABCD", readObject);
		} else if (client instanceof net.spy.memcached.MemcachedClient) {			
			//does not support noreply			
		}		
		
		c.delete(key);		
		if(client instanceof net.rubyeye.xmemcached.MemcachedClient) {
			net.rubyeye.xmemcached.MemcachedClient xmc = (net.rubyeye.xmemcached.MemcachedClient) client;
			try {
				xmc.replaceWithNoReply(key, 3600, "XYZ");
			} catch (InterruptedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MemcachedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			//read old value i.e. no value
			readObject = (String) c.get(key);
			assertNull(readObject);	
		} else if (client instanceof net.spy.memcached.MemcachedClient) {			
			//does not support noreply			
		}			
	}

	public void testIncrement() throws TimeoutException {
		String readObject = null;
		String key = null;
		String value = null;
		
		//1
		key = "testinc1";
        value = "10";		

		c.set(key, 3600, value);
		c.increment(key, 10);

		readObject = (String) c.get(key);
		assertNotNull(readObject);
		assertEquals("20", readObject);

		c.increment(key, 1);
		readObject = (String) c.get(key);
		assertNotNull(readObject);
		assertEquals("21", readObject);
		
		//2
		Object client = c.getBaseClient();
		if(client instanceof net.rubyeye.xmemcached.MemcachedClient) {
			net.rubyeye.xmemcached.MemcachedClient xmc = (net.rubyeye.xmemcached.MemcachedClient) client;
			try {
				xmc.incrWithNoReply(key, 4);
			} catch (InterruptedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MemcachedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			readObject = (String) c.get(key);
			assertNotNull(readObject);
			assertEquals("25", readObject);
		} else if (client instanceof net.spy.memcached.MemcachedClient) {			
			//does not support noreply
		}		
	}

	public void testDecrement() throws TimeoutException {
		String readObject = null;
		String key = null;
		String value = null;
		
        //1
		key = "testdec1";
        value = "25";		

		c.set(key, 3600, value);
		c.decrement(key, 10);

		readObject = (String) c.get(key);
		assertNotNull(readObject);
		assertEquals("15", readObject);

		c.decrement(key, 1);
		readObject = (String) c.get(key);
		assertNotNull(readObject);
		assertEquals("14", readObject);
		
		//2
		Object client = c.getBaseClient();
		if(client instanceof net.rubyeye.xmemcached.MemcachedClient) {
			net.rubyeye.xmemcached.MemcachedClient xmc = (net.rubyeye.xmemcached.MemcachedClient) client;
			try {
				xmc.decrWithNoReply(key, 4);
			} catch (InterruptedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MemcachedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			readObject = (String) c.get(key);
			assertNotNull(readObject);
			assertEquals("10", readObject);
		} else if (client instanceof net.spy.memcached.MemcachedClient) {			
			//does not support noreply
		}
		
	}

	public void testDelete() throws TimeoutException {
		String readObject = null;
		String key = null;
		String value = null;
		
		//1
		key = "testdel1";
		
		c.set(key, 3600, "World");
		readObject = (String) c.get(key);

		assertNotNull(readObject);
		assertEquals("World",  readObject);

		c.delete(key);
		readObject = (String) c.get(key);
		assertNull(readObject);
		
		//2
		c.set(key, 3600, "World");
		readObject = (String) c.get(key);

		assertNotNull(readObject);
		assertEquals("World",  readObject);
		Object client = c.getBaseClient();
		if(client instanceof net.rubyeye.xmemcached.MemcachedClient) {
			net.rubyeye.xmemcached.MemcachedClient xmc = (net.rubyeye.xmemcached.MemcachedClient) client;
			try {
				xmc.deleteWithNoReply(key);
			} catch (InterruptedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MemcachedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			readObject = (String) c.get(key);
			assertNull(readObject);
		} else if (client instanceof net.spy.memcached.MemcachedClient) {			
			//does not support noreply
		}
		
	}

	public void testVersion() throws TimeoutException {
		Map ver = c.getVersions();
		assertNotNull(ver);
		//System.out.println("ver: "+ver);
		Iterator iterator = ver.keySet().iterator();
		InetSocketAddress key = null;
		while(iterator.hasNext()) {
			key = (InetSocketAddress) iterator.next();
			assertNotNull(key);
			//assertEquals("1.4.5",  (String) ver.get(key));
		}
	}

	public void testStats() throws Exception {
		Map stats = c.getStats();
		assertNotNull(stats);

		Iterator iterator = stats.keySet().iterator();
		InetSocketAddress key = null;
		while(iterator.hasNext()) {
			key = (InetSocketAddress) iterator.next();
			assertNotNull(key);
			//System.out.println("Stat for "+key+" " +stats.get(key));
		}
	}

	public void testFlush() throws TimeoutException {
		String readObject = null;
		String key = null;
		String value = null;
		
		//1
		key = "testflush1";
		c.set(key, 3600, "World");
		readObject = (String) c.get(key);

		assertNotNull(readObject);
		assertEquals("World",  readObject);

		c.flushAll();

		readObject = (String) c.get(key);
		assertNull(readObject);
		
		//2
		c.set(key, 3600, "World");
		readObject = (String) c.get(key);

		assertNotNull(readObject);
		assertEquals("World",  readObject);
		
		Object client = c.getBaseClient();
		if(client instanceof net.rubyeye.xmemcached.MemcachedClient) {
			net.rubyeye.xmemcached.MemcachedClient xmc = (net.rubyeye.xmemcached.MemcachedClient) client;
			try {
				xmc.flushAllWithNoReply();
			} catch (InterruptedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MemcachedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			readObject = (String) c.get(key);
			assertNull(readObject);
		} else if (client instanceof net.spy.memcached.MemcachedClient) {			
			//does not support noreply
		}
		
	}
	
	public void testDoubleSet1() throws TimeoutException {
		String readObject = null;
		String key = null;
		String value = null;
		
		//1
		key = "testdset1";
        value = "v1";
		c.set(key, 3600, value);
		readObject = (String) c.get(key);

		assertNotNull(readObject);
		assertEquals("v1",  readObject);
		
		value = "v2";
		c.set(key, 3600, value);
		readObject = (String) c.get(key);

		assertNotNull(readObject);
		assertEquals("v2",  readObject);

		//2
		key = "testdset2";
        Map valuemap = new HashMap();
		valuemap.put("key1", "v1");
		
		c.set(key, 3600, valuemap);
		Map readObjectMap = (Map) c.get(key);

		assertNotNull(readObjectMap);
		assertEquals(valuemap,  readObjectMap);
		
		valuemap.put("key2", "v2");
		c.set(key, 3600, valuemap);
		readObjectMap = (Map) c.get(key);

		assertNotNull(readObjectMap);
		assertEquals(valuemap,  readObjectMap);
		
		//3
		valuemap.put("key2", "v3");
		Object client = c.getBaseClient();
		if(client instanceof net.rubyeye.xmemcached.MemcachedClient) {
			net.rubyeye.xmemcached.MemcachedClient xmc = (net.rubyeye.xmemcached.MemcachedClient) client;
			try {
				xmc.setWithNoReply(key, 3600, valuemap);
			} catch (InterruptedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MemcachedException ex) {
				Logger.getLogger(ProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			readObjectMap = (Map) c.get(key);

			assertNotNull(readObjectMap);
			assertEquals(valuemap,  readObjectMap);
		} else if (client instanceof net.spy.memcached.MemcachedClient) {			
			//does not support noreply
		}
		
	}

}
