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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.groupware.calendar;

import static com.openexchange.groupware.calendar.tools.CalendarAssertions.assertResourceParticipants;
import static com.openexchange.groupware.calendar.tools.CalendarAssertions.assertUserParticipants;
import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import static com.openexchange.tools.events.EventAssertions.assertModificationEventWithOldObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.request.AppointmentRequest;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.database.Database;
import com.openexchange.group.Group;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.tools.CalendarContextToolkit;
import com.openexchange.groupware.calendar.tools.CalendarFolderToolkit;
import com.openexchange.groupware.calendar.tools.CalendarTestConfig;
import com.openexchange.groupware.calendar.tools.CommonAppointments;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.session.ServerSession;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CalendarSqlTest extends TestCase {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(CalendarSqlTest.class);
    
    private final List<CalendarDataObject> clean = new ArrayList<CalendarDataObject>();
    private final List<FolderObject> cleanFolders = new ArrayList<FolderObject>();

    private String participant1, participant2, participant3;
    private String resource1, resource2, resource3;

    private String group,member;

    private String user;
    private String secondUser;

    private int userId, secondUserId;

    private Context ctx;
    private CommonAppointments appointments;
    private CalendarFolderToolkit folders;

    private Session session;
    private TestResult result;

    @Override
	public void setUp() throws Exception {
        Init.startServer();

        TestEventAdmin.getInstance().clearEvents();

        final CalendarTestConfig config = new CalendarTestConfig();

        user = config.getUser();
        secondUser = config.getSecondUser();

        final CalendarContextToolkit tools = new CalendarContextToolkit();
        ctx = tools.getDefaultContext();

        appointments = new CommonAppointments(ctx, user);

        participant1 = config.getParticipant1();
        participant2 = config.getParticipant2();
        participant3 = config.getParticipant3();

        resource1 = config.getResource1();
        resource2 = config.getResource2();
        resource3 = config.getResource3();

        folders = new CalendarFolderToolkit();

        group = config.getGroup();
        final int groupid = tools.resolveGroup(group, ctx);
        final Group group = tools.loadGroup(groupid, ctx);
        final int memberid = group.getMember()[0];
        member = tools.loadUser(memberid, ctx).getLoginInfo();

        userId = tools.resolveUser(user, ctx);
        secondUserId = tools.resolveUser(secondUser, ctx);

        appointments.deleteAll(ctx);

        session = tools.getSessionForUser(user, ctx);
    }

    @Override
	public void tearDown() throws OXException, SQLException {
        appointments.removeAll(user, clean);
        folders.removeAll(session, cleanFolders);
        Init.stopServer();
    }

    // Bug #11148
    public void testUpdateWithInvalidRecurrencePatternShouldFail() throws OXException, SQLException {
        final CalendarDataObject cdao = appointments.buildRecurringAppointment();
        appointments.save( cdao );
        clean.add( cdao );
        final CalendarDataObject modified = new CalendarDataObject();
        modified.setStartDate(cdao.getStartDate());
        modified.setEndDate(cdao.getEndDate());
        modified.setObjectID(cdao.getObjectID());
        modified.setParentFolderID(cdao.getParentFolderID());
        modified.setRecurrenceType(CalendarDataObject.MONTHLY);
        modified.setInterval(1);
        modified.setDays(CalendarObject.TUESDAY);
        modified.setDayInMonth(666); // Must be between 1 and 5, usually, so 666 is invalid
        modified.setContext(cdao.getContext());
        try {
            appointments.save( modified );
            fail("Could save invalid dayInMonth value");
        } catch (final OXException x) {
            // Passed. The invalid value wasn't accepted.
        }
    }

    // Bug #11148
    public void testShouldRebuildEntireRecurrencePatternOnUpdate() throws SQLException, OXException {
        final CalendarDataObject cdao = appointments.buildRecurringAppointment();
        appointments.save( cdao );
        clean.add( cdao );

        final CalendarDataObject modified = new CalendarDataObject();
        modified.setObjectID(cdao.getObjectID());
        modified.setParentFolderID(cdao.getParentFolderID());
        modified.setContext(cdao.getContext());
        modified.setRecurrenceType(CalendarDataObject.MONTHLY);
        modified.setDayInMonth(12);
        modified.setInterval(2);
        modified.setOccurrence(3); // Every 12th of every 2nd month for 3 appointments

        try {
            appointments.save( modified );

            final CalendarDataObject reloaded = appointments.reload( modified );

            assertEquals(0, reloaded.getDays());
            assertEquals(12, reloaded.getDayInMonth());
            assertEquals(2, reloaded.getInterval());
            assertEquals(CalendarObject.MONTHLY, reloaded.getRecurrenceType());
        } catch (final OXException x) {
            x.printStackTrace();
            fail(x.toString());
        } catch (final SQLException x) {
            x.printStackTrace();
            fail(x.toString());
        }
    }

    // Bug #11148
    public void testShouldOnlyUpdateRecurrencePatternIfNeeded() throws SQLException, OXException {
        final CalendarDataObject cdao = appointments.buildRecurringAppointment();
        appointments.save( cdao );
        clean.add( cdao );

        final CalendarDataObject modified = new CalendarDataObject();
        modified.setObjectID(cdao.getObjectID());
        modified.setParentFolderID(cdao.getParentFolderID());
        modified.setContext(cdao.getContext());
        modified.setLocation("updated location");

        try {
            appointments.save( modified );

            final CalendarDataObject reloaded = appointments.reload( modified );

            assertEquals(cdao.getDays(), reloaded.getDays());
            assertEquals(cdao.getDayInMonth(), reloaded.getDayInMonth());
            assertEquals(cdao.getInterval(), reloaded.getInterval());
            assertEquals(cdao.getRecurrenceType(), reloaded.getRecurrenceType());
        } catch (final OXException x) {
            x.printStackTrace();
            fail(x.toString());
        } catch (final SQLException x) {
            x.printStackTrace();
            fail(x.toString());
        }
    }

    // Bug #11148
    public void testShouldSurviveLoadingInvalidPattern() throws SQLException, OXException, DBPoolingException {
        final CalendarDataObject cdao = appointments.buildRecurringAppointment();
        
        appointments.save( cdao );
        clean.add( cdao );

        invalidatePattern( cdao );

        try {
            final CalendarDataObject reloaded = appointments.reload( cdao );
            assertTrue(reloaded.getRecurrenceType() == CalendarDataObject.NO_RECURRENCE);
            assertTrue(reloaded.getEndDate().getTime() < System.currentTimeMillis()+240*3600000);
            // Check load by list requests


            final AppointmentSQLInterface sqlInterface = appointments.getCurrentAppointmentSQLInterface();
            final Date SUPER_START = new Date(-2);
            final Date SUPER_END = new Date(Long.MAX_VALUE);
            final int[] COLS = new int[]{CalendarDataObject.OBJECT_ID, CalendarDataObject.START_DATE, CalendarDataObject.END_DATE, CalendarDataObject.FOLDER_ID, CalendarDataObject.USERS};
            final int USER_ID = userId;
            final int FOLDER_ID = cdao.getParentFolderID();
            SearchIterator iter = sqlInterface.getActiveAppointments(USER_ID, SUPER_START, SUPER_END, COLS);
            assertContains(iter, cdao);

            iter = sqlInterface.getAppointmentsBetween(USER_ID, SUPER_START, SUPER_END, COLS, AppointmentObject.OBJECT_ID, null);
            assertContains(iter, cdao);

            iter = sqlInterface.getAppointmentsBetweenInFolder(FOLDER_ID, COLS, SUPER_START, SUPER_END, AppointmentObject.OBJECT_ID, null);
            assertContains(iter, cdao);

            final AppointmentSearchObject search = new AppointmentSearchObject();
            search.setFolder(cdao.getParentFolderID());
            search.setPattern("*");
            iter = sqlInterface.getAppointmentsByExtendedSearch(search, AppointmentObject.OBJECT_ID,  null, COLS);
            assertContains(iter, cdao);

            iter = sqlInterface.getFreeBusyInformation(USER_ID,Participant.USER, SUPER_START, SUPER_END);
            assertContains(iter, cdao);

            iter = sqlInterface.getModifiedAppointmentsBetween(USER_ID, SUPER_START, SUPER_END, COLS, SUPER_START, AppointmentObject.OBJECT_ID, null);
            assertContains(iter, cdao);

            iter = sqlInterface.getObjectsById(new int[][] {{cdao.getObjectID(), cdao.getParentFolderID()}}, COLS);
            assertContains(iter, cdao);

            iter = sqlInterface.searchAppointments("*", cdao.getParentFolderID(), AppointmentObject.OBJECT_ID ,null, COLS);
            assertContains(iter, cdao);

            sqlInterface.hasAppointmentsBetween(SUPER_START, new Date(SUPER_START.getTime() + 3600000l * 24 * 30));

            // Check AppointmentRequest interface methods

            final StringBuilder cols = new StringBuilder();
            for(final int col : COLS) { cols.append(col).append(","); }
            cols.setLength(cols.length()-1);
            final String COLS_STRING = cols.toString();

            // ALL
            final AppointmentRequest req = new AppointmentRequest(session, ctx);
            JSONObject requestData = json(
                    AJAXServlet.PARAMETER_COLUMNS, COLS_STRING,
                    AJAXServlet.PARAMETER_FOLDERID, String.valueOf(cdao.getParentFolderID()),
                    AJAXServlet.PARAMETER_START, String.valueOf(SUPER_START.getTime()),
                    AJAXServlet.PARAMETER_END, String.valueOf(SUPER_END.getTime())
            );
            JSONArray arr = req.actionAll(requestData);
            assertContains(arr, cdao);

            // Recurrence Master
            requestData.put(AppointmentRequest.RECURRENCE_MASTER, true);
            arr = req.actionAll(requestData);
            assertContains(arr, cdao);

            // Freebusy
            arr = req.actionFreeBusy(json(
                    AJAXServlet.PARAMETER_ID, String.valueOf(userId),
                    "type", String.valueOf(Participant.USER),
                    AJAXServlet.PARAMETER_START, String.valueOf(SUPER_START.getTime()),
                    AJAXServlet.PARAMETER_END, String.valueOf(SUPER_END.getTime())
            ));
            assertContainsAsJSONObject(arr, cdao);

            // Get
            final JSONObject loaded = req.actionGet(json(
                    AJAXServlet.PARAMETER_ID, String.valueOf(cdao.getObjectID()),
                    AJAXServlet.PARAMETER_FOLDERID, String.valueOf(cdao.getParentFolderID())
            ));
            assertEquals(loaded.getInt("id"), cdao.getObjectID());

            // Has
            req.actionHas(json(
                    AJAXServlet.PARAMETER_START, String.valueOf(SUPER_START.getTime()),
                    AJAXServlet.PARAMETER_END, String.valueOf(SUPER_START.getTime() + 3600000l * 24 * 30)
            ));

            // List
            final JSONArray idArray = new JSONArray();
            idArray.put(json(AJAXServlet.PARAMETER_ID, cdao.getObjectID(), AJAXServlet.PARAMETER_FOLDERID, cdao.getParentFolderID()));
            final JSONObject jsonRequest = json(
                    AJAXServlet.PARAMETER_COLUMNS, COLS_STRING,
                    AJAXServlet.PARAMETER_DATA, idArray
            );
            arr = req.actionList(jsonRequest);
            assertContains(arr, cdao);
            // Recurrence Master
            jsonRequest.put(AppointmentRequest.RECURRENCE_MASTER, true);
            arr = req.actionList(jsonRequest);
            assertContains(arr, cdao);

            // New Appointments Search
            arr = req.actionNewAppointmentsSearch(json(
                    AJAXServlet.PARAMETER_COLUMNS, COLS_STRING,
                    AJAXServlet.PARAMETER_START, String.valueOf(SUPER_START.getTime()),
                    AJAXServlet.PARAMETER_END, String.valueOf(SUPER_END.getTime()),
                    "limit" , Integer.MAX_VALUE
            ));
            assertContains(arr, cdao);

            // Search
            requestData = json(
                    AJAXServlet.PARAMETER_COLUMNS, COLS_STRING,
                    AJAXServlet.PARAMETER_DATA, json(
                            SearchFields.PATTERN, "*",
                            AJAXServlet.PARAMETER_INFOLDER, cdao.getParentFolderID()
                    ),
                    AJAXServlet.PARAMETER_SORT, AppointmentObject.START_DATE,
                    AJAXServlet.PARAMETER_ORDER, "ASC"
            );
            arr = req.actionSearch(requestData);
            assertContains(arr, cdao);

            // With start and end date
            requestData.put(AJAXServlet.PARAMETER_START, SUPER_START.getTime());
            requestData.put(AJAXServlet.PARAMETER_END, SUPER_END.getTime());
            arr = req.actionSearch(requestData);
            assertContains(arr, cdao);

            // Recurrence Master
            requestData.remove(AJAXServlet.PARAMETER_START);
            requestData.remove(AJAXServlet.PARAMETER_END);
            requestData.put(AppointmentRequest.RECURRENCE_MASTER, true);

            arr = req.actionSearch(requestData);
            assertContains(arr, cdao);

            requestData.put(AJAXServlet.PARAMETER_START, SUPER_START.getTime());
            requestData.put(AJAXServlet.PARAMETER_END, SUPER_END.getTime());
            arr = req.actionSearch(requestData);
            assertContains(arr, cdao);

            // Updates

            requestData = json(
                    AJAXServlet.PARAMETER_COLUMNS, COLS_STRING,
                    AJAXServlet.PARAMETER_START, String.valueOf(SUPER_START.getTime()),
                    AJAXServlet.PARAMETER_END, String.valueOf(SUPER_END.getTime()),
                    AJAXServlet.PARAMETER_TIMESTAMP, 0,
                    AJAXServlet.PARAMETER_FOLDERID, cdao.getParentFolderID()
            );
            arr = req.actionUpdates(requestData);
            assertContains(arr, cdao);

            // Recurrence Master
            requestData.put(AppointmentRequest.RECURRENCE_MASTER, true);
            arr = req.actionUpdates(requestData);
            assertContains(arr, cdao);
            

        } catch (final Exception x) {
            x.printStackTrace();
            fail(x.toString());
        }


    }

    private JSONObject json(final Object...objects) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        for(int i = 0; i < objects.length; i++) {
            jsonObject.put(objects[i++].toString(), objects[i]);
        }
        return jsonObject;
    }

    private void assertContains(final SearchIterator iter, final CalendarDataObject cdao) throws OXException, SearchIteratorException {
        boolean found = false;
        while(iter.hasNext()) {
            final CalendarDataObject cdao2 = (CalendarDataObject)iter.next();
            found = found || cdao.getObjectID() == cdao2.getObjectID();
        }
        assertTrue(found);
    }

    private void assertContains(final JSONArray arr, final CalendarDataObject cdao) throws JSONException {
        for(int i = 0, size = arr.length(); i < size; i++) {
            final JSONArray row = arr.getJSONArray(i);
            if(row.getInt(0) == cdao.getObjectID()) {
                return;
            }
        }
        fail("Could not find appointment in respone: "+arr);
    }

    private void assertContainsAsJSONObject(final JSONArray arr, final CalendarDataObject cdao) throws JSONException {
        for(int i = 0, size = arr.length(); i < size; i++) {
            final JSONObject row = arr.getJSONObject(i);
            if(row.getInt("id") == cdao.getObjectID()) {
                return;
            }
        }
        fail("Could not find appointment in respone: "+arr);
    }


    private void invalidatePattern(final CalendarDataObject cdao) throws DBPoolingException {
        Connection con = null;
        PreparedStatement pstmt = null;

        final String invalidPattern = "t|6|i|1|a|32|b|21|c|3|s|"+ (System.currentTimeMillis()+240*3600000) +"|";
        try {
            con = Database.get(ctx, true);
            pstmt = con.prepareStatement("UPDATE prg_dates SET field06 = ? WHERE intfield01 = ?");
            pstmt.setString(1, invalidPattern);
            pstmt.setInt(2, cdao.getObjectID());
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if(pstmt != null) {
                try {
                    pstmt.close();
                } catch (final SQLException e) {
                    // IGNORE
                }
            }
            if(con != null) {
                Database.back(ctx, true, con);
            }
        }
    }


    // Node 1077
    public void testShouldSupplyConflictingUserParticipants() throws SQLException, OXException {
        final CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user, participant1, participant2);
        appointments.save( appointment ); clean.add( appointment );
        final CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithUserParticipants(user, participant1, participant3);
        conflictingAppointment.setIgnoreConflicts(false);
        final CalendarDataObject[] conflicts = appointments.save( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        final CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getObjectID(), conflict.getObjectID());
        assertUserParticipants(conflict, user, participant1);
    }

    // Node 1077
    public void testShouldSupplyConflictingResourceParticipants() throws SQLException, OXException {
        final CalendarDataObject appointment = appointments.buildAppointmentWithResourceParticipants(resource1, resource2);
        appointments.save( appointment );  clean.add( appointment );
        final CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithResourceParticipants(resource1, resource3);
        conflictingAppointment.setIgnoreConflicts(false);
        final CalendarDataObject[] conflicts = appointments.save( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        final CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getObjectID(), conflict.getObjectID());
        assertResourceParticipants(conflict, resource1);
    }

    // Node 1077
    public void testShouldSupplyConflictingUserParticipantsInGroup() throws OXException {
        final CalendarDataObject appointment = appointments.buildAppointmentWithGroupParticipants(group);
        appointments.save( appointment ); clean.add( appointment );

        final CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithUserParticipants( member );
        conflictingAppointment.setIgnoreConflicts( false );

        final CalendarDataObject[] conflicts = appointments.save( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        final CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getObjectID(), conflict.getObjectID());
        assertUserParticipants(conflict, user, member ); // Current User is added by default, so always conflicts
    }

    // Node 1077
    public void testShouldSupplyTitleIfPermissionsAllowIt() throws OXException {
        final CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(participant1, participant2);
        appointments.save( appointment ); clean.add( appointment );
        final CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithUserParticipants(participant1, participant3);
        conflictingAppointment.setIgnoreConflicts(false);
        final CalendarDataObject[] conflicts = appointments.save( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        final CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getTitle(), conflict.getTitle());
    }

    // Node 1077
    public void testShouldSuppressTitleIfPermissionsDenyIt() throws OXException {
        final CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(participant1, participant2);
        appointments.save( appointment ); clean.add( appointment );

        appointments.switchUser( secondUser );

        final CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithUserParticipants(participant1, participant3);
        conflictingAppointment.setIgnoreConflicts(false);
        final CalendarDataObject[] conflicts = appointments.save( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        final CalendarDataObject conflict = conflicts[0];
        assertNull(conflict.getTitle());
    }

    //Bug 11269

    public void testShouldIncludeCurrentUserInConflictsWithCurrentUserOnly() throws OXException {
        final CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user);
        appointments.save( appointment ); clean.add( appointment );

        final CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithUserParticipants(user);
        conflictingAppointment.setIgnoreConflicts(false);
        final CalendarDataObject[] conflicts = appointments.save( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        final CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getTitle(), conflict.getTitle());
        assertUserParticipants(conflict, user );

    }

    // Bug 11316 Updating an appointment should leave it in private folder

    public void testUpdatePublicAppointmentTimeShouldUpdateParticipantStatus() throws OXException, SQLException {
        final FolderObject folder = folders.createPublicFolderFor(session, ctx,"A nice public folder",  FolderObject.SYSTEM_PUBLIC_FOLDER_ID, userId, secondUserId);
        cleanFolders.add( folder );

        CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user, secondUser);
        appointment.setParentFolderID(folder.getObjectID());
        appointments.save( appointment ); clean.add( appointment );

        appointments.switchUser(secondUser);
        appointment = appointments.reload(appointment);

        boolean found = false;
        for(final UserParticipant participant : appointment.getUsers()) {
            if(participant.getIdentifier() == secondUserId) {
                found = true;
                participant.setConfirm(CalendarDataObject.ACCEPT);
            }
        }
        assertTrue(found);
        appointments.save( appointment );
        appointments.switchUser( user );

        appointment = appointments.reload(appointment);
        found = false;
        for(final UserParticipant participant : appointment.getUsers()) {
            if(participant.getIdentifier() == secondUserId) {
                found = true;
                assertEquals(participant.getConfirm(), CalendarDataObject.ACCEPT);
            }
        }

        assertTrue("SecondUser disappeared from users!", found);

        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setStartDate(appointment.getStartDate());
        cdao.setEndDate(new Date(appointment.getEndDate().getTime() + 36000000));
        cdao.setObjectID(appointment.getObjectID());
        cdao.setParentFolderID(appointment.getParentFolderID());
        cdao.setContext(appointment.getContext());

        appointments.save( cdao );

        appointment = appointments.reload(appointment);

        found = false;
        for(final UserParticipant participant : appointment.getUsers()) {
            if(participant.getIdentifier() == secondUserId) {
                found = true;
                assertEquals(participant.getConfirm(), CalendarDataObject.NONE);
            }
        }

        assertTrue("SecondUser disappeared from users!", found);
        assertEquals(appointment.getParticipants().length, appointment.getUsers().length);

    }

    // Bug 11424

    public void testUpdateInSharedFolderShouldAutoAcceptTimeChange() throws OXException, SQLException {

        folders.sharePrivateFolder(session, ctx, secondUserId);
        try {
            appointments.switchUser( secondUser );
            CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user);
            appointment.setParentFolderID(folders.getStandardFolder(userId, ctx));

            appointments.save( appointment );

            appointment = appointments.reload(appointment);

            boolean found = false;
            for(final UserParticipant participant : appointment.getUsers()) {
                if(participant.getIdentifier() == userId) {
                    found = true;
                    assertEquals(CalendarDataObject.ACCEPT, participant.getConfirm());
                }
            }

            assertTrue(found);


            final CalendarDataObject cdao = new CalendarDataObject();
            cdao.setStartDate(appointment.getStartDate());
            cdao.setEndDate(new Date(appointment.getEndDate().getTime() + 36000000));
            cdao.setObjectID(appointment.getObjectID());
            cdao.setParentFolderID(appointment.getParentFolderID());
            cdao.setContext(appointment.getContext());

            appointments.save( cdao );

            appointment = appointments.reload(appointment);

            found = false;
            for(final UserParticipant participant : appointment.getUsers()) {
                if(participant.getIdentifier() == userId) {
                    found = true;
                    assertEquals(CalendarDataObject.ACCEPT, participant.getConfirm());
                }
            }

            assertTrue(found);

        } finally {
            // Unshare
            folders.unsharePrivateFolder(session, ctx);
        }
    }

    // Bug 10154

    public void testShouldKeepParticipantsInSharedFolder() throws OXException, SQLException {
        folders.sharePrivateFolder(session, ctx, secondUserId);
        try {
            appointments.switchUser( secondUser );
            CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user);
            appointment.setParentFolderID(folders.getStandardFolder(userId, ctx));

            appointments.save( appointment );

            appointment = appointments.reload(appointment);

            appointments.switchUser(user);

            final ArrayList<Participant> participants = new ArrayList<Participant>(java.util.Arrays.asList(appointment.getParticipants()));

            final CalendarContextToolkit tk = new CalendarContextToolkit();

            final UserParticipant participant = new UserParticipant(tk.resolveUser(participant1));
            participants.add(participant);


            CalendarDataObject cdao = new CalendarDataObject();
            cdao.setObjectID(appointment.getObjectID());
            cdao.setParentFolderID(appointment.getParentFolderID());
            cdao.setContext(appointment.getContext());
            cdao.setParticipants(participants);

            appointments.save( cdao );

            appointments.switchUser(secondUser);

            cdao = new CalendarDataObject();
            cdao.setStartDate(appointment.getStartDate());
            cdao.setEndDate(new Date(appointment.getEndDate().getTime() + 36000000));
            cdao.setObjectID(appointment.getObjectID());
            cdao.setParentFolderID(appointment.getParentFolderID());
            cdao.setContext(appointment.getContext());

            appointments.save( cdao );

            appointments.switchUser(user);
            appointment = appointments.reload(appointment);

            assertUserParticipants(appointment, user, participant1);

        } finally {
            // Unshare
            folders.unsharePrivateFolder(session, ctx);
        }
    }

    // Bug 11059
    public void testShouldRespectReadPermissions() throws OXException {
        final FolderObject folder = folders.createPublicFolderFor(session, ctx,"A nice public folder",  FolderObject.SYSTEM_PUBLIC_FOLDER_ID, userId);
        cleanFolders.add( folder );

        boolean found = false;
        final ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(folder.getPermissions());
        for(final OCLPermission permission : permissions) {
            if(OCLPermission.ALL_GROUPS_AND_USERS == permission.getEntity()) {
                found = true;
                permission.setReadObjectPermission(OCLPermission.NO_PERMISSIONS);
                permission.setWriteObjectPermission(OCLPermission.NO_PERMISSIONS);
                permission.setDeleteObjectPermission(OCLPermission.NO_PERMISSIONS);
                permission.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
                permission.setGroupPermission(true);
            }
        }

        if(!found) {
            final OCLPermission permission = new OCLPermission();
            permission.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
            permission.setReadObjectPermission(OCLPermission.NO_PERMISSIONS);
            permission.setWriteObjectPermission(OCLPermission.NO_PERMISSIONS);
            permission.setDeleteObjectPermission(OCLPermission.NO_PERMISSIONS);
            permission.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
            permission.setGroupPermission(true);
            permissions.add(permission);
        }
        final FolderObject update = new FolderObject();
        update.setObjectID(folder.getObjectID());
        update.setPermissions(permissions);

        folders.save( update , ctx, session ) ;

        final CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user, secondUser);
        appointment.setParentFolderID(folder.getObjectID());
        appointments.save( appointment ); clean.add( appointment );

        appointments.switchUser(secondUser);

        // Read

        try {
            appointments.getAppointmentsInFolder(folder.getObjectID());
            fail("I could read the content!");
        } catch (final OXException x) {
            assertTrue(x.getMessage().contains("APP-0013"));
        }

        // Modified

        try {
            appointments.getModifiedInFolder(folder.getObjectID(), 0);
            fail("I could read the content!");
        } catch (final OXException x) {
            assertTrue(x.getMessage().contains("APP-0013"));
        }

        // Deleted

        try {
            appointments.getDeletedInFolder(folder.getObjectID(), 0);
            fail("I could read the content!");
        } catch (final OXException x) {
            assertTrue(x.getMessage().contains("APP-0013"));
        }

    }

    // Bug 11307

    public void testRecurringAppointmentShouldBeConvertibleToSingleAppointment() throws OXException, SQLException {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setDays(CalendarDataObject.MONDAY);
        appointment.setInterval(1);
        appointment.setRecurrenceCount(3);
        appointments.save( appointment ); clean.add(appointment);

        final CalendarDataObject update = appointments.createIdentifyingCopy(appointment);
        update.setRecurrenceType(CalendarDataObject.NO_RECURRENCE);
        update.setStartDate(start);
        update.setEndDate(end);
        appointments.save(update);

        final CalendarDataObject reloaded = appointments.reload(appointment);
        assertEquals(start.getTime(), reloaded.getStartDate().getTime());
        assertEquals(end.getTime(), reloaded.getEndDate().getTime());
        assertEquals(CalendarDataObject.NO_RECURRENCE, reloaded.getRecurrenceType());

    }

    // Bug 4778

    public void testFreebusyResultShouldContainTitleIfItIsReadableViaASharedFolder() throws OXException, SearchIteratorException {
        final CalendarDataObject appointment = appointments.buildBasicAppointment(D("24/02/1981 10:00"), D("24/02/1981 12:00"));
        appointments.save( appointment ); clean.add(appointment);

        folders.sharePrivateFolder(session,ctx, secondUserId);
        try {
            appointments.switchUser(secondUser);

            final SearchIterator<CalendarDataObject> freebusy = appointments.getCurrentAppointmentSQLInterface()
                    .getFreeBusyInformation(userId,Participant.USER,D("23/02/1981 00:00"), D("25/02/1981 00:00"));

            final List<CalendarDataObject> appointments = read(freebusy);


            assertEquals(1, appointments.size());
            final CalendarDataObject result = appointments.get(0);
            // Assert the title is visible

            assertEquals(appointment.getTitle(), result.getTitle());
        } finally {
            folders.unsharePrivateFolder(session, ctx);
        }

    }

    // Bug 11051

    public void testShouldSurviveInvalidDaysValueInWeeklyRecurrenceWithOccurrence() throws OXException {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");
        final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
        appointments.save(appointment); clean.add(appointment);

        final CalendarDataObject update = appointments.createIdentifyingCopy(appointment);
        update.setStartDate(start);
        update.setEndDate(end);
        update.setRecurrenceType(CalendarDataObject.MONTHLY);
        update.setMonth(3);
        update.setDays(666);
        update.setInterval(2);
        update.setOccurrence(2);

        try {
            appointments.save( update );
        } catch (final OXCalendarException x) {
            x.printStackTrace();
            assertEquals(x.getMessage(), 45, x.getDetailNumber());
        } catch (final Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }


    // Bug 10806

    public void testReschedulingOfPrivateRecurringAppointmentWithOneResourceParticipant() throws OXException, SQLException {
        final Date start = D("04/06/2007 10:00");
        final Date end = D("04/06/2007 12:00");

        final CalendarDataObject appointment = appointments.buildAppointmentWithResourceParticipants(resource1);
        appointment.setStartDate(start);
        appointment.setEndDate(end);
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setDays(CalendarDataObject.MONDAY);
        appointment.setInterval(1);

        appointments.save( appointment ); clean.add(appointment);

        final Date newStart = D("04/06/2007 13:00");
        final Date newEnd = D("04/06/2007 14:00");
                
        final CalendarDataObject update = appointments.createIdentifyingCopy(appointment);
        update.setStartDate(newStart);
        update.setEndDate(newEnd);
        appointments.save(update);

        final CalendarDataObject reloaded = appointments.reload(appointment);
        assertEquals(reloaded.getStartDate(), newStart);
        assertEquals(reloaded.getEndDate(), newEnd);

    }


    // Bug #9950

    public void testParticipantChangeTriggersEvent() throws OXException {
        final CalendarContextToolkit tools = new CalendarContextToolkit();

        final CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(participant1);
        appointments.save( appointment ); clean.add( appointment );

        TestEventAdmin.getInstance().clearEvents();

        final CalendarDataObject update = appointments.createIdentifyingCopy( appointment );
        update.setParticipants( tools.users( ctx, participant1,  participant2) );
        appointments.save( update );

        assertModificationEventWithOldObject(AppointmentObject.class, appointment.getParentFolderID(), appointment.getObjectID());


    }

    // Bug 11453

    public void testShouldOmitEventOnUpdateToAlarmFlag() throws OXException {
        final Date start = D("04/06/2007 10:00");
        final Date end = D("04/06/2007 12:00");

        final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
        appointment.setAlarm(15);
        appointment.setAlarmFlag(true);

        appointments.save( appointment ); clean.add(appointment);

        final CalendarDataObject update = appointments.createIdentifyingCopy(appointment);
        update.setAlarmFlag(false);
        update.setAlarm(-1);

        final TestEventAdmin eventAdmin = TestEventAdmin.getInstance();
        eventAdmin.clearEvents();
        appointments.save( update );

        assertTrue(eventAdmin.getEvents().isEmpty());
        

    }

    // Bug 11881
    public void testPrivateAppointmentsDontHaveOtherParticpantsButTheOwner() throws OXException {
        final Date start = D("04/06/2007 10:00");
        final Date end = D("04/06/2007 12:00");

        CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(participant1, participant2, user);
        appointment.setPrivateFlag(true);

        try {
            appointments.save(appointment);
            clean.add(appointment);
            fail("Could create private appoinment with other participants");
        } catch (final OXException e) {
            e.printStackTrace();
            assertTrue(true);
        }

        // Try setting private flag later
        appointment = appointments.buildAppointmentWithUserParticipants(participant1, participant2, user);
        appointments.save( appointment );
        clean.add(appointment);

        CalendarDataObject update = appointments.createIdentifyingCopy(appointment);
        update.setPrivateFlag(true);

        try {
            appointments.save(update);
            fail("Could create private appoinment with other participants");
        } catch (final OXException e) {
            e.printStackTrace();
            assertTrue(true);
        }

        // Try adding participants later

        final CalendarContextToolkit tools = new CalendarContextToolkit();
        appointment = appointments.buildBasicAppointment(start, end);
        appointment.setPrivateFlag(true);
        appointments.save(appointment);

        update = appointments.createIdentifyingCopy(appointment);
        update.setParticipants( tools.users( ctx, participant1,  participant2) );

        try {
            appointments.save(update);
            fail("Could create private appoinment with other participants");
        } catch (final OXException e) {
            e.printStackTrace();
            assertTrue(true);
        }
        

    }

    // Bug 11803

    public void testFreeBusyResultShouldOnlyContainRecurrenceInSpecifiedInterval() throws OXException, SearchIteratorException {
        final Date start = D("07/02/2008 10:00");
        final Date end = D("07/02/2008 12:00");
        // Create Weekly recurrence
        final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setDays(CalendarDataObject.WEDNESDAY);
        appointment.setTitle("Everything can happen on a Wednesday");
        appointment.setInterval(1);
        appointments.save(appointment); clean.add(appointment);

        // Ask for freebusy information in one week containing one ocurrence

        final SearchIterator<CalendarDataObject> iterator = appointments.getCurrentAppointmentSQLInterface().getFreeBusyInformation(userId, Participant.USER, D("18/02/2008 00:00"), D("25/02/2008 00:00"));
        // Verify only one ocurrence was returned
        try {
            assertTrue("Should find exactly one ocurrence. Found none.", iterator.hasNext());
            final CalendarDataObject occurrence = iterator.next();
            assertFalse("Should find exactly one ocurrence. Found more than one", iterator.hasNext());

            assertEquals(D("20/02/2008 10:00"), occurrence.getStartDate());
            assertEquals(D("20/02/2008 12:00"), occurrence.getEndDate());
        } finally {
            iterator.close();
        }
    }

    // Bug 11865

    public void testShouldDisallowTurningAnExceptionIntoASeries() throws OXException {
        final Date start = D("07/02/2008 10:00");
        final Date end = D("07/02/2008 12:00");
        // Create Weekly recurrence
        final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setDays(CalendarDataObject.WEDNESDAY);
        appointment.setTitle("Everything can happen on a Wednesday");
        appointment.setInterval(1);
        appointments.save(appointment); clean.add(appointment);

        CalendarDataObject update = appointments.createIdentifyingCopy(appointment);
        update.setRecurrenceType(CalendarDataObject.WEEKLY);
        update.setDays(CalendarDataObject.MONDAY);
        update.setTitle("Monday! Monday!");
        update.setInterval(1);
        update.setRecurrencePosition(3);

        appointments.save( update );

        update = appointments.createIdentifyingCopy(update);
        update.setRecurrencePosition(1);
        update.setTitle("Exception");

        try {
			appointments.save( update );
			fail("Could change recurrence position for change exception");
		} catch (final OXCalendarException e) {
			if ((OXCalendarException.Code.INVALID_RECURRENCE_POSITION_CHANGE.getDetailNumber() != e.getDetailNumber()) &&
					(OXCalendarException.Code.INVALID_RECURRENCE_TYPE_CHANGE.getDetailNumber() != e.getDetailNumber())) {
				fail("Unexpected error code: " + e.getDetailNumber());
			}
		}

    }

	/**
	 * Test for <a href=
	 * "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12072">bug
	 * #12072</a>
	 * 
	 * @throws OXException
	 *             If an OX error occurs
	 */
	public void testShouldNotIndicateConflictingResources() throws OXException {
		final long today = CalendarRecurringCollection.normalizeLong(System.currentTimeMillis());
		final int weekDayOfToday;
		{
			final Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
			cal.setTimeInMillis(today);
			weekDayOfToday = cal.get(Calendar.DAY_OF_WEEK);
		}
		Date start = new Date(today + (10 * CalendarRecurringCollection.MILLI_HOUR));
		Date end = new Date(today + (11 * CalendarRecurringCollection.MILLI_HOUR));
		// Create Weekly recurrence
		final CalendarDataObject appointment = appointments.buildAppointmentWithResourceParticipants(resource1);
		appointment.setParentFolderID(appointments.getPrivateFolder());
		appointment.setIgnoreConflicts(true);
		appointment.setStartDate(start);
		appointment.setEndDate(end);
		appointment.setContext(ctx);
		appointment.setTimezone("utc");
		appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
		appointment.setDays(convertCalendarDAY_OF_WEEK2CalendarDataObjectDAY_OF_WEEK(weekDayOfToday));
		appointment.setTitle("Everything can happen on a X-day");
		appointment.setInterval(1);
		appointment.setOccurrence(5);
		appointments.save(appointment);
		clean.add(appointment);
		// Now create a second weekly recurrence with demanding resource on
		// following day which should not indicate any conflicts
		start = new Date(start.getTime() + CalendarRecurringCollection.MILLI_DAY);
		end = new Date(end.getTime() + CalendarRecurringCollection.MILLI_DAY);
		final CalendarDataObject update = appointments.buildAppointmentWithResourceParticipants(resource1);
		update.setParentFolderID(appointments.getPrivateFolder());
		update.setIgnoreConflicts(true);
		update.setStartDate(start);
		update.setEndDate(end);
		update.setContext(ctx);
		update.setTimezone("utc");
		update.setRecurrenceType(CalendarDataObject.WEEKLY);
		update.setDays(convertCalendarDAY_OF_WEEK2CalendarDataObjectDAY_OF_WEEK(weekDayOfToday + 1));
		update.setTitle("Everything can happen on a X1-day");
		update.setInterval(1);
		update.setOccurrence(5);
		final CalendarDataObject[] conflicts = appointments.save(update);
		clean.add(update);
		assertTrue("", conflicts == null || conflicts.length == 0);
	}

	/**
	 * Test for <a href=
	 * "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=11695">bug
	 * #11695</a>
	 * 
	 * @throws OXException
	 *             If an OX error occurs
	 */
	public void testShouldCalculateProperWeeklyRecurrence() throws OXException {
		final Date start = D("04/09/2008 22:00");
		final Date end = D("04/09/2008 23:00");
		// Create Weekly recurrence
		final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
		appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
		appointment.setDays(CalendarDataObject.MONDAY + CalendarDataObject.FRIDAY);
		appointment.setTitle("testShouldCalculateProperWeeklyRecurrence");
		appointment.setInterval(1);
		appointment.setOccurrence(2);
		appointments.save(appointment);
		clean.add(appointment);
		// Check for 2 occurrences
		final RecurringResults results = CalendarRecurringCollection.calculateRecurring(appointment, 0, 0, 0);
		assertEquals("Unexpected size in recurring results of weekly recurrence appointment", 2, results.size());
		final RecurringResult firstResult = results.getRecurringResult(0);
		assertEquals("Unexpected first occurrence", D("05/09/2008 22:00"), new Date(firstResult.getStart()));
		final RecurringResult secondResult = results.getRecurringResult(1);
		assertEquals("Unexpected second occurrence", D("08/09/2008 22:00"), new Date(secondResult.getStart()));
	}

	/**
	 * Another test for <a href=
	 * "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=11695">bug
	 * #11695</a>
	 * 
	 * @throws OXException
	 *             If an OX error occurs
	 */
	public void testShouldCalculateProperWeeklyRecurrence2() throws OXException {
		final Date start = D("14/09/2008 22:00");
		final Date end = D("14/09/2008 23:00");
		// Create Weekly recurrence
		final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
		appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
		appointment.setDays(62); // Monday til friday
		appointment.setTitle("testShouldCalculateProperWeeklyRecurrence2");
		appointment.setInterval(1);
		appointment.setUntil(new Date(1222041600000l));
		appointments.save(appointment);
		clean.add(appointment);
		// Check for 6 occurrences
		final long[] expectedLongs = new long[] { D("15/09/2008 22:00").getTime(), D("16/09/2008 22:00").getTime(),
				D("17/09/2008 22:00").getTime(), D("18/09/2008 22:00").getTime(), D("19/09/2008 22:00").getTime(),
				D("22/09/2008 22:00").getTime() };
		final RecurringResults results = CalendarRecurringCollection.calculateRecurring(appointment, 0, 0, 0);
		assertEquals("Unexpected size in recurring results of weekly recurrence appointment", expectedLongs.length,
				results.size());
		for (int i = 0; i < expectedLongs.length; i++) {
			assertEquals("Unexpected " + (i + 1) + " occurrence", new Date(expectedLongs[i]), new Date(results.getRecurringResult(i)
					.getStart()));
		}
	}

    // Bug 12377

    public void testShouldDoCallbackWhenHavingCreatedAnException() throws OXException {
        final TestCalendarListener calendarListener = new TestCalendarListener();
        CalendarCallbacks.getInstance().addListener(calendarListener);
        try {
            final CalendarDataObject master = appointments.buildBasicAppointment(D("10/02/2008 10:00"), D("10/02/2008 12:00"));
            master.setRecurrenceType(CalendarDataObject.DAILY);
            master.setInterval(1);
            master.setOccurrence(10);
            appointments.save(master);
            clean.add(master);

            final CalendarDataObject exception = appointments.createIdentifyingCopy(master);
            exception.setRecurrencePosition(3);
            exception.setStartDate(D("13/02/2008 13:00"));
            exception.setEndDate(D("13/02/2008 15:00"));
            calendarListener.clear();
            final int[] changeExceptionId = new int[1];
            calendarListener.setVerifyer(new Verifyer() {

                public void verify(final TestCalendarListener calendarListener) {
                    assertEquals("createdChangeExceptionInRecurringAppointment", calendarListener.getCalledMethodName());
                    final CalendarDataObject masterFromEvent = (CalendarDataObject) calendarListener.getArg(0);
                    final CalendarDataObject changeExceptionFromEvent = (CalendarDataObject) calendarListener.getArg(1);

                    assertEquals(masterFromEvent.getObjectID(), master.getObjectID());
                    assertEquals(masterFromEvent.getObjectID(), changeExceptionFromEvent.getRecurrenceID());
                    changeExceptionId[0] = changeExceptionFromEvent.getObjectID();

                }
            });

            appointments.save(exception);

            assertEquals(exception.getObjectID(), changeExceptionId[0]);

            assertTrue("Callback was not triggered", calendarListener.wasCalled());


        } finally {
            CalendarCallbacks.getInstance().removeListener(calendarListener);
        }
    }

    /**
     * Test for <a href="http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12489">bug #12489</a>:<br>
     * <b>Error message thrown during change of recurring appointment</b>
     */
    public void testTitleUpdateOfRecAppWithException() throws OXException, SQLException {
		// create monthly recurring appointment
		/*-
		 * {"alarm":"-1","days":32,"title":"BlubberFOo","shown_as":1,"end_date":1226048400000,"note":"",
		 * "interval":1,"recurrence_type":3,"folder_id":"116","day_in_month":1,"private_flag":false,"occurrences":10,
		 * "start_date":1226044800000,"full_time":false}
		 */
		final String oldTitle = "testTitleUpdateOfRecAppWithException";
		final CalendarDataObject master = appointments.buildBasicAppointment(new Date(1226044800000L), new Date(
				1226048400000L));
		master.setTitle(oldTitle);
		master.setRecurrenceType(CalendarDataObject.MONTHLY);
		master.setInterval(1);
		master.setDays(32);
		master.setDayInMonth(1);
		master.setOccurrence(10);
		// Save
		appointments.save(master);
		clean.add(master);
		// Reload master to get real start/end
		final Date masterStart;
		final Date masterEnd;
		{
			final CalendarDataObject tmp = appointments.reload(master);
			masterStart = tmp.getStartDate();
			masterEnd = tmp.getEndDate();
		}
		// Create change exception
		/*-
		 * {"alarm":"-1","recurrence_position":1,"categories":"","end_date":1226055600000,"note":null,"recurrence_type":0,
		 * "until":null,"folder_id":"116","private_flag":false,"notification":true,"start_date":1226052000000,"location":"",
		 * "full_time":false}
		 */
		final CalendarDataObject exception = appointments.createIdentifyingCopy(master);
		exception.setRecurrencePosition(1);
		exception.setStartDate(new Date(1226052000000L));
		exception.setEndDate(new Date(1226052000000L));
		exception.setIgnoreConflicts(true);
		// exception.setTimezone("utc");
		appointments.save(exception);
		clean.add(exception);
		// Now try to change master's title only: error-prone GUI request which
		// contains until
		/*-
		 * {"alarm":"-1","until":null,"folder_id":"116","private_flag":false,"title":"BlubberFOo Renamed",
		 * "notification":true,"categories":"","end_date":1226048400000,"location":"","note":null,
		 * "start_date":1226044800000,"full_time":false}
		 */
		final String newTitle = "testTitleUpdateOfRecAppWithException RENAMED";
		final CalendarDataObject updateMaster = appointments.createIdentifyingCopy(master);
		updateMaster.setTitle(newTitle);
		appointments.save(updateMaster);
		// Reload and check name for master
		final CalendarDataObject reloadedMaster = appointments.reload(updateMaster);
		assertEquals("Master's start date changed although only its title was updated", masterStart, reloadedMaster
				.getStartDate());
		assertEquals("Master's end date changed although only its title was updated", masterEnd, reloadedMaster
				.getEndDate());
		assertEquals("Master's title did not change", newTitle, reloadedMaster.getTitle());
		// Reload and check exception
		final CalendarDataObject reloadedException = appointments.reload(exception);
		assertEquals("Change-exception's title changed", oldTitle, reloadedException.getTitle());
		assertEquals("Change-exception's start changed", new Date(1226052000000L), reloadedException.getStartDate());
		assertEquals("Change-exception's end changed", new Date(1226052000000L), reloadedException.getEndDate());
	}

    private static int convertCalendarDAY_OF_WEEK2CalendarDataObjectDAY_OF_WEEK(final int calendarDAY_OF_WEEK) {
    	switch (calendarDAY_OF_WEEK) {
    	case Calendar.SUNDAY:
    		return CalendarDataObject.SUNDAY;
    	case Calendar.MONDAY:
    		return CalendarDataObject.MONDAY;
    	case Calendar.TUESDAY:
    		return CalendarDataObject.TUESDAY;
    	case Calendar.WEDNESDAY:
    		return CalendarDataObject.WEDNESDAY;
    	case Calendar.THURSDAY:
    		return CalendarDataObject.THURSDAY;
    	case Calendar.FRIDAY:
    		return CalendarDataObject.FRIDAY;
    	case Calendar.SATURDAY:
    		return CalendarDataObject.SATURDAY;
    	default:
    		return -1;
    	}
    }

    private List<CalendarDataObject> read(final SearchIterator<CalendarDataObject> si) throws OXException, SearchIteratorException {
        final List<CalendarDataObject> appointments = new ArrayList<CalendarDataObject>();
        while(si.hasNext()) { appointments.add( si.next() ); }
        return appointments;
    }

    private static interface Verifyer {
        public void verify(TestCalendarListener listener);
    }

    private static final class TestCalendarListener extends AbstractCalendarListener {
        private String called;
        List<Object> args = new ArrayList<Object>();
        private Verifyer verifyer;

        @Override
		public void createdChangeExceptionInRecurringAppointment(final CalendarDataObject master, final CalendarDataObject changeException, final ServerSession session) {
            this.called = "createdChangeExceptionInRecurringAppointment";
            this.args.add(master);
            this.args.add(changeException);
            this.args.add(session);
            verifyer.verify(this);
        }

        public void clear() {
            called = null;
            args.clear();
        }

        public String getCalledMethodName() {
            return called;
        }

        public List<Object> getArgs() {
            return args;
        }

        public boolean wasCalled() {
            return called != null;
        }

        public Object getArg(final int i) {
            return args.get(i);
        }

        public Verifyer getVerifyer() {
            return verifyer;
        }

        public void setVerifyer(final Verifyer verifyer) {
            this.verifyer = verifyer;
        }
    }

}
