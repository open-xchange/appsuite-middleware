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
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.common.test.TestClassConfig;


/**
 * {@link Bug53073Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.4
 */
public class Bug53073Test extends AbstractAJAXSession {


    private AJAXClient client;
    private AJAXClient client2;
    private CalendarTestManager catm2;
    private FolderTestManager ftm2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        client = getClient();
        client2 = testUser2.getAjaxClient();
        catm2 = new CalendarTestManager(client2);
        ftm2 = new FolderTestManager(client2);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug53073() throws Exception {
        FolderObject sharedFolder = ftm.generatePrivateFolder("Bug 53073 Shared Folder " + System.currentTimeMillis(), FolderObject.CALENDAR, client.getValues().getPrivateAppointmentFolder(), client.getValues().getUserId());
        OCLPermission permissions = new OCLPermission();
        permissions.setEntity(client2.getValues().getUserId());
        permissions.setGroupPermission(false);
        permissions.setFolderAdmin(false);
        permissions.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        sharedFolder.getPermissions().add(permissions);
        ftm.insertFolderOnServer(sharedFolder);

        FolderObject privateFolder = ftm2.generatePrivateFolder("Bug 53073 Private Folder " + System.currentTimeMillis(), FolderObject.CALENDAR, client2.getValues().getPrivateAppointmentFolder(), client2.getValues().getUserId());
        ftm2.insertFolderOnServer(privateFolder);

        Appointment app = new Appointment();
        app.setTitle("Bug53073");
        app.setStartDate(D("01.05.2017 08:00"));
        app.setEndDate(D("01.05.2017 08:00"));
        app.setPrivateFlag(true);
        app.setIgnoreConflicts(true);
        app.setParentFolderID(sharedFolder.getObjectID());
        catm.insert(app);

        Appointment update = catm2.createIdentifyingCopy(app);
        update.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
        System.out.println("Shared: " + sharedFolder.getObjectID() + ", " + privateFolder.getObjectID() + " -> " + client2.getValues().getPrivateAppointmentFolder());
        //Appointment llll = catm2.get(sharedFolder.getObjectID(), app.getObjectID());
        catm2.update(privateFolder.getObjectID(), update);
        assertTrue("Expected error.", catm2.getLastResponse().hasError());
        assertTrue("Wrong exception", catm2.getLastResponse().getException().similarTo(OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_2.create()));
        System.out.println(catm2.getLastException().getMessage());
    }

}
