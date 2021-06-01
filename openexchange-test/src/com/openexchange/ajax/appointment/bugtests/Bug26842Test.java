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
import com.openexchange.groupware.container.Participant;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug26842Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug26842Test extends AbstractAJAXSession {

    private FolderObject folder;

    public Bug26842Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = ftm.generatePublicFolder("26842-" + UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.SYSTEM_PUBLIC_FOLDER_ID);

        OCLPermission permission = new OCLPermission();
        permission.setEntity(getClient().getValues().getUserId());
        permission.setGroupPermission(false);
        permission.setFolderAdmin(true);
        permission.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        folder.addPermission(permission);

        ftm.insertFolderOnServer(folder);
    }

    @Test
    public void testBug() throws Exception {
        Appointment app = new Appointment();
        app.setTitle("Bug 26842 Test");
        app.setStartDate(D("29.05.2013 08:00"));
        app.setEndDate(D("29.05.2013 08:00"));
        app.setParticipants(new Participant[] {});
        app.setParentFolderID(folder.getObjectID());
        app.setIgnoreConflicts(true);

        catm.insert(app);
        Appointment appointment = catm.get(app.getParentFolderID(), app.getObjectID());
        assertEquals("Wrong participants.", getClient().getValues().getUserId(), appointment.getParticipants()[0].getIdentifier());
    }

}
