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

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.groupware.calendar.TimeTools.D;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;


/**
 * {@link Bug29566Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug29566Test extends AbstractAJAXSession {

    private AJAXClient client2;
    private CalendarTestManager ctm;
    private CalendarTestManager ctm2;
    private Appointment appointment;

    public Bug29566Test(String name) {
        super(name);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        client2 = new AJAXClient(User.User2);

        ctm = new CalendarTestManager(client);
        ctm2 = new CalendarTestManager(client2);

        appointment = new Appointment();
        appointment.setStartDate(D("18.11.2013 08:00"));
        appointment.setEndDate(D("18.11.2013 09:00"));
        appointment.setTitle("Test Bug 29146");
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        UserParticipant user = new UserParticipant(client.getValues().getUserId());
        user.setConfirm(Appointment.NONE);
        appointment.setParticipants(new Participant[] { user });
        appointment.setUsers(new UserParticipant[] { user });
    }
    
    public void testAddParticipantWithExternalOrganizerAndUid() throws Exception {
        addParticipantWithExternalOrganizerAndUid(false);
    }
    
    public void testAddParticipantWithExternalOrganizerAndUidShared() throws Exception {
        addParticipantWithExternalOrganizerAndUid(true);
    }
    
    private void addParticipantWithExternalOrganizerAndUid(boolean shared) throws Exception {
        String uid = generateUid();
        appointment.setUid(uid);
        String organizer = "test@extern.example.invalid";
        appointment.setOrganizer(organizer);
        ctm.insert(appointment);
        
        Appointment clone = appointment.clone();
        
        if (!shared) {
            clone.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        }
        UserParticipant user = new UserParticipant(client.getValues().getUserId());
        user.setConfirm(Appointment.NONE);
        UserParticipant user2 = new UserParticipant(client2.getValues().getUserId());
        user2.setConfirm(Appointment.NONE);
        clone.setParticipants(new Participant[] { user, user2 });
        clone.setUsers(new UserParticipant[] { user, user2 });
        
        ctm2.update(clone);

        assertFalse("No error expected.", ctm2.getLastResponse().hasError());

        Appointment loaded = ctm.get(appointment);
        assertEquals("Expected two participants.", 2, loaded.getParticipants().length);
        assertEquals("Expected two users.", 2, loaded.getUsers().length);

    }
    
    public void testAddParticipantWithoutInfoShared() throws Exception {
        addParticipantWithoutInfo(true);
    }
    
    public void testAddParticipantWithoutInfo() throws Exception {
        addParticipantWithoutInfo(false);
    }
    
    private void addParticipantWithoutInfo(boolean shared) throws Exception {
        String uid = generateUid();
        appointment.setUid(uid);
        String organizer = "test@extern.example.invalid";
        appointment.setOrganizer(organizer);
        ctm.insert(appointment);
        
        Appointment clone = appointment.clone();
        clone.removeOrganizer();
        clone.removeUid();
        
        if (!shared) {
            clone.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        }
        UserParticipant user = new UserParticipant(client.getValues().getUserId());
        user.setConfirm(Appointment.NONE);
        UserParticipant user2 = new UserParticipant(client2.getValues().getUserId());
        user2.setConfirm(Appointment.NONE);
        clone.setParticipants(new Participant[] { user, user2 });
        clone.setUsers(new UserParticipant[] { user, user2 });
        
        ctm2.update(clone);
        AbstractAJAXResponse updateResponse = ctm2.getLastResponse();
        assertTrue("Should fail.", updateResponse.hasError());
        assertEquals("Wrong error.", shared ? OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_5.getNumber() : OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_2.getNumber() , updateResponse.getException().getCode());
    }
    
    public void testAddParticipantWithOnlyExternalOrganizer() throws Exception {
        addParticipantWithOnlyExternalOrganizer(false);
    }
    
    public void testAddParticipantWithOnlyExternalOrganizerShared() throws Exception {
        addParticipantWithOnlyExternalOrganizer(true);
    }
    
    private void addParticipantWithOnlyExternalOrganizer(boolean shared) throws Exception {
        String uid = generateUid();
        appointment.setUid(uid);
        String organizer = "test@extern.example.invalid";
        appointment.setOrganizer(organizer);
        ctm.insert(appointment);
        
        Appointment clone = appointment.clone();
        clone.removeUid();
        
        if (!shared) {
            clone.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        }
        UserParticipant user = new UserParticipant(client.getValues().getUserId());
        user.setConfirm(Appointment.NONE);
        UserParticipant user2 = new UserParticipant(client2.getValues().getUserId());
        user2.setConfirm(Appointment.NONE);
        clone.setParticipants(new Participant[] { user, user2 });
        clone.setUsers(new UserParticipant[] { user, user2 });
        
        ctm2.update(clone);
        AbstractAJAXResponse updateResponse = ctm2.getLastResponse();
        assertTrue("Should fail.", updateResponse.hasError());
        assertEquals("Wrong error.", shared ? OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_5.getNumber() : OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_2.getNumber() , updateResponse.getException().getCode());
    }
    
    public void testAddParticipantWithOnlyUid() throws Exception {
        addParticipantWithOnlyUid(false);
    }
    
    public void testAddParticipantWithOnlyUidShared() throws Exception {
        addParticipantWithOnlyUid(true);
    }
    
    private void addParticipantWithOnlyUid(boolean shared) throws Exception {
        String uid = generateUid();
        appointment.setUid(uid);
        String organizer = "test@extern.example.invalid";
        appointment.setOrganizer(organizer);
        ctm.insert(appointment);
        
        Appointment clone = appointment.clone();
        clone.removeOrganizer();
        
        if (!shared) {
            clone.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        }
        UserParticipant user = new UserParticipant(client.getValues().getUserId());
        user.setConfirm(Appointment.NONE);
        UserParticipant user2 = new UserParticipant(client2.getValues().getUserId());
        user2.setConfirm(Appointment.NONE);
        clone.setParticipants(new Participant[] { user, user2 });
        clone.setUsers(new UserParticipant[] { user, user2 });
        
        ctm2.update(clone);
        AbstractAJAXResponse updateResponse = ctm2.getLastResponse();
        assertTrue("Should fail.", updateResponse.hasError());
        assertEquals("Wrong error.", shared ? OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_5.getNumber() : OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_2.getNumber() , updateResponse.getException().getCode());
        }

    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();

        super.tearDown();
    }

    private String generateUid() {
        return "UID" + System.currentTimeMillis();
    }

}
