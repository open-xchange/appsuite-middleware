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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.AbstractResourceAwareAjaxSession;
import com.openexchange.ajax.resource.ResourceTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug39571Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Bug39571Test extends AbstractResourceAwareAjaxSession {

    private Appointment series;
    private int nextYear;
    private Appointment single;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        UserParticipant up1 = new UserParticipant(getClient().getValues().getUserId());
        ResourceParticipant resourceParticipant = new ResourceParticipant(ResourceTools.getSomeResource(getClient()));

        series = new Appointment();
        series.setTitle("Bug 39571 Series");
        series.setStartDate(TimeTools.D("01.08." + nextYear + " 08:00"));
        series.setEndDate(TimeTools.D("01.08." + nextYear + " 08:30"));
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(1);
        series.setOccurrence(3);
        series.setParticipants(new Participant[] { up1, resourceParticipant });
        series.setIgnoreConflicts(true);
        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.insert(series);

        single = new Appointment();
        single.setTitle("Bug 39571 Single");
        single.setStartDate(TimeTools.D("02.08." + nextYear + " 09:00"));
        single.setEndDate(TimeTools.D("02.08." + nextYear + " 09:30"));
        single.setParticipants(new Participant[] { up1, resourceParticipant });
        single.setIgnoreConflicts(true);
        single.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.insert(single);
    }

    @Test
    public void testBug39571() throws Exception {
        Appointment exception = catm.createIdentifyingCopy(series);
        exception.setStartDate(TimeTools.D("02.08." + nextYear + " 06:00"));
        exception.setEndDate(TimeTools.D("02.08." + nextYear + " 06:30"));
        exception.setRecurrencePosition(2);
        catm.update(exception);

        series.setStartDate(TimeTools.D("01.08." + nextYear + " 09:00"));
        series.setEndDate(TimeTools.D("01.08." + nextYear + " 09:30"));
        series.setLastModified(new Date(Long.MAX_VALUE));
        catm.update(series);

        if (false == catm.getLastResponse().hasConflicts()) {
            // new implementation might leave existing exception unchanged, so expect no conflict if still there
            Appointment reloadedException = catm.get(exception);
            assertNotNull(reloadedException);
            assertEquals(exception.getStartDate(), reloadedException.getStartDate());
            assertEquals(exception.getEndDate(), reloadedException.getEndDate());
        } else {
            assertTrue("Excpected conflicting ressource.", catm.getLastResponse().hasConflicts());
        }
    }
}
