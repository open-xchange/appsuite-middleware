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
import java.util.Date;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug38404Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Bug38404Test extends AbstractAJAXSession {

    private AJAXClient client2;
    private CalendarTestManager ctm1;
    private CalendarTestManager ctm2;
    private Appointment appointment;

    public Bug38404Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        client2 = new AJAXClient(User.User2);
        ctm1 = new CalendarTestManager(client);
        ctm2 = new CalendarTestManager(client2);

        appointment = new Appointment();
        appointment.setTitle("Bug 38404");
        appointment.setStartDate(D("10.08.2015 08:00"));
        appointment.setEndDate(D("10.08.2015 09:00"));
        UserParticipant user1 = new UserParticipant(client.getValues().getUserId());
        UserParticipant user2 = new UserParticipant(client2.getValues().getUserId());
        appointment.setParticipants(new Participant[] { user1, user2 });
        appointment.setUsers(new UserParticipant[] { user1, user2 });
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
    }

    public void testBug38404() throws Exception {
        ctm1.insert(appointment);
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setDays(Appointment.TUESDAY + Appointment.WEDNESDAY + Appointment.THURSDAY);
        appointment.setInterval(2);
        ctm1.update(appointment);
        appointment.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        ctm2.confirm(appointment, Appointment.ACCEPT, "yes", 1);
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        Appointment loaded = ctm1.get(appointment);
        assertNotNull("Missing change exception.", loaded.getChangeException());
        assertEquals("Wrong amount of change exceptions.", 1, loaded.getChangeException().length);
        assertEquals("Wrong amount of participants.", 2, loaded.getUsers().length);
        for (UserParticipant up : loaded.getUsers()) {
            if (up.getIdentifier() == client.getValues().getUserId()) {
                assertEquals("Wrong confirmation status for user: " + up.getIdentifier(), Appointment.ACCEPT, up.getConfirm());
            } else if (up.getIdentifier() == client2.getValues().getUserId()) {
                assertEquals("Wrong confirmation status for user: " + up.getIdentifier(), Appointment.NONE, up.getConfirm());
            }
        }

        Appointment[] all = ctm2.all(client2.getValues().getPrivateAppointmentFolder(), D("11.08.2015 08:00"), D("11.08.2015 09:00"));
        int exceptionId = 0;
        for (Appointment app : all) {
            if (app.getRecurrenceID() == appointment.getObjectID() && app.getRecurrenceID() != app.getObjectID()) {
                exceptionId = app.getObjectID();
                break;
            }
        }
        assertFalse("Unable to find exception.", exceptionId == 0);
        Appointment loadedException = ctm2.get(client2.getValues().getPrivateAppointmentFolder(), exceptionId);
        assertEquals("Wrong amount of participants.", 2, loadedException.getUsers().length);
        for (UserParticipant up : loadedException.getUsers()) {
            if (up.getIdentifier() == client.getValues().getUserId()) {
                assertEquals("Wrong confirmation status for user: " + up.getIdentifier(), Appointment.ACCEPT, up.getConfirm());
            } else if (up.getIdentifier() == client2.getValues().getUserId()) {
                assertEquals("Wrong confirmation status for user: " + up.getIdentifier(), Appointment.ACCEPT, up.getConfirm());
            }
        }

        Appointment updateAlarm = new Appointment();
        updateAlarm.setObjectID(appointment.getObjectID());
        updateAlarm.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        updateAlarm.setAlarm(15);
        updateAlarm.setLastModified(new Date(Long.MAX_VALUE));
        ctm2.update(updateAlarm);

        loaded = ctm1.get(appointment);
        assertNotNull("Missing change exception.", loaded.getChangeException());
        assertEquals("Wrong amount of change exceptions.", 1, loaded.getChangeException().length);
        assertEquals("Wrong amount of participants.", 2, loaded.getUsers().length);
        for (UserParticipant up : loaded.getUsers()) {
            if (up.getIdentifier() == client.getValues().getUserId()) {
                assertEquals("Wrong confirmation status for user: " + up.getIdentifier(), Appointment.ACCEPT, up.getConfirm());
            } else if (up.getIdentifier() == client2.getValues().getUserId()) {
                assertEquals("Wrong confirmation status for user: " + up.getIdentifier(), Appointment.NONE, up.getConfirm());
            }
        }

        loadedException = ctm2.get(client2.getValues().getPrivateAppointmentFolder(), exceptionId);
        assertEquals("Wrong amount of participants.", 2, loadedException.getUsers().length);
        for (UserParticipant up : loadedException.getUsers()) {
            if (up.getIdentifier() == client.getValues().getUserId()) {
                assertEquals("Wrong confirmation status for user: " + up.getIdentifier(), Appointment.ACCEPT, up.getConfirm());
            } else if (up.getIdentifier() == client2.getValues().getUserId()) {
                assertEquals("Wrong confirmation status for user: " + up.getIdentifier(), Appointment.ACCEPT, up.getConfirm());
            }
        }
    }
    
    public void testSomethingElse() throws Exception {
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setDays(Appointment.TUESDAY + Appointment.WEDNESDAY + Appointment.THURSDAY);
        appointment.setInterval(2);
        ctm1.insert(appointment);
        Appointment loaded = ctm1.get(appointment);
        assertEquals("Wrong start.", D("11.08.2015 08:00"), loaded.getStartDate());
        assertEquals("Wrong end.", D("11.08.2015 09:00"), loaded.getEndDate());
    }

    @Override
    public void tearDown() throws Exception {
        ctm1.cleanUp();
        ctm2.cleanUp();
        super.tearDown();
    }

}
