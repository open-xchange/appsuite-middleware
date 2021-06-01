/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertFalse;
import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.AbstractResourceAwareAjaxSession;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.resource.ResourceTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug35355Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug35355Test extends AbstractResourceAwareAjaxSession {

    private CalendarTestManager ctm3;

    private Appointment appointment;

    private AJAXClient client3;

    private Appointment exception;

    private Appointment blockingApp;

    private UserParticipant up1;

    private UserParticipant up2;

    private UserParticipant up3;

    private ResourceParticipant resourceParticipant;

    private int nextYear;

    public Bug35355Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client3 = testContext.acquireUser().getAjaxClient();

        up1 = new UserParticipant(getClient().getValues().getUserId());
        up2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        up3 = new UserParticipant(client3.getValues().getUserId());
        resourceParticipant = new ResourceParticipant(ResourceTools.getSomeResource(getClient()));

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
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setParticipants(new Participant[] { up1, up2, resourceParticipant });
        catm.insert(appointment);

        // Remove resource on a specific exception
        exception = new Appointment();
        exception.setTitle("Bug 35355 Exception");
        exception.setObjectID(appointment.getObjectID());
        exception.setStartDate(D("07.11." + nextYear + " 08:00"));
        exception.setEndDate(D("07.11." + nextYear + " 09:00"));
        exception.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        exception.setLastModified(new Date(Long.MAX_VALUE));
        exception.setRecurrencePosition(2);
        exception.setParticipants(new Participant[] { up1, up2 });
        exception.setIgnoreConflicts(true);
        catm.update(exception);

        // Third party creates appointment with resource on exception position
        blockingApp = new Appointment();
        blockingApp.setTitle("Bug 35355 Blocking Appointment");
        blockingApp.setStartDate(D("07.11." + nextYear + " 07:00"));
        blockingApp.setEndDate(D("07.11." + nextYear + " 10:00"));
        blockingApp.setParticipants(new Participant[] { up3, resourceParticipant });
        blockingApp.setIgnoreConflicts(true);
        blockingApp.setParentFolderID(client3.getValues().getPrivateAppointmentFolder());
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug35355() throws Exception {
        ctm3.insert(blockingApp);
        assertFalse(ctm3.getLastResponse().hasConflicts());
        assertFalse(ctm3.getLastResponse().hasError());

        Appointment updateSeries = new Appointment();
        updateSeries.setObjectID(appointment.getObjectID());
        updateSeries.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        updateSeries.setStartDate(D("06.11." + nextYear + " 08:00"));
        updateSeries.setEndDate(D("06.11." + nextYear + " 09:00"));
        updateSeries.setRecurrenceType(Appointment.DAILY);
        updateSeries.setInterval(1);
        updateSeries.setOccurrence(3);
        updateSeries.setLastModified(new Date(Long.MAX_VALUE));
        updateSeries.setParticipants(new Participant[] { up1, resourceParticipant });
        catm.update(updateSeries);
        assertFalse("No conflict expected.", catm.getLastResponse().hasConflicts());
    }

}
