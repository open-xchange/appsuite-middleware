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
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug31810Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug31810Test extends AbstractAJAXSession {

    private int nextYear;
    private Appointment single;
    private Appointment conflict;

    public Bug31810Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        single = new Appointment();
        single.setTitle("Bug 31810 appointment.");
        single.setStartDate(D("03.03." + nextYear + " 08:00"));
        single.setEndDate(D("03.03." + nextYear + " 09:00"));
        single.setIgnoreConflicts(true);
        single.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.insert(single);

        conflict = new Appointment();
        conflict.setTitle("Bug 31810 appointment.");
        conflict.setStartDate(D("03.03." + nextYear + " 08:00"));
        conflict.setEndDate(D("03.03." + nextYear + " 09:00"));
        conflict.setIgnoreConflicts(false);
        conflict.setShownAs(Appointment.FREE);
        conflict.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.insert(conflict);
    }

    @Test
    public void testBug31810() {
        conflict.setFullTime(true);
        conflict.removeShownAs();
        catm.update(conflict);
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();
        assertFalse("No conflict expected.", conflicts != null && conflicts.size() > 1);
    }
}
