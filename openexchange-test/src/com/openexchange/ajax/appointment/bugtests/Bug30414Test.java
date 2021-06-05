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
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug30414Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug30414Test extends AbstractAJAXSession {

    private Appointment series;
    private Appointment single;
    private int nextYear;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        single = new Appointment();
        single.setTitle("Bug 30414 single appointment.");
        single.setStartDate(D("03.02." + nextYear + " 08:00"));
        single.setEndDate(D("03.02." + nextYear + " 09:00"));
        single.setIgnoreConflicts(true);
        single.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.insert(single);

        series = new Appointment();
        series.setTitle("Bug 30414 series appointment.");
        series.setStartDate(D("01.02." + nextYear + " 08:00"));
        series.setEndDate(D("01.02." + nextYear + " 09:00"));
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(2);
        series.setOccurrence(3);
        series.setIgnoreConflicts(true);
        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.insert(series);
    }

    @Test
    public void testBug30414() {
        Appointment exception2 = catm.createIdentifyingCopy(series);
        exception2.setStartDate(D("02.02." + nextYear + " 08:00"));
        exception2.setEndDate(D("02.02." + nextYear + " 09:00"));
        exception2.setRecurrencePosition(2);
        Appointment exception = series.clone();
        exception.removeRecurrenceType();
        exception.removeInterval();
        exception.removeOccurrence();
        exception.setIgnoreConflicts(false);
        exception.setStartDate(D("02.02." + nextYear + " 08:00"));
        exception.setEndDate(D("02.02." + nextYear + " 09:00"));
        exception.setRecurrenceType(Appointment.NO_RECURRENCE);
        exception.setRecurrencePosition(2);
        catm.update(exception);

        List<ConflictObject> conflicts = ((UpdateResponse) catm.getLastResponse()).getConflicts();
        boolean foundBadConflict = false;
        if (conflicts != null) {
            for (ConflictObject co : conflicts) {
                if (co.getId() == single.getObjectID()) {
                    foundBadConflict = true;
                    break;
                }
            }
        }
        assertFalse("Found conflict", foundBadConflict);
    }
}
