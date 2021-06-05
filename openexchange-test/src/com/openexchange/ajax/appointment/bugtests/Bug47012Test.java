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
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug38079Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.2
 */
public class Bug47012Test extends AbstractAJAXSession {

    private Appointment app;

    public Bug47012Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        app = new Appointment();
        app.setTitle("Bug 47012 Test");
        app.setStartDate(TimeTools.D("07.07.2016 08:00"));
        app.setEndDate(TimeTools.D("07.07.2016 09:00"));
        app.setRecurrenceType(Appointment.WEEKLY);
        app.setDays(Appointment.THURSDAY);
        app.setInterval(1);
        app.setIgnoreConflicts(true);
        app.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.insert(app);
    }

    @Test
    public void testChangeStartDate() throws Exception {
        Appointment changeException = catm.createIdentifyingCopy(app);
        changeException.setRecurrencePosition(1);
        changeException.setStartDate(TimeTools.D("07.07.2016 08:30"));
        changeException.setIgnoreConflicts(true);
        catm.update(changeException);

        Appointment loadedException = catm.get(changeException.getParentFolderID(), changeException.getObjectID());
        assertEquals("Wrong start date.", changeException.getStartDate(), loadedException.getStartDate());
        assertEquals("Wrong end date.", TimeTools.D("07.07.2016 09:00"), loadedException.getEndDate());
    }

    @Test
    public void testChangeEndDate() throws Exception {
        Appointment changeException = catm.createIdentifyingCopy(app);
        changeException.setRecurrencePosition(1);
        changeException.setEndDate(TimeTools.D("07.07.2016 09:30"));
        changeException.setIgnoreConflicts(true);
        catm.update(changeException);

        Appointment loadedException = catm.get(changeException.getParentFolderID(), changeException.getObjectID());
        assertEquals("Wrong start date.", TimeTools.D("07.07.2016 08:00"), loadedException.getStartDate());
        assertEquals("Wrong end date.", changeException.getEndDate(), loadedException.getEndDate());
    }

    @Test
    public void testChangeStartAndEndDate() throws Exception {
        Appointment changeException = catm.createIdentifyingCopy(app);
        changeException.setRecurrencePosition(1);
        changeException.setStartDate(TimeTools.D("07.07.2016 08:30"));
        changeException.setEndDate(TimeTools.D("07.07.2016 09:30"));
        changeException.setIgnoreConflicts(true);
        catm.update(changeException);

        Appointment loadedException = catm.get(changeException.getParentFolderID(), changeException.getObjectID());
        assertEquals("Wrong start date.", changeException.getStartDate(), loadedException.getStartDate());
        assertEquals("Wrong end date.", changeException.getEndDate(), loadedException.getEndDate());
    }

    @Test
    public void testMakeFulltime() throws Exception {
        Appointment changeException = catm.createIdentifyingCopy(app);
        changeException.setRecurrencePosition(1);
        changeException.setFullTime(true);
        changeException.setIgnoreConflicts(true);
        catm.update(changeException);

        Appointment loadedException = catm.get(changeException.getParentFolderID(), changeException.getObjectID());
        assertTrue("Expected fulltime appointment.", loadedException.getFullTime());
        assertEquals("Wrong start date.", TimeTools.D("07.07.2016 00:00"), loadedException.getStartDate());
        assertEquals("Wrong end date.", TimeTools.D("08.07.2016 00:00"), loadedException.getEndDate());
    }
}
