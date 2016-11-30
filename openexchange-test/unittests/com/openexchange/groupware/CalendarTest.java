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

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import com.openexchange.api2.ReminderService;
import com.openexchange.calendar.CalendarMySQL;
import com.openexchange.calendar.CalendarOperation;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.calendar.CalendarSqlImp;
import com.openexchange.calendar.ConflictHandler;
import com.openexchange.calendar.api.AppointmentSqlFactory;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.event.impl.EventConfigImpl;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.test.AjaxInit;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import junit.framework.TestCase;

public class CalendarTest extends TestCase {


    private final static int TEST_PASS = 1;
    private final static int TEST_PASS_HOT_SPOT = 1;
    //public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)

    // Override these in setup
    private static int userid = 11; // bishoph
    public static int contextid = 1;

    private static boolean init = false;

    private static boolean do_not_delete = false;

    static int cols[] = new int[] { Appointment.START_DATE, Appointment.END_DATE, Appointment.TITLE, Appointment.RECURRENCE_ID, Appointment.RECURRENCE_POSITION, Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.USERS, Appointment.FULL_TIME };

    protected static Date decrementDate(final Date d) {
        return new Date(d.getTime() - 1);
    }

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        Init.startServer();
        init = true;
        final EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);

        final TestConfig config = new TestConfig();
        final String userName = config.getUser();
        final TestContextToolkit tools = new TestContextToolkit();
        final String ctxName = config.getContextName();
        final Context ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);
        final int user = tools.resolveUser(userName, ctx);

        contextid = ctx.getContextId();
        userid = user;
        ContextStorage.start();
        
        CalendarMySQL.setApppointmentSqlFactory(new AppointmentSqlFactory());
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

    private static int resolveUser(final String user) throws Exception {
        final UserStorage uStorage = UserStorage.getInstance();
        return uStorage.getUserId(user, getContext());
    }

    public static void dontDelete() {
        do_not_delete = true;
    }

    public static void doDelete() {
        do_not_delete = false;
    }

    public static int getUserId() throws Exception {
        if (!init) {
            Init.startServer();
            init = true;
        }
        final String user = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant2", "");
        return resolveUser(user);
    }

    public static Context getContext() {
        return new ContextImpl(contextid);
    }

    public static SessionObject getSession() throws Exception {
        return SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), String.valueOf(System.currentTimeMillis()));
    }

    public static final void fillDatesInDao(final CalendarDataObject cdao) {
        long start = System.currentTimeMillis();
        long mod = start % Constants.MILLI_HOUR;
        // start should be a full hour.
        start = start - mod;
        // end is one hour after start.
        final long end = start + Constants.MILLI_HOUR;
        // until should be 10 days after start
        long until = start + (Constants.MILLI_DAY * 10);
        mod = until % Constants.MILLI_DAY;
        // and on day start
        until = until - mod;

        cdao.setStartDate(new Date(start));
        cdao.setEndDate(new Date(end));
    }

    public static int getPrivateFolder() throws Exception {
        int privatefolder = 0;
        final Context context = getContext();
        final Connection readcon = DBPool.pickup(context);
        privatefolder = getCalendarDefaultFolderForUser(userid, context);
        DBPool.push(context, readcon);
        return privatefolder;
    }

    public static int getCalendarDefaultFolderForUser(final int userid, final Context context) throws OXException {
        final OXFolderAccess access = new OXFolderAccess(context);
        final FolderObject fo = access.getDefaultFolder(userid, FolderObject.CALENDAR);
        return fo.getObjectID();
    }

    public static void testDelete(final Appointment cdao) throws Exception {
        final Connection writecon = DBPool.pickupWriteable(getContext());
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "delete test");
        final CalendarSql csql = new CalendarSql(so);
        final CalendarDataObject deleteit = new CalendarDataObject();
        deleteit.setContext(getContext());
        deleteit.setObjectID(cdao.getObjectID());
        final int fid = cdao.getParentFolderID();
        try {
            csql.deleteAppointmentObject(deleteit, fid, new Date(Long.MAX_VALUE));
        } catch(final Exception e) {
            e.printStackTrace();
        }
        DBPool.pushWrite(context, writecon);
    }

    void deleteAllAppointments() throws Exception  {
        if (do_not_delete) {
            return;
        }

        final Connection readcon = DBPool.pickup(getContext());
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "deleteAllApps");
        final CalendarSql csql = new CalendarSql(so);
        final SearchIterator<Appointment> si = csql.getAppointmentsBetween(userid, new Date(0), new Date(253402210800000L), cols, 0,  null);
        while (si.hasNext()) {
            final Appointment cdao = si.next();
            testDelete(cdao);
        }
        si.close();
        DBPool.push(context, readcon);
    }

    /* ----- test cases -------*/

    public void testWholeDay() throws Throwable { // TODO: Need connection
        final long s = 1149768000000L; // 08.06.2006 12:00 (GMT)
        final long e = 1149771600000L; // 08.06.2006 13:00 (GMT)
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(new ContextImpl(contextid));

        cdao.setObjectID(1);
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setFullTime(true);
        cdao.setTitle("Simple Whole Day Test");
        final CalendarOperation co = new CalendarOperation();
        Connection readcon = null;

        final Context context = new ContextImpl(contextid);

        readcon = DBPool.pickup(context);

        final int privatefolder = getCalendarDefaultFolderForUser(userid, context);

        assertFalse("Checking for update", co.prepareUpdateAction(cdao, cdao, userid, privatefolder, "Europe/Berlin"));
        final long realstart = 1149724800000L;
        assertEquals("Testing start time", cdao.getStartDate().getTime(), realstart);
        assertEquals("Testing end time", cdao.getEndDate().getTime(), realstart+Constants.MILLI_DAY);
        DBPool.push(context, readcon);

    }


    public void testWholeDayWithDB() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final int fid = getCalendarDefaultFolderForUser(userid, context);
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testWholeDayWithDB - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setIgnoreConflicts(true);
        cdao.setFullTime(true);
        cdao.setGlobalFolderID(fid);
        fillDatesInDao(cdao);
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        csql.getObjectById(object_id, fid);
        final long check_start = cdao.getStartDate().getTime();
        final long check_end = cdao.getEndDate().getTime();

        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setObjectID(object_id);
        update.setTitle("testWholeDayWithDB - Step 1 - Update");

        csql.updateAppointmentObject(update, fid, new Date());

        final CalendarDataObject testobject_update = csql.getObjectById(object_id, fid);
        assertTrue("Contains fulltime ", testobject_update.getFullTime());
        assertEquals("Check start time ", check_start, testobject_update.getStartDate().getTime());
        assertEquals("Check end time ", check_end, testobject_update.getEndDate().getTime());

    }

    public void testMultiSpanWholeDay() throws Throwable {
        final int wanted_length = 3;
        final Context context = new ContextImpl(contextid);
        final int fid = getCalendarDefaultFolderForUser(userid, context);
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testMultiSpanWholeDay - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setIgnoreConflicts(true);
        cdao.setFullTime(true);
        cdao.setGlobalFolderID(fid);
        fillDatesInDao(cdao);
        cdao.removeUntil();
        final long start_date_long = new CalendarCollection().normalizeLong(cdao.getStartDate().getTime());
        final long end_date_long = (start_date_long + (Constants.MILLI_DAY * wanted_length));
        final Date start_date = new Date(start_date_long);
        final Date end_date = new Date(end_date_long);
        cdao.setStartDate(start_date);
        cdao.setEndDate(end_date);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        csql.getObjectById(object_id, fid);
        final long check_start = cdao.getStartDate().getTime();
        final long check_end = cdao.getEndDate().getTime();

        final int test_length = (int)((check_end-check_start)/1000/60/60/24);

        assertEquals("Check app length ", wanted_length, test_length);


    }

    public void testfillUserParticipantsWithoutGroups() throws Exception  {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(getContext());
        final Participants participants = new Participants();
        final Participant p = new UserParticipant(userid);
        participants.add(p);

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");

        final int uid2 = resolveUser(user2);
        final Participant p2 = new UserParticipant(uid2);
        participants.add(p2);

        cdao.setParticipants(participants.getList());
        CalendarOperation.fillUserParticipants(cdao, null);

        assertEquals("Check participant size", cdao.getParticipants().length, cdao.getUsers().length);
    }

    public void cisco_testFreeBusy() throws Exception  {
        final Connection readcon = DBPool.pickup(getContext());
        final CalendarSqlImp calmysql = CalendarSql.getCalendarSqlImplementation();
        final PreparedStatement prep = calmysql.getFreeBusy(userid, getContext(), new Date(0), new Date(253402210800000L), readcon);
        calmysql.getResultSet(prep);
    }

    public void testInsertAndLabel() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final int fid = getCalendarDefaultFolderForUser(userid, context);
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testInsertAndLabel - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setIgnoreConflicts(true);
        fillDatesInDao(cdao);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setObjectID(object_id);
        update.setIgnoreConflicts(true);
        cdao.setTitle("testInsertAndLabel - Step 2 - Update (only Label)");
        update.setLabel(3);
        csql.updateAppointmentObject(update, fid, new Date());
        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertEquals("Check label", 3, testobject.getLabel());

    }

    public void testNoAlarm() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final int fid = getCalendarDefaultFolderForUser(userid, context);
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testNoAlarm - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setIgnoreConflicts(true);
        fillDatesInDao(cdao);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertTrue("Alarm should not be set", !testobject.containsAlarm());

        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setObjectID(object_id);
        update.setTitle("testNoAlarm - Step 2 - Update");

        csql.updateAppointmentObject(update, fid, cdao.getLastModified());

        final CalendarDataObject testobject2 = csql.getObjectById(object_id, fid);
        assertTrue("Alarm should not be set", !testobject2.containsAlarm());

        final CalendarDataObject update2 = testobject2.clone();
        update2.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update2.setObjectID(object_id);
        update2.setTitle("testNoAlarm - Step 3 - Update 2");

        csql.updateAppointmentObject(update2, fid, update.getLastModified());

        final CalendarDataObject testobject3 = csql.getObjectById(object_id, fid);
        assertTrue("Alarm should not be set", !testobject3.containsAlarm());

    }

    public void testInsertAndAlarm() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final int fid = getCalendarDefaultFolderForUser(userid, context);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testInsertAndAlarm - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setIgnoreConflicts(true);

        final UserParticipant up = new UserParticipant(userid);
        up.setAlarmMinutes(5);
        cdao.setUsers(new UserParticipant[] { up });


        final Participants participants = new Participants();
        final Participant p = new UserParticipant(userid);
        participants.add(p);

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");

        final Participant p2 = new UserParticipant(resolveUser(user2));
        participants.add(p2);

        cdao.setParticipants(participants.getList());

        fillDatesInDao(cdao);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int object_id = cdao.getObjectID();
        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertEquals("Check Alarm" , 5, testobject.getAlarm());

        final CalendarDataObject cdao2 = new CalendarDataObject();
        cdao2.setTitle("testInsertAndAlarm(2) - Step 1 - Insert");
        cdao2.setParentFolderID(fid);

        cdao2.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao2.setIgnoreConflicts(true);

        cdao2.setAlarm(5);

        fillDatesInDao(cdao2);

        cdao2.setParticipants(participants.getList());

        csql.insertAppointmentObject(cdao2);
        object_id = cdao2.getObjectID();
        final CalendarDataObject testobject2 = csql.getObjectById(object_id, fid);
        assertEquals("Check Alarm" , 5, testobject2.getAlarm());

        csql.deleteAppointmentObject(testobject2, fid, new Date());

        final ReminderService rsql = new ReminderHandler(context);
        assertTrue("Check if reminder has been deleted", rsql.existsReminder(object_id, userid, Types.APPOINTMENT) == false);


    }

    public void testBasicSearch() throws Exception  {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int folder_id = getPrivateFolder();

        //OXFolderAction ofa = new OXFolderAction(so);
        final CalendarSql csql = new CalendarSql(so);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, csql, readcon, writecon);
        final OCLPermission oclp = new OCLPermission();
        oclp.setEntity(userid);
        oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp.setFolderAdmin(true);
        FolderObject fo = new FolderObject();
        fo.setFolderName("MyTestFolder"+System.currentTimeMillis());
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PUBLIC);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp });
        int public_folder_id = 0;
        java.util.List<Integer> appointments2Delete = new ArrayList<Integer>(2);
        try {
            //ofa.createFolder(fo, so, true, readcon, writecon, false);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            public_folder_id = fo.getObjectID();

            {
                CalendarDataObject cdao = new CalendarDataObject();
                cdao.setContext(context);
                cdao.setTitle("test");
                cdao.setParentFolderID(folder_id);
                cdao.setIgnoreConflicts(true);

                fillDatesInDao(cdao);
                cdao.setIgnoreConflicts(true);

                csql.insertAppointmentObject(cdao);
                appointments2Delete.add(cdao.getObjectID());
            }

            AppointmentSearchObject searchObj = new AppointmentSearchObject();
            searchObj.setQueries(Collections.singleton("test"));
            searchObj.setFolderIDs(Collections.singleton(Integer.valueOf(folder_id)));
            SearchIterator<Appointment> si = csql.searchAppointments(searchObj, 0, Order.ASCENDING, cols);
            boolean gotresults = si.hasNext();
            assertTrue("Got real results by searching \"test\"", gotresults);
            while (si.hasNext()) {
                final CalendarDataObject tcdao = (CalendarDataObject)si.next();
                assertTrue("Found only results with something like *test*", tcdao.getTitle().toLowerCase().indexOf("test") != -1);
                assertTrue("Got real folder id ", tcdao.getParentFolderID() == folder_id);
            }
            si.close();
            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(context);
            cdao.setTitle("test Public Folder Search - Step 1 - Insert");
            cdao.setParentFolderID(public_folder_id);
            cdao.setIgnoreConflicts(true);

            fillDatesInDao(cdao);
            cdao.setIgnoreConflicts(true);

            csql.insertAppointmentObject(cdao);
            final int test_folder_id = cdao.getEffectiveFolderId();
            cdao.getObjectID();

            assertEquals("Test correct folder id", public_folder_id, test_folder_id);

            searchObj = new AppointmentSearchObject();
            searchObj.setQueries(Collections.singleton("*"));
            searchObj.setFolderIDs(Collections.singleton(Integer.valueOf(public_folder_id)));

            si = csql.searchAppointments(searchObj, 0, Order.ASCENDING, cols);
            gotresults = si.hasNext();
            assertTrue("Got real results by searching \"*\"", gotresults);
            while (si.hasNext()) {
                final CalendarDataObject tcdao = (CalendarDataObject)si.next();
                assertTrue("Got real folder id ", tcdao.getParentFolderID() == public_folder_id);
            }
            si.close();

            searchObj.setQueries(Collections.singleton("*.*"));
            si = csql.searchAppointments(searchObj, 0, Order.ASCENDING, cols);
            gotresults = si.hasNext();
            assertTrue("Got some results by searching \"*e*\"", !gotresults);
            si.close();
        } finally {
        	oxma.deleteFolder(new FolderObject(public_folder_id), true, System.currentTimeMillis());
        	for (Integer objectId : appointments2Delete) {
        	    CalendarDataObject cdao = new CalendarDataObject();
        	    cdao.setObjectID(objectId.intValue());
        	    cdao.setContext(context);
        	    csql.deleteAppointmentObject(cdao, folder_id, new Date(Long.MAX_VALUE), false);
            }
        }


        try {
            DBPool.push(context, readcon);
            DBPool.pushWrite(context, writecon);
        } catch(final Exception ignore) {
            ignore.printStackTrace();
        }

    }


    public void testInsertMoveAndDeleteAppointments() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final CalendarDataObject cdao = new CalendarDataObject();

        cdao.setTitle("testInsertMoveAndDeleteAppointments - Step 1 - Insert");
        cdao.setParentFolderID(getCalendarDefaultFolderForUser(userid, context));

        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));

        fillDatesInDao(cdao);
        cdao.setIgnoreConflicts(true);
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int private_folder_id = cdao.getEffectiveFolderId();

        final int object_id = cdao.getObjectID();

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, csql, readcon, writecon);
        final OCLPermission oclp = new OCLPermission();
        oclp.setEntity(userid);
        oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp.setFolderAdmin(true);
        FolderObject fo = new FolderObject();
        fo.setFolderName("MyTestFolder"+System.currentTimeMillis());
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PUBLIC);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp });
        //ofa.createFolder(fo, so, true, readcon, writecon, false);
        fo = oxma.createFolder(fo, true, System.currentTimeMillis());
        final int public_folder_id = fo.getObjectID();
        CalendarDataObject testobject = null;
        try {
            // TODO: "Move" folder to a public folder
            final CalendarDataObject update1 = new CalendarDataObject();
            update1.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            update1.setObjectID(object_id);
            update1.setParentFolderID(public_folder_id);
            update1.setTitle("testInsertMoveAndDeleteAppointments - Step 2 - Update");
            update1.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update1, private_folder_id, new Date());

            // TODO: LoadObject by ID and make some tests
            testobject = csql.getObjectById(object_id, public_folder_id);
            final UserParticipant up[] = testobject.getUsers();
            for (int a = 0; a < up.length; a++) {
                assertTrue("check that folder id IS NULL", up[a].getPersonalFolderId() == UserParticipant.NO_PFID);
            }

            assertEquals("testInsertMoveAndDeleteAppointments - Step 2 - Update", update1.getTitle());

            // TODO: Move again to private folder

            final CalendarDataObject update2 = csql.getObjectById(object_id, public_folder_id);

            update2.setTitle("testInsertMoveAndDeleteAppointments - Step 3 - Update");
            update2.setParentFolderID(private_folder_id);
            update2.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update2, public_folder_id, new Date());

            // TODO: LoadObject by ID and make some tests

            final CalendarDataObject testobject2 = csql.getObjectById(object_id, private_folder_id);

            assertEquals("Test folder id ", testobject2.getEffectiveFolderId(), private_folder_id);

            final UserParticipant up2[] = testobject2.getUsers();

            assertEquals("check length ", up2.length, 1);

            for (int a = 0; a < up2.length; a++) {
                assertEquals("check that folder id private folder ", up2[a].getPersonalFolderId(), private_folder_id);
            }

            // TODO: Move again to public folder and delete complete folder

            final CalendarDataObject update3 = csql.getObjectById(object_id, private_folder_id);

            update3.setTitle("testInsertMoveAndDeleteAppointments - Step 4 - Update");
            update3.setParentFolderID(public_folder_id);
            update3.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update3, private_folder_id, new Date());
        } finally {
        	oxma.deleteFolder(new FolderObject(public_folder_id), true, System.currentTimeMillis());
            //ofa.deleteFolder(public_folder_id, so, true, SUPER_END);
        }

        try {
            DBPool.push(context, readcon);
            DBPool.pushWrite(context, writecon);
        } catch(final Exception ignore) {
            ignore.printStackTrace();
        }

        try {
            testobject = csql.getObjectById(object_id, public_folder_id);
            throw new Exception("Object not deleted! Test failed!");
        } catch (final Exception not_exist) {
            // this is normal because the object has been deleted before
        }

    }



    public void testInsertMoveAllDelete() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final CalendarDataObject cdao = new CalendarDataObject();

        cdao.setTitle("testInsertMoveAllDelete - Step 1 - Insert");
        cdao.setParentFolderID(getCalendarDefaultFolderForUser(userid, context));

        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));

        fillDatesInDao(cdao);
        cdao.setIgnoreConflicts(true);
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int private_folder_id = cdao.getEffectiveFolderId();

        final int object_id = cdao.getObjectID();

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, csql, readcon, writecon);
        final OCLPermission oclp = new OCLPermission();
        oclp.setEntity(userid);
        oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp.setFolderAdmin(true);
        FolderObject fo = new FolderObject();
        fo.setFolderName("MyTestFolder"+System.currentTimeMillis());
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PUBLIC);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp });
        //ofa.createFolder(fo, so, true, readcon, writecon, false);
        fo = oxma.createFolder(fo, true, System.currentTimeMillis());

        final int public_folder_id = fo.getObjectID();
        CalendarDataObject testobject = null;
        try {
        	final long startTime = System.currentTimeMillis();
            // TODO: "Move" folder to a public folder
            final CalendarDataObject update1 = new CalendarDataObject();
            update1.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            update1.setObjectID(object_id);
            update1.setParentFolderID(public_folder_id);
            update1.setTitle("testInsertMoveAllDelete - Step 2 - Update");
            update1.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update1, private_folder_id, new Date());

            // TODO: LoadObject by ID and make some tests
            testobject = csql.getObjectById(object_id, public_folder_id);
            final UserParticipant up[] = testobject.getUsers();
            for (int a = 0; a < up.length; a++) {
                assertTrue("check that folder id IS NULL", up[a].getPersonalFolderId() == UserParticipant.NO_PFID);
            }

            assertEquals("testInsertMoveAllDelete - Step 2 - Update", update1.getTitle());

            // TODO: Move again to private folder

            final CalendarDataObject update2 = csql.getObjectById(object_id, public_folder_id);

            update2.setTitle("testInsertMoveAllDelete - Step 3 - Update");
            update2.setParentFolderID(private_folder_id);
            update2.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update2, public_folder_id, new Date());

            // TODO: LoadObject by ID and make some tests

            final CalendarDataObject testobject2 = csql.getObjectById(object_id, private_folder_id);

            assertEquals("Test folder id ", testobject2.getEffectiveFolderId(), private_folder_id);

            final UserParticipant up2[] = testobject2.getUsers();

            assertEquals("check length ", up2.length, 1);

            for (int a = 0; a < up2.length; a++) {
                assertEquals("check that folder id private folder ", up2[a].getPersonalFolderId(), private_folder_id);
            }

            // TODO: Move again to public folder and delete complete folder

            final CalendarDataObject update3 = csql.getObjectById(object_id, private_folder_id);

            update3.setTitle("testInsertMoveAllDelete - Step 4 - Update");
            update3.setParentFolderID(public_folder_id);
            update3.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update3, private_folder_id, new Date());

            deleteAllAppointments();

            SearchIterator<Appointment> si = csql.getModifiedAppointmentsInFolder(public_folder_id, cols, new Date(0));
            boolean found = false;
            while (si.hasNext()) {
                final Appointment tdao = si.next();
                if (tdao != null) {
                    found = true;
                }
            }
            si.close();
            assertFalse("Found unexpected Appointment.", found);

            // Magic test

            si = csql.getAppointmentsBetween(userid, new Date(0), new Date(253402210800000L), cols, 0,  null);
            while (si.hasNext()) {
            	final Appointment tdao = si.next();
            	final Date compare = tdao.getLastModified();
            	if (compare != null) {
					assertFalse("Got results. An available appointment created in test case! " + tdao.getTitle(), compare.getTime() >= startTime && tdao.getTitle().startsWith("testInsertMoveAllDelete"));
				} else {
					fail("Missing last-modified time stamp in appointment");
				}
            }

            //assertTrue("Got results", !si.hasNext());

            si.close();
        } finally {
        	oxma.deleteFolder(new FolderObject(public_folder_id), true, System.currentTimeMillis());
            //ofa.deleteFolder(public_folder_id, so, true, SUPER_END);
        }

        try {
            DBPool.push(context, readcon);
            DBPool.pushWrite(context, writecon);
        } catch(final Exception ignore) {
            ignore.printStackTrace();
        }

        try {
            testobject = csql.getObjectById(object_id, public_folder_id);
            throw new Exception("Object not deleted! Test failed!");
        } catch (final Exception not_exist) {
            // this is normal because the object has been deleted before
        }

    }

    public void testConflictHandling() throws Exception  {
        final Context context = new ContextImpl(contextid);
        final CalendarDataObject cdao = new CalendarDataObject();

        cdao.setTitle("testConflict Step 1 - Insert - ignore conflicts");
        cdao.setParentFolderID(getCalendarDefaultFolderForUser(userid, context));

        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));

        fillDatesInDao(cdao);
        cdao.setIgnoreConflicts(true);
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        cdao.getObjectID();

        final CalendarDataObject conflict_cdao = new CalendarDataObject();

        conflict_cdao.setTitle("testConflict Step 2 - Insert - Must conflict");
        conflict_cdao.setParentFolderID(getCalendarDefaultFolderForUser(userid, context));

        conflict_cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));

        fillDatesInDao(conflict_cdao);
        conflict_cdao.setIgnoreConflicts(false);
        final CalendarDataObject conflicts[] = csql.insertAppointmentObject(conflict_cdao);

        if (conflicts != null) { // could be that the whole conflict detection is deactivated
            assertTrue("Check if the conflict has been detected", conflicts.length > 0);

            boolean found_first_appointment = false;
            for (int a  = 0; a < conflicts.length; a++) {
                if (conflicts[a].getObjectID() == cdao.getObjectID()) {
                    found_first_appointment = true;
                    break;
                }
            }

            if (conflicts.length != ConflictHandler.MAX_CONFLICT_RESULTS) {
                assertTrue("Check for conflict object ", found_first_appointment);
            }

            assertTrue("Check that we did not create the second appointment", conflict_cdao.containsObjectID() == false);
        } else {
            fail("No conflict!");
        }

    }

    public void testGetAllAppointmentsFromUserInAllFolders() throws Exception {
        final Connection readcon = DBPool.pickup(getContext());
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "testGetAllAppointmentsFromUserInAllFolders");
        final CalendarSql csql = new CalendarSql(so);

        SearchIterator<Appointment> si = csql.getAppointmentsBetween(userid, new Date(0), new Date(253402210800000L), cols, 0,  null);
        assertTrue("Test if we got appointments", si.hasNext());
        while (si.hasNext()) {
            final Appointment cdao = si.next();
            assertTrue("Check folder ", cdao.getParentFolderID() != 0);
            testDelete(cdao);

        }
        si.close();
        si = csql.getAppointmentsBetween(userid, new Date(0), new Date(253402210800000L), cols, 0, null);

        while (si.hasNext()) {
			final Appointment cdao = si.next();

			final EffectivePermission ep = new OXFolderAccess(context).getFolderPermission(cdao.getParentFolderID(),
					userid, UserConfigurationStorage.getInstance().getUserConfiguration(userid, context));

			if (ep.getDeletePermission() != OCLPermission.NO_PERMISSIONS
					&& (ep.canDeleteAllObjects() || (cdao.getCreatedBy() == userid && ep.canDeleteOwnObjects()))) {
				fail("Not all appointments were deleted: objectID=" + cdao.getObjectID() + " title=" + cdao.getTitle());
			}
		}

        DBPool.push(context, readcon);
    }


    public void testResourceConflictHandling() throws Exception {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        final int folder_id = getCalendarDefaultFolderForUser(userid, context);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone("Europe/Berlin");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(folder_id);

        CalendarTest.fillDatesInDao(cdao);

        cdao.setTitle("testResourceConflictHandling - Step 1 - Insert");


        final Participants p = new Participants();

        final Participant resource = new ResourceParticipant(100);

        p.add(resource);

        cdao.setParticipants(p.getList());

        final CalendarDataObject cdao_conflict = cdao.clone();

        cdao.setIgnoreConflicts(true);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        assertTrue("Check that the object really exists ", object_id > 0);

        cdao.getLastModified();

        cdao.setIgnoreConflicts(true);

        cdao_conflict.setParentFolderID(folder_id);

        CalendarDataObject conflicts[] = csql.insertAppointmentObject(cdao_conflict);

        assertTrue("Got conflicts ", conflicts != null);
        boolean found = false;
        for (int a = 0; a < conflicts.length; a++) {
            final CalendarDataObject tcdao = conflicts[a];
            if (tcdao.getObjectID() == object_id) {
                found = true;
            }
        }
        assertTrue("Conflict object not found", found);

        final Participants p2 = new Participants();
        final Participant resource2 = new ResourceParticipant(1001);
        p2.add(resource2);

        cdao.setParticipants(p2.getList());
        final String titel = "testResourceConflictHandling - Step 2 - Update";
        cdao.setTitle(titel);

        conflicts = csql.updateAppointmentObject(cdao, folder_id, new Date());

        final CalendarDataObject testobject = csql.getObjectById(object_id, folder_id);

        final Participant test_participants[] = testobject.getParticipants();
        assertEquals("Check size after update ", 2, test_participants.length);
        assertEquals("Check changed titlel", titel, testobject.getTitle());
        found = false;
        for (int a = 0; a < test_participants.length; a++) {
            if (test_participants[a].getIdentifier() == 1001) {
                found = true;
            }

        }
        assertTrue("Check for updated (new) resource", found);

        csql.deleteAppointmentObject(cdao, folder_id, new Date());
    }

    public void testComplexConflictHandling() throws Exception  {
        deleteAllAppointments(); // Clean up !

        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        final int fid = getCalendarDefaultFolderForUser(userid, context);
        final CalendarSql csql = new CalendarSql(so);

        final SearchIterator<Appointment> si = csql.getAppointmentsBetween(userid, new Date(0), new Date(253402210800000L), cols, 0,  null);
        while (si.hasNext()) {
			final Appointment cdao = si.next();

			final EffectivePermission ep = new OXFolderAccess(context).getFolderPermission(cdao.getParentFolderID(),
					userid, UserConfigurationStorage.getInstance().getUserConfiguration(userid, context));

			if (ep.getDeletePermission() != OCLPermission.NO_PERMISSIONS
					&& (ep.canDeleteAllObjects() || (cdao.getCreatedBy() == userid && ep.canDeleteOwnObjects()))) {
				fail("Not all appointments were deleted: objectID=" + cdao.getObjectID() + " title=" + cdao.getTitle());
			}
		}
        si.close();

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone("Europe/Berlin");
        cdao.setTitle("testComplexConflictHandling - Step 1 - Insert");
        cdao.setParentFolderID(fid);

        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setShownAs(Appointment.FREE);
        fillDatesInDao(cdao);

        final CalendarDataObject cdao_conflict = cdao.clone();
        TimeZone conflictTimeZone = TimeZone.getTimeZone(cdao_conflict.getTimezone());
        int conflictOffset = conflictTimeZone.getOffset(cdao_conflict.getStartDate().getTime());
        cdao_conflict.setStartDate(new Date(cdao_conflict.getStartDate().getTime() - conflictOffset));
        cdao_conflict.setEndDate(new Date(cdao_conflict.getEndDate().getTime() - conflictOffset));
        cdao_conflict.setShownAs(Appointment.RESERVED);
        cdao_conflict.setTitle("testComplexConflictHandling - Step 2 - Insert");

        CalendarDataObject conflicts[] = csql.insertAppointmentObject(cdao);
        assertTrue("Found no conflicts", conflicts == null);

        conflicts = csql.insertAppointmentObject(cdao_conflict);
        assertTrue("Found no conflicts (free)", conflicts == null);

        assertTrue("ID check", cdao.getObjectID() != cdao_conflict.getObjectID());

        cdao.setTitle("testComplexConflictHandling - Step 3 - Update");
        cdao.setShownAs(Appointment.RESERVED);
        cdao.setFullTime(true);
        conflicts = csql.updateAppointmentObject(cdao, fid, new Date());
        assertNotNull("Did not found conflicts ", conflicts);
        assertEquals("Check correct result size", 1, conflicts.length);
        assertEquals("Check conflict results", conflicts[0].getObjectID(), cdao_conflict.getObjectID());
        cdao.setIgnoreConflicts(true);
        conflicts = csql.updateAppointmentObject(cdao, fid, new Date());
        assertTrue("Found conflicts ", conflicts == null);

        cdao_conflict.setTitle("testComplexConflictHandling - Step 4 - Update");
        cdao_conflict.setShownAs(Appointment.TEMPORARY);

        conflicts = csql.updateAppointmentObject(cdao_conflict, fid, new Date());
        assertNotNull("Found no conflicts ", conflicts);
        assertEquals("Check correct result size", 1, conflicts.length);
        assertEquals("Check conflict results", conflicts[0].getObjectID(), cdao.getObjectID());

        // TODO: Convert cdao_conflict to daily recurring app and check more conflicts

    }


    public void testConfirmation() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final int fid = getCalendarDefaultFolderForUser(userid, context);
        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testConfirmation - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setIgnoreConflicts(true);

        final UserParticipant userparticipants = new UserParticipant(userid);
        userparticipants.setConfirm(Appointment.ACCEPT);
        final String check_confirm_message = "Check this";
        userparticipants.setConfirmMessage(check_confirm_message);
        cdao.setUsers(new UserParticipant[] { userparticipants });

        assertEquals("Check id" , userid, userparticipants.getIdentifier());
        assertEquals("Check confirm state" , Appointment.ACCEPT, userparticipants.getConfirm());
        assertEquals("Check confirm message", check_confirm_message, userparticipants.getConfirmMessage());

        final Participants participants = new Participants();

        final Participant p2 = new UserParticipant(resolveUser(user2));
        participants.add(p2);

        cdao.setParticipants(participants.getList());

        fillDatesInDao(cdao);


        UserParticipant up[] = cdao.getUsers();
        assertTrue("Check participants (1)", up != null);
        assertTrue("Check participants (1)", up.length > 0);
        boolean found = false;
        for (int a = 0; a < up.length; a++) {
            if (up[a].getIdentifier() == userid) {
                assertEquals("Check confirm state (1)" , Appointment.ACCEPT, up[a].getConfirm());
                found = true;
            }
        }
        assertTrue("Check correct participants", found);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        up = testobject.getUsers();
        assertTrue("Check participants (2)", up != null);
        assertTrue("Check participants (2)", up.length > 0);
        found = false;
        for (int a = 0; a < up.length; a++) {
            if (up[a].getIdentifier() == userid) {
                assertEquals("Check confirm state (2)" , Appointment.ACCEPT, up[a].getConfirm());
                assertEquals("Check confirm message", check_confirm_message, up[a].getConfirmMessage());
                found = true;
            }
        }
        assertTrue("Check correct participants", found);


        //csql.deleteAppointmentObject(testobject, fid, new Date(SUPER_END));

    }

    public void testSharedFolder() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int uid2 = resolveUser(user2);
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int fid = getPrivateFolder();
        //OXFolderAction ofa = new OXFolderAction(so);
        final CalendarSql csql = new CalendarSql(so);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, csql, readcon, writecon);
        FolderObject fo = new FolderObject();

        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        final OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(uid2);
        oclp2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        fo.setFolderName("testSharedFolder_" + String.valueOf(System.currentTimeMillis()));
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });

        /*
         * Create folder
         */
        fo = oxma.createFolder(fo, true, System.currentTimeMillis());

        int shared_folder_id = 0;
        try {
            //ofa.createFolder(fo, so, true, readcon, writecon, false);
            shared_folder_id = fo.getObjectID();

            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setParentFolderID(shared_folder_id);
            fillDatesInDao(cdao);
            cdao.setTitle("testSharedFolder");
            cdao.setIgnoreConflicts(true);
            csql.insertAppointmentObject(cdao);
            final int object_id = cdao.getObjectID();

            final CalendarSql csql2 = new CalendarSql(so2);
            SearchIterator<Appointment> si = csql2.getModifiedAppointmentsInFolder(shared_folder_id, cols, new Date(0));
            boolean found = false;
            while (si.hasNext()) {
                final Appointment tdao = si.next();
                if (object_id == tdao.getObjectID()) {
                    found = true;
                }
            }
            si.close();
            assertTrue("User2 got object in shared folder created by user1 ", found);

            final CalendarDataObject ddao = new CalendarDataObject();
            ddao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            ddao.setObjectID(object_id);
            csql.deleteAppointmentObject(ddao, shared_folder_id, new Date());
            boolean found_deleted = false;
            si = csql2.getDeletedAppointmentsInFolder(shared_folder_id, cols, new Date(0));
            while (si.hasNext()) {
                si.next();
                if (object_id == cdao.getObjectID()) {
                found_deleted = true;
                }
            }
            si.close();
            assertTrue("User2 got no object in shared folder created by user1 ", found_deleted);

        } finally {
            try {
                if (shared_folder_id > 0) {
                	oxma.deleteFolder(new FolderObject(shared_folder_id), true, System.currentTimeMillis());
                    //ofa.deleteFolder(shared_folder_id, so, true, SUPER_END);
                } else {
                    fail("Folder was not created.");
                }
            } catch(final Exception e) {
                e.printStackTrace();
                fail("Error deleting folder object.");
            }
        }
        try {
            DBPool.push(context, readcon);
            DBPool.pushWrite(context, writecon);
        } catch(final Exception ignore) {
            ignore.printStackTrace();
        }
    }


    public void testHasAppointmentsBetween() throws Throwable {

        deleteAllAppointments();

        new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        final int fid = getPrivateFolder();
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setTitle("testHasAppointmentsBetween - Normal app");
        cdao.setParentFolderID(fid);
        cdao.setIgnoreConflicts(true);
        cdao.setStartDate(D("01.10.2013 08:00"));
        cdao.setEndDate(D("01.10.2013 09:00"));


        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        cdao.getObjectID();

        RecurringResultsInterface m = null;
        final CalendarDataObject cdao2 = new CalendarDataObject();
        cdao2.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao2.setParentFolderID(fid);
        cdao2.setTimezone(CalendarRecurringTests.TIMEZONE);
        cdao2.setStartDate(D("04.10.2013 08:00"));
        cdao2.setEndDate(D("04.10.2013 09:00"));
        cdao2.setTitle("testHasAppointmentsBetween - Rec app");
        cdao2.setRecurrenceType(CalendarDataObject.WEEKLY);
        cdao2.setInterval(1);
        cdao2.setDays(Appointment.FRIDAY);
        cdao2.setIgnoreConflicts(true);

        csql.insertAppointmentObject(cdao2);
        cdao2.getObjectID();

        Date range_start = D("01.10.2013 00:00");
        Date range_end = D("01.11.2013 00:00");

        boolean[] check_array = new boolean[]{true, false, false, true, false, false, false, false, false, false, true, false, false, false, false, false, false, true, false, false, false, false, false, false, true, false, false, false, false, false, false};
        boolean test_array[] = csql.hasAppointmentsBetween(range_start, range_end);

        assertEquals("Check arrays (length)", check_array.length, test_array.length);

        for (int a = 0; a < check_array.length; a++) {
            assertEquals("Check arrays (position "+a+")", check_array[a], test_array[a]);
        }

    }


   public void testInsertUpdateAlarm() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final int fid = getCalendarDefaultFolderForUser(userid, context);
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testInsertUpdateAlarm - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setIgnoreConflicts(true);
        cdao.setAlarm(15);
        cdao.setAlarmFlag(true);
        fillDatesInDao(cdao);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertTrue("Check alarm", testobject.containsAlarm());
        assertEquals("Check correct alarm value", 15, testobject.getAlarm());

        final CalendarDataObject update = new CalendarDataObject();
        update.setTitle("testInsertUpdateAlarm - Step 2 - Update");
        update.setObjectID(object_id);
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setIgnoreConflicts(true);
        csql.updateAppointmentObject(update, fid, testobject.getLastModified());

        testobject = csql.getObjectById(object_id, fid);
        assertTrue("Check alarm", testobject.containsAlarm());
        assertEquals("Check correct alarm value", 15, testobject.getAlarm());

        final CalendarDataObject update2 = new CalendarDataObject();
        update2.setTitle("testInsertUpdateAlarm - Step 3 - Update");
        update2.setObjectID(object_id);
        update2.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update2.setIgnoreConflicts(true);

        final UserParticipant up = new UserParticipant(userid);
        update2.setUsers(new UserParticipant[] { up });

        csql.updateAppointmentObject(update2, fid, testobject.getLastModified());

        testobject = csql.getObjectById(object_id, fid);
        assertTrue("Check alarm", testobject.containsAlarm());
        assertEquals("Check correct alarm value", 15, testobject.getAlarm());

    }

    public void testInsertMoveAndDeleteAppointmentsWithPrivateFlag() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final CalendarDataObject cdao = new CalendarDataObject();

        cdao.setTitle("testInsertMoveAndDeleteAppointmentsWithPrivateFlag - Step 1 - Insert");
        cdao.setParentFolderID(getCalendarDefaultFolderForUser(userid, context));

        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));

        fillDatesInDao(cdao);
        cdao.setIgnoreConflicts(true);
        cdao.setPrivateFlag(true);
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int private_folder_id = cdao.getEffectiveFolderId();

        final int object_id = cdao.getObjectID();

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, csql, readcon, writecon);
        final OCLPermission oclp = new OCLPermission();
        oclp.setEntity(userid);
        oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp.setFolderAdmin(true);
        FolderObject fo = new FolderObject();
        fo.setFolderName("testInsertMoveAndDeleteAppointmentsWithPrivateFlagFolder"+System.currentTimeMillis());
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PUBLIC);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp });
        //ofa.createFolder(fo, so, true, readcon, writecon, false);
        fo = oxma.createFolder(fo, true, System.currentTimeMillis());
        final int public_folder_id = fo.getObjectID();
        try {
            // TODO: "Move" object to a public folder
            final CalendarDataObject update1 = new CalendarDataObject();
            update1.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            update1.setObjectID(object_id);
            update1.setParentFolderID(public_folder_id);
            update1.setTitle("testInsertMoveAndDeleteAppointments - Step 2 - Update");
            update1.setIgnoreConflicts(true);
            try {
                csql.updateAppointmentObject(update1, private_folder_id, new Date());
                fail("Move from a private folder with private flag should not be possibe!");
            } catch(final OXException e) {
                // Very good if we get an error
            } catch(final Exception e) {
                fail ("Nooo "+e.getMessage());
            }

        } finally {
        	oxma.deleteFolder(new FolderObject(public_folder_id), true, System.currentTimeMillis());
            //ofa.deleteFolder(public_folder_id, so, true, SUPER_END);
        }

        try {
            DBPool.push(context, readcon);
            DBPool.pushWrite(context, writecon);
        } catch(final Exception ignore) {
            ignore.printStackTrace();
        }

        try {
            csql.getObjectById(object_id, public_folder_id);
            throw new Exception("Object not deleted! Test failed!");
        } catch (final Exception not_exist) {
            // this is normal because the object has been deleted before
        }

    }

    public void testAlarmAndUpdate() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final int fid = getCalendarDefaultFolderForUser(userid, context);
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testAlarmAndUpdate - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setIgnoreConflicts(true);
        cdao.setAlarm(15);
        fillDatesInDao(cdao);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertTrue("Alarm should be set", testobject.containsAlarm());
        assertEquals("Test correct alarm value", 15, testobject.getAlarm());

        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setObjectID(object_id);
        update.setTitle("testAlarmAndUpdate - Step 2 - Update");
        update.setIgnoreConflicts(true);

        final Participants participants = new Participants();
        final Participant p = new UserParticipant(userid);
        participants.add(p);
        final UserParticipant up = new UserParticipant(userid);

        update.setUsers(new UserParticipant[] { up });
        update.setParticipants(participants.getList());

        csql.updateAppointmentObject(update, fid, testobject.getLastModified());

        final CalendarDataObject testobject2 = csql.getObjectById(object_id, fid);
        assertTrue("Alarm should be set", testobject2.containsAlarm());
        assertEquals("Test correct alarm value", 15, testobject2.getAlarm());

        final ReminderService rsql = new ReminderHandler(ContextStorage.getInstance().getContext(so.getContextId()));
        ReminderObject ro = rsql.loadReminder(object_id, userid, Types.APPOINTMENT);
        long check_date = new Date((testobject2.getStartDate().getTime() - (15*60*1000))).getTime();
        assertEquals("Check correct alam in reminder object" , check_date, ro.getDate().getTime());

        final CalendarDataObject update2 = new CalendarDataObject();
        update2.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update2.setObjectID(object_id);
        final String title = "testAlarmAndUpdate - Step 3 - Update";
        update2.setTitle(title);
        update2.setAlarm(30);
        update2.setIgnoreConflicts(true);

        csql.updateAppointmentObject(update2, fid, testobject2.getLastModified());

        final CalendarDataObject testobject3 = csql.getObjectById(object_id, fid);
        assertEquals("Check title", title, testobject3.getTitle());
        assertTrue("Alarm should be set", testobject3.containsAlarm());
        assertEquals("Test correct alarm value", 30, testobject3.getAlarm());


        ro = rsql.loadReminder(object_id, userid, Types.APPOINTMENT);
        check_date = new Date((testobject3.getStartDate().getTime() - (30*60*1000))).getTime();
        assertEquals("Check correct alam in reminder object" , check_date, ro.getDate().getTime());


    }

    public void testDataTuncationException() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final int fid = getCalendarDefaultFolderForUser(userid, context);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        final CalendarDataObject cdao = new CalendarDataObject();
        final StringBuilder sb = new StringBuilder("testDataTuncationException ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        cdao.setTitle(sb.toString());
        cdao.setParentFolderID(fid);
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setIgnoreConflicts(true);
        fillDatesInDao(cdao);
        final CalendarSql csql = new CalendarSql(so);
        int object_id = 0;
        try {
            csql.insertAppointmentObject(cdao);
            object_id = cdao.getObjectID();
        } catch(final OXException oxe) {
            final ProblematicAttribute[] problematics = oxe.getProblematics();
            if (problematics.length == 0) {
                fail("Got no TruncatedIds ");
            }
        } catch(final Exception e) {
            fail("Wrong exception!");
        }

        if (object_id > 0) {
            fail("Test not executed correctly");
        }

    }

    public void testExternalParticipants() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final int fid = getCalendarDefaultFolderForUser(userid, context);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testExternalParticipants - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setIgnoreConflicts(true);

        final UserParticipant up = new UserParticipant(userid);
        up.setAlarmMinutes(5);
        cdao.setUsers(new UserParticipant[] { up });

        final String mail_address = "firsttest@example.org";
        final String display_name = "Externer test user";


        final String mail_address2 = "othertest@example.org";
        final String display_name2 = "Externer test user2";

        Participants participants = new Participants();
        final ExternalUserParticipant p1 = new ExternalUserParticipant(mail_address);
        p1.setDisplayName(display_name);
        participants.add(p1);

        final ExternalUserParticipant p2 = new ExternalUserParticipant(mail_address2);
        p2.setDisplayName(display_name2);
        participants.add(p2);

        cdao.setParticipants(participants.getList());

        fillDatesInDao(cdao);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);

        final Participant test_participants[] = testobject.getParticipants();
        assertTrue("Check return is not null" , test_participants != null);
        boolean found = false;
        for (int a = 0; a < test_participants.length; a++) {
            if (test_participants[a].getType() == Participant.EXTERNAL_USER) {
                if (test_participants[a].getEmailAddress().equals(mail_address)) {
                    assertEquals("Check display name", display_name, test_participants[a].getDisplayName());
                    assertEquals("Check mail address", mail_address, test_participants[a].getEmailAddress());
                } else if (test_participants[a].getEmailAddress().equals(mail_address2)) {
                    assertEquals("Check display name", display_name2, test_participants[a].getDisplayName());
                    assertEquals("Check mail address", mail_address2, test_participants[a].getEmailAddress());
                }
                found = true;
            }
        }

        assertTrue("Got external participant", found);

        final CalendarDataObject update = new CalendarDataObject();

        final String display_name_2_fail = "Externer test user without mail address";
        participants = new Participants();
        final ExternalUserParticipant p_fail = new ExternalUserParticipant(null);
        p_fail.setDisplayName(display_name_2_fail);
        participants.add(p_fail);

        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setObjectID(object_id);
        update.setParticipants(participants.getList());
        update.setIgnoreConflicts(true);
        boolean check_update = false;


        try {
            csql.updateAppointmentObject(update, fid, cdao.getLastModified());
            check_update = true;
        } catch(final Exception e) {
            e.printStackTrace();
        }
        assertFalse("Participant has no mail, update must fail!", check_update);

        final CalendarDataObject update_participants = new CalendarDataObject();

        final String update_new_display_1 = "External Update 1";
        final String update_new_mail_1 = "abc@de";
        final String update_new_display_2 = "External Update 2";
        final String update_new_mail_2 = "def2@de";

        final Participants participants_update = new Participants();
        final ExternalUserParticipant p3 = new ExternalUserParticipant(update_new_mail_1);
        p3.setDisplayName(update_new_display_1);
        participants_update.add(p3);

        final ExternalUserParticipant p4 = new ExternalUserParticipant(update_new_mail_2);
        p4.setDisplayName(update_new_display_2);
        participants_update.add(p4);

        participants_update.add(p1);
        participants_update.add(p2);

        update_participants.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update_participants.setObjectID(object_id);
        update_participants.setParticipants(participants_update.getList());
        update_participants.setIgnoreConflicts(true);

        csql.updateAppointmentObject(update_participants, fid, cdao.getLastModified());


        final CalendarDataObject testobject_update = csql.getObjectById(object_id, fid);

        final Participant test_participants_update[] = testobject_update.getParticipants();

        int found_external = 0;
        for (int a = 0; a < test_participants_update.length; a++) {
            if (test_participants_update[a].getType() == Participant.EXTERNAL_USER) {
                if (test_participants_update[a].getEmailAddress().equals(mail_address)) {
                    assertEquals("Check display name", display_name, test_participants_update[a].getDisplayName());
                    assertEquals("Check mail address", mail_address, test_participants_update[a].getEmailAddress());
                    found_external++;
                } else if (test_participants_update[a].getEmailAddress().equals(mail_address2)) {
                    assertEquals("Check display name", display_name2, test_participants_update[a].getDisplayName());
                    assertEquals("Check mail address", mail_address2, test_participants_update[a].getEmailAddress());
                    found_external++;
                } else if (test_participants_update[a].getEmailAddress().equals(update_new_mail_1)) {
                    assertEquals("Check display name", update_new_display_1, test_participants_update[a].getDisplayName());
                    assertEquals("Check mail address", update_new_mail_1, test_participants_update[a].getEmailAddress());
                    found_external++;
                } else if (test_participants_update[a].getEmailAddress().equals(update_new_mail_2)) {
                    assertEquals("Check display name", update_new_display_2, test_participants_update[a].getDisplayName());
                    assertEquals("Check mail address", update_new_mail_2, test_participants_update[a].getEmailAddress());
                    found_external++;
                }
            }
        }

        assertEquals("Found all 4 external participants after update", 4, found_external);

        final CalendarDataObject update_participants2 = new CalendarDataObject();


        final Participants participants_update2 = new Participants();

        participants_update2.add(p1);

        participants_update2.add(p2);



        update_participants2.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update_participants2.setObjectID(object_id);
        update_participants2.setParticipants(participants_update2.getList());
        update_participants2.setIgnoreConflicts(true);

        csql.updateAppointmentObject(update_participants2, fid, update_participants.getLastModified());


        final CalendarDataObject testobject_update2 = csql.getObjectById(object_id, fid);

        final Participant test_participants_update2[] = testobject_update2.getParticipants();

        found_external = 0;
        for (int a = 0; a < test_participants_update2.length; a++) {
            if (test_participants_update2[a].getType() == Participant.EXTERNAL_USER) {
                if (test_participants_update2[a].getEmailAddress().equals(mail_address)) {
                    assertEquals("Check display name", display_name, test_participants_update2[a].getDisplayName());
                    assertEquals("Check mail address", mail_address, test_participants_update2[a].getEmailAddress());
                    found_external++;
                } else if (test_participants_update2[a].getEmailAddress().equals(mail_address2)) {
                    assertEquals("Check display name", display_name2, test_participants_update2[a].getDisplayName());
                    assertEquals("Check mail address", mail_address2, test_participants_update2[a].getEmailAddress());
                    found_external++;
                } else if (test_participants_update2[a].getEmailAddress().equals(update_new_mail_1)) {
                    assertEquals("Check display name", update_new_display_1, test_participants_update2[a].getDisplayName());
                    assertEquals("Check mail address", update_new_mail_1, test_participants_update2[a].getEmailAddress());
                    found_external++;
                } else if (test_participants_update2[a].getEmailAddress().equals(update_new_mail_2)) {
                    assertEquals("Check display name", update_new_display_2, test_participants_update2[a].getDisplayName());
                    assertEquals("Check mail address", update_new_mail_2, test_participants_update2[a].getEmailAddress());
                    found_external++;
                }
            }
        }

        assertEquals("Found last 2 external participants after second update", 2, found_external);

    }

    public void testGroupZero() throws Exception  {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(getContext());
        final Participants participants = new Participants();
        final Participant p = new GroupParticipant(0);
        participants.add(p);
        cdao.setParticipants(participants.getList());
        cdao.setTitle("testGroupZero");
        CalendarTest.fillDatesInDao(cdao);

        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setIgnoreConflicts(true);

        final int fid = getCalendarDefaultFolderForUser(userid, getContext());
        cdao.setGlobalFolderID(fid);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        cdao.getObjectID();


    }

    public void testSetAlarmAndConfirmStateWithParticipantsAndChangeTime() throws Exception {
        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int userid2 = resolveUser(user2);
        final int fid = getCalendarDefaultFolderForUser(userid, getContext());
        final int fid2 = getCalendarDefaultFolderForUser(userid2, getContext());
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testSetAlarmAndConfirmStateWithParticipantsAndChangeTime");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);

        final UserParticipant userA = new UserParticipant(userid);
        userA.setAlarmMinutes(15);

        final UserParticipant userB = new UserParticipant(userid2);

        cdao.setParticipants(new UserParticipant[] { userA, userB });
        cdao.setUsers(new UserParticipant[] { userA, userB });

        final CalendarSql csql = new CalendarSql(so);
        final CalendarSql csql2 = new CalendarSql(so2);

        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject update_user2 = csql2.getObjectById(object_id, fid2);

        update_user2.setContext(getContext());
        update_user2.setIgnoreConflicts(true);
        update_user2.setAlarm(30);

        csql2.updateAppointmentObject(update_user2, fid2, cdao.getLastModified());


        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);

        SearchIterator si = csql.getModifiedAppointmentsInFolder(fid, cols, decrementDate(cdao.getLastModified()));
        boolean found = false;
        while (si.hasNext()) {
            final CalendarDataObject tdao = (CalendarDataObject)si.next();
            if (tdao.getObjectID() == object_id) {
                found = true;
                assertTrue("Check that userA has an alarm set in the cdao", tdao.containsAlarm());
                assertEquals("Check that we got the correct reminder", 15, tdao.getAlarm());
            }
        }
        assertTrue("Found our object (userA)", found);

        SearchIterator si2 = csql2.getModifiedAppointmentsInFolder(fid2, cols, decrementDate(cdao.getLastModified()));
        found = false;
        while (si2.hasNext()) {
            final CalendarDataObject tdao = (CalendarDataObject)si2.next();
            if (tdao.getObjectID() == object_id) {
                found = true;
                assertTrue("Check that userB has an alarm set in the cdao", tdao.containsAlarm());
                assertEquals("Check that we got the correct reminder", 30, tdao.getAlarm());
            }
        }
        assertTrue("Found our object (userB)", found);


        final CalendarDataObject update_with_time_change = testobject.clone();
        update_with_time_change.setContext(getContext());
        update_with_time_change.setIgnoreConflicts(true);
        update_with_time_change.setObjectID(object_id);

        final Date test_date = new Date(testobject.getStartDate().getTime()+3600000);

        update_with_time_change.setStartDate(test_date);
        update_with_time_change.setEndDate(new Date(testobject.getEndDate().getTime()+3600000));

        csql.updateAppointmentObject(update_with_time_change, fid, testobject.getLastModified());


        si = csql.getModifiedAppointmentsInFolder(fid, cols, decrementDate(cdao.getLastModified()));
        found = false;
        while (si.hasNext()) {
            final CalendarDataObject tdao = (CalendarDataObject)si.next();
            if (tdao.getObjectID() == object_id) {
                found = true;
                assertTrue("Check that userA has an alarm set in the cdao", tdao.containsAlarm());
                assertEquals("Check that we got the correct reminder", 15, tdao.getAlarm());
            }
        }
        assertTrue("Found our object (userA)", found);

        si2 = csql2.getModifiedAppointmentsInFolder(fid2, cols, decrementDate(cdao.getLastModified()));
        found = false;
        while (si2.hasNext()) {
            final CalendarDataObject tdao = (CalendarDataObject)si2.next();
            if (tdao.getObjectID() == object_id) {
                found = true;
                assertTrue("Check that userB has an alarm set in the cdao", tdao.containsAlarm());
                assertEquals("Check that we got the correct reminder", 30, tdao.getAlarm());
                final UserParticipant up_test[] = tdao.getUsers();
                for (int a = 0; a < up_test.length; a++) {
                    if (up_test[a].getIdentifier() == userid2) {
                        assertEquals("Check Confirm State", CalendarDataObject.NONE, up_test[a].getConfirm());
                        final ReminderHandler rh = new ReminderHandler(getContext());
                        final ReminderObject ro = rh.loadReminder(object_id, userid2, Types.APPOINTMENT);
                        final Date check_date = ro.getDate();
                        assertEquals("Check correct Alarm", new Date(test_date.getTime()-(30*60000)), check_date);
                    } else if (up_test[a].getIdentifier() == userid) {
                        assertEquals("Check Confirm State", CalendarDataObject.ACCEPT, up_test[a].getConfirm());
                        final ReminderHandler rh = new ReminderHandler(getContext());
                        final ReminderObject ro = rh.loadReminder(object_id, userid, Types.APPOINTMENT);
                        final Date check_date = ro.getDate();
                        assertEquals("Check correct Alarm", new Date(test_date.getTime()-(15*60000)), check_date);
                    }
                }

            }
        }
        assertTrue("Found our object (userB)", found);

        final CalendarDataObject test_fid = csql.getObjectById(object_id, fid);
        assertEquals("Check folder for userA", fid, test_fid.getParentFolderID());

        final CalendarDataObject test_fid2 = csql2.getObjectById(object_id, fid2);
        assertEquals("Check folder for userB", fid2, test_fid2.getParentFolderID());

    }


}
