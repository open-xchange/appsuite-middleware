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
import static org.junit.Assert.assertEquals;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug41995Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class Bug41995Test extends AbstractAJAXSession {

    private CalendarTestManager ctm2;
    private Appointment appointment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        catm.setFailOnError(true);
        ctm2 = new CalendarTestManager(testUser2.getAjaxClient());
        ctm2.setFailOnError(true);

        SetRequest setRequest = new SetRequest(Tree.TimeZone, "America/New_York");
        getClient().execute(setRequest);
        catm.setTimezone(TimeZone.getTimeZone("America/New_York"));
        setRequest = new SetRequest(Tree.TimeZone, "Europe/Berlin");
        testUser2.getAjaxClient().execute(setRequest);
        ctm2.setTimezone(TimeZone.getTimeZone("Europe/Berlin"));

        appointment = new Appointment();
        appointment.setTitle("Bug 41995 Test");
        appointment.setStartDate(D("01.11.2016 07:00"));
        appointment.setEndDate(D("01.11.2016 08:00"));
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setInterval(1);
        appointment.setDays(Appointment.TUESDAY);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setParticipants(new Participant[] { new UserParticipant(getClient().getValues().getUserId()), new UserParticipant(testUser2.getAjaxClient().getValues().getUserId()) });

        catm.insert(appointment);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug41995() throws Exception {
        Appointment update = new Appointment();
        update.setObjectID(appointment.getObjectID());
        update.setLastModified(new Date(Long.MAX_VALUE));
        update.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        update.setAlarm(15);
        ctm2.update(update);

        Appointment loaded = catm.get(getClient().getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        assertEquals("Wrong start date.", appointment.getStartDate(), loaded.getStartDate());
        assertEquals("Wrong end date.", appointment.getEndDate(), loaded.getEndDate());
    }

}
