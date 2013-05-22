package org.quickcached;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quickserver.net.server.QuickServer;

/**
 *
 * @author Akshathkumar Shetty
 */
public class StatsReportGenerator {
	private static final Logger logger = Logger.getLogger(StatsReportGenerator.class.getName());
	
	private static final SimpleDateFormat sdfFile = new SimpleDateFormat("dd");
	
	private static int writeInterval = 1000*60;//1m
	
	private static List entriesToLog = new ArrayList();
	
	static {
		entriesToLog.add("datetime");
		entriesToLog.add("curr_connections");
		entriesToLog.add("total_connections");
	}

	public static List getEntriesToLog() {
		return entriesToLog;
	}
	
	public static void start(final QuickServer quickserver) {
		Thread t = new Thread() {
			public void run() {
				logger.info("Started..");
				Map stats = new LinkedHashMap(25);
				while(true) {
					try {
						sleep(getWriteInterval());
					} catch (InterruptedException ex) {
						Logger.getLogger(StatsReportGenerator.class.getName()).log(
								Level.WARNING, "Error", ex);
						break;
					}
					CommandHandler.getStats(quickserver, stats);
					writeReport(quickserver.getPort(), stats);
					stats.clear();
				}
				logger.info("Done");
			}
		};
		t.setName("StatsReportGenerator-Thread");
		t.setDaemon(true);
		t.start();
	}

	public static void setEntriesToLog(List aEntriesToLog) {
		entriesToLog = aEntriesToLog;
	}
	
	public static void writeReport(int port, Map stats) {
		if(stats==null || stats.isEmpty()) return;
		
		BufferedWriter out = null;
		try {
			File reportDir = new File("./stats/"+port+"/");
			if(!reportDir.canRead())
				reportDir.mkdirs();
			//day files  - so max of 31 files only.. should be good for analysis.. saves disk space
			File reportFile = new File("./stats/"+port+"/"+sdfFile.format(new Date())+".csv");
			
			boolean append = true;
			if((reportFile.lastModified()+1000*60*60*24*5) < System.currentTimeMillis()) {
				append = false;
			}
			
			boolean writeheader = false;
			if(reportFile.canRead()==false) {
				writeheader = true;
			}
            out = new BufferedWriter(new FileWriter(reportFile, append));					
			
			Iterator iterator = entriesToLog.iterator();
			
			String key = null;
			String value = null;
			
			if(writeheader) {
				while(iterator.hasNext()) {
					key = (String) iterator.next();	
					out.write(key, 0, key.length());
					
					if(iterator.hasNext()) {
						out.write(", ", 0, 2);
					}
				}
				
				out.write("\r\n", 0, 2);
				writeheader = false;
			}
			
			iterator = entriesToLog.iterator();
			while(iterator.hasNext()) {
				key = (String) iterator.next();		
				value = (String) stats.get(key);

				out.write(value, 0, value.length());
				if(iterator.hasNext()) {
					out.write(", ", 0, 2);
				}				
			}
			out.write("\r\n", 0, 2);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error writing to report file : " + e, e);
		} finally {
			if(out!=null) {
				try {
					out.flush();
					out.close();
				} catch (Exception ee) {
					logger.warning("Error closing Report file : " + ee);
				}
			}
		}				
	}

	public static int getWriteInterval() {
		return writeInterval;
	}

	public static void setWriteInterval(int aWriteInterval) {
		writeInterval = aWriteInterval;
	}
	
}
