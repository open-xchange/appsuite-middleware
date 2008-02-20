/*
 * CalendarPerformanceTests.java
 *
 * Created on 12. September 2006, 16:50
 *
 */

package com.openexchange.groupware;

import com.openexchange.event.impl.EventConfigImpl;
import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.test.AjaxInit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import junit.framework.TestCase;

/**
 *
 * @author bishoph
 */
public class CalendarPerformanceTests extends TestCase { 
    
    public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
    public static final String TIMEZONE = "Europe/Berlin";
    private static int userid = 11; // bishoph
    public final static int contextid = 1;
    
    private boolean debug = true;
    
    private static boolean init = false;
    
    private static final String testfile = "/home/bishoph/tmp/supertemp/sqloutput"; // "/home/bishoph/tmp/supertemp/sql_dump_dev_prototype"
    
    
    protected void setUp() throws Exception {
        super.setUp();
        //com.openexchange.groupware.Init.initContext();
        EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);
        this.userid = getUserId();
        ContextStorage.init();
    }
    
    protected void tearDown() throws Exception {
        if (init) {
            init = false;
            Init.stopServer();
        }
        super.tearDown();
    }
    
    private static Properties getAJAXProperties() {
        Properties properties = AjaxInit.getAJAXProperties();
        return properties;
    }
    
    private static int resolveUser(String u) throws Exception {
        UserStorage uStorage = UserStorage.getInstance();
        return uStorage.getUserId(u, getContext());
    }
    
    public static int getUserId() throws Exception {
        if (!init) {
            Init.startServer();
            init = true;
        }
        String user = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant2", "");
        return resolveUser(user);
    }
    
    public static Context getContext() {
        return new ContextImpl(contextid);
    }
    
    public static int getPrivateFolder() throws Exception {
        int privatefolder = 0;
        Context context = getContext();
        privatefolder = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        return privatefolder;
    }
    
    
    public void testSimple() throws Throwable {
    	File f = new File(testfile);
    	calc(f, 0);
    }
    
    public void testThreaded() throws Throwable {
    	Runner[] runner = new Runner[100];
    	Thread[] thread = new Thread[runner.length];
        for (int a = 0; a < runner.length; a++) {
            runner[a] = new Runner(a);
            thread[a] = new Thread(runner[a]);
            thread[a].start();
        }
        for (int b = 0; b < thread.length; b++) {
            thread[b].join();
        }        
    }
    
    public void calc(File f, int number) throws Throwable {    	
    	Reader r = new BufferedReader(new FileReader(f));
    	LineNumberReader lnr = new LineNumberReader(r);
    	String line; 
    	long all = 0L;
    	int counter = 0;
    	long high = 0L;
    	String shigh = "";
    	long all_start = System.currentTimeMillis();
    	System.out.println("Start");
    	HashMap<String, Integer> count_cid = new HashMap<String, Integer>();
    	while ((line = lnr.readLine()) != null){
    		StringTokenizer st = new StringTokenizer(line);
    		if (st.countTokens() == 8) {
    			String startdate = st.nextToken();
    			String starttime = st.nextToken();
    			String enddate = st.nextToken();
    			String endtime = st.nextToken();
    			String cid = st.nextToken();
    			if (count_cid.containsKey(cid)) {
    				int cid_count = count_cid.get(cid);
    				cid_count++;
    				count_cid.put(cid, cid_count);
    			} else {
    				count_cid.put(cid, 1);
    			}
    			String recstring = st.nextToken();
    			DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    			Date sdate = dateformat.parse(startdate + " "+starttime);
    			Date edate = dateformat.parse(enddate + " "+endtime);
    			if (debug)
    				System.out.print("Processing (" + number + ")"+recstring);
    			long start = System.currentTimeMillis();
    	        CalendarDataObject cdao = new CalendarDataObject();
    	        cdao.setTimezone(TIMEZONE);
    	        cdao.setTitle("Daily Appointment Test");
    	        cdao.setRecurrence(recstring);
    	        cdao.setStartDate(sdate);
    	        cdao.setEndDate(edate);
    	        CalendarRecurringCollection.fillDAO(cdao);
    	        RecurringResults rss = CalendarRecurringCollection.calculateRecurring(cdao, 0L, 5709135600000L, 0);    	        
    	        long end = System.currentTimeMillis();
    	        long duration = end-start;
    	        all = all + duration;
    	        counter ++;
    	        if (duration > high) {
    	        	high = duration;
    	        	shigh = recstring;
    	        }
    	        if (debug)
    	        	System.out.println(" "+duration + " ms");
    	        
    		}
    	}
    	long all_end = System.currentTimeMillis();
    	long total = all_end - all_start;
    	System.out.println("Highest :" +high + " "+shigh);
    	System.out.println("Avg :" + (all/counter));
    	System.out.println("Time total :" + total);
    	System.out.println("CID total :" + count_cid.size());
    	Iterator it = count_cid.keySet().iterator();
    	int hit = 0;
    	while(it.hasNext()) {
    		String key = (String)it.next();
    		int check = count_cid.get(key);
    		if (check > hit) {
    			hit = check;
    		}
    	}
    	System.out.println("CID highest :" + hit);
    }    
    
    
    private class Runner implements Runnable  {
    	
    	int current; 
    	
    	public Runner(int current) {
    		this.current = current;
    	}
    	
        boolean run = true;
        public void run() {
            while (run) {
            	File f = new File(testfile);
            	try {
            		calc(f, current);
            	} catch(Throwable t) {
            		t.printStackTrace();
            	}
            }  
        }
    }    
    
    
}
