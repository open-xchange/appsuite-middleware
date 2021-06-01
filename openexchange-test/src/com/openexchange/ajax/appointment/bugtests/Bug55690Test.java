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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug55690Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Bug55690Test extends AbstractAJAXSession {

    private AJAXClient client;
    private AJAXClient client2;
    private CalendarTestManager catm2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        client = getClient();
        client2 = testUser2.getAjaxClient();
        catm2 = new CalendarTestManager(client2);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug() throws Exception {
        FolderObject sharedFolder = ftm.generateSharedFolder("Bug 53714 Folder " + System.currentTimeMillis(), FolderObject.CALENDAR, client.getValues().getPrivateAppointmentFolder(), client.getValues().getUserId());
        ftm.insertFolderOnServer(sharedFolder);
        Appointment appointment = new Appointment();
        appointment.setTitle("Bug 53714 test");
        appointment.setStartDate(D("01.06.2017 08:00"));
        appointment.setEndDate(D("01.06.2017 08:00"));
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        appointment.setParticipants(new Participant[] { new UserParticipant(getClient().getValues().getUserId()), new UserParticipant(client2.getValues().getUserId()) });
        catm2.insert(appointment);

        Appointment loadForUpdate = catm.get(client.getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        loadForUpdate.setParentFolderID(sharedFolder.getObjectID());
        catm.update(client.getValues().getPrivateAppointmentFolder(), loadForUpdate);

        ftm.deleteFolderOnServer(sharedFolder);
        assertFalse("No exception expected", ftm.getLastResponse().hasWarnings() || ftm.getLastResponse().hasError());
    }

}
