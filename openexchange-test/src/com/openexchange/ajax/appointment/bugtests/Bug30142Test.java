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
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug30142Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug30142Test extends AbstractAJAXSession {

    private FolderObject folder;

    private Appointment appointment;

    public Bug30142Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        folder = ftm.generatePrivateFolder("Bug 30142 " + UUID.randomUUID().toString(), FolderObject.CALENDAR, getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId());
        ftm.insertFolderOnServer(folder);

        appointment = new Appointment();
        appointment.setTitle("Bug 30142");
        appointment.setStartDate(D("01.12.2013 08:00"));
        appointment.setEndDate(D("01.12.2013 09:00"));
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setIgnoreConflicts(true);
        catm.insert(appointment);

    }

    @Test
    public void testBug30142() throws Exception {
        Appointment update = catm.createIdentifyingCopy(appointment);
        update.setCategories("Test");
        catm.update(update);

        Appointment loaded = catm.get(folder.getObjectID(), appointment.getObjectID());
        assertEquals("Missing category.", "Test", loaded.getCategories());
        assertEquals("Bad folder id.", loaded.getParentFolderID(), folder.getObjectID());
    }
}
