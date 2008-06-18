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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.tools.CalendarContextToolkit;
import com.openexchange.groupware.calendar.tools.CalendarTestConfig;
import com.openexchange.groupware.calendar.tools.CommonAppointments;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CalendarSqlTest extends TestCase {

    private final List<CalendarDataObject> clean = new ArrayList<CalendarDataObject>();
        

    private String participant1, participant2, participant3;
    private String resource1, resource2, resource3;

    private String group,member;

    private String user;
    private String secondUser;
    private Context ctx;
    private CommonAppointments appointments;

    @Override
	public void setUp() throws Exception {
        Init.startServer();

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

        group = config.getGroup();
        final int groupid = tools.resolveGroup(group, ctx);
        final int memberid = tools.loadGroup(groupid, ctx).getMember()[0];
        member = tools.loadUser(memberid, ctx).getLoginInfo();

        appointments.deleteAll(ctx);
    }

    @Override
	public void tearDown() throws OXException, SQLException {
        Init.stopServer();
        appointments.removeAll(user, clean);
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
        modified.setRecurrenceCount(3); // Every 12th of every 2nd month for 3 appointments

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
}
