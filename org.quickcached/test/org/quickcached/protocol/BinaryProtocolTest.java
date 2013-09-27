package org.quickcached.protocol;

import java.io.IOException;
import java.util.logging.*;
import org.quickcached.client.MemcachedClient;
import java.util.Date;
import org.quickcached.client.TimeoutException;

/**
 *
 * @author akshath
 */
public class BinaryProtocolTest extends ProtocolTest {
	public BinaryProtocolTest(String name) {
        super(name);
    }

	public void setUp(){
		try {
			c = MemcachedClient.getInstance();
			c.setUseBinaryConnection(true);
			c.setAddresses("localhost:11211");
			c.setDefaultTimeoutMiliSec(3000);//3 sec
			c.setConnectionPoolSize(1);
			c.init();
		} catch (Exception ex) {
			Logger.getLogger(BinaryProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void tearDown(){
		if(c!=null) {
			try {
				c.stop();
			} catch (IOException ex) {
				Logger.getLogger(BinaryProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

    public static void main(String args[]) {
        junit.textui.TestRunner.run(BinaryProtocolTest.class);
    }
	
	public void testTouch() throws TimeoutException {
		String readObject = null;
		String key = null;
		String value = null;
		
		//1
		key = "testtuc1";
        Date datevalue = new Date();

		c.set(key, 50, datevalue);
		Date readObjectDate = (Date) c.get(key);

		assertNotNull(readObjectDate);
		assertEquals(datevalue.getTime(),  readObjectDate.getTime());
		
		c.touch(key, 3600);
		
		readObjectDate = (Date) c.get(key);

		assertNotNull(readObjectDate);
		assertEquals(datevalue.getTime(),  readObjectDate.getTime());
	
		//2
		key = "testtuc2";
		c.set(key, 50, "World");
		readObject = (String) c.get(key);

		assertNotNull(readObject);
		assertEquals("World",  readObject);
		
		c.touch(key, 3600);
		
		readObject = (String) c.get(key);

		assertNotNull(readObject);
		assertEquals("World",  readObject);
	}
	
	public void testGat() throws TimeoutException {		
		String readObject = null;
		String key = null;
		String value = null;
		
		//1
		key = "testgat1";
		c.set(key, 50, "World");
		readObject = (String) c.gat(key, 3600);

		assertNotNull(readObject);
		assertEquals("World",  readObject);	
	
		//2
		key = "testgat2";
		Date datevalue = new Date();
		c.set(key, 50, datevalue);
		Date readObjectDate = (Date) c.gat(key, 3600);

		assertNotNull(readObjectDate);
		assertEquals(datevalue.getTime(),  readObjectDate.getTime());
	}
}
