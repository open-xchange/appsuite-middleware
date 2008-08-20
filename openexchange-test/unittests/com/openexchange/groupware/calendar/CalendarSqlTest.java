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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
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
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

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
        modified.setObjectID(cdao.getObjectID());
        modified.setParentFolderID(cdao.getParentFolderID());
        modified.setRecurrenceType(CalendarDataObject.MONTHLY);
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
            assertEquals(x.getMessage(), 42, x.getDetailNumber());
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
        } catch (OXException e) {
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
        } catch (OXException e) {
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
        } catch (OXException e) {
            e.printStackTrace();
            assertTrue(true);
        }
        

    }

    // Bug 11803

    public void testFreeBusyResultShouldOnlyContainRecurrenceInSpecifiedInterval() throws OXException, SearchIteratorException {
        Date start = D("07/02/2008 10:00");
        Date end = D("07/02/2008 12:00");
        // Create Weekly recurrence
        CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setDays(CalendarDataObject.WEDNESDAY);
        appointment.setTitle("Everything can happen on a Wednesday");
        appointment.setInterval(1);
        appointments.save(appointment); clean.add(appointment);

        // Ask for freebusy information in one week containing one ocurrence

        SearchIterator<CalendarDataObject> iterator = appointments.getCurrentAppointmentSQLInterface().getFreeBusyInformation(userId, Participant.USER, D("18/02/2008 00:00"), D("25/02/2008 00:00"));
        // Verify only one ocurrence was returned
        try {
            assertTrue("Should find exactly one ocurrence. Found none.", iterator.hasNext());
            CalendarDataObject occurrence = iterator.next();
            assertFalse("Should find exactly one ocurrence. Found more than one", iterator.hasNext());

            assertEquals(D("20/02/2008 10:00"), occurrence.getStartDate());
            assertEquals(D("20/02/2008 12:00"), occurrence.getEndDate());
        } finally {
            iterator.close();
        }
    }

    // Bug 11865

    public void testShouldDisallowTurningAnExceptionIntoASeries() throws OXException {
        Date start = D("07/02/2008 10:00");
        Date end = D("07/02/2008 12:00");
        // Create Weekly recurrence
        CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
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
        update.setRecurrencePosition(0);
        update.setTitle("Exception");

        appointments.save( update );
 
    }

    private List<CalendarDataObject> read(final SearchIterator<CalendarDataObject> si) throws OXException, SearchIteratorException {
        final List<CalendarDataObject> appointments = new ArrayList<CalendarDataObject>();
        while(si.hasNext()) { appointments.add( si.next() ); }
        return appointments;
    }

}
