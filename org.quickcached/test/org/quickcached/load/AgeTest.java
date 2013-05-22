package org.quickcached.load;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import java.util.logging.*;
import org.quickcached.client.*;

/**
 *
 * @author akshath
 */
public class AgeTest {	
	private int eachUnitCount;
	private int runTimeSec;
			
	private String name;
	private String hostList;
	private int timeouts;
	
	private static MemcachedClient c = null;
	private static int objectSize = 1024;//1kb

	//thread list txn/sec runtimesec
	//5 127.0.0.1:11211 10 60
	public static void main(String args[]) {		
		int threads = 5;
		String host = "127.0.0.1:11211";
		int txnPerSec = 10;
		int runTimeSec = 60;//1m

		if(args.length==4) {
			threads = Integer.parseInt(args[0]);
			host = args[1];
			txnPerSec = Integer.parseInt(args[2]);
			runTimeSec = Integer.parseInt(args[3]);
		}
		host = host.replaceAll(",", " ");
		doMultiThreadTest(host, txnPerSec, threads, runTimeSec);
    }

	public static void doMultiThreadTest(String host, int txnPerSec, int threads, int runTimeSec) {
		int eachUnitCount = (int) (txnPerSec/threads + 0.5);
		

		final AgeTest at[] = new AgeTest[threads];
		for(int i=0;i<threads;i++) {
			at[i] = new AgeTest("Test-"+i, host, eachUnitCount, runTimeSec);
			at[i].setUp();
		}

		Thread threadPool[] = new Thread[threads];
		for(int i=0;i<threads;i++) {
			final int myi = i;
			threadPool[i] = new Thread() {
				public void run() {
					at[myi].test();
				}
			};
		}

		System.out.println("=============");
		System.out.println("Txn/Sec/Thread: "+eachUnitCount);
		System.out.println("Total Threads: "+threads);
		System.out.println("Host List: "+host);
		System.out.println("ETC: "+new Date(System.currentTimeMillis()+runTimeSec*1000));
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
				Logger.getLogger(AgeTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			timeoutCount = timeoutCount + at[i].timeouts;
		}
		long etime = System.currentTimeMillis();

		float ttime = etime-stime;
		double atime = ttime/(eachUnitCount*threads*runTimeSec);

		System.out.println("\nDone....");
		System.out.println("=============");

		System.out.println("=============");
		System.out.println("Host List: "+host);
		System.out.println("Total Txn: "+eachUnitCount*threads*runTimeSec);
		System.out.println("Total Threads: "+threads);
		System.out.println("Txn/Sec/Thread: "+eachUnitCount);
		System.out.println("Total Time: "+ttime+ " ms");
		System.out.println("Timeouts: "+timeoutCount);
		System.out.println("Avg Time: "+atime+ " ms");
		System.out.println("=============");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			Logger.getLogger(AgeTest.class.getName()).log(Level.SEVERE, null, ex);
		}

		for(int i=0;i<threads;i++) {
			at[i].tearDown();
		}
	}
	
	public AgeTest(String name, String host, int eachUnitCount, int runTimeSec) {
		this.name = name;
		this.hostList = host;
		this.eachUnitCount = eachUnitCount;
		this.runTimeSec = runTimeSec;
    }

	public void setUp(){
		if(c==null) {
			try {
				c = MemcachedClient.getInstance(MemcachedClient.XMemcachedImpl);
				//c.setUseBinaryConnection(true);
				c.setAddresses(hostList);
				c.setDefaultTimeoutMiliSec(3000);//3 sec
				c.init();
			} catch (Exception ex) {
				Logger.getLogger(AgeTest.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public void tearDown(){
		if(c!=null) {
			try {
				c.stop();
			} catch (IOException ex) {
				Logger.getLogger(AgeTest.class.getName()).log(Level.SEVERE, null, ex);
			}
			c = null;
		}
	}
	
	public void test() {
		long stime = System.currentTimeMillis();
		long etime = System.currentTimeMillis();
		
		long timeStartTest = 0;
		long timePerTest = 0;
		long timeToSleep = 0;
		
		System.out.print("*");
		Random r = new Random();
		String seed = null;
		StringBuilder sb = new StringBuilder();
		while(true) {
			timeStartTest = System.currentTimeMillis();			
			
			char c = '0';
			for(int k=0;k<20;k++) {
				c = (char)(r.nextInt(26) + 'a');
				sb.append(c);
			}
			seed = sb.toString();
			sb.setLength(0);
			
			testSec(seed);
			
			timePerTest = System.currentTimeMillis() - timeStartTest;
			timeToSleep = 1000 - timePerTest;
			if(timeToSleep>0) {
				try {
					Thread.sleep(timeToSleep);
				} catch (InterruptedException ex) {
					Logger.getLogger(AgeTest.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			
			etime = System.currentTimeMillis();
			if(runTimeSec < (etime-stime)/1000) {
				break;
			}
		}
		System.out.print("#");
	}

	public void testSec(String seed) {
		for(int i=0;i<eachUnitCount;i++) {
			doSet(seed, i);
		}
		for(int i=0;i<eachUnitCount;i++) {
			doGet(seed, i);
		}
		for(int i=0;i<eachUnitCount;i++) {
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
		} catch (TimeoutException ex) {
			Logger.getLogger(AgeTest.class.getName()).log(Level.SEVERE, null, ex);
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
			System.out.println("Timeout: "+e+" for "+key);
		} 
	}

	public void doDelete(String seed, int i) {
		String key = name+"-"+i+"-"+seed;
		try {
			c.delete(key);
		} catch (TimeoutException ex) {
			Logger.getLogger(AgeTest.class.getName()).log(Level.SEVERE, null, ex);
		} 
	}
}
