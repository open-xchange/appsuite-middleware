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
import java.util.Calendar;
import java.util.Date;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug35610Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug35610Test extends AbstractAJAXSession {

    private AJAXClient client2;

    private CalendarTestManager ctm1;

    private CalendarTestManager ctm2;

    private Appointment app;

    private int nextYear;

    public Bug35610Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        client2 = new AJAXClient(User.User2);
        ctm1 = new CalendarTestManager(getClient());
        ctm2 = new CalendarTestManager(client2);

        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        app = new Appointment();
        app.setTitle("Bug 35610 Test");
        app.setStartDate(D("26.11." + nextYear + " 08:00"));
        app.setEndDate(D("26.11." + nextYear + " 09:00"));
        app.setRecurrenceType(Appointment.DAILY);
        app.setInterval(1);
        app.setOccurrence(5);
        UserParticipant user1 = new UserParticipant(client.getValues().getUserId());
        UserParticipant user2 = new UserParticipant(client2.getValues().getUserId());
        app.setParticipants(new Participant[] { user1, user2 });
        app.setUsers(new UserParticipant[] { user1, user2 });
        app.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        app.setIgnoreConflicts(true);
    }

    public void testTimeChange() throws Exception {
        ctm1.insert(app);
        ctm2.confirm(app, Appointment.ACCEPT, "yay");

        Appointment exception = ctm1.createIdentifyingCopy(app);
        exception.setRecurrenceType(Appointment.NO_RECURRENCE);
        exception.removeInterval();
        exception.removeOccurrence();
        exception.setRecurrencePosition(3);
        exception.setStartDate(D("28.11." + nextYear + " 12:00"));
        exception.setEndDate(D("28.11." + nextYear + " 13:00"));
        exception.setLastModified(new Date(Long.MAX_VALUE));
        exception.setIgnoreConflicts(true);

        ctm1.update(exception);

        Appointment loadedException = ctm1.get(exception);
        for (UserParticipant up : loadedException.getUsers()) {
            if (up.getIdentifier() == client.getValues().getUserId()) {
                assertEquals("Wrong confirmation status.", Appointment.ACCEPT, up.getConfirm());
            }
            if (up.getIdentifier() == client2.getValues().getUserId()) {
                assertEquals("Wrong confirmation status.", Appointment.NONE, up.getConfirm());
            }
        }
    }

    public void testNoTimeChange() throws Exception {
        ctm1.insert(app);
        ctm2.confirm(app, Appointment.ACCEPT, "yay");

        Appointment exception = ctm1.createIdentifyingCopy(app);
        exception.setRecurrenceType(Appointment.NO_RECURRENCE);
        exception.removeInterval();
        exception.removeOccurrence();
        exception.setRecurrencePosition(3);
        exception.setStartDate(D("28.11." + nextYear + " 08:00"));
        exception.setEndDate(D("28.11." + nextYear + " 09:00"));
        exception.setLastModified(new Date(Long.MAX_VALUE));
        exception.setIgnoreConflicts(true);

        ctm1.update(exception);

        Appointment loadedException = ctm1.get(exception);
        for (UserParticipant up : loadedException.getUsers()) {
            if (up.getIdentifier() == client.getValues().getUserId()) {
                assertEquals("Wrong confirmation status.", Appointment.ACCEPT, up.getConfirm());
            }
            if (up.getIdentifier() == client2.getValues().getUserId()) {
                assertEquals("Wrong confirmation status.", Appointment.ACCEPT, up.getConfirm());
            }
        }
    }

    @Override
    public void tearDown() throws Exception {
        ctm1.cleanUp();
        ctm2.cleanUp();
        super.tearDown();
    }

}
