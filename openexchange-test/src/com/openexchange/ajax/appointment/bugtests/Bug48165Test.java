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

import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug42018Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class Bug48165Test extends AbstractAJAXSession {

    private CalendarTestManager ctm2;
    private Appointment         conflict;
    private Appointment         series;
    private int                 nextYear;

    public Bug48165Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        ctm2 = new CalendarTestManager(testUser2.getAjaxClient());

        conflict = new Appointment();
        conflict.setTitle("Bug 48165 Test - conflict");
        conflict.setStartDate(TimeTools.D("03.08." + nextYear + " 11:00"));
        conflict.setEndDate(TimeTools.D("03.08." + nextYear + " 12:00"));
        conflict.setIgnoreConflicts(true);
        conflict.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());

        series = new Appointment();
        series.setTitle("Bug 48165 Test - series");
        series.setStartDate(TimeTools.D("01.08." + nextYear + " 09:00"));
        series.setEndDate(TimeTools.D("01.08." + nextYear + " 10:00"));
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(1);
        series.setIgnoreConflicts(true);
        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        series.setParticipants(new Participant[] { new UserParticipant(getClient().getValues().getUserId()), new UserParticipant(testUser2.getAjaxClient().getValues().getUserId()) });
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug48165() {
        ctm2.insert(conflict);
        catm.insert(series);

        Appointment exception = new Appointment();
        exception.setObjectID(series.getObjectID());
        exception.setRecurrenceID(series.getObjectID()); // This is crucial
        exception.setRecurrenceType(Appointment.NO_RECURRENCE); // This is crucial
        exception.setParentFolderID(series.getParentFolderID());
        exception.setLastModified(series.getLastModified());
        exception.setRecurrencePosition(3);
        exception.setStartDate(TimeTools.D("03.08." + nextYear + " 11:00"));
        exception.setEndDate(TimeTools.D("03.08." + nextYear + " 12:00"));
        exception.setIgnoreConflicts(false);

        catm.update(exception);
        assertTrue("Expect conflicts.", catm.getLastResponse().hasConflicts());
        boolean found = false;
        for (ConflictObject conflictObject : catm.getLastResponse().getConflicts()) {
            if (conflictObject.getId() == conflict.getObjectID()) {
                found = true;
                break;
            }
        }
        assertTrue("Expect conflict.", found);
    }

}
