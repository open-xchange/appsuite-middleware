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

package com.openexchange.ajax.appointment.recurrence;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForModifyingChangeExceptions extends ManagedAppointmentTest {

    private final int exceptionPosition = 2;

    private Changes changes;

    private Appointment update;

    private Appointment app;

    public TestsForModifyingChangeExceptions() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        app = generateDailyAppointment();
        app.setOccurrence(3);

        catm.insert(app);

        changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, I(exceptionPosition));
        changes.put(Appointment.START_DATE, D("2/1/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("2/1/2008 2:00", utc));

        update = app.clone();
        changes.update(update);
        catm.update(update);

    }

    @Test
    public void testShouldNotAllowTurningAChangeExceptionIntoASeries() {
        Changes secondChange = new Changes();
        secondChange.put(Appointment.RECURRENCE_TYPE, I(Appointment.DAILY));
        secondChange.put(Appointment.INTERVAL, I(1));

        Appointment secondUpdate = new Appointment();
        secondUpdate.setParentFolderID(update.getParentFolderID());
        secondUpdate.setObjectID(update.getObjectID());
        secondUpdate.setLastModified(update.getLastModified());
        secondChange.update(secondUpdate);

        catm.update(secondUpdate);

        assertTrue("Should get exception when trying to make a change exception a series", catm.hasLastException());
        int code = ((OXException) catm.getLastException()).getCode();
        assertTrue("Should have correct exception", 99 == code || 4035 == code);
    }

    @Test
    public void testDeletingAChangeException() {
        Appointment secondUpdate = new Appointment();
        secondUpdate.setParentFolderID(update.getParentFolderID());
        secondUpdate.setObjectID(update.getObjectID());
        secondUpdate.setLastModified(update.getLastModified());
        secondUpdate.setRecurrencePosition(exceptionPosition);
        secondUpdate.setRecurrenceType(Appointment.MONTHLY);

        catm.delete(secondUpdate);

        assertFalse("Should get no error when trying to delete a change exception", catm.hasLastException());
    }
}
