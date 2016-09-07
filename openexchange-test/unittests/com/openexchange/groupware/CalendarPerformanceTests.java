/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.groupware;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.event.impl.EventConfigImpl;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.test.AjaxInit;
import junit.framework.TestCase;

/**
 *
 * @author bishoph
 */
public class CalendarPerformanceTests extends TestCase {

    public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
    public static final String TIMEZONE = "Europe/Berlin";

    // Override these in setup
    private static int userid = 11; // bishoph
    public static int contextid = 1;

    private static boolean init = false;

    private static final String testfile = "/home/bishoph/tmp/supertemp/sqloutput"; // "/home/bishoph/tmp/supertemp/sql_dump_dev_prototype"


    @Override
	protected void setUp() throws Exception {
        super.setUp();
        Init.startServer();
        init = true;
        //com.openexchange.groupware.Init.initContext();
        final EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);
        final TestConfig config = new TestConfig();
        contextid = ContextStorage.getInstance().getContextId(config.getContextName());
        userid = getUserId();
        ContextStorage.start();
    }

    @Override
	protected void tearDown() throws Exception {
        if (init) {
            init = false;
            Init.stopServer();
        }
        super.tearDown();
    }

    private static Properties getAJAXProperties() {
        final Properties properties = AjaxInit.getAJAXProperties();
        return properties;
    }

    private static int resolveUser(final String u) throws Exception {
        final UserStorage uStorage = UserStorage.getInstance();
        return uStorage.getUserId(u, getContext());
    }

    public static int getUserId() throws Exception {
        if (!init) {
            Init.startServer();
            init = true;
        }
        final String user = getAJAXProperties().getProperty("user_participant2", "");
        return resolveUser(user);
    }

    public static Context getContext() {
        return new ContextImpl(contextid);
    }

    public static int getPrivateFolder() throws Exception {
        int privatefolder = 0;
        final Context context = getContext();
        privatefolder = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        return privatefolder;
    }


    public void testSimple() throws Throwable {
    	final File f = new File(testfile);
    	calc(f, 0);
    }

    public void testThreaded() throws Throwable {
    	final Runner[] runner = new Runner[100];
    	final Thread[] thread = new Thread[runner.length];
        for (int a = 0; a < runner.length; a++) {
            runner[a] = new Runner(a);
            thread[a] = new Thread(runner[a]);
            thread[a].start();
        }
        for (int b = 0; b < thread.length; b++) {
            thread[b].join();
        }
    }

    public static void calc(final File f, final int number) throws Throwable {
    	final Reader r = new BufferedReader(new FileReader(f));
    	final LineNumberReader lnr = new LineNumberReader(r);
    	String line;
    	long all = 0L;
    	long high = 0L;
//    	final long all_start = System.currentTimeMillis();
    	final HashMap<String, Integer> count_cid = new HashMap<String, Integer>();
    	while ((line = lnr.readLine()) != null){
    		final StringTokenizer st = new StringTokenizer(line);
    		if (st.countTokens() == 8) {
    			final String startdate = st.nextToken();
    			final String starttime = st.nextToken();
    			final String enddate = st.nextToken();
    			final String endtime = st.nextToken();
    			final String cid = st.nextToken();
    			if (count_cid.containsKey(cid)) {
    				int cid_count = count_cid.get(cid).intValue();
    				cid_count++;
    				count_cid.put(cid, Integer.valueOf(cid_count));
    			} else {
    				count_cid.put(cid, Integer.valueOf(1));
    			}
    			final String recstring = st.nextToken();
    			final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    			final Date sdate = dateformat.parse(startdate + " "+starttime);
    			final Date edate = dateformat.parse(enddate + " "+endtime);
    			final long start = System.currentTimeMillis();
    	        final CalendarDataObject cdao = new CalendarDataObject();
    	        cdao.setTimezone(TIMEZONE);
    	        cdao.setTitle("Daily Appointment Test");
    	        cdao.setRecurrence(recstring);
    	        cdao.setStartDate(sdate);
    	        cdao.setEndDate(edate);
    	        new CalendarCollection().fillDAO(cdao);
    	        new CalendarCollection().calculateRecurring(cdao, 0L, 5709135600000L, 0);
    	        final long end = System.currentTimeMillis();
    	        final long duration = end-start;
    	        all = all + duration;
    	        if (duration > high) {
    	        	high = duration;
    	        }
    		}
    	}
//    	final long all_end = System.currentTimeMillis();
    	final Iterator<String> it = count_cid.keySet().iterator();
    	int hit = 0;
    	while (it.hasNext()) {
    		final String key = it.next();
    		final int check = count_cid.get(key).intValue();
    		if (check > hit) {
    			hit = check;
    		}
    	}
    }


    private class Runner implements Runnable  {

    	int current;

    	public Runner(final int current) {
    		this.current = current;
    	}

        boolean run = true;
        @Override
        public void run() {
            while (run) {
            	final File f = new File(testfile);
            	try {
            		calc(f, current);
            	} catch(final Throwable t) {
            		t.printStackTrace();
            	}
            }
        }
    }


}
