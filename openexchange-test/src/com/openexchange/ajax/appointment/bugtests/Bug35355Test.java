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
import com.openexchange.ajax.resource.ResourceTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug35355Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug35355Test extends AbstractAJAXSession {

    private CalendarTestManager ctm1;

    private CalendarTestManager ctm3;

    private Appointment appointment;

    private AJAXClient client2;

    private AJAXClient client3;

    private Appointment exception;

    private Appointment blockingApp;

    private UserParticipant up1;

    private UserParticipant up2;

    private UserParticipant up3;

    private ResourceParticipant resourceParticipant;

    private int nextYear;

    public Bug35355Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        client2 = new AJAXClient(User.User2);
        client3 = new AJAXClient(User.User3);

        up1 = new UserParticipant(client.getValues().getUserId());
        up2 = new UserParticipant(client2.getValues().getUserId());
        up3 = new UserParticipant(client3.getValues().getUserId());
        resourceParticipant = new ResourceParticipant(ResourceTools.getSomeResource(client));

        ctm1 = new CalendarTestManager(client);
        ctm1.setFailOnError(true);
        ctm3 = new CalendarTestManager(client3);
        ctm3.setFailOnError(true);

        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        // Series appointment with resource
        appointment = new Appointment();
        appointment.setTitle("Bug 35355 Test");
        appointment.setStartDate(D("06.11." + nextYear + " 08:00"));
        appointment.setEndDate(D("06.11." + nextYear + " 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(3);
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setParticipants(new Participant[] { up1, up2, resourceParticipant });
        ctm1.insert(appointment);

        // Remove resource on a specific exception
        exception = new Appointment();
        exception.setTitle("Bug 35355 Exception");
        exception.setObjectID(appointment.getObjectID());
        exception.setStartDate(D("07.11." + nextYear + " 08:00"));
        exception.setEndDate(D("07.11." + nextYear + " 09:00"));
        exception.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        exception.setLastModified(new Date(Long.MAX_VALUE));
        exception.setRecurrencePosition(2);
        exception.setParticipants(new Participant[] { up1, up2 });
        exception.setIgnoreConflicts(true);
        ctm1.update(exception);

        // Third party creates appointment with resource on exception position
        blockingApp = new Appointment();
        blockingApp.setTitle("Bug 35355 Blocking Appointment");
        blockingApp.setStartDate(D("07.11." + nextYear + " 07:00"));
        blockingApp.setEndDate(D("07.11." + nextYear + " 10:00"));
        blockingApp.setParticipants(new Participant[] { up3, resourceParticipant });
        blockingApp.setIgnoreConflicts(true);
        blockingApp.setParentFolderID(client3.getValues().getPrivateAppointmentFolder());
    }

    public void testBug35355() throws Exception {
        ctm3.insert(blockingApp);
        assertFalse(ctm3.getLastResponse().hasConflicts());
        assertFalse(ctm3.getLastResponse().hasError());

        Appointment updateSeries = new Appointment();
        updateSeries.setObjectID(appointment.getObjectID());
        updateSeries.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        updateSeries.setStartDate(D("06.11." + nextYear + " 08:00"));
        updateSeries.setEndDate(D("06.11." + nextYear + " 09:00"));
        updateSeries.setRecurrenceType(Appointment.DAILY);
        updateSeries.setInterval(1);
        updateSeries.setOccurrence(3);
        updateSeries.setLastModified(new Date(Long.MAX_VALUE));
        updateSeries.setParticipants(new Participant[] { up1, resourceParticipant });
        ctm1.update(updateSeries);
        assertFalse("No conflict expected.", ctm1.getLastResponse().hasConflicts());
    }

    @Override
    public void tearDown() throws Exception {
        ctm1.cleanUp();
        ctm3.cleanUp();
        super.tearDown();
    }

}
