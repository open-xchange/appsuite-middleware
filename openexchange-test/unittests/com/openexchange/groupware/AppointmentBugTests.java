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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import org.junit.Before;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.calendar.CalendarMySQL;
import com.openexchange.calendar.CalendarOperation;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.calendar.api.AppointmentSqlFactory;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.event.impl.EventConfigImpl;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
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
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.test.AjaxInit;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.session.ServerSessionAdapter;

public class AppointmentBugTests extends CalendarTest {

    int cols[] = new int[] { Appointment.START_DATE, Appointment.END_DATE, Appointment.TITLE, Appointment.RECURRENCE_ID, Appointment.RECURRENCE_POSITION, Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.USERS, Appointment.FULL_TIME };
    public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
    public static final String TIMEZONE = "Europe/Berlin";
    // Override these in setup
    private static int userid = 11; // bishoph
    public static int contextid = 1;

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        Init.startServer();
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
        
        CalendarMySQL.setApppointmentSqlFactory(new AppointmentSqlFactory());
    }

    @Override
    protected void tearDown() throws Exception {
        Init.stopServer();
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

        final String user = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant2", "");
        return resolveUser(user);
    }

    public static Context getContext() {
        try {
            final TestConfig config = new TestConfig();
            final TestContextToolkit tools = new TestContextToolkit();
            final String ctxName = config.getContextName();
            return null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);
        } catch (final OXException e) {
            e.printStackTrace();
            return null;
        }
    }

    void deleteAllAppointments() throws Exception  {
        final Connection readcon = DBPool.pickup(getContext());
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "deleteAllApps");
        final CalendarSql csql = new CalendarSql(so);
        final SearchIterator<Appointment> si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0,  null);
        while (si.hasNext()) {
            final Appointment cdao = si.next();
            testDelete(cdao);
        }
        si.close();
        DBPool.push(context, readcon);
    }

    private static final String SQL_DEL_WORKING_DATES = "DELETE FROM prg_dates WHERE cid = ? AND intfield01 = ?";

    private static final String SQL_DEL_WORKING_MEMBERS = "DELETE FROM prg_dates_members WHERE cid = ? AND object_id = ?";

    private static final String SQL_DEL_WORKING_RIGHTS = "DELETE FROM prg_date_rights WHERE cid = ? AND object_id = ?";

    private void hardDelete(final int oid, final Context ctx) {
        final Connection writecon;
        try {
            writecon = DBPool.pickup(ctx);
        } catch (final OXException e) {
            e.printStackTrace();
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = writecon.prepareStatement(SQL_DEL_WORKING_DATES);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = writecon.prepareStatement(SQL_DEL_WORKING_MEMBERS);
            pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;

            stmt = writecon.prepareStatement(SQL_DEL_WORKING_RIGHTS);
            pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, oid);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            new CalendarCollection().closePreparedStatement(stmt);
        }
    }

    public static int getPrivateFolder(final int userid) throws Exception {
        int privatefolder = 0;
        final Context context = getContext();
        final Connection readcon = DBPool.pickup(context);
        privatefolder = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        DBPool.push(context, readcon);
        return privatefolder;
    }
    
    /**
     * Test for <a href=
     * "https://bugs.open-xchange.com/show_bug.cgi?id=20972">bug
     * #20972</a>:<br>
     * &quot;<i>Appointment invitations cannot be accepted if ical field content exceeds the DB field size</i>&quot;
     *
     * @throws Exception
     *             If an error occurs
     */
    public void testBug20972() throws Exception {
        final int fid = getPrivateFolder(userid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "testIdentifierForBug20972");
        
        final UserParticipant userParticipant = new UserParticipant(userid);
        
        StringBuilder sb = new StringBuilder();
        int cap = 0;
        String locString = "0123456789";
        while(cap < locString.length() * 25) {
            sb.append(locString);
            cap += locString.length();
        }
        sb.append("TRUNCATE ME");
        
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setLocation(sb.toString());
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setIgnoreConflicts(true);
        cdao.setTitle("Testing bug 20972");
        cdao.addParticipant(userParticipant);
        cdao.setExternalOrganizer(true);
        
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        
        final int oid = cdao.getObjectID();
        final CalendarDataObject expected = csql.getObjectById(oid, fid);
        
        assertNotNull("Object not created", expected);
        assertNotSame("Location truncated: ", expected.getLocation(), cdao.getLocation());
    }
    

    /*
     1.  Create an appointment
     2.  Click on apppointment series/recurring
     3.  Set pattern to montly; every 2 weeks; 6 times
     4.  Check the entries in the month view
     5.  Check entries in OX6 Calendar
    */
    public void testBug4467() throws Throwable {

        final int fid = getPrivateFolder(userid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");

        RecurringResultsInterface m = null;
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setTimezone(TIMEZONE);
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setOccurrence(10);
        cdao.setTitle("testBug4467");
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);
        cdao.setDays(Appointment.MONDAY + Appointment.WEDNESDAY + Appointment.FRIDAY);
        cdao.setIgnoreConflicts(true);
        new CalendarCollection().fillDAO(cdao);
        cdao.removeUntil();
        m = new CalendarCollection().calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check calculation", 10, m.size());
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        final CalendarDataObject check_cdao = csql.getObjectById(object_id, fid);
        assertEquals("Check calculated rec_string and loaded rec_string", cdao.getRecurrence(), check_cdao.getRecurrence());
        assertEquals("Check calculated occurrence and loaded occurrence", cdao.getOccurrence(), check_cdao.getOccurrence());
        assertEquals("Check calculated until and loaded until", cdao.getUntil(), check_cdao.getUntil());

    }

    /*
     1. Create a recurring whole-day appointment in Outlook (with Outlook OXtender)
     2. Set the recurrence to "every day, for 2 days" and save the appointment
     3. Verify the appointment in OX6 GUI in the different appointment views
    */
    public void testBug4377() throws Throwable {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(getContext());
        cdao.setTimezone(TIMEZONE);
        cdao.setTitle("testBug4377");
        CalendarTest.fillDatesInDao(cdao);
        cdao.setEndDate(cdao.getStartDate());
        cdao.setFullTime(true);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(2);
        RecurringResultsInterface rss = null;
        final CalendarOperation co = new CalendarOperation();
        co.prepareUpdateAction(cdao, null, userid, getPrivateFolder(userid), TIMEZONE);
        assertEquals("Check that the recurring calculator is 1", 1, cdao.getRecurrenceCalculator());
        rss = new CalendarCollection().calculateRecurring(cdao, 0, 0, 0);
        for (int a = 0; a < rss.size(); a++) {
            final RecurringResultInterface rs = rss.getRecurringResult(a);
            final long check_day = rs.getEnd() - rs.getStart();
            assertEquals("Check that we got a 24 hours appointment ", new CalendarCollection().MILLI_DAY, check_day);
        }
    }

    /*
     1. Create appointment with User A and add another participant (User B) in a private calendar folder
     2. Login with User B and delete the app in
     3. Check that User B does not have the appointment anymore in his own calendar
     4. Check that original appointment has only one participant left (User A)
    */
    public void testBug4497() throws Throwable {
        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int userid2 = resolveUser(user2);
        final int fid = getPrivateFolder(userid);
        final int fid2 = getPrivateFolder(userid2);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug4497");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);

        final UserParticipant userA = new UserParticipant(userid);
        final UserParticipant userB = new UserParticipant(userid2);

        cdao.setParticipants(new UserParticipant[] { userA, userB });

        final CalendarSql csql = new CalendarSql(so);

        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject delete = new CalendarDataObject();
        delete.setContext(ContextStorage.getInstance().getContext(so2.getContextId()));
        delete.setObjectID(object_id);

        final CalendarSql csql2 = new CalendarSql(so2);
        csql2.deleteAppointmentObject(delete, fid2, new Date());

        boolean test_exists = false;
        try {
            csql2.getObjectById(object_id, fid2);
        } catch(final OXException oxe) {
            test_exists = true;
        }

        assertTrue("Check that User B does not have the appointment anymore in his own calendar", test_exists);

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        final UserParticipant up[] = testobject.getUsers();
        assertEquals("Check that original appointment has only one participant left (User A)", 1, up.length);

    }

    /*
     Create an appointment (Termin) with another participant (Teilnehmer). Save the
     appointment. Select it and edit (bearbeiten) it. Modify it to occur weekly
     (woechentlich). FYI: I did not change the defaults, so it was an the same day and
     recurred every 4 weeks.
    */
    public void testBug4276() throws Throwable {
        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int userid2 = resolveUser(user2);
        final int fid = getPrivateFolder(userid);
        getPrivateFolder(userid2);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug4497");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);

        final UserParticipant userA = new UserParticipant(userid);
        final UserParticipant userB = new UserParticipant(userid2);

        cdao.setUsers(new UserParticipant[] { userA, userB });

        final CalendarSql csql = new CalendarSql(so);

        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setObjectID(object_id);
        update.setRecurrenceType(Appointment.WEEKLY);
        update.setInterval(1);
        update.setDays(Appointment.MONDAY);
        update.setIgnoreConflicts(true);
        csql.updateAppointmentObject(update, fid, new Date());

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertEquals("Test that app is a recurring appointment", Appointment.WEEKLY, testobject.getRecurrenceType());

    }

    /*
     when i open a multi participant appt and add one resource to the appt, the
     following error is thrown:
    */
    public void testBug4119() throws Throwable {

        // Clean up appointments
        deleteAllAppointments();

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int userid2 = resolveUser(user2);
        final int fid = getPrivateFolder(userid);
        final int fid2 = getPrivateFolder(userid2);

        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");

        // Clean up appointments
        FolderObject fo = new FolderObject();
        fo.setObjectID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        new RdbFolderSQLInterface(ServerSessionAdapter.valueOf(so)).clearFolder(fo, new Date());
        fo = new FolderObject();
        fo.setObjectID(fid2);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        new RdbFolderSQLInterface(ServerSessionAdapter.valueOf(so2)).clearFolder(fo, new Date());


        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug4497");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);

        final UserParticipant userA = new UserParticipant(userid);
        final UserParticipant userB = new UserParticipant(userid2);

        cdao.setUsers(new UserParticipant[] { userA, userB });

        final CalendarSql csql = new CalendarSql(so);

        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setObjectID(object_id);

        final Participants p = new Participants();
        final Participant resource = new ResourceParticipant(100);
        p.add(resource);
        p.add(userB);
        final Participant pu[] = p.getList();
        assertEquals("Check that we send a resource and userB", 2, pu.length); // TODO: Maybe this must be fixed!
        update.setParticipants(pu);
        update.setIgnoreConflicts(true);

        final CalendarDataObject conflicts[] = csql.updateAppointmentObject(update, fid, new Date());
        assertTrue("Got conflicts ", conflicts == null);

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        final Participant participants[] = testobject.getParticipants();

        boolean check_userA = false;
        boolean check_userB = false;
        boolean check_Resource = false;
        for (int a = 0; a < participants.length; a++) {
            if (participants[a].getType() == Participant.USER) {
                if (participants[a].getIdentifier() == userid) {
                    check_userA = true;
                } else if (participants[a].getIdentifier() == userid2) {
                    check_userB = true;
                }
            } else if (participants[a].getType() == Participant.RESOURCE) {
                if (participants[a].getIdentifier() == 100) {
                    check_Resource = true;
                }
            }
        }

        assertTrue("Test userA", check_userA);
        assertTrue("Test userB", check_userB); // Remove if above check "Check that we send a resource and userB" runs
        assertTrue("Test Resource", check_Resource);

    }

    /*
    Steps:
    1. Create an appointment
       "Send customer invoices", 11:00 - 12:00
    2. Series
       Monthly, Each 30th of the month,
       starting 30.12.2006, ending 30.12.2007
    3. Save the appointment
    4. Check the Calendar, Month view

    Results:
    Appointments initially get displayed, but always on the 1st of the following months
    */
    public void testBug4473() throws Throwable {
        RecurringResultsInterface m = null;
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        final long test_start_date = 1167472800000L; // 30.12.2006 11:00
        final long test_end_date = 1167476400000L; // 30.12.2006 12:00
        final long test_until = 1293706800000L; // 30.12.2010 12:00:00
        final int check_day = 30;
        cdao.setStartDate(new Date(test_start_date));
        cdao.setEndDate(new Date(test_end_date));
        cdao.setUntil(new Date(test_until));
        cdao.setTitle("testBug4473");
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.MONTHLY);
        cdao.setDayInMonth(check_day);
        //cdao.setDays(0);
        cdao.setInterval(1);
        new CalendarCollection().fillDAO(cdao);
        m = new CalendarCollection().calculateRecurring(cdao, 0, 0, 0);
        for (int a = 0; a < m.size(); a++) {
            final RecurringResultInterface rs = m.getRecurringResult(a);
            final Calendar test = Calendar.getInstance();
            test.setTime(new Date(rs.getStart()));
            assertEquals("Check day of month", check_day, test.get(Calendar.DAY_OF_MONTH));
        }

    }


    /*
    1. Create a new appointment in the OX6 GUI (whole day, recurring every day for 2
    days)
    2. Save the appointment and verify the calendar views

    -> The appointment is shown on every day starting at the given start date of the
    recurring whole-day appointment. In the correct case this appointment should
    only be shown on two days at all. I suppose this is a GUI bug because most
    recurring whole-day appointments work with Outlook OXtender 6 and are
    communicated correctly by the server.
    */
    public void testBug4766() throws Throwable {
        RecurringResultsInterface m = null;
        final int fid = getPrivateFolder(userid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setTitle("testBug4766");
        cdao.setFullTime(true);
        cdao.setRecurrenceType(CalendarDataObject.DAILY);
        cdao.setInterval(1);
        cdao.setOccurrence(2);
        cdao.setIgnoreConflicts(true);

        new CalendarCollection().fillDAO(cdao);
        cdao.removeUntil();
        m = new CalendarCollection().calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check size of calulated results", 2, m.size());

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        new CalendarCollection().fillDAO(testobject);
        m = new CalendarCollection().calculateRecurring(testobject, 0, 0, 0);
        assertEquals("Check size of calulated results", 2, m.size());
    }

    /*
    User A:
    1. Share the personal calendar folder to User B with Level 6 permissions
    User B:
    2. Access the shared calendar via Outlook (with Outlook OXtender)
    3. Create a new appointment in the shared calendar of User A
    4. Select the appointment in the shared calendar and move it to another timefram
    -> A Exception is shown in Outlook OXtenders debug log and occurs in
    open-xchange.log (see attachment)
     */
    public void testBug4717() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int uid2 = resolveUser(user2);
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int fid = getPrivateFolder(userid);
        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, readcon, writecon);
        FolderObject fo = new FolderObject();

        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        final OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(uid2);
        oclp2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        fo.setFolderName("testSharedFolder4717_" + String.valueOf(System.currentTimeMillis()));
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });

        int shared_folder_id = 0;
        try {
            //ofa.createFolder(fo, so, true, readcon, writecon, false);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();

            final CalendarSql csql2 = new CalendarSql(so2);
            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setParentFolderID(shared_folder_id);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug4717 - created by "+user2);
            cdao.setIgnoreConflicts(true);

            csql2.insertAppointmentObject(cdao);
            final int object_id = cdao.getObjectID();

            final CalendarDataObject original_object = csql2.getObjectById(object_id, shared_folder_id);

            final CalendarDataObject udao = new CalendarDataObject();
            udao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            udao.setObjectID(object_id);
            final Date check_start_date = new Date(cdao.getStartDate().getTime()+3600000); // move 1 h
            final Date check_end_date = new Date(cdao.getEndDate().getTime()+3600000); // move 1 h
            udao.setStartDate(check_start_date);
            udao.setEndDate(check_end_date);
            udao.setTitle("testBug4717 - updated by "+user2);

            final Participants participants = new Participants();
            final Participant p = new UserParticipant(userid);
            participants.add(p);

            final Participant p2 = new UserParticipant(uid2);
            participants.add(p2);
            udao.setParticipants(participants.getList());
            udao.setUsers(participants.getUsers());
            udao.setIgnoreConflicts(true);

            csql2.updateAppointmentObject(udao, shared_folder_id, cdao.getLastModified());

            final CalendarDataObject testobject = csql2.getObjectById(object_id, shared_folder_id);
            final UserParticipant up[] = testobject.getUsers();
            assertTrue("UserParticipant not null", up != null);
            assertEquals("Check that we got two participants ", 2, up.length);
            assertEquals("Check start date" , check_start_date, testobject.getStartDate());
            assertEquals("Check end date" , check_end_date, testobject.getEndDate());

            final CalendarDataObject second_update = new CalendarDataObject();
            second_update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            second_update.setObjectID(object_id);
            second_update.setStartDate(original_object.getStartDate()); // back to origin time
            second_update.setEndDate(original_object.getEndDate()); // back to origin time
            second_update.setTitle("testBug4717 - updated (2) by "+user2);
            second_update.setIgnoreConflicts(true);

            csql2.updateAppointmentObject(second_update, shared_folder_id, testobject.getLastModified());

            final CalendarDataObject testobject2 = csql2.getObjectById(object_id, shared_folder_id);
            assertEquals("Check start date" , cdao.getStartDate(), testobject2.getStartDate());
            assertEquals("Check end date" , cdao.getEndDate(), testobject2.getEndDate());

        } finally {
            try {
                if (shared_folder_id > 0) {
                    //ofa.deleteFolder(shared_folder_id, so, true, SUPER_END);
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, System.currentTimeMillis());
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

    /*
    after creating a not ending series appointment every first Monday every month,
    only the first appointment can be seen in the calendar.
     */

    public void testBug4838() throws Throwable {
        RecurringResultsInterface m = null;
        final int fid = getPrivateFolder(userid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.removeUntil();
        cdao.setTitle("testBug4838");
        cdao.setRecurrenceType(CalendarDataObject.MONTHLY);
        cdao.setInterval(1);
        cdao.setDays(CalendarDataObject.MONDAY);
        cdao.setDayInMonth(1);
        cdao.setIgnoreConflicts(true);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);

        m = new CalendarCollection().calculateRecurring(testobject, 0, 0, 0);
        assertTrue("Calculated results are > 0 ", m.size() > 1);
        int last_month = 0;
        for (int a = 0; a < m.size(); a++) {
            final RecurringResultInterface rr = m.getRecurringResult(a);
            final Calendar test = Calendar.getInstance();
            test.setFirstDayOfWeek(Calendar.MONDAY);
            final Date date = new Date(rr.getStart());
            test.setTime(date);
            if (a > 0 && last_month != 11) {
                assertEquals("Compare month", last_month+1, test.get(Calendar.MONTH));
            }
            assertEquals("Check day of month", Calendar.MONDAY, test.get(Calendar.DAY_OF_WEEK));
            last_month = test.get(Calendar.MONTH);
        }

    }

    /*
    Changing the recurrence of a weekly appointment to daily appointment removes all
    recurrence information.
     */

    public void testBug5010() throws Throwable {
        final int fid = getPrivateFolder(userid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setTitle("testBug5010");
        cdao.setRecurrenceType(CalendarDataObject.WEEKLY);
        cdao.setInterval(1);
        cdao.setDays(CalendarDataObject.MONDAY);
        cdao.setIgnoreConflicts(true);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);

        assertEquals("Check rec type ", CalendarDataObject.WEEKLY, testobject.getRecurrenceType());

        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setObjectID(object_id);
        update.setRecurrenceType(CalendarDataObject.DAILY);
        update.setInterval(1);
        update.setIgnoreConflicts(true);

        csql.updateAppointmentObject(update, fid, cdao.getLastModified());

        final CalendarDataObject testobject2 = csql.getObjectById(object_id, fid);

        assertTrue("Check that the recurring has been changed ", testobject.getRecurrenceType() != testobject2.getRecurrenceType());

        assertEquals("Check rec type ", CalendarDataObject.DAILY, testobject2.getRecurrenceType());

    }

    /*
    Create a new appointment and make it occur every 2 days. Save it. Go to
    "bearbeiten" and change it to every 3 days. Save. See that it does not change.
     */

    public void testBug5012() throws Throwable {
        final int fid = getPrivateFolder(userid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setTitle("testBug5012");
        cdao.setRecurrenceType(CalendarDataObject.DAILY);
        cdao.setInterval(1);
        cdao.setIgnoreConflicts(true);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();


        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);


        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setObjectID(object_id);
        update.setStartDate(cdao.getStartDate());
        update.setEndDate(cdao.getEndDate());
        update.setRecurrenceType(CalendarDataObject.DAILY);
        update.setInterval(2);
        update.setOccurrence(0);
        update.setIgnoreConflicts(true);

        csql.updateAppointmentObject(update, fid, cdao.getLastModified());

        final CalendarDataObject testobject2 = csql.getObjectById(object_id, fid);

        assertEquals("Check that the sequence type is identical ", testobject.getRecurrenceType(), testobject2.getRecurrenceType());
        assertFalse("Check that the interval has been changed", testobject.getInterval() == testobject2.getInterval());


        final CalendarDataObject update2 = new CalendarDataObject();
        update2.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update2.setObjectID(object_id);
        update2.setStartDate(cdao.getStartDate());
        update2.setEndDate(cdao.getEndDate());
        update2.setRecurrenceType(CalendarDataObject.DAILY);
        update2.setInterval(1);
        update2.setIgnoreConflicts(true);
        final Date check_until_date = new Date(cdao.getUntil().getTime()+new CalendarCollection().MILLI_DAY);
        update2.setUntil(check_until_date);

        csql.updateAppointmentObject(update2, fid, testobject2.getLastModified());

        final CalendarDataObject testobject3 = csql.getObjectById(object_id, fid);

        assertEquals("Check that the sequence type is identical ", testobject.getRecurrenceType(), testobject3.getRecurrenceType());
        assertTrue("Check that the interval has been changed", 1 == testobject3.getInterval());
        assertEquals("Check correct until ", check_until_date, testobject3.getUntil());

    }

    /*
    1. Login as user A
    2. Share your private calendar folder to user B
    3. Use rights: View Folder, Read all objects, Write all objects, no delete
    permission.
    4. Logout
    5. Login as user B
    6. Go to shared calendar folder of user A
    7. Create appointment
    */
    public void testBug5130() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int uid2 = resolveUser(user2);
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int fid = getPrivateFolder(userid);
        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, readcon, writecon);
        FolderObject fo = new FolderObject();

        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        final OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(uid2);
        oclp2.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.NO_PERMISSIONS);
        fo.setFolderName("testSharedFolder5130_" + String.valueOf(System.currentTimeMillis()));
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });

        int shared_folder_id = 0;
        try {
            //ofa.createFolder(fo, so, true, readcon, writecon, false);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();

            final CalendarSql csql2 = new CalendarSql(so2);
            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setParentFolderID(shared_folder_id);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug4717 - created by "+user2);
            cdao.setIgnoreConflicts(true);

            int object_id = 0;
            try {
                csql2.insertAppointmentObject(cdao);
                object_id = cdao.getObjectID();
            } catch(final OXException ope) {
                // exzellent
                ope.printStackTrace();
            }
            if (object_id > 0) {
                fail("Object could be created !");
            }
        } finally {
            try {
                if (shared_folder_id > 0) {
                    //ofa.deleteFolder(shared_folder_id, so, true, SUPER_END);
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, System.currentTimeMillis());
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

    /*
    1. As User A: Create a new appointment and invite User B
    2. As User B: Verify the appointment in the calendar
    -> The appointment is shown to User B, OK
    3. As User B: Delete the appointment from the personal calendar
    4. As User A: Verify the appointment participants.
    */
    public void testBug5144() throws Throwable {
        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int userid2 = resolveUser(user2);
        final int fid = getPrivateFolder(userid);
        final int fid2 = getPrivateFolder(userid2);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug5144");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);

        final UserParticipant userA = new UserParticipant(userid);
        final UserParticipant userB = new UserParticipant(userid2);

        cdao.setParticipants(new UserParticipant[] { userA, userB });

        final CalendarSql csql = new CalendarSql(so);
        final CalendarSql csql2 = new CalendarSql(so2);

        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject delete = new CalendarDataObject();
        delete.setContext(ContextStorage.getInstance().getContext(so2.getContextId()));
        delete.setObjectID(object_id);

        csql2.deleteAppointmentObject(delete, fid2, new Date());

        try {
            csql2.getObjectById(object_id, fid2);
            fail("User should get an OXException ");
        } catch(final OXException ope) {
            // Excellent
        }

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        final UserParticipant up[] = testobject.getUsers();
        final Participant p[] = testobject.getParticipants();

        assertEquals("Check that only one userparticipant exists", 1, up.length);
        assertEquals("Check that only one participant exists", 1, p.length);

     }

    /*
    A yearly recurring event can be set to the "first Tuesday in january".
    If so, it happens on the first Tuesday in february(!) in the year 2009 (!)
    The date is ok in 2008 and 2010.
     */
    public void testBug5202() throws Throwable {
        RecurringResultsInterface m = null;
        final int fid = getPrivateFolder(userid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.removeUntil();
        cdao.setTitle("testBug5202");
        cdao.setRecurrenceType(CalendarDataObject.YEARLY);
        cdao.setInterval(1);
        cdao.setDays(CalendarDataObject.TUESDAY);
        cdao.setMonth(Calendar.JANUARY);
        cdao.setDayInMonth(1);
        cdao.setIgnoreConflicts(true);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);

        m = new CalendarCollection().calculateRecurring(testobject, 0, 0, 0);
        assertTrue("Calculated results are > 0 ", m.size() > 1);

        for (int a = 0; a < m.size(); a++) {
            final RecurringResultInterface rr = m.getRecurringResult(a);
            final Calendar test = Calendar.getInstance();
            test.setFirstDayOfWeek(Calendar.MONDAY);
            final Date date = new Date(rr.getStart());
            test.setTime(date);
            assertEquals("Test that the day is a TUESDAY", Calendar.TUESDAY, test.get(Calendar.DAY_OF_WEEK));
            assertEquals("Test that the month is JAN", Calendar.JANUARY, test.get(Calendar.MONTH));
        }

    }


    /*
    User A:
    1. RMB the personal calendar -> "Properties"
    2. Click "Rights" and add User B
    3. Assign the following rights and save the folder permissions:

    Rechtevergabe: None
    Ordnerrechte: Objekte anlegen
    Leserechte: Alle
    Schreibrechte: Alle
    Loeschrechte: Keine

    4. Create a new appointment with title "foobar" in the personal calendar which
    is now shared to User B

    User B:
    5. Select the shared calendar of User A
    6. Select "foobar" in the shared calendar and click "Appointment" -> "Edit" in
    the panel
    7. Change the title to "foobar2" and save the appointment

    8. Verify the calendar as User A and User B
    -> "foobar" has been deleted from the personal folder of User A and now exists
    in the personal folder of User B. This must not be possible because User B has
    no delete rights to the shared calendar of User A.
     */
    public void testBug5194() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int uid2 = resolveUser(user2);
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int fid = getPrivateFolder(userid);
        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, readcon, writecon);
        FolderObject fo = new FolderObject();

        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        final OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(uid2);
        oclp2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.NO_PERMISSIONS);
        fo.setFolderName("testSharedFolder5194_" + String.valueOf(System.currentTimeMillis()));
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });

        int shared_folder_id = 0;
        try {
            //ofa.createFolder(fo, so, true, readcon, writecon, false);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();

            final CalendarSql csql = new CalendarSql(so);
            final CalendarSql csql2 = new CalendarSql(so2);

            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setParentFolderID(shared_folder_id);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug5194 - created by "+user2);
            cdao.setIgnoreConflicts(true);
            csql.insertAppointmentObject(cdao);
            final int object_id = cdao.getObjectID();

            final CalendarDataObject testobject = csql2.getObjectById(object_id, shared_folder_id);

            UserParticipant up[] = testobject.getUsers();
            assertEquals("Check that only user 1 is participant", 1, up.length);
            assertEquals("Check that only user 1 is participant", userid, up[0].getIdentifier());

            final CalendarDataObject update = new CalendarDataObject();
            update.setContext(ContextStorage.getInstance().getContext(so2.getContextId()));
            update.setObjectID(object_id);
            update.setTitle("testBug5194 - updated by "+user2);

            csql2.updateAppointmentObject(update, shared_folder_id, testobject.getLastModified());


            final CalendarDataObject testobject2 = csql.getObjectById(object_id, shared_folder_id);
            assertEquals("Check folder for user 1", shared_folder_id, testobject.getParentFolderID());


            up = testobject2.getUsers();
            assertEquals("Check that only user 1 is participant", 1, up.length);
            assertEquals("Check that only user 1 is participant", userid, up[0].getIdentifier());
            assertEquals("Check correct folder", shared_folder_id, testobject2.getParentFolderID());


            csql2.getObjectById(object_id, shared_folder_id);
            up = testobject2.getUsers();
            assertEquals("Check that only user 1 is participant", 1, up.length);
            assertEquals("Check that only user 1 is participant", userid, up[0].getIdentifier());
            assertEquals("Check correct folder", shared_folder_id, testobject2.getParentFolderID());

        } finally {
            try {
                if (shared_folder_id > 0) {
                    //ofa.deleteFolder(shared_folder_id, so, true, SUPER_END);
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, System.currentTimeMillis());
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


    /*
     Whole day apps should be visible in all views
    */
    public void testBug5222AND5171() throws Throwable {
        deleteAllAppointments();
        new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        final int fid = getPrivateFolder(userid);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug5171");
        cdao.setStartDate(new Date(1168426800000L)); // 01.2007 12:00
        cdao.setEndDate(new Date(1170154800000L)); // 30.01.2007 12:00
        cdao.setFullTime(true);
        cdao.setIgnoreConflicts(true);



        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertEquals("Check object_id", object_id, testobject.getObjectID());


        long range_start = 1168815600000L; // 15.01.2007 00:00
        long range_end = 1169420400000L; // 22.01.2007 00:00

        SearchIterator si = csql.getAppointmentsBetweenInFolder(fid, cols, new Date(range_start), new Date(range_end), 0, null);
        boolean found = false;
        while (si.hasNext()) {
            final CalendarDataObject temp = (CalendarDataObject)si.next();
            if (temp.getObjectID() == object_id) {
                assertTrue("Fulltime is set to true", temp.getFullTime());
                if (new CalendarCollection().inBetween(temp.getStartDate().getTime(), temp.getEndDate().getTime(), range_start, range_end)) {
                    found = true;
                }

            }
        }
        assertTrue("Found no appointment (testBug5141)", found);


        final CalendarDataObject cdao2 = new CalendarDataObject();
        cdao2.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao2.setParentFolderID(fid);
        cdao2.setTitle("testBug5222");
        CalendarTest.fillDatesInDao(cdao2);
        cdao2.removeUntil();
        cdao2.setFullTime(true);
        cdao2.setIgnoreConflicts(true);


        csql.insertAppointmentObject(cdao2);
        final int object_id2 = cdao2.getObjectID();

        testobject = csql.getObjectById(object_id2, fid);
        assertEquals("Check object_id2", object_id2, testobject.getObjectID());

        range_start = new CalendarCollection().normalizeLong(cdao2.getStartDate().getTime());
        range_end = range_start + new CalendarCollection().MILLI_DAY;

        si = csql.getAppointmentsBetweenInFolder(fid, cols, new Date(range_start), new Date(range_end), 0, null);

        found = false;
        while (si.hasNext()) {
            final CalendarDataObject temp = (CalendarDataObject)si.next();
            if (temp.getObjectID() == object_id2) {
                assertTrue("Fulltime is set to true", temp.getFullTime());
                if (new CalendarCollection().inBetween(temp.getStartDate().getTime(), temp.getEndDate().getTime(), range_start, range_end)) {
                    found = true;
                }

            }
        }

        assertTrue("Found no appointment (testBug5222)", found);

    }

    /*
    Recurring multi-whole-day appointments are shown only on one day
    Create a new appointment, whole-day, spaning 2 days (22.01.2007 - 24.01.2007)
    */
    public void testBug4987() throws Throwable {
        new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        final int fid = getPrivateFolder(userid);
        final CalendarSql csql = new CalendarSql(so);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        long start = cdao.getStartDate().getTime();
        start = new CalendarCollection().normalizeLong(start);
        final long end = (start + (new CalendarCollection().MILLI_DAY * 2));

        final int calculator = (int)((end-start)/new CalendarCollection().MILLI_DAY);
        assertEquals("Check Calculator result ", 2, calculator);

        cdao.setStartDate(new Date(start));
        cdao.setEndDate(new Date(end));
        cdao.setFullTime(true);
        cdao.removeUntil();
        cdao.setTitle("testBug4987");
        cdao.setRecurrenceType(CalendarDataObject.DAILY);
        cdao.setInterval(3);
        cdao.setIgnoreConflicts(true);

        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);

        final RecurringResultsInterface m = new CalendarCollection().calculateRecurring(testobject, 0, 0, 0);
        assertTrue("Calculated results are > 0 ", m.size() > 0);
        assertTrue("Fulltime is set", testobject.getFullTime());
        assertEquals("Check that the ", calculator, testobject.getRecurrenceCalculator());

        for (int a = 0; a < m.size(); a++) {
            final RecurringResultInterface rr = m.getRecurringResult(a);
            final long check_start = rr.getStart();
            final long check_end = rr.getEnd();
            final int check_calculator = (int)((check_end-check_start)/new CalendarCollection().MILLI_DAY);
            assertEquals("Check calculated results", calculator, check_calculator);
        }

    }

    public void testBug5306() throws Throwable {
        new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        final int fid = getPrivateFolder(userid);
        new CalendarSql(so);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setStartDate(D("24.12.2012 08:00"));
        cdao.setEndDate(D("24.12.2012 09:00"));
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(1);
        cdao.removeUntil();
        final int OCCURRENCE_TEST = 6;
        cdao.setOccurrence(OCCURRENCE_TEST);
        new CalendarCollection().fillDAO(cdao);
        RecurringResultsInterface m = new CalendarCollection().calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Calculated results is correct", OCCURRENCE_TEST, m.size());

        long super_start_test = new CalendarCollection().normalizeLong(m.getRecurringResult(m.size()-1).getStart());
        long super_end_test = super_start_test + new CalendarCollection().MILLI_DAY;

        m = new CalendarCollection().calculateRecurring(cdao, super_start_test, super_end_test, 0);

        assertEquals("Calculated results is correct", 1, m.size());
        assertTrue("Occurrence is set", cdao.containsOccurrence());

        super_start_test = super_start_test+new CalendarCollection().MILLI_DAY;
        super_end_test = super_end_test+new CalendarCollection().MILLI_DAY;

        new Date(super_start_test);
        new Date(super_end_test);

        final RecurringResultsInterface m2 = new CalendarCollection().calculateRecurring(cdao, super_start_test, super_end_test, 0);

        assertTrue("Should not got results", m2 == null || m2.getRecurringResult(0) == null);

        final Calendar calc = Calendar.getInstance();
        calc.setFirstDayOfWeek(Calendar.MONDAY);
        calc.setTime(cdao.getStartDate());
        calc.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        long check_week_start = calc.getTimeInMillis();
        calc.setTime(cdao.getEndDate());
        calc.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        long check_week_end = calc.getTimeInMillis();

        final RecurringResultsInterface m3 = new CalendarCollection().calculateRecurring(cdao, check_week_start, check_week_end, 0);
        final int sub_value = m3.size();

        calc.setTimeInMillis(check_week_start);
        calc.add(Calendar.WEEK_OF_YEAR, 1);
        check_week_start = calc.getTimeInMillis();
        calc.setTimeInMillis(check_week_end);
        calc.add(Calendar.WEEK_OF_YEAR, 1);

        check_week_end = calc.getTimeInMillis();

        final RecurringResultsInterface m4 = new CalendarCollection().calculateRecurring(cdao, check_week_start, check_week_end, 0);
        final int rest_value = m4.size();

        assertEquals("Calculated results is correct", OCCURRENCE_TEST, sub_value+rest_value);

    }

     /*
     As User A:
     1. Create a new appointment in your default private calendar
     2. Edit the appointment, and change the folder to the shared folder of user B
     3. Verify the shared folder of user B
     4. Verify the private calendar folder of user A

     Note: The server should not move such an appointment because the current user would be removed
     and the shared folder owner must be added as participant.
     */

     /*
      * This test is no longer valid due to bug #12923
      */
     public void noTestBug6910() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int uid2 = resolveUser(user2);
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int fid = getPrivateFolder(userid);
        final int fid2 = getPrivateFolder(uid2);

        final OXFolderManager oxma = OXFolderManager.getInstance(so2, readcon, writecon);
        FolderObject fo = new FolderObject();

        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(uid2);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        final OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(userid);
        oclp2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        fo.setFolderName("testSharedFolder6910");
        fo.setParentFolderID(fid2);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });

        int shared_folder_id = 0;
        try {
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();

            final CalendarSql csql = new CalendarSql(so);

            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setParentFolderID(fid);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug6910 - Step 1");
            cdao.setIgnoreConflicts(true);
            csql.insertAppointmentObject(cdao);
            final int object_id = cdao.getObjectID();

            CalendarDataObject testobject = csql.getObjectById(object_id, fid);

            final UserParticipant up[] = testobject.getUsers();
            assertTrue("up > 0", up.length > 0);

            final CalendarDataObject update = new CalendarDataObject();
            update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            update.setObjectID(object_id);
            update.setParentFolderID(shared_folder_id);
            update.setIgnoreConflicts(true);
            update.setTitle("testBug6910 - Step 2");

            csql.updateAppointmentObject(update, fid, cdao.getLastModified());

            final CalendarSql csql2 = new CalendarSql(so2);
            testobject = csql2.getObjectById(object_id, shared_folder_id);
            final UserParticipant user_test[] = testobject.getUsers();

            boolean found_user1 = false;
            boolean found_user2 = false;

            for (int a = 0; a < user_test.length; a++) {
                if (user_test[a].getIdentifier() == userid) {
                    found_user1 = true;
                }
                if (user_test[a].getIdentifier() == uid2) {
                    found_user2 = true;
                }
            }

            assertTrue("User A is not in the participants", found_user1);
            assertTrue("User B is not in the participants", found_user2);



        } finally {
            try {
                if (shared_folder_id > 0) {
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, System.currentTimeMillis());
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

     /*

      Precondition:
       - A main-calendar, the users personal calendar folder
       - A sub-calendar, a subfolder of the users calendar

      1. Create a new appointment series in a sub-calendar of the personal calendar

      2. Select one occurrence of the series and click "Edit" in the panel

      3. Select "Single Appointment" in the dialog popup

      4. Select another calendar folder in the edit contact window (e.g. the main
      personal calendar)

      5. Save the changes

      6. Verify the appointment sequence in the sub-calendar and the main-calendar

      -> The selected occurrence has been kept in the sub-calendar while the other
      part of the occurrence has been moved to the main-calendar. This is exactly the
      opposite of which is expected.
      */
     //TODO: check if this behaviour is still expected...
     public void _testBug6400() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");


        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int fid = getPrivateFolder(userid);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, readcon, writecon);
        FolderObject fo = new FolderObject();

        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);


        fo.setFolderName("testBug6400 - "+System.currentTimeMillis());
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1 });

        int subfolder_id = 0;
        try {
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            subfolder_id = fo.getObjectID();

            final CalendarSql csql = new CalendarSql(so);


            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setParentFolderID(subfolder_id);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug6400 - Step 1");
            cdao.setIgnoreConflicts(true);
            cdao.setRecurrenceType(CalendarObject.DAILY);
            cdao.setInterval(1);

            csql.insertAppointmentObject(cdao);
            final int object_id = cdao.getObjectID();

            final CalendarDataObject testobject = csql.getObjectById(object_id, subfolder_id);


            testobject.getStartDate();
            testobject.getEndDate();


            final CalendarDataObject update = new CalendarDataObject();
            update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            update.setTitle("testBug6400 - Step 2");
            update.setObjectID(object_id);
            update.setRecurrencePosition(1);
            update.setIgnoreConflicts(true);
            update.setParentFolderID(fid);

            try {
                csql.updateAppointmentObject(update, subfolder_id, new Date());
                update.getObjectID();
                fail("Test failed. An exception can not be moved into a different folder.");
            } catch(final OXException e) {
                // Must fail
                assertEquals("Check correct error message", 66, e.getCode());
            }


        } finally {
            try {
                if (subfolder_id > 0) {
                    oxma.deleteFolder(new FolderObject(subfolder_id), true, System.currentTimeMillis());
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

     /*

     Steps:
     1. In your private calendar "Kalendar" create a new appointment:
     Beschreibung: please don't remind me
     Beginnt am: 9:00 (assuming it's 9:30 currently)
     Endet am: 10:00
     Ort: testort
     Anmerkung: testanmerkung
     Don't set a reminder

     2. LMB-click to select the appointment "please don't remind me"
     3. LMB-click on Panel --> Kalender --> Verschieben
     4. In the popup-window select your calendar "subFol" to move it to your other
     calendar


     Result:
     After some time you see a reminder popup for the appointment "please don't
     remind me"
     */
     public void testBug6214() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");


        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int fid = getPrivateFolder(userid);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, readcon, writecon);
        FolderObject fo = new FolderObject();

        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);


        fo.setFolderName("testBug6214 "+System.currentTimeMillis());
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1 });

        int subfolder_id = 0;
        try {
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            subfolder_id = fo.getObjectID();

            final CalendarSql csql = new CalendarSql(so);


            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setParentFolderID(fid);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug6214 - Step 1");
            cdao.setIgnoreConflicts(true);


            csql.insertAppointmentObject(cdao);
            final int object_id = cdao.getObjectID();

            final CalendarDataObject update = csql.getObjectById(object_id, fid);

            final UserParticipant up_insert[] = update.getUsers();

            assertTrue("Got a participant", up_insert != null);

            assertEquals("Check that no alarm is set (insert)", false, up_insert[0].containsAlarm());

            update.setTitle("testBug6214 - Step 2");
            update.setIgnoreConflicts(true);
            update.setParentFolderID(subfolder_id);

            csql.updateAppointmentObject(update, fid, cdao.getLastModified());

            final CalendarDataObject testobject = csql.getObjectById(object_id, subfolder_id);

            final UserParticipant up_update[] = testobject.getUsers();

            assertTrue("Got a participant", up_update != null);

            assertEquals("Check that no alarm is set (update)", false, up_update[0].containsAlarm());

        } finally {
            try {
                if (subfolder_id > 0) {
                    oxma.deleteFolder(new FolderObject(subfolder_id), true, System.currentTimeMillis());
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


     public void testBug6535() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        final int fid = CalendarTest.getCalendarDefaultFolderForUser(userid, context);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);

        CalendarTest.fillDatesInDao(cdao);

        cdao.setTitle("testBug6535 - Step 1");
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(1);

        cdao.setIgnoreConflicts(true);

        final UserParticipant userparticipants = new UserParticipant(userid);
        userparticipants.setConfirm(Appointment.ACCEPT);
        cdao.setUsers(new UserParticipant[] { userparticipants });

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        final CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        final UserParticipant up1[] = testobject.getUsers();
        assertTrue("Check for null object", up1 != null);
        assertEquals("Check confirm status", CalendarObject.ACCEPT, up1[0].getConfirm());

        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setObjectID(object_id);
        update.setParentFolderID(fid);
        update.setRecurrencePosition(1);
        update.setStartDate(new Date(testobject.getStartDate().getTime() + 3600000));
        update.setEndDate(new Date(testobject.getEndDate().getTime() + 3600000));
        update.setIgnoreConflicts(true);
        update.setUsers(up1);
        update.setTitle("testBug6535 - Exception");

        csql.updateAppointmentObject(update, fid, cdao.getLastModified());

        final CalendarDataObject testobject_exception = csql.getObjectById(update.getObjectID(), fid);
        final UserParticipant up2[] = testobject_exception.getUsers();
        assertTrue("Check for null object", up2 != null);
        assertEquals("Check confirm status", CalendarObject.ACCEPT, up2[0].getConfirm());

        final CalendarDataObject testobject_after_update = csql.getObjectById(object_id, fid);
        final UserParticipant up3[] = testobject_after_update.getUsers();
        assertTrue("Check for null object", up3 != null);
        assertEquals("Check confirm status", CalendarObject.ACCEPT, up3[0].getConfirm());


     }

     /*
     * Create a recurrence appointment
     * Create an exception
     * Delete this exception
     * The "recurrence_id" xmltag is missing, so there is no way for the OXtender to
       reference the deleted item.
     */
     public void testBug6960() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        final int folder_id = CalendarTest.getCalendarDefaultFolderForUser(userid, context);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(folder_id);

        CalendarTest.fillDatesInDao(cdao);

        cdao.setTitle("testBug6960");
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(1);

        cdao.setIgnoreConflicts(true);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        cdao.getLastModified();

        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setObjectID(object_id);
        update.setIgnoreConflicts(true);


        final RecurringResultsInterface rss = new CalendarCollection().calculateRecurring(cdao, 0, 0, 3);
        final RecurringResultInterface rs = rss.getRecurringResult(0);
        final long new_start = rs.getStart()+3600000;
        final long new_end = rs.getEnd()+3600000;

        final Date test_new_start_date = new Date(new_start);
        final Date test_new_end_date = new Date(new_end);

        update.setStartDate(test_new_start_date);
        update.setEndDate(test_new_end_date);

        update.setTitle("testBug6960 - Exception");

        final Date test_exception_date = new Date(new CalendarCollection().normalizeLong(new_start));

        update.setRecurrenceDatePosition(test_exception_date);


        csql.updateAppointmentObject(update, folder_id, new Date());

        final CalendarDataObject testdelete = new CalendarDataObject();
        testdelete.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        testdelete.setObjectID(update.getObjectID());
        csql.deleteAppointmentObject(testdelete, folder_id, new Date());

        assertTrue("Check that we got the recurrence id back", testdelete.containsRecurrenceID());
        assertEquals("Check that we got the correct recurrence id", object_id, testdelete.getRecurrenceID());




     }



    /*
    1) User A creates a group appointment with User B & User C and a reminder 15 min
    2) User B click on the calendar (outlook) and receives the appointment and a
    reminder
    3) User B clicks on the reminder (close)


    This bug also test bug #8196

    */
    public void testBug7883() throws Throwable {
        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int userid2 = resolveUser(user2);
        final int fid = getPrivateFolder(userid);
        final int fid2 = getPrivateFolder(userid2);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug7883");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);

        UserParticipant userA = new UserParticipant(userid);
        userA.setAlarmMinutes(15);
        final UserParticipant userB = new UserParticipant(userid2);

        cdao.setUsers(new UserParticipant[] { userA, userB });
        cdao.setParticipants(new UserParticipant[] { userA, userB });

        final CalendarSql csql = new CalendarSql(so);
        final CalendarSql csql2 = new CalendarSql(so2);

        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        CalendarDataObject testobject = csql.getObjectById(object_id, fid);

        assertTrue("Check that userA has an alarm set in the cdao", testobject.containsAlarm());
        assertEquals("Check that we got a 15", 15, testobject.getAlarm());

        CalendarDataObject testobject2 = csql2.getObjectById(object_id, fid2);
        assertTrue("Check that userB has no alarm set in the cdao", !testobject2.containsAlarm());

        SearchIterator si = csql.getModifiedAppointmentsInFolder(fid, cols, decrementDate(cdao.getLastModified()));
        boolean found = false;
        while (si.hasNext()) {
            final CalendarDataObject tdao = (CalendarDataObject)si.next();
            if (tdao.getTitle().equals("testBug7883")) {
                found = true;
                assertTrue("Check that userA has an alarm set in the cdao", tdao.containsAlarm());
                assertEquals("Check that we got a 15", 15, tdao.getAlarm());
            }
        }
        assertTrue("Found our object (userA)", found);

        SearchIterator si2 = csql2.getModifiedAppointmentsInFolder(fid2, cols, decrementDate(cdao.getLastModified()));
        found = false;
        while (si2.hasNext()) {
            final CalendarDataObject tdao = (CalendarDataObject)si2.next();
            if (tdao.getTitle().equals("testBug7883")) {
                found = true;
                assertTrue("Check that userB has no alarm set in the cdao", !tdao.containsAlarm());
            }
        }
        assertTrue("Found our object (userB)", found);

        final CalendarDataObject cdao_update = new CalendarDataObject();
        cdao_update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao_update.setTitle("testBug7883-update");
        cdao_update.setObjectID(object_id);
        userA = new UserParticipant(userid);
        cdao_update.setAlarm(-1);
        cdao_update.setUsers(new UserParticipant[] { userA, userB });
        cdao_update.setIgnoreConflicts(true);

        csql.updateAppointmentObject(cdao_update, fid, cdao.getLastModified());

        testobject = csql.getObjectById(object_id, fid);

        assertTrue("Check that userA has no alarm set in the cdao", !testobject.containsAlarm());

        testobject2 = csql2.getObjectById(object_id, fid2);
        assertTrue("Check that userB has no alarm set in the cdao", !testobject2.containsAlarm());

        si = csql.getModifiedAppointmentsInFolder(fid, cols, decrementDate(cdao.getLastModified()));
        found = false;
        while (si.hasNext()) {
            final CalendarDataObject tdao = (CalendarDataObject)si.next();
            if (tdao.getTitle().equals("testBug7883-update")) {
                found = true;
                assertTrue("Check that userA has no alarm set in the cdao", !tdao.containsAlarm());

            }
        }
        assertTrue("Found our object (userA)", found);

        si2 = csql2.getModifiedAppointmentsInFolder(fid2, cols, decrementDate(cdao.getLastModified()));
        found = false;
        while (si2.hasNext()) {
            final CalendarDataObject tdao = (CalendarDataObject)si2.next();
            if (tdao.getTitle().equals("testBug7883-update")) {
                found = true;
                assertTrue("Check that userB has no alarm set in the cdao", !tdao.containsAlarm());
            }
        }
        assertTrue("Found our object (userB)", found);


    }

    /*
     1) Create an appointment from 2-4pm
     2) Add a resource and save
     3) Create an appointment from 1-2pm and save
     4) Edit the appointment and add resource

     --> Conflict is reported although the appointments do not overlap.
     */
    public void testBug7646() throws Throwable {

        deleteAllAppointments();

        final Context context = new ContextImpl(contextid);

        final int fid = getPrivateFolder(userid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");

        // Clean up appointments
        final FolderObject fo = new FolderObject();
        fo.setObjectID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        new RdbFolderSQLInterface(ServerSessionAdapter.valueOf(so)).clearFolder(fo, new Date());

        final CalendarSql csql = new CalendarSql(so);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setTitle("testBug7646 - #1");
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setStartDate(new Date(cdao.getStartDate().getTime()+ 3600000));
        cdao.setEndDate(new Date(cdao.getEndDate().getTime()+ 7200000));
        final Participants p = new Participants();
        final Participant resource = new ResourceParticipant(100);
        p.add(resource);
        cdao.setParticipants(p.getList());
        cdao.setIgnoreConflicts(true);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();

        assertTrue("Got an object id", object_id > 0);

        final CalendarDataObject cdao_conflict_test = new CalendarDataObject();
        cdao_conflict_test.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao_conflict_test.setTitle("testBug7646 - #2");
        cdao_conflict_test.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao_conflict_test);
        cdao_conflict_test.setIgnoreConflicts(false);

        CalendarDataObject conflicts[] = csql.insertAppointmentObject(cdao_conflict_test);
        final int object_id2 = cdao_conflict_test.getObjectID();

        assertTrue("Got an object id", object_id2 > 0);

        boolean found_object_1 = false;
        if (conflicts != null) {
            for (int a = 0; a < conflicts.length; a++) {
                if (conflicts[a].getObjectID() == object_id) {
                    found_object_1 = true;
                }
            }
        }
        assertTrue("Conflicted with object #1!", !found_object_1);

        final CalendarDataObject testobject = csql.getObjectById(object_id2, fid);
        testobject.removeStartDate();
        testobject.removeEndDate();
        testobject.setParticipants(p.getList());
        testobject.setIgnoreConflicts(false);
        conflicts = csql.updateAppointmentObject(testobject, fid, cdao_conflict_test.getLastModified());


        found_object_1 = false;
        if (conflicts != null) {
            for (int a = 0; a < conflicts.length; a++) {
                if (conflicts[a].getObjectID() == object_id) {
                    found_object_1 = true;
                }
            }
        }
        assertTrue("Conflicted with object #1!", !found_object_1);

        csql.deleteAppointmentObject(cdao, fid, cdao.getLastModified());
        csql.deleteAppointmentObject(testobject, fid, testobject.getLastModified());
    }

    /*
     Steps to reproduce (informal):
     1. Create a recurring yearly appointment "on third tuesday in april"
     2. Verify the appointment series

     Expected results:
     - Step 2: The appointment occurs every year on the third tuesday in april

     Result was:
     The appointment occurs:
     - 2008-04-15 (correct)
     - 2009-04-14 (wrong, should be 2009-04-21)
     - 2010-04-13 (wrong, should be 2010-04-20)
     - ...
     */
    public void testBug7064() throws Throwable {
        final Context context = new ContextImpl(contextid);
        SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");

        final int fid = CalendarTest.getCalendarDefaultFolderForUser(userid, context);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        cdao.setTimezone(TIMEZONE);
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug7064");

        final long startdate = 1175421600000L; // 01.04.2007 10:00 UTC
        final long enddate = 1175425200000L; // 01.04.2007 11:00 UTC
        final long testmatrix[] = { 1176804000000L, 1208253600000L, 1240308000000L, 1271757600000L };

        cdao.setStartDate(new Date(startdate));
        cdao.setEndDate(new Date(enddate));

        cdao.setRecurrenceType(CalendarDataObject.YEARLY);

        cdao.setDays(CalendarDataObject.TUESDAY);
        cdao.setDayInMonth(3);
        cdao.setMonth(Calendar.APRIL);
        cdao.setInterval(1);

        cdao.setOccurrence(testmatrix.length);

        new CalendarCollection().fillDAO(cdao);

        final RecurringResultsInterface rrs = new CalendarCollection().calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check result size", testmatrix.length, rrs.size());
        for (int a = 0; a < rrs.size(); a++) {
            final RecurringResultInterface rs = rrs.getRecurringResult(a);
            assertEquals("Check start time ("+a+")", new Date(testmatrix[a]), new Date(rs.getStart()));
        }
    }


    /*
    Create the following appointment in your private calendar.
    From 20.08.2007 Till 31.08.2007
    Wholeday



    Steps to Reproduce:
    1. Login
    2. On start page move to August on mini calendar
    3. Verify that the days 20,21,22...31 are marked bold (correct)
    4. Go to 'September' on minicalendar
    5. Verify bold days

    Actual Results:
    The 1,23,4,5,6,7 are also marked bold.!! But there are no appointments
     */
    public void testBug8290() throws Throwable {
        deleteAllAppointments();

        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");

        final int fid = CalendarTest.getCalendarDefaultFolderForUser(userid, context);

        final long teststarttime = 1188165600000L; // 27.08.2007 00:00
        final long testendtime = 1191103200000L; // 30.09.2007 00:00

        final Date teststartdate = new Date(teststarttime);
        final Date testenddate = new Date(testendtime);

        final CalendarSql csql = new CalendarSql(so);
        boolean testarray[] = csql.hasAppointmentsBetween(teststartdate, testenddate);

        boolean check_all_false = true;
        for (int a = 0; a < testarray.length; a++) {
            if (testarray[a]) {
                check_all_false = false;
            }
        }

        assertTrue("Got results, but this test works only if no results are given at start time!", check_all_false);

        final long starttime = 1187560800000L; //  20.08.2007 00:00
        final long endtime = 1188511200000L; // 31.08.2007 00:00

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        cdao.setTimezone(TIMEZONE);
        cdao.setParentFolderID(fid);
        cdao.setStartDate(new Date(starttime));
        cdao.setEndDate(new Date(endtime));
        cdao.setFullTime(true);
        cdao.setTitle("testBug8290");
        cdao.setIgnoreConflicts(true);

        csql.insertAppointmentObject(cdao);

        testarray = csql.hasAppointmentsBetween(teststartdate, testenddate);
        final boolean checkarray[] = { true, true, true, true, false, false, false, false, false, false } ;

        for (int a = 0; a < checkarray.length; a++) {
            assertEquals("Check array position "+a+" failed!", checkarray[a], testarray[a]);
        }

    }


    /*
     Steps to reproduce:
     1. Go to the Calendar module
     2. Click 'View' -> 'Calendar' in the panel
     3. Click 'Time range' -> 'Month' in the panel
     4. Navigate to the month 06-2007
     5. Double LMB on the calendar cell for 2007-06-01
     6. Enter a "3x mondays" as subject for the new appointment
     7. LMB the 'Series' button
     8. Click the 'Weekly' radio button
     9. Check the checkbox next to 'Monday'
     10. Click the 'after _ appointments' radio buttons and change the value to "3"
     11. Save the appointment
     12. Verify the appoinmtent in the calendar

     Expected Results:
     - Step 12: The appointment occurs on 2007-06-04, 2007-06-11 and 2007-06-18

     Result was:
     - Step 12: The appointment occurs on 2007-06-04 and 2007-06-11 (the last
     occurence is missing)
    */
    public void testBug7134() throws Throwable {
        deleteAllAppointments();

        final Context context = new ContextImpl(contextid);
        SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");

        final int fid = CalendarTest.getCalendarDefaultFolderForUser(userid, context);

        final long starttime = 1180692000000L; // 01.06.2007 12:00
        final long endtime = 1180695600000L; //  01.06.2007 13:00

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        cdao.setTimezone(TIMEZONE);
        cdao.setParentFolderID(fid);
        cdao.setStartDate(new Date(starttime));
        cdao.setEndDate(new Date(endtime));
        cdao.setFullTime(true);
        cdao.setTitle("testBug7134");
        cdao.setIgnoreConflicts(true);
        cdao.setRecurrenceType(CalendarDataObject.WEEKLY);
        cdao.setInterval(1);
        cdao.setDays(Appointment.MONDAY);
        cdao.setOccurrence(3);

        new CalendarCollection().fillDAO(cdao);
        final RecurringResultsInterface rrs = new CalendarCollection().calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check that we got 3 results", 3, rrs.size());


    }

    /*
     Steps to Reproduce:
     1. Login
     2. Select 'Calendar' from OX-Module
     3. From panel press 'New'
     4. Create a whole-day appointment for current day
     5. Press 'New' from panel again to create a second appointment
     6. Select '00:30' for start and '01:00' for end time and save for current day
     */
    public void testBug8317() throws Throwable {
        deleteAllAppointments();

        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");

        final int fid = CalendarTest.getCalendarDefaultFolderForUser(userid, context);

        final CalendarSql csql = new CalendarSql(so);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        cdao.setParentFolderID(fid);
        cdao.setTimezone(TIMEZONE);
        // Full day appointment must be inserted with UTC times. Otherwise day is wrong.
        Calendar calendar = TimeTools.createCalendar(TimeZone.getTimeZone("UTC"));
        // Conflicts are only found in the future.
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        // full time appointment is on tomorrow.
        cdao.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        cdao.setEndDate(calendar.getTime());
        cdao.setFullTime(true);
        cdao.setTitle("testBug8317");
        cdao.setIgnoreConflicts(true);
        csql.insertAppointmentObject(cdao);

        final int object_id = cdao.getObjectID();
        assertTrue("Got an object id", object_id > 0);

        final CalendarDataObject cdao2 = new CalendarDataObject();
        cdao2.setContext(context);
        cdao2.setParentFolderID(fid);
        cdao2.setTimezone(TIMEZONE);
        // Normal time based appointments must be inserted with local time zone.
        calendar = TimeTools.createCalendar(TimeZone.getTimeZone(TIMEZONE));
        // Conflicts are only found in the future.
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 30);
        cdao2.setStartDate(calendar.getTime());
        calendar.add(Calendar.MINUTE, 30);
        cdao2.setEndDate(calendar.getTime());
        cdao2.setTitle("testBug8317 - 2");
        final CalendarDataObject conflicts[] = csql.insertAppointmentObject(cdao2);

        assertTrue("Got no conflicts", conflicts != null && conflicts.length > 0);

        boolean found_object_1 = false;
        if (conflicts != null) {
            for (int a = 0; a < conflicts.length; a++) {
                if (conflicts[a].getObjectID() == object_id) {
                    found_object_1 = true;
                }
            }
        }
        assertTrue("No conflict with object #1!", found_object_1);

    }

    /*
     1. Create a new calendar subfolder and share it to user B with the following
     access rights:
       Admin: No
       Folder rights: see
       Read rights: all
       Modify rights: none
       Delete rights: none
     2. Create a new appointment in the just created subfolder
       Description: Test
       Start: 2007-07-18 13:00
       End: 2007-07-18 14:00
       Participants: User A, User B

     As User B:

     3. Open the calendar subfolder shared by user A
     4. Edit the appointment created by user A in this calendar
     5. Change the start and end date to 2007-07-19 and save
     6. Verify the calendar subfolder shared by user A

     Expected result: OXException
    */
    public void testBug8490() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int uid2 = resolveUser(user2);
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int fid = AppointmentBugTests.getPrivateFolder(userid);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, readcon, writecon);
        FolderObject fo = new FolderObject();

        final CalendarSql csql = new CalendarSql(so);
        final CalendarSql csql2 = new CalendarSql(so2);


        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        final OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(uid2);
        oclp2.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        fo.setFolderName("testSharedFolder8490 - "+System.currentTimeMillis());
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });

        int shared_folder_id = 0;
        try {

            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();

            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setParentFolderID(shared_folder_id);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setIgnoreConflicts(true);

            final UserParticipant up = new UserParticipant(userid);
            final UserParticipant up2 = new UserParticipant(uid2);

            cdao.setUsers(new UserParticipant[] { up, up2 });

            cdao.setTitle("testBug8490");

            csql.insertAppointmentObject(cdao);
            final int object_id = cdao.getObjectID();

            final CalendarDataObject update = new CalendarDataObject();
            update.setContext(ContextStorage.getInstance().getContext(so2.getContextId()));
            update.setObjectID(object_id);
            update.setTimezone("testBug8490- update (user2)");
            update.setIgnoreConflicts(true);

            try {
                csql2.updateAppointmentObject(update, shared_folder_id, new Date());
            } catch(final OXException oxpe) {
            }

            boolean found = false;
            final SearchIterator si = csql2.getModifiedAppointmentsInFolder(shared_folder_id, CalendarTest.cols, new Date(0));
            while (si.hasNext()) {
                final CalendarDataObject tdao = (CalendarDataObject)si.next();
                if (tdao.getObjectID() == object_id) {
                    found = true;
                    assertEquals("Check for correct folder id in response object", shared_folder_id, tdao.getParentFolderID());
                }
            }


            assertTrue("Found object", found);


        } finally {
            try {
                if (shared_folder_id > 0) {
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, System.currentTimeMillis());
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


    /*
     Steps to Reproduce:
     1. Login to groupware
     2. Navigate to your calendar
     3. Create a new private appointment via LMB-click on "Panel -> New", check the
     private checkbox, and save
     4. Edit the appointment via Double-LMB-click and change the folder-path to the
     shared calendar-folder
     5. Save the appointment

     Expected results:
     Step 4: The folder cannot be chosen in the "Select Folder" dialogue, as long
       as the appointment is a private one
    */
    public void testBug8482() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int uid2 = resolveUser(user2);
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int fid = AppointmentBugTests.getPrivateFolder(userid);
        final int fid2 = AppointmentBugTests.getPrivateFolder(uid2);

        final OXFolderManager oxma = OXFolderManager.getInstance(so2, readcon, writecon);
        FolderObject fo = new FolderObject();

        final CalendarSql csql = new CalendarSql(so);
        new CalendarSql(so2);


        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(uid2);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        final OCLPermission oclp2 = new OCLPermission();

        oclp2.setEntity(userid);
        oclp2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        fo.setFolderName("testSharedFolder8482 - "+System.currentTimeMillis());
        fo.setParentFolderID(fid2);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });

        int shared_folder_id = 0;
        try {

            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();

            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setTitle("testBug8482");
            CalendarTest.fillDatesInDao(cdao);
            cdao.setPrivateFlag(true);
            cdao.setParentFolderID(fid);
            cdao.setIgnoreConflicts(true);

            csql.insertAppointmentObject(cdao);
            final int object_id = cdao.getObjectID();

            final CalendarDataObject move = new CalendarDataObject();
            move.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            move.setObjectID(object_id);
            move.setParentFolderID(shared_folder_id);

            try {
                csql.updateAppointmentObject(move, fid, new Date());
                fail("Move not allowed");
            } catch(final OXException oxce) {
            } catch(final Exception e) {
                fail("Noooo "+e.getMessage());
            }

        } finally {
            try {
                if (shared_folder_id > 0) {
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, System.currentTimeMillis());
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

    /*
    Steps to Reproduce:
    1. Login to Outlook (OXtender started green icon in taskbar)
    2. Go to private calendar and set view to 'Week'
    3. Select a day (e.g Friday) and create a new series appointment like:
    Weekly on Friday and Saturday series ands after 10 appointments NOT whole day
    only from 8:00 - 9:00
    4. Set reminder to 'none' (DO NOT SET ANY REMINDER)
    5. Save
    6. DLMB on any appointment of the series (e.g on Saturday (the second
    appointment)
    7. Select WHOLE SERIES to edit
    8. Set reminder to 1hour for the WHOLE Series and save
    9. DLMB on any appointment of the series
    10. Select ONLY this appointment (not the whole series)
    11. Verify that 1hour is set and set the reminder to 30minutes do not change
    anything else only the reminder from 1hour to 30minutes
    12. DLMB again on any appointment of the series (NOT the exception)
    13. Verify reminder
    14. Select (DLMB) the exception where the 30minutes reminder is set and verify
    reminder
    */
    public void testBug8510() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        final int fid = AppointmentBugTests.getPrivateFolder(userid);
        final CalendarSql csql = new CalendarSql(so);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        cdao.setTimezone(TIMEZONE);
        cdao.setTitle("testBug8510");
        CalendarTest.fillDatesInDao(cdao);
        cdao.setParentFolderID(fid);
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(1);
        final int rec_reminder = 60;
        cdao.setAlarm(rec_reminder);
        cdao.setIgnoreConflicts(true);

        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        assertTrue("Got object_id", object_id > 0);

        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(context);
        update.setObjectID(object_id);
        update.setTimezone(TIMEZONE);
        final int exc_reminder = 15;
        update.setAlarm(exc_reminder);
        update.setTitle("testBug8510 - Exception 2");
        update.setIgnoreConflicts(true);
        update.setRecurrencePosition(2);

        csql.updateAppointmentObject(update, fid, new Date());
        final int object_id2 = update.getObjectID();
        assertTrue("Got new object_id", object_id2 > 0 && object_id2 != object_id);

        final SearchIterator si = csql.getModifiedAppointmentsInFolder(fid, CalendarTest.cols, cdao.getLastModified());

        int check_rec_reminder = -1;
        int check_exc_reminder = -1;

        while (si.hasNext()) {
            final CalendarDataObject tcdao = (CalendarDataObject)si.next();
            if (tcdao.getObjectID() == object_id) {
                check_rec_reminder = tcdao.getAlarm();
            } else if (tcdao.getObjectID() == object_id2){
                check_exc_reminder = tcdao.getAlarm();
            }
        }
        si.close();
        assertTrue("Got two reminder", check_exc_reminder > 0 && check_rec_reminder > 0);
        assertEquals("Recurring reminder", rec_reminder, check_rec_reminder);
        assertEquals("Excption reminder", exc_reminder, check_exc_reminder);
    }

    /*
    Steps to Reproduce:
    As User A:

    1.  Select your Calendar in the treeview
    2.  RMB -> 'New subfolder' and enter "My public write folder" as 'Name'
    3.  Select "calendar" from the 'Type' dropdown list
    4.  Click the 'Rights' tab and select 'Users' -> 'Add'
    5.  Search for "*" and add the group "sales" to the temporary user list
    6.  Click 'OK' to add the selected users to the permission table
    7.  On the permission table, select "sales" and grant the following rights:

    Administrator: No
    Ordnerrechte: Objekte anlegen
    Leserechte: Alle
    Schreibrechte: Alle
    L\u00f6schrechte: Alle

    8.  Click 'Save' on the panel to store the folder permissions
    9.  Log out and log in as User B (member of "sales")
    10.  Go to the calendar of User B in treeview and select a previously added
    appointment
    11.  Click 'Appointment' -> 'Move' on the panel and select the public folder
    created by User A as destination
    12.  Verify that the appointment is removed from the personal calendar
    13.  Select 'My public write folder' and verify the appointments in it
    14.  Drag & Drop an appointment from your calendar to the shared folder 'My
    public write folder'

    Actual Results:
    12. The appointment isn't moved no error is shown
    13. The app. wasn't moved to the folder
    14. Fehlermeldung: com.openexchange.tools.iterator.SearchIterator$1
    (FLD-9999,-215490580-513) is shown, the appointment isn't moved
    */
    public void testBug8495() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int uid2 = resolveUser(user2);
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int fid = AppointmentBugTests.getPrivateFolder(userid);
        final int fid2 = AppointmentBugTests.getPrivateFolder(uid2);

        final OXFolderManager oxma = OXFolderManager.getInstance(so, readcon, writecon);
        FolderObject fo = new FolderObject();

        final CalendarSql csql = new CalendarSql(so);
        final CalendarSql csql2 = new CalendarSql(so2);


        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);

        final OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(uid2);
        oclp2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp2.setFolderAdmin(false);

        fo.setFolderName("testSharedFolder8495 - "+System.currentTimeMillis());
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });

        int shared_folder_id = 0;
        try {

            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();

            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setTitle("testBug8495");
            CalendarTest.fillDatesInDao(cdao);
            cdao.setParentFolderID(fid);
            cdao.setIgnoreConflicts(true);

            csql.insertAppointmentObject(cdao);
            final int object_id = cdao.getObjectID();
            assertTrue("Object was created", object_id > 0);

            csql2.getObjectById(object_id, shared_folder_id);

            final CalendarDataObject move = new CalendarDataObject();
            move.setContext(ContextStorage.getInstance().getContext(so2.getContextId()));
            move.setObjectID(object_id);
            move.setParentFolderID(fid2);

            csql2.updateAppointmentObject(move, shared_folder_id, new Date());
            final CalendarDataObject moved = csql2.getObjectById(object_id, shared_folder_id);
            assertNotNull("Should find the newly moved appointment", moved);

        } finally {
            try {
                if (shared_folder_id > 0) {
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, System.currentTimeMillis());
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

    /*
    0. Assume the current time is 09:00
    1. Create a new appointment on 22:00, set a 15 Minute reminder and save
    2. Move the appointment to 09:30
    3. Wait till the reminder occurs
    -> The reminder does not pop up on 09:30
    */
    public void testBug7734() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        final int fid = AppointmentBugTests.getPrivateFolder(userid);
        final CalendarSql csql = new CalendarSql(so);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setParentFolderID(fid);
        cdao.setTimezone(TIMEZONE);
        cdao.setTitle("testBug7734");

        final int alarm = 60;
        final long long_alarm = alarm*60*1000L;

        final long start_long = cdao.getStartDate().getTime()+new CalendarCollection().MILLI_DAY;
        final Date start_date = new Date(start_long);
        final long end_long = cdao.getEndDate().getTime()+new CalendarCollection().MILLI_DAY;
        final Date end_date = new Date(end_long);
        cdao.setStartDate(start_date);
        cdao.setEndDate(end_date);
        cdao.setAlarm(alarm);

        final Date check_alarm = new Date(start_long - long_alarm);

        cdao.setIgnoreConflicts(true);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        assertTrue("Got object_id", object_id > 0);

        final ReminderHandler rh = new ReminderHandler(getContext());
        ReminderObject ro = rh.loadReminder(object_id, userid, Types.APPOINTMENT);
        final Date check_date = ro.getDate();
        assertEquals("Check correct Alarm", check_alarm, check_date);

        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(context);
        update.setObjectID(object_id);

        final long start_long_update = start_long - 3600000L;
        final Date update_start = new Date(start_long_update);
        final Date update_end = new Date(end_long - 3600000L);
        final Date check_alarm_update =  new Date(start_long_update - 3600000L);

        update.setStartDate(update_start);
        update.setEndDate(update_end);
        update.setIgnoreConflicts(true);

        csql.updateAppointmentObject(update, fid, new Date());

        ro = rh.loadReminder(object_id, userid, Types.APPOINTMENT);
        final Date check_date_update = ro.getDate();

        assertEquals("Check correct Alarm", check_alarm_update, check_date_update);

    }

    /*
    Steps to Reproduce:
    1. Create a simple appointment in a public folder
    2. Save the appointment
    3. Open the appointment for editing
    4. Activate the checkbox 'private' and save the appointment

    Actual Results:
    The appointment is saved and marked as private after editing it.

    Expected Results:
    It shouldn't be possible to save the appointment in the public folder with the
    private flag set when creating or editing it.
    */
    public void testBug9089() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);
        AppointmentBugTests.getPrivateFolder(userid);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, readcon, writecon);
        FolderObject fo = new FolderObject();
        final CalendarSql csql = new CalendarSql(so);
        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        new OCLPermission();
        fo.setFolderName("testPublicFolder9089 - "+System.currentTimeMillis());
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PUBLIC);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1 });
        int public_folder_id = 0;
        try {
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            public_folder_id = fo.getObjectID();

            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setTitle("testBug9089");
            CalendarTest.fillDatesInDao(cdao);
            cdao.setParentFolderID(public_folder_id);
            cdao.setIgnoreConflicts(true);

            csql.insertAppointmentObject(cdao);
            final int object_id = cdao.getObjectID();
            assertTrue("Object was created", object_id > 0);

            final CalendarDataObject update = new CalendarDataObject();
            update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            update.setNote("Mark as private should not worl!");
            update.setObjectID(object_id);
            update.setIgnoreConflicts(true);
            update.setPrivateFlag(true);
            try {
                csql.updateAppointmentObject(update, public_folder_id, new Date());
                fail("Set the private flag is not allowed in a public folder");
            } catch(final OXException oxca) {
                // this is what we want
                assertTrue("Check correct error number ", oxca.similarTo(OXCalendarExceptionCodes.PIVATE_FLAG_ONLY_IN_PRIVATE_FOLDER));
            }
        } finally {
            try {
                if (public_folder_id > 0) {
                    oxma.deleteFolder(new FolderObject(public_folder_id), true, System.currentTimeMillis());
                } else {
                    fail("Public folder was not created");
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

    /*
    1. Login to OX
    2. Create a new appointment and add at least 5 participants
    3. Now let the participants accept or deny this appointment and they must add a
    confirmation comment!
    4. Verify that appointment and the confirmation comments in calendar detail
    view of the appointment
    5. Press 'Edit' from panel
    6. Add 2 more (or at least one) new participant to this appointment
    7. Press 'Save' and verify the 'Detail' -> 'Participant' tab

    Expected Results:
    After you have added new participants the confirmations comments are still
    visible and not lost.
    */
    public void testBug9599() throws Throwable {
        new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        final int fid = AppointmentBugTests.getPrivateFolder(userid);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug9599");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);

        final Participants participants = new Participants();
        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int userid2 = resolveUser(user2);
        final Participant p2 = new UserParticipant(userid2);
        participants.add(p2);


        final SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestSearch");

        cdao.setParticipants(participants.getList());

        final CalendarSql csql = new CalendarSql(so);
        final CalendarSql csql2 = new CalendarSql(so2);

        csql.insertAppointmentObject(cdao);

        final int object_id = cdao.getObjectID();
        assertTrue("Object was created", object_id > 0);

        final String confirm_message = "jaja";

        final int fid2 = AppointmentBugTests.getPrivateFolder(userid2);
        csql.setUserConfirmation(object_id, fid, userid, CalendarDataObject.ACCEPT, confirm_message);
        csql2.setUserConfirmation(object_id, fid2, userid2, CalendarDataObject.ACCEPT, confirm_message);

        final String user3 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant1", "");
        final int userid3 = resolveUser(user3);
        final Participant p3 = new UserParticipant(userid3);

        participants.add(p3);

        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        update.setParticipants(participants.getList());
        update.setIgnoreConflicts(true);
        update.setObjectID(object_id);

        csql.updateAppointmentObject(update, fid, new Date());

        final CalendarDataObject temp = csql.getObjectById(object_id, fid);
        final UserParticipant up[] = temp.getUsers();

        for (int a = 0; a < up.length; a++) {
            if (up[a].getIdentifier() == userid) {
                assertEquals("Check confirm state for user "+up[a].getIdentifier(), Appointment.ACCEPT, up[a].getConfirm());
                assertEquals("Check confirm message for user "+up[a].getIdentifier(), confirm_message, up[a].getConfirmMessage());
            } else if (up[a].getIdentifier() == userid2) {
                assertEquals("Check confirm state for user "+up[a].getIdentifier(), Appointment.ACCEPT, up[a].getConfirm());
                assertEquals("Check confirm message for user "+up[a].getIdentifier(), confirm_message, up[a].getConfirmMessage());
            }
        }
    }

    /*
    Similar to bug #10061
    Steps to Reproduce:
    1. As User A, having the required access rights, create a new appointment in
    User B's personal calendar
    2. As User B, add one or more participants
    3. As User A, access the shared calendar and shift the appointment one hour
    earlier
    Actual Results:
    The participants are removed from the appointment!
    */
    public void testBug10154() throws Throwable {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");

        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int uid2 = resolveUser(user2);
        final SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");

        final String user3 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant1", "");
        final int userid3 = resolveUser(user3);

        final Participant p1 = new UserParticipant(userid);
        final Participant p2 = new UserParticipant(uid2);
        final Participant p3 = new UserParticipant(userid3);

        final Connection readcon = DBPool.pickup(context);
        final Connection writecon = DBPool.pickupWriteable(context);

        final int fid = getPrivateFolder(userid);
        final OXFolderManager oxma = OXFolderManager.getInstance(so, readcon, writecon);
        FolderObject fo = new FolderObject();

        final OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        final OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(uid2);
        oclp2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        fo.setFolderName("testSharedFolder10154 - "+System.currentTimeMillis());
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });

        int shared_folder_id = 0;
        try {
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();

            final CalendarSql csql = new CalendarSql(so);
            final CalendarSql csql2 = new CalendarSql(so2);

            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setParentFolderID(shared_folder_id);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug10154 - step 1");
            cdao.setIgnoreConflicts(true);

            csql2.insertAppointmentObject(cdao);
            final int object_id = cdao.getObjectID();
            assertTrue("Object was created", object_id > 0);

            final CalendarDataObject update = new CalendarDataObject();
            update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            final Participants participants = new Participants();
            participants.add(p1);
            participants.add(p2);
            participants.add(p3);
            update.setParticipants(participants.getList());
            update.setIgnoreConflicts(true);
            update.setObjectID(object_id);
            update.setTitle("testBug10154 - step 2");

            csql.updateAppointmentObject(update, shared_folder_id, new Date());


            final CalendarDataObject update2 = new CalendarDataObject();
            update2.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            update2.setObjectID(object_id);
            final Date check_start_date = new Date(cdao.getStartDate().getTime()+3600000); // move 1 h
            final Date check_end_date = new Date(cdao.getEndDate().getTime()+3600000); // move 1 h
            update2.setStartDate(check_start_date);
            update2.setEndDate(check_end_date);
            update2.setTitle("testBug10154 - step 3");

            csql2.updateAppointmentObject(update2, shared_folder_id, new Date());

            final CalendarDataObject temp = csql.getObjectById(object_id, shared_folder_id);
            final UserParticipant up[] = temp.getUsers();
            final int check[] = { userid, uid2, userid3 } ;
            assertEquals("Check participants", 3, check.length);

            Arrays.sort(up);
            Arrays.sort(check);

            for (int a = 0; a < check.length; a++) {
                final int x = Arrays.binarySearch(check, up[a].getIdentifier());
                if (x < 0) {
                    fail("User "+up[a].getIdentifier() + " not found!");
                }
            }

        } finally {
            try {
                if (shared_folder_id > 0) {
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, System.currentTimeMillis());
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

    /*
    When the notification subsystem creates a mail, mysteriously only the old
    participants are listed in the mail.

    1. Create an appointment with one participant.
    2. Update the appointment and add one more participant.
    3. Check if the cdao contains both participants
    4. Delete on participant
    5. Check if the cdao contains only one participant
    */
    public void testBug10717() throws Throwable {
        new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        final int fid = AppointmentBugTests.getPrivateFolder(userid);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug10717");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);

        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        assertTrue("Object was created", object_id > 0);


        final CalendarDataObject update = new CalendarDataObject();
        update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));

        Participants participants = new Participants();
        final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
        final int userid2 = resolveUser(user2);

        final Participant p1 = new UserParticipant(userid);
        participants.add(p1);

        final Participant p2 = new UserParticipant(userid2);
        participants.add(p2);

        update.setParticipants(participants.getList());
        update.setIgnoreConflicts(true);
        update.setObjectID(object_id);
        update.setTitle("testBug10154 - step 2");

        csql.updateAppointmentObject(update, fid, new Date());
        assertEquals("Check participants length", 2, update.getParticipants().length);

        final CalendarDataObject update_user_delete = new CalendarDataObject();
        update_user_delete.setContext(ContextStorage.getInstance().getContext(so.getContextId()));

        participants = new Participants();
        participants.add(p1);

        update_user_delete.setParticipants(participants.getList());
        update_user_delete.setIgnoreConflicts(true);
        update_user_delete.setObjectID(object_id);
        update_user_delete.setTitle("testBug10154 - step 2");

        csql.updateAppointmentObject(update_user_delete, fid, new Date());
        assertEquals("Check participants length", 1, update_user_delete.getParticipants().length);


    }

    /**
     * Test for <a href=
     * "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=10663">bug
     * #10663</a>:<br>
     * <ul>
     * <li>Create a recurring appointment on 2007-12-21 with its series pattern
     * set to &quot;every second moday each month&quot;</li>
     * <li>Check recurring string to match
     * <i>&quot;t|5|i|1|a|2|b|2|s|1198238400000|e|1211068800000|&quot;</i></li>
     * <li>Check number of occurrences to be equal to 5</li>
     * </ul>
     *
     * @throws Exception If an error occurs
     */
    public void testBug10663() throws Exception {
        new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(),
                "myTestSearch");
        final int fid = AppointmentBugTests.getPrivateFolder(userid);
        // Create calendar data object
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug10663");
        cdao.setIgnoreConflicts(true);
        // 21.12.2007 13:00:00 CET
        cdao.setStartDate(new Date(1198238400000L));
        // 21.12.2007 14:00:00 CET
        cdao.setEndDate(new Date(1198242000000L));
        // 18.05.2008
        cdao.setUntil(new Date(1211068800000L));
        // Recurrence calculator aka duration of a single appointment in days
        cdao.setRecurrenceCalculator(0);
        // Recurrence type: 3
        cdao.setRecurrenceType(3);
        // Interval: 1
        cdao.setInterval(1);
        // Days: 2
        cdao.setDays(2);
        // Day in month: 2
        cdao.setDayInMonth(2);

        // Create in storage
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        assertTrue("Object was created", object_id > 0);

        // Check recurrence string
        assertTrue("Unexpected recurrence string: " + cdao.getRecurrence()
                + ". Should be: t|5|i|1|a|2|b|2|s|1198238400000|e|1211068800000|",
                "t|5|i|1|a|2|b|2|s|1198238400000|e|1211068800000|".equals(cdao.getRecurrence()));

        final RecurringResultsInterface rss = new CalendarCollection().calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Unexpected number of occurrences: " + rss.size() + ". Should be: " + 5, 5, rss.size());

    }

    /**
     * Test for <a href=
     * "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=8516">bug
     * #8516</a>:<br>
     *
     * @throws Exception If an error occurs
     */
    public void testBug8516() throws Exception {
        new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(),
                "myTestSearch");
        final int fid = AppointmentBugTests.getPrivateFolder(userid);
        // Create calendar data object
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug8516");
        cdao.setIgnoreConflicts(true);
        cdao.setTimezone("America/New_York");
        //
        cdao.setStartDate(new Date(1219271400000L));
        //
        cdao.setEndDate(new Date(1219271400000L + Constants.MILLI_HOUR));
        //
        cdao.setUntil(new Date(1219449600000L));
        // Recurrence calculator aka duration of a single appointment in days
        cdao.setRecurrenceCalculator(0);
        // Recurrence type: 1
        cdao.setRecurrenceType(1);
        // Interval: 1
        cdao.setInterval(1);

        // Create in storage
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        assertTrue("Object was created", object_id > 0);

        final RecurringResultsInterface rss = new CalendarCollection().calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Unexpected number of occurrences: " + rss.size() + ". Should be: " + 4, 4, rss.size());
    }

    /**
     * Test for <a href=
     * "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=9823">bug
     * #9823</a>:<br>
     *
     * @throws Exception If an error occurs
     */
    public void testBug9823() throws Exception {
        new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(),
                "myTestSearch");
        final int fid = AppointmentBugTests.getPrivateFolder(userid);
        // Create calendar data object
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug9823");
        cdao.setIgnoreConflicts(true);
        cdao.setTimezone(TIMEZONE);
        //
        cdao.setStartDate(new Date(1193011200000L));
        //
        cdao.setEndDate(new Date(1193270400000L));

        // Recurrence calculator aka duration of a single appointment in days
        cdao.setRecurrenceCalculator(3);
        // Recurrence type: 1
        cdao.setRecurrenceType(1);
        // Interval: 1
        cdao.setInterval(7);
        // Occurrences
        cdao.setOccurrence(2);
        // Full-time
        cdao.setFullTime(true);

        // Create in storage
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        assertTrue("Object was created", object_id > 0);

        final RecurringResultsInterface rss = new CalendarCollection().calculateRecurring(cdao, 1193176800000L, 1193263200000L, 0);
        assertEquals("Unexpected number of occurrences: " + rss.size() + ". Should be: " + 1, 1, rss.size());
    }

    /**
     * Test for <a href=
     * "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=11719">bug
     * #11719</a>:<br>&quot;<i>Last day of a recurring appointment is dropped</i>&quot;
     *
     * @throws Exception If an error occurs
     */
    public void testBug11719() throws Exception {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(),
                "testBug11719");
        final int fid = AppointmentBugTests.getPrivateFolder(userid);
        // Create calendar data object
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug11719");
        cdao.setIgnoreConflicts(true);
        cdao.setTimezone(TIMEZONE);
        // 28.07.2008 00:00h
        cdao.setStartDate(new Date(1217203200000L));
        // 29.07.2008 00:00h
        cdao.setEndDate(new Date(1217289600000L));
        // Full-time
        cdao.setFullTime(true);
        // Recurrence calculator aka duration of a single appointment in days
        cdao.setRecurrenceCalculator(1);
        // Recurrence type: 1
        cdao.setRecurrenceType(1);
        // Interval: 1
        cdao.setInterval(1);
        // Until 03.08.2008
        cdao.setUntil(new Date(1217721600000L));

        // Create in storage
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        assertTrue("Object was not created", object_id > 0);

        try {
            // Load from storage
            final CalendarDataObject edao = csql.getObjectById(object_id, fid);
            assertTrue("Loading from storage failed", edao != null);

            // edao should denote the first occurrence in recurring appointment
            // Start time: 1217203200000l = 28.07.2008 00:00h
            assertEquals("Unexpected start time in first occurrence", 1217203200000L, edao.getStartDate().getTime());

            // End time: 1217289600000l = 29.07.2008 00:00h
            assertEquals("Unexpected end time in first occurrence", 1217289600000L, edao.getEndDate().getTime());

            // Calculate last occurrence
            final RecurringResultsInterface rrs = new CalendarCollection().calculateRecurring(edao, 0, 0, 0, CalendarCollection.MAX_OCCURRENCESE, true);
            final RecurringResultInterface rr = rrs.getRecurringResultByPosition(rrs.size());
            assertTrue("Calculated last occurrence is null", rr != null);

            // Last occurrence should be on 03.08.2008: 1217721600000
            assertEquals("Unexpected last occurrence's start time: ", 1217721600000L, rr.getStart());

            // Check end date which should be 04.08.2008: 1217808000000
            assertEquals("Unexpected last occurrence's end time: ", 1217808000000L, rr.getEnd());
        } finally {
            hardDelete(object_id, context);
        }
    }

    /**
     * Test for <a href=
     * "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=11695">bug
     * #11695</a>:<br>&quot;<i>Recurring whole day appointment isn't created correctly</i>&quot;
     *
     * @throws Exception If an error occurs
     */
    public void testBug11695() throws Exception {
        final Context context = new ContextImpl(contextid);
        final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(),
                "testBug11695");
        final int fid = AppointmentBugTests.getPrivateFolder(userid);
        // Create calendar data object
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug11695");
        cdao.setIgnoreConflicts(true);
        cdao.setTimezone(TIMEZONE);
        // 22.09.2008 12:00h
        cdao.setStartDate(new Date(1222084800000L));
        // 22.09.2008 13:00h
        cdao.setEndDate(new Date(1222088400000L));
        // No full-time
        cdao.setFullTime(false);
        // Recurrence calculator aka duration of a single appointment in days
        cdao.setRecurrenceCalculator(0);
        // Recurrence type: 1
        cdao.setRecurrenceType(2);
        // Interval: 1
        cdao.setInterval(1);
        // days: Mo - Fr
        cdao.setDays(62);
        // Until 29.09.2008 00:00h
        cdao.setUntil(new Date(1222646400000L));

        // Create in storage
        final CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        final int object_id = cdao.getObjectID();
        assertTrue("Object was not created", object_id > 0);

        try {
            // Load from storage
            final CalendarDataObject edao = csql.getObjectById(object_id, fid);
            assertTrue("Loading from storage failed", edao != null);

            // edao should denote the first occurrence in recurring appointment
            // Start time: 1217203200000l = 28.07.2008 00:00h
            assertEquals("Unexpected start time in first occurrence", 1222084800000L, edao.getStartDate().getTime());

            // End time: 1217289600000l = 29.07.2008 00:00h
            assertEquals("Unexpected end time in first occurrence", 1222088400000L, edao.getEndDate().getTime());

            // Calculate last occurrence
            final RecurringResultsInterface rrs = new CalendarCollection().calculateRecurring(edao, 0, 0, 0, CalendarCollection.MAX_OCCURRENCESE, true);
            assertTrue("Calculating recurrence failed", rrs != null);
            assertEquals("Unexpected number of occurrences: ", 6, rrs.size());

            final RecurringResultInterface rr = rrs.getRecurringResultByPosition(rrs.size());
            assertTrue("Calculated last occurrence is null", rr != null);

            // Last occurrence should be on 29.09.2008 12:00h: 1222689600000
            assertEquals("Unexpected last occurrence's start time: ", 1222689600000L, rr.getStart());

            // Check end date which should be 29.09.2008 13:00h: 1222693200000
            assertEquals("Unexpected last occurrence's end time: ", 1222693200000L, rr.getEnd());
        } finally {
            hardDelete(object_id, context);
        }
    }

    /**
     * Test for <a href=
     * "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=11881">bug
     * #11881</a>:<br>
     * &quot;<i>It is possible to create a private appointment with one or more
     * participants</i>&quot;
     *
     * @throws Exception
     *             If an error occurs
     */
    // Invalidated due to US 29162801: "As a calendar user I want to have other participants in a private appointment."
    public void _testBug11881() throws Exception {
        final Context context = new ContextImpl(contextid);
        int object_id = -1;
        try {
            final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(),
                    "myTestSearch");
            final int fid = AppointmentBugTests.getPrivateFolder(userid);

            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
            cdao.setParentFolderID(fid);
            cdao.setTitle("testBug11881");
            cdao.setIgnoreConflicts(true);
            CalendarTest.fillDatesInDao(cdao);

            final Participants participants = new Participants();
            final String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");
            final int userid2 = resolveUser(user2);

            final Participant p1 = new UserParticipant(userid);
            participants.add(p1);

            final Participant p2 = new UserParticipant(userid2);
            participants.add(p2);

            cdao.setParticipants(participants.getList());

            final CalendarSql csql = new CalendarSql(so);
            csql.insertAppointmentObject(cdao);
            object_id = cdao.getObjectID();
            assertTrue("Object creation failed", object_id > 0);

            final CalendarDataObject update = new CalendarDataObject();
            update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));

            update.setIgnoreConflicts(true);
            update.setObjectID(object_id);
            update.setTitle("testBug11881 - step 2");
            update.setPrivateFlag(true);

            try {
                csql.updateAppointmentObject(update, fid, new Date());
                fail("Update successfull although private appointment contains multiple participants");
            } catch (final OXException e) {
                assertTrue(e.similarTo(OXCalendarExceptionCodes.PRIVATE_FLAG_AND_PARTICIPANTS));
            }
        } finally {
            if (object_id != -1) {
                hardDelete(object_id, context);
            }
        }
    }

    /**
     * Test for <a href=
     * "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12045">bug
     * #12045</a>:<br>
     * &quot;<i>Reminder settings won't be updated when moving series
     * appointment to another folder</i>&quot;
     *
     * @throws Exception
     *             If an error occurs
     */

    //TODO: check if this behaviour is still expected...
    public void _testBug12045() throws Exception {
        int object_id = -1;
        try {
            final SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(),
                    "myTestSearch");

//            final int userid2 = resolveUser(AbstractConfigWrapper.parseProperty(getAJAXProperties(),
//                    "user_participant3", ""));

            final int fid = AppointmentBugTests.getPrivateFolder(userid);

            final long start;
            final long end;
            {
                long l = System.currentTimeMillis();
                l = (l - (l % Constants.MILLI_HOUR)) + Constants.MILLI_HOUR;
                start = l;
                end = start + Constants.MILLI_HOUR;
            }


            final CalendarSql csql = new CalendarSql(so);
            {
                /*
                 * Create recurring appointment with reminder
                 */
                final CalendarDataObject cdao = new CalendarDataObject();
                cdao.setContext(ContextStorage.getInstance().getContext(so.getContextId()));
                cdao.setParentFolderID(fid);
                cdao.setTitle("testBug12045");
                cdao.setIgnoreConflicts(true);

                cdao.setStartDate(new Date(start));
                cdao.setEndDate(new Date(end));
                cdao.setFullTime(false);

                cdao.setShownAs(1);

                cdao.setAlarm(15);
                cdao.setNotification(true);

                cdao.setRecurrenceType(1);
                cdao.setInterval(1);
                cdao.setOccurrence(3);

//                {
//                    final Participants participants = new Participants();
//
//                    final Participant p1 = new UserParticipant(userid);
//                    participants.add(p1);
//
//                    final Participant p2 = new UserParticipant(userid2);
//                    participants.add(p2);
//
//                    cdao.setParticipants(participants.getList());
//                }

                csql.insertAppointmentObject(cdao);
                object_id = cdao.getObjectID();
                assertTrue("Object creation failed", object_id > 0);
            }

            /*
             * Check reminder
             */
            final int reminderId;
            {
                final ReminderObject ro = new ReminderHandler(getContext()).loadReminder(object_id, userid,
                        Types.APPOINTMENT);
                assertTrue("Folder ID mismatch in reminder", ro.getFolder() == fid);
                reminderId = ro.getObjectId();
            }

            /*
             * Create a temporary folder for move operation
             */
            final OXFolderManager folderManager = OXFolderManager.getInstance(so);
            final FolderObject fo = new FolderObject();
            fo.setFolderName("TestFolder-testBug12045-" + String.valueOf(System.currentTimeMillis()));
            fo.setParentFolderID(fid);
            fo.setModule(FolderObject.CALENDAR);
            fo.setType(FolderObject.PRIVATE);
            {
                final OCLPermission p = new OCLPermission(userid, fid);
                p.setFolderAdmin(true);
                p.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                fo.setPermissionsAsArray(new OCLPermission[] { p });
            }
            folderManager.createFolder(fo, true, System.currentTimeMillis());
            assertTrue("Folder creation failed", fo.getObjectID() > 0);
            try {
                /*
                 * Move that appointment to another folder
                 */
                final CalendarDataObject update = new CalendarDataObject();
                update.setContext(ContextStorage.getInstance().getContext(so.getContextId()));

                update.setIgnoreConflicts(true);
                update.setObjectID(object_id);
                update.setTitle("testBug12045 - step 2");

                update.setStartDate(new Date(start));
                update.setEndDate(new Date(end));

                update.setNotification(true);

                update.setParentFolderID(fo.getObjectID());

                csql.updateAppointmentObject(update, fid, new Date());

                final ReminderObject ro = new ReminderHandler(getContext()).loadReminder(reminderId);
                assertTrue("Reminder's folder ID not updated properly", ro.getFolder() == fo.getObjectID());
            } finally {
                try {
                    new ReminderHandler(getContext()).deleteReminder(object_id, userid, Types.APPOINTMENT);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                try {
                    folderManager.deleteFolder(fo, true, System.currentTimeMillis());
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            if (object_id != -1) {
                hardDelete(object_id, getContext());
            }
        }
    }
}
