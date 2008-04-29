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
import com.openexchange.api2.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.CalendarTest;
import com.openexchange.groupware.container.*;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.configuration.AJAXConfig;

import java.util.*;
import java.sql.SQLException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CalendarSqlTest extends TestCase {

    private CalendarSql calendar = null;
    private List<CalendarDataObject> clean = new ArrayList<CalendarDataObject>();
    private int privateFolder;
    private Context ctx;

    private String participant1, participant2, participant3;
    private String resource1, resource2, resource3;

    private String group,member;

    private final long FUTURE = System.currentTimeMillis()+24*3600000;
    private String user;
    private String secondUser;

    public void setUp() throws Exception {
        Init.startServer();
        AJAXConfig.init();

        user = AJAXConfig.getProperty(AJAXConfig.Property.LOGIN);
        secondUser = AJAXConfig.getProperty(AJAXConfig.Property.SECONDUSER);

        CalendarContextToolkit tools = new CalendarContextToolkit();
        ctx = tools.getDefaultContext();

        switchUser( user );

        participant1 = AJAXConfig.getProperty(AJAXConfig.Property.USER_PARTICIPANT1);
        participant2 = AJAXConfig.getProperty(AJAXConfig.Property.USER_PARTICIPANT2);
        participant3 = AJAXConfig.getProperty(AJAXConfig.Property.USER_PARTICIPANT3);

        resource1 = AJAXConfig.getProperty(AJAXConfig.Property.RESOURCE_PARTICIPANT1);
        resource2 = AJAXConfig.getProperty(AJAXConfig.Property.RESOURCE_PARTICIPANT2);
        resource3 = AJAXConfig.getProperty(AJAXConfig.Property.RESOURCE_PARTICIPANT3);

        group = AJAXConfig.getProperty(AJAXConfig.Property.GROUP_PARTICIPANT);
        int groupid = tools.resolveGroup(group, ctx);
        int memberid = tools.loadGroup(groupid, ctx).getMember()[0];
        member = tools.loadUser(memberid, ctx).getLoginInfo();
    }

    public void tearDown() throws OXException, SQLException {
        switchUser( user );
        for(CalendarDataObject cdao : clean) {
            calendar.deleteAppointmentObject(cdao,privateFolder,new Date(Long.MAX_VALUE));
        }
        Init.stopServer();
    }

    // Bug #11148
    public void testUpdateWithInvalidRecurrencePatternShouldFail() throws OXException, SQLException {
        CalendarDataObject cdao = buildRecurringAppointment();
        save( cdao );
        clean.add( cdao );

        CalendarDataObject modified = new CalendarDataObject();
        modified.setObjectID(cdao.getObjectID());
        modified.setParentFolderID(cdao.getParentFolderID());
        modified.setRecurrenceType(CalendarDataObject.MONTHLY);
        modified.setDays(CalendarObject.TUESDAY);
        modified.setDayInMonth(666); // Must be between 1 and 5, usually, so 666 is invalid
        modified.setContext(cdao.getContext());
        try {
            save( modified );
            fail("Could save invalid dayInMonth value");
        } catch (OXException x) {
            // Passed. The invalid value wasn't accepted.       
        }
    }

    public void testShouldRebuildEntireRecurrencePatternOnUpdate() throws SQLException, OXException {
        CalendarDataObject cdao = buildRecurringAppointment();
        save( cdao );
        clean.add( cdao );

        CalendarDataObject modified = new CalendarDataObject();
        modified.setObjectID(cdao.getObjectID());
        modified.setParentFolderID(cdao.getParentFolderID());
        modified.setContext(cdao.getContext());
        modified.setRecurrenceType(CalendarDataObject.MONTHLY);
        modified.setDayInMonth(12);
        modified.setInterval(2);
        modified.setRecurrenceCount(3); // Every 12th of every 2nd month for 3 appointments

        try {
            save( modified );
            
            CalendarDataObject reloaded = reload( modified );

            assertEquals(0, reloaded.getDays());
            assertEquals(12, reloaded.getDayInMonth());
            assertEquals(2, reloaded.getInterval());
            assertEquals(CalendarObject.MONTHLY, reloaded.getRecurrenceType());
        } catch (OXException x) {
            x.printStackTrace();
            fail(x.toString());
        } catch (SQLException x) {
            x.printStackTrace();
            fail(x.toString());
        }
    }

    public void testShouldOnlyUpdateRecurrencePatternIfNeeded() throws SQLException, OXException {
        CalendarDataObject cdao = buildRecurringAppointment();
        save( cdao );
        clean.add( cdao );

        CalendarDataObject modified = new CalendarDataObject();
        modified.setObjectID(cdao.getObjectID());
        modified.setParentFolderID(cdao.getParentFolderID());
        modified.setContext(cdao.getContext());
        modified.setLocation("updated location");

        try {
            save( modified );

            CalendarDataObject reloaded = reload( modified );

            assertEquals(cdao.getDays(), reloaded.getDays());
            assertEquals(cdao.getDayInMonth(), reloaded.getDayInMonth());
            assertEquals(cdao.getInterval(), reloaded.getInterval());
            assertEquals(cdao.getRecurrenceType(), reloaded.getRecurrenceType());
        } catch (OXException x) {
            x.printStackTrace();
            fail(x.toString());
        } catch (SQLException x) {
            x.printStackTrace();
            fail(x.toString());
        }
    }

    // Node 1077
    public void testShouldSupplyConflictingUserParticipants() throws SQLException, OXException {
        CalendarDataObject appointment = buildAppointmentWithUserParticipants(participant1, participant2);
        save( appointment ); clean.add( appointment );
        CalendarDataObject conflictingAppointment = buildAppointmentWithUserParticipants(participant1, participant3);
        conflictingAppointment.setIgnoreConflicts(false);
        CalendarDataObject[] conflicts = save( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getObjectID(), conflict.getObjectID());
        assertUserParticipants(conflict, participant1);
    }

    // Node 1077
    public void testShouldSupplyConflictingResourceParticipants() throws SQLException, OXException {
        CalendarDataObject appointment = buildAppointmentWithResourceParticipants(resource1, resource2);
        save( appointment );  clean.add( appointment );
        CalendarDataObject conflictingAppointment = buildAppointmentWithResourceParticipants(resource1, resource3);
        conflictingAppointment.setIgnoreConflicts(false);
        CalendarDataObject[] conflicts = save( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getObjectID(), conflict.getObjectID());
        assertResourceParticipants(conflict, resource1);
    }

    // Node 1077
    public void testShouldSupplyConflictingUserParticipantsInGroup() throws OXException {
        CalendarDataObject appointment = buildAppointmentWithGroupParticipants(group);
        save( appointment ); clean.add( appointment );

        CalendarDataObject conflictingAppointment = buildAppointmentWithUserParticipants( member );
        conflictingAppointment.setIgnoreConflicts( false );

        CalendarDataObject[] conflicts = save( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getObjectID(), conflict.getObjectID());
        assertUserParticipants(conflict, member );
    
    }


    // Node 1077
    public void testShouldSupplyTitleIfPermissionsAllowIt() throws OXException {
        CalendarDataObject appointment = buildAppointmentWithUserParticipants(participant1, participant2);
        save( appointment ); clean.add( appointment );
        CalendarDataObject conflictingAppointment = buildAppointmentWithUserParticipants(participant1, participant3);
        conflictingAppointment.setIgnoreConflicts(false);
        CalendarDataObject[] conflicts = save( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        CalendarDataObject conflict = conflicts[0];
        assertEquals(appointment.getTitle(), conflict.getTitle());
    }

    // Node 1077    
    public void testShouldSuppressTitleIfPermissionsDenyIt() throws OXException {
        CalendarDataObject appointment = buildAppointmentWithUserParticipants(participant1, participant2);
        save( appointment ); clean.add( appointment );

        switchUser( secondUser );
        
        CalendarDataObject conflictingAppointment = buildAppointmentWithUserParticipants(participant1, participant3);
        conflictingAppointment.setIgnoreConflicts(false);
        CalendarDataObject[] conflicts = save( conflictingAppointment );

        assertNotNull(conflicts);
        assertEquals(1, conflicts.length);
        CalendarDataObject conflict = conflicts[0];
        assertNull(conflict.getTitle());
    }

    private void switchUser(String user) {
        CalendarContextToolkit tools = new CalendarContextToolkit();
        int userId = tools.resolveUser(user,ctx);
        privateFolder = new CalendarFolderToolkit().getStandardFolder(userId, ctx);
        calendar = new CalendarSql( tools.getSessionForUser(user, ctx) );
    }

    private CalendarDataObject[] save(CalendarDataObject cdao) throws OXException {
        if(cdao.containsObjectID()) {
            return calendar.updateAppointmentObject(cdao, cdao.getParentFolderID(), new Date(Long.MAX_VALUE));
        } else {
            return calendar.insertAppointmentObject(cdao);            
        }
    }

    private CalendarDataObject reload(CalendarDataObject which) throws SQLException, OXException {
        return calendar.getObjectById(which.getObjectID(), which.getParentFolderID());
    }

    private CalendarDataObject buildRecurringAppointment() {
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("recurring");
        cdao.setParentFolderID(privateFolder);
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setRecurrenceType(CalendarObject.MONTHLY);
        cdao.setRecurrenceCount(5);
        cdao.setDayInMonth(3);
        cdao.setInterval(2);
        cdao.setDays(CalendarObject.TUESDAY);
        assertTrue(cdao.isSequence());

        cdao.setContext(ctx);
        return cdao;
    }

    private CalendarDataObject buildAppointmentWithUserParticipants(String...usernames) {
        return buildAppointmentWithParticipants(usernames, new String[0], new String[0]);
    }

    private CalendarDataObject buildAppointmentWithResourceParticipants(String...resources) {
        return buildAppointmentWithParticipants(new String[0], resources, new String[0]);
    }

    private CalendarDataObject buildAppointmentWithGroupParticipants(String...groups) {
        return buildAppointmentWithParticipants(new String[0], new String[0], groups);
    }

    private CalendarDataObject buildAppointmentWithParticipants(String[] users, String[] resources, String[] groups) {
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("with participants");
        cdao.setParentFolderID(privateFolder);
        cdao.setIgnoreConflicts(true);

        long FIVE_DAYS = 5l*24l*3600000l;
        long THREE_HOURS = 3l*3600000l;

        cdao.setStartDate(new Date(FUTURE + FIVE_DAYS));
        cdao.setEndDate(new Date(FUTURE + FIVE_DAYS + THREE_HOURS));
        cdao.setContext(ctx);

        List<Participant> participants = new ArrayList<Participant>(users.length+resources.length+groups.length);
        participants.addAll( users(users) );
        participants.addAll( resources(resources) );
        participants.addAll( groups(groups) );

        cdao.setParticipants(participants);
        return cdao;
    }

    private void assertUserParticipants(CalendarDataObject cdao, String...users) {
        assertParticipants(cdao, users, new String[0]);    
    }

    private void assertResourceParticipants(CalendarDataObject cdao, String...resources) {
        assertParticipants(cdao, new String[0], resources);    
    }

    private void assertParticipants(CalendarDataObject cdao, String[] users, String[] resources) {
        assertNotNull("Participants should be set! ", cdao.getParticipants());

        Set<UserParticipant> userParticipants = new HashSet<UserParticipant>(users(users));
        Set<ResourceParticipant> resourceParticipants = new HashSet<ResourceParticipant>(resources(resources));
        Set<Participant> unexpected = new HashSet<Participant>();
        for(Participant participant : cdao.getParticipants()) {
            if(!(userParticipants.remove(participant)) && !(resourceParticipants.remove(participant))) {
               unexpected.add( participant );
            }
        }
        StringBuilder problems = new StringBuilder();
        boolean mustFail = false;
        if(!unexpected.isEmpty()) {
            mustFail = true;
            problems.append("Didn't expect: ").append(unexpected).append(". ");
        }
        if(!userParticipants.isEmpty()) {
            mustFail = true;
            problems.append("Missing user participants: ").append(userParticipants).append(". ");
        }
        if(!resourceParticipants.isEmpty()) {
            mustFail = true;
            problems.append("Missing resource participants: ").append(resourceParticipants).append(". ");
        }
        if( mustFail ) { fail( problems.toString() ); }
    }

    private List<UserParticipant> users(String...users) {
        List<UserParticipant> participants = new ArrayList<UserParticipant>(users.length);
        CalendarContextToolkit tools = new CalendarContextToolkit();
        for(String user : users) {
            int id = tools.resolveUser(user, ctx);
            UserParticipant participant = new UserParticipant(id);
            participants.add( participant );
        }
        return participants;
    }

    private List<ResourceParticipant> resources(String...resources) {
        List<ResourceParticipant> participants = new ArrayList<ResourceParticipant>(resources.length);
        CalendarContextToolkit tools = new CalendarContextToolkit();
        for(String resource : resources) {
            int id = tools.resolveResource(resource, ctx);
            ResourceParticipant participant = new ResourceParticipant(id);
            participants.add( participant );
        }
        return participants;
    }

    private List<GroupParticipant> groups(String...groups) {
        List<GroupParticipant> participants = new ArrayList<GroupParticipant>(groups.length);
        CalendarContextToolkit tools = new CalendarContextToolkit();
        for(String group : groups) {
            int id = tools.resolveGroup(group, ctx);
            GroupParticipant participant = new GroupParticipant(id);
            participants.add( participant );
        }
        return participants;
    }

}
