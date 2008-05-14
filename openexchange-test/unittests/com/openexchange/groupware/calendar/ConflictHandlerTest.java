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

import junit.framework.TestCase;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.calendar.tools.CalendarTestConfig;
import com.openexchange.groupware.calendar.tools.CalendarContextToolkit;
import com.openexchange.groupware.calendar.tools.CommonAppointments;
import static com.openexchange.groupware.calendar.tools.CalendarAssertions.assertUserParticipants;
import static com.openexchange.groupware.calendar.tools.CalendarAssertions.assertResourceParticipants;
import com.openexchange.api2.OXException;
import com.openexchange.session.Session;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ConflictHandlerTest extends TestCase {

    private String participant1, participant2, participant3;
    private String resource1, resource2, resource3;

    private String group,member;

    private String user;
    private String secondUser;
    private Context ctx;
    private CommonAppointments appointments;

    List<CalendarDataObject> clean = new ArrayList<CalendarDataObject>();

    public void setUp() throws Exception {
        Init.startServer();

        CalendarTestConfig config = new CalendarTestConfig();

        user = config.getUser();
        secondUser = config.getSecondUser();

        CalendarContextToolkit tools = new CalendarContextToolkit();
        ctx = tools.getDefaultContext();

        appointments = new CommonAppointments(ctx, user);

        participant1 = config.getParticipant1();
        participant2 = config.getParticipant2();
        participant3 = config.getParticipant3();

        resource1 = config.getResource1();
        resource2 = config.getResource2();
        resource3 = config.getResource3();

        group = config.getGroup();
        int groupid = tools.resolveGroup(group, ctx);
        int memberid = tools.loadGroup(groupid, ctx).getMember()[0];
        member = tools.loadUser(memberid, ctx).getLoginInfo();

        appointments.deleteAll(ctx);
    }

    public void tearDown() throws OXException, SQLException {
        Init.stopServer();
        appointments.removeAll(user, clean);
    }

     // Node 1077
    public void testShouldSupplyConflictingUserParticipants() throws SQLException, OXException {
        CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants( user, participant1, participant2);  // User is added in private folder anyway
        appointments.save( appointment ); clean.add( appointment );
        CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithUserParticipants( user, participant1, participant3);
        conflictingAppointment.setIgnoreConflicts(false);
        CalendarDataObject[] conflicts = getConflicts( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        CalendarDataObject conflict = conflicts[0];
                
        assertEquals(appointment.getObjectID(), conflict.getObjectID());
        assertUserParticipants(conflict, user, participant1);
    }

    // Node 1077
    public void testShouldSupplyConflictingResourceParticipants() throws SQLException, OXException {
        CalendarDataObject appointment = appointments.buildAppointmentWithResourceParticipants(resource1, resource2);
        appointments.save( appointment );  clean.add( appointment );
        CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithResourceParticipants(resource1, resource3);
        conflictingAppointment.setIgnoreConflicts(false);
        CalendarDataObject[] conflicts = getConflicts( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getObjectID(), conflict.getObjectID());
        assertResourceParticipants(conflict, resource1);
    }

    // Node 1077
    public void testShouldSupplyConflictingUserParticipantsInGroup() throws OXException {
        CalendarDataObject appointment = appointments.buildAppointmentWithGroupParticipants(group);
        appointments.save( appointment ); clean.add( appointment );

        CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithUserParticipants( member );
        conflictingAppointment.setIgnoreConflicts( false );

        CalendarDataObject[] conflicts = getConflicts( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getObjectID(), conflict.getObjectID());
        assertUserParticipants(conflict, member );
    }

    // Node 1077
    public void testShouldSupplyTitleIfPermissionsAllowIt() throws OXException {
        // Permissions of the current user are only relevant if he is also a participant.
        // Can someone create a private appointment where she is not a participant? Where is this rule enforced?
        CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user, participant1, participant2);
        appointments.save( appointment ); clean.add( appointment );
        CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithUserParticipants(user, participant1, participant3);
        conflictingAppointment.setIgnoreConflicts(false);
        CalendarDataObject[] conflicts = getConflicts( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getTitle(), conflict.getTitle());
    }

    // Node 1077
    public void testShouldSuppressTitleIfPermissionsDenyIt() throws OXException {
        CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user, participant1, participant2);
        appointments.save( appointment ); clean.add( appointment );

        appointments.switchUser( secondUser );

        CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithUserParticipants(user, participant1, participant3);
        conflictingAppointment.setIgnoreConflicts(false);
        CalendarDataObject[] conflicts = getConflicts( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        CalendarDataObject conflict = conflicts[0];
        assertNull(conflict.getTitle());
    }

    //Bug 11269

    public void testShouldIncludeCurrentUserInConflictsWithCurrentUserOnly() throws OXException {
        CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user);
        appointments.save( appointment ); clean.add( appointment );

        CalendarDataObject conflictingAppointment = appointments.buildAppointmentWithUserParticipants(user);
        conflictingAppointment.setIgnoreConflicts(false);
        CalendarDataObject[] conflicts = getConflicts( conflictingAppointment );
        
        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getTitle(), conflict.getTitle());
        assertUserParticipants(conflict, user );

    }

    private CalendarDataObject[] getConflicts(CalendarDataObject conflictingAppointment) throws OXException {
        ConflictHandler ch = new ConflictHandler(conflictingAppointment, appointments.getSession(), false);
        CalendarDataObject[] conflicts = ch.getConflicts();
        for(CalendarDataObject conflict : conflicts) {
            conflict.setContext(ctx);
        }
        return conflicts;
    }

}
