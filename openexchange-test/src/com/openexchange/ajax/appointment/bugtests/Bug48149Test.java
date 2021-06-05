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

import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug38079Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.3
 */
public class Bug48149Test extends AbstractAJAXSession {

    private AJAXClient client3;
    private CalendarTestManager ctm2;
    private CalendarTestManager ctm3;
    private FolderTestManager ftm2;
    private FolderObject sharedFolder1;
    private Appointment app1;
    private Appointment app2;

    public Bug48149Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client3 = testContext.acquireUser().getAjaxClient();
        ctm2 = new CalendarTestManager(testUser2.getAjaxClient());
        ctm3 = new CalendarTestManager(client3);
        ftm2 = new FolderTestManager(testUser2.getAjaxClient());

        // Remove all permissions
        FolderObject folderUpdate = new FolderObject(getClient().getValues().getPrivateAppointmentFolder());
        folderUpdate.setPermissionsAsArray(new OCLPermission[] { com.openexchange.ajax.folder.Create.ocl(
            getClient().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) });
        folderUpdate.setLastModified(new Date(Long.MAX_VALUE));
        ftm.updateFolderOnServer(folderUpdate);

        folderUpdate = new FolderObject(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        folderUpdate.setPermissionsAsArray(new OCLPermission[] { com.openexchange.ajax.folder.Create.ocl(
            testUser2.getAjaxClient().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) });
        folderUpdate.setLastModified(new Date(Long.MAX_VALUE));
        ftm2.updateFolderOnServer(folderUpdate);

        // Add new shared folder.
        sharedFolder1 = ftm.generateSharedFolder("Shared Folder" + UUID.randomUUID().toString(), FolderObject.CALENDAR, getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId(), client3.getValues().getUserId());
        ftm.insertFolderOnServer(sharedFolder1);

        // Appointments not visible for user 3.
        app1 = new Appointment();
        app1.setTitle("app1");
        app1.setStartDate(TimeTools.D("07.08.2016 08:00"));
        app1.setEndDate(TimeTools.D("07.08.2016 09:00"));
        app1.setIgnoreConflicts(true);
        app1.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.insert(app1);

        app2 = new Appointment();
        app2.setTitle("app1");
        app2.setStartDate(TimeTools.D("07.08.2016 08:00"));
        app2.setEndDate(TimeTools.D("07.08.2016 09:00"));
        app2.setIgnoreConflicts(true);
        app2.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        ctm2.insert(app2);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(3).build();
    }

    @Test
    public void testLoadAppointmentFromUserWithShared() {
        try {
            ctm3.get(sharedFolder1.getObjectID(), app1.getObjectID());
        } catch (Exception e) {
            // ignore
        }
        assertTrue("Expected error.", ctm3.getLastResponse().hasError());
        assertTrue("Excpected something with permissions. (" + ctm3.getLastResponse().getErrorMessage() + ")", ctm3.getLastResponse().getErrorMessage().contains("ermission"));
    }

    @Test
    public void testLoadAppointmentFromUserWithoutAnyShares() {
        try {
            ctm3.get(sharedFolder1.getObjectID(), app2.getObjectID());
        } catch (Exception e) {
            // ignore
        }
        assertTrue("Expected error.", ctm3.getLastResponse().hasError());
        assertTrue("Excpected something with permissions. (" + ctm3.getLastResponse().getErrorMessage() + ")", ctm3.getLastResponse().getErrorMessage().contains("ermission"));
    }

}
