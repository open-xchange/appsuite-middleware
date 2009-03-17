package com.openexchange.groupware;

import java.sql.Connection;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;

import com.openexchange.event.impl.EventConfigImpl;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.test.AjaxInit;
import com.openexchange.tools.iterator.SearchIterator;


public class AppointmentDeleteNoCommit extends TestCase {
     
    int cols[] = new int[] { AppointmentObject.START_DATE, AppointmentObject.END_DATE, AppointmentObject.TITLE, AppointmentObject.RECURRENCE_ID, AppointmentObject.RECURRENCE_POSITION, AppointmentObject.OBJECT_ID, AppointmentObject.FOLDER_ID, AppointmentObject.USERS, AppointmentObject.FULL_TIME };
    public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
    public static final String TIMEZONE = "Europe/Berlin";
 // Override these in setup
    private static int userid = 11; // bishoph
    public final static int contextid = 1;
    
    private static boolean init = false;
    
    @Override
	protected void setUp() throws Exception {        
        super.setUp();
        final EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);
        this.userid = getUserId();
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
        final String user = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        return resolveUser(user);
    }
    
    public static Context getContext() {
        return new ContextImpl(contextid);
    }
    
    void deleteAllAppointments() throws Exception  {
        final Connection readcon = DBPool.pickup(getContext());
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "deleteAllApps");
        final CalendarSql csql = new CalendarSql(so);        
        final SearchIterator<CalendarDataObject> si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0,  null);
        while (si.hasNext()) {
            final CalendarDataObject cdao = si.next();
            testDelete(cdao);
        }
        si.close();
        DBPool.push(context, readcon);                
    }    
    
    private void testDelete(final CalendarDataObject cdao) throws Exception {        
        final Connection writecon = DBPool.pickupWriteable(getContext());
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "delete test");
        final CalendarSql csql = new CalendarSql(so);
        final CalendarDataObject deleteit = new CalendarDataObject();
        deleteit.setContext(cdao.getContext());
        deleteit.setObjectID(cdao.getObjectID());
        final int fid = cdao.getEffectiveFolderId();
        try {
            if (fid == 0) {
                final int x = 0;
            }
            csql.deleteAppointmentObject(deleteit, fid, new Date(SUPER_END));
        } catch(final Exception e) { 
            e.printStackTrace();
        }
        DBPool.pushWrite(context, writecon);
    }
    
    
    /* ------------------------------------- */
    
    public static int getPrivateFolder(final int userid) throws Exception {
        int privatefolder = 0;
        final Context context = getContext();
        final Connection readcon = DBPool.pickup(context);
        privatefolder = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        DBPool.push(context, readcon);
        return privatefolder;        
    }
    
    /*
     when i open a multi participant appt and add one resource to the appt, the
     following error is thrown:
    */
    public void testDeleteAll() throws Exception {
        
        // Clean up appointments
        deleteAllAppointments();
        
    }
    
}
