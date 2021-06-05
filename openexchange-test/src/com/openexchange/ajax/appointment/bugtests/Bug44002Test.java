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

import static org.junit.Assert.fail;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug44002Test}
 * 
 * Ui change (don't send all appointment fields on update but only real changes) revealed this bug.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class Bug44002Test extends AbstractAJAXSession {

    private Appointment conflict;
    private Appointment series;

    public Bug44002Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
       
        conflict = new Appointment();
        series = new Appointment();

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        c.add(Calendar.DAY_OF_MONTH, 7);
        c.set(Calendar.HOUR_OF_DAY, 9);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        // Series next Thursday, weekly
        series.setTitle("Series next Thursday, weekly.");
        series.setStartDate(new Date(c.getTimeInMillis()));
        c.add(Calendar.HOUR_OF_DAY, 1);
        series.setEndDate(new Date(c.getTimeInMillis()));
        series.setRecurrenceType(Appointment.WEEKLY);
        series.setDays(Appointment.THURSDAY);
        series.setInterval(1);
        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        series.setIgnoreConflicts(true);
        series.setAlarm(15);

        conflict.setTitle("Not matching appointment");
        c.add(Calendar.DAY_OF_MONTH, 1);
        conflict.setStartDate(new Date(c.getTimeInMillis()));
        c.add(Calendar.HOUR_OF_DAY, 1);
        conflict.setEndDate(new Date(c.getTimeInMillis()));
        conflict.setIgnoreConflicts(true);
        conflict.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        conflict.setAlarm(15);

        catm.insert(series);
        catm.insert(conflict);
    }

    /**
     */
    @Test
    public void testBug44002() {
        boolean old = false; // Switch to old UI behaviour for debugging purposes
        if (old) {
            series.setRecurrenceType(Appointment.NO_RECURRENCE);
            series.removeInterval();
            series.removeDays();
            catm.update(series);
        } else {
            Appointment updateForSeries = new Appointment();
            updateForSeries.setParentFolderID(series.getParentFolderID());
            updateForSeries.setObjectID(series.getObjectID());
            updateForSeries.setRecurrenceType(Appointment.NO_RECURRENCE);
            updateForSeries.setLastModified(series.getLastModified());
            updateForSeries.setIgnoreConflicts(false);
            catm.update(updateForSeries);
        }
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();
        if (conflicts != null) {
            for (ConflictObject conf : conflicts) {
                if (conf.getId() == conflict.getObjectID()) {
                    fail("Should not conflict with appointment.");
                }
            }
        }
    }
}
