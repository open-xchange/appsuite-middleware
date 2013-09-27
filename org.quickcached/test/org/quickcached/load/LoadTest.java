package org.quickcached.load;

import java.io.IOException;
import java.util.Random;

import java.util.logging.*;
import org.quickcached.client.*;

/**
 *
 * @author Akshathkumar Shetty
 */
public class LoadTest {
	
	private int count;
	private String name;
	private String hostList;
	private int timeouts;
	
	private static MemcachedClient c = null;
	private static int objectSize = 1024;//1kb
	private static boolean useBinaryConnection = true;

	public static void main(String args[]) {
		String mode = "s";
		int threads = 10;
		String host = "192.168.1.10:11211";
		int txn = 10;

		if(args.length==4) {
			mode = args[0];
			threads = Integer.parseInt(args[1]);
			host = args[2];
			txn = Integer.parseInt(args[3]);
		}
		
		host = host.replaceAll(",", " ");
		
		if(mode.equals("s")) {
			doSingleThreadTest(host, txn);
		} else {
			doMultiThreadTest(host, txn, threads);
		}
    }

	public static void doSingleThreadTest(String host, int txn) {
		LoadTest ltu = new LoadTest("Test1", host, txn);
		ltu.setUp();
		long stime = System.currentTimeMillis();
        ltu.test1();
		long etime = System.currentTimeMillis();

		float ttime = etime-stime;
		double atime = ttime/txn;
		ltu.tearDown();
		System.out.println("Total Time for "+txn+" txn was "+ttime);
		System.out.println("Avg Time for "+txn+" txn was "+atime);
	}

	public static void doMultiThreadTest(String host, int txn, int threads) {
		int eachUnitCount = txn/threads;

		final LoadTest ltu[] = new LoadTest[threads];
		for(int i=0;i<threads;i++) {
			ltu[i] = new LoadTest("Test-"+i, host, eachUnitCount);
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
				Logger.getLogger(LoadTest.class.getName()).log(Level.SEVERE, null, ex);
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
			Thread.sleep(5000);
		} catch (InterruptedException ex) {
			Logger.getLogger(LoadTest.class.getName()).log(Level.SEVERE, null, ex);
		}

		for(int i=0;i<threads;i++) {
			ltu[i].tearDown();
		}
	}
	
	public LoadTest(String name, String host, int count) {
        this.count = count;
		this.name = name;
		this.hostList = host;
    }

	public void setUp(){
		if(c==null) {
			try {
				c = MemcachedClient.getInstance();
				//c.setUseBinaryConnection(useBinaryConnection);
				c.setAddresses(hostList);
				c.setDefaultTimeoutMiliSec(3000);//3 sec
				c.init();
			} catch (Exception ex) {
				Logger.getLogger(LoadTest.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public void tearDown(){
		if(c!=null) {
			try {
				c.stop();
			} catch (IOException ex) {
				Logger.getLogger(LoadTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			c = null;
		}
	}

	public void test1() {
		StringBuilder sb = new StringBuilder();
		Random r = new Random();
		char c = '0';
		for(int k=0;k<20;k++) {
			c = (char)(r.nextInt(26) + 'a');
			sb.append(c);
		}
		String seed = sb.toString();		
			
		for(int i=0;i<count;i++) {
			doSet(seed, i);
		}
		for(int i=0;i<count;i++) {
			doGet(seed, i);
		}
		for(int i=0;i<count;i++) {
			doDelete(seed, i);
		}
	}

	private String largeData = null;
    public void doSet(String seed, int i) {
		String key = name+"-"+i+"-"+seed;
		if(largeData==null) {
			StringBuilder sb = new StringBuilder();
			Random r = new Random();
			char c = '0';
			for(int k=0;k<objectSize;k++) {
				c = (char)(r.nextInt(26) + 'a');
				sb.append(c);
			}
			largeData = sb.toString();
		}
		String value = name+"-"+(i*2)+"-"+largeData;
		try {
			c.set(key, 3600, value);
		} catch (TimeoutException e) {
			System.out.println("Timeout(set): "+e+" for "+key);
			timeouts++;
		} 
	}

	public void doGet(String seed, int i) {
		String key = name+"-"+i+"-"+seed;
		try {
			Object readObject = (String) c.get(key);
			if(readObject==null) {
				System.out.println("get was null! for "+key);
				timeouts++;
			}
		} catch(TimeoutException e) {
			timeouts++;
			System.out.println("Timeout(get): "+e+" for "+key);
		} 
	}

	public void doDelete(String seed, int i) {
		String key = name+"-"+i+"-"+seed;
		try {
			c.delete(key);
		} catch (TimeoutException e) {
			timeouts++;
			System.out.println("Timeout(del): "+e+" for "+key);
		} 
	}
}
