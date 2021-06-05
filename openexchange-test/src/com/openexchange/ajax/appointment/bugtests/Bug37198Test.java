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

import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug37198Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Bug37198Test extends AbstractAJAXSession {

    private Appointment app;
    private TimeZone utc;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        app = new Appointment();
        app.setTitle("Bug 37198 Test");
        utc = TimeZone.getTimeZone("UTC");
        app.setStartDate(TimeTools.D("12.03.2015 00:00", utc));
        app.setEndDate(TimeTools.D("13.03.2015 00:00", utc));
        app.setFullTime(true);
        app.setRecurrenceType(Appointment.DAILY);
        app.setUntil(TimeTools.D("13.03.2015 00:00", utc));
        app.setInterval(1);
        app.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        app.setIgnoreConflicts(true);
    }

    @Test
    public void testBug37198() {
        catm.insert(app);

        Appointment update = new Appointment();
        update.setObjectID(app.getObjectID());
        update.setParentFolderID(app.getParentFolderID());
        update.setEndDate(TimeTools.D("14.03.2015 00:00", utc));
        update.setRecurrenceType(Appointment.NO_RECURRENCE);
        update.setLastModified(new Date(Long.MAX_VALUE));
        update.setIgnoreConflicts(true);

        catm.update(update);
    }
}
