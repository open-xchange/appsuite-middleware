package org.quickcached.protocol;

import java.io.IOException;
import java.util.Random;
import java.util.logging.*;
import org.quickcached.client.*;

/**
 *
 * @author akshath
 */
public class LoadMemTest {
	private static MemcachedClient c = null;
	
	private int count;
	private String name;
	private String hostList;
	private int timeouts;
	
	private static int objectSize = 1024;//1kb
	private String seed = null;

	public static void main(String args[]) {
		String mode = "m";
		int threads = 10;
		String host = "127.0.0.1:11211";
		int txn = 100000;

		if(args.length==4) {
			mode = args[0];
			threads = Integer.parseInt(args[1]);
			host = args[2];
			txn = Integer.parseInt(args[3]);
		}
		if(mode.equals("s")) {
			doSingleThreadTest(host, txn);
		} else {
			doMultiThreadTest(host, txn, threads);
		}
    }

	public static void doSingleThreadTest(String host, int txn) {
		LoadMemTest ltu = new LoadMemTest("Test1", host, txn);
		ltu.setUp();
		long stime = System.currentTimeMillis();
        ltu.test1();
		long etime = System.currentTimeMillis();

		float ttime = etime-stime;
		double atime = ttime/txn;
		ltu.tearDown();
	}

	public static void doMultiThreadTest(String host, int txn, int threads) {
		int eachUnitCount = txn/threads;

		final LoadMemTest ltu[] = new LoadMemTest[threads];
		for(int i=0;i<threads;i++) {
			ltu[i] = new LoadMemTest("Test-"+i, host, eachUnitCount);
			ltu[i].setUp();
		}

		Thread threadPool[] = new Thread[threads];
		for(int i=0;i<threads;i++) {
			final int myi = i;
			threadPool[i] = new Thread() {
				public void run() {
					ltu[myi].test1();
				}
			};
		}

		System.out.println("Starting....");
		System.out.println("=============");
		long stime = System.currentTimeMillis();
		for(int i=0;i<threads;i++) {
			threadPool[i].start();
		}

		long timeoutCount = 0;
		for(int i=0;i<threads;i++) {
			try {
				threadPool[i].join();
			} catch (InterruptedException ex) {
				Logger.getLogger(LoadMemTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			timeoutCount = timeoutCount + ltu[i].timeouts;
		}
		long etime = System.currentTimeMillis();

		float ttime = etime-stime;
		double atime = ttime/txn;

		System.out.println("Done....");
		System.out.println("=============");

		System.out.println("=============");
		System.out.println("Host List: "+host);
		System.out.println("Total Txn: "+txn);
		System.out.println("Total Threads: "+threads);
		System.out.println("Total Time: "+ttime+ " ms");
		System.out.println("Timeouts: "+timeoutCount);
		System.out.println("Avg Time: "+atime+ " ms");
		System.out.println("=============");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			Logger.getLogger(LoadMemTest.class.getName()).log(Level.SEVERE, null, ex);
		}

		for(int i=0;i<threads;i++) {
			ltu[i].tearDown();
		}
	}

	public LoadMemTest(String name, String host, int count) {
        this.count = count;
		this.name = name;
		this.hostList = host;
		
		StringBuilder sb = new StringBuilder();
		Random r = new Random();
		char c = '0';
		for(int k=0;k<objectSize;k++) {
			c = (char)(r.nextInt(26) + 'a');
			sb.append(c);
		}
		seed = sb.toString();	
    }

	public void setUp(){
		if(c==null) {
			try {
				c = MemcachedClient.getInstance(MemcachedClient.XMemcachedImpl);
				c.setUseBinaryConnection(true);
				c.setAddresses(hostList);
				c.setDefaultTimeoutMiliSec(10000);//10 sec
				c.init();
			} catch (Exception ex) {
				Logger.getLogger(TextProtocolTest.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public void tearDown(){
		if(c!=null) {
			try {
				c.stop();
			} catch (IOException ex) {
				Logger.getLogger(LoadMemTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			c = null;
		}
	}

	public void test1() {
		for(int i=0;i<count;i++) {
			doSet(i);
			
			System.out.print(".");
		}
		
		for(int i=0;i<count;i++) {
			doGet(i);
		}
	}

    public void doSet(int i) {
		String key = name+"-"+i;
		String value = name+"-"+(i*2)+"-"+seed;
		try {
			c.set(key, 3600, value);
		} catch (TimeoutException ex) {
			Logger.getLogger(LoadMemTest.class.getName()).log(Level.SEVERE, "Timeout: "+ex, ex);
		}
	}

	public void doGet(int i) {
		String key = name+"-"+i;
		try {
			Object readObject = null;
			try {
				readObject = (String) c.get(key);
			} catch (TimeoutException ex) {
				Logger.getLogger(LoadMemTest.class.getName()).log(Level.SEVERE, "Timeout: "+ex, ex);
			}
			if(readObject==null) {
				System.out.println("get was null! for "+key);
			}
		} catch(net.spy.memcached.OperationTimeoutException e) {
			timeouts++;
			System.out.println("Timeout: "+e+" for "+key);
		}
	}

	public void doDelete(int i) {
		String key = name+"-"+i;
		try {
			c.delete(key);
		} catch (TimeoutException ex) {
			Logger.getLogger(LoadMemTest.class.getName()).log(Level.SEVERE, "Timeout: "+ex, ex);
		}
	}
}
