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
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug35687Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug35687Test extends AbstractAJAXSession {

    private FolderObject folder;
    private Appointment app;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        folder = ftm.generateSharedFolder("Bug35687Folder" + UUID.randomUUID().toString(), FolderObject.CALENDAR, getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId(), testUser2.getAjaxClient().getValues().getUserId());
        folder = ftm.insertFolderOnServer(folder);

        catm.setClient(testUser2.getAjaxClient());

        int nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        app = new Appointment();
        app.setTitle("Bug 35687 Test");
        app.setStartDate(D("16.12." + nextYear + " 08:00"));
        app.setEndDate(D("16.12." + nextYear + " 09:00"));
        app.setParentFolderID(folder.getObjectID());
        app.setAlarm(15);
        app.setIgnoreConflicts(true);

        app = catm.insert(app);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug35687() throws Exception {
        Appointment loaded = catm.get(app);
        assertEquals("Wrong alarm value", 15, loaded.getAlarm());

        List<Appointment> listAppointment = catm.list(new ListIDs(folder.getObjectID(), app.getObjectID()), new int[] { Appointment.ALARM });
        assertTrue("Missing alarm value for list request.", listAppointment.get(0).containsAlarm());
        assertEquals("Wrong alarm value for list request.", 15, listAppointment.get(0).getAlarm());
    }
}
