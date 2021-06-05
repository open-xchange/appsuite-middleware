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
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link FolderIdTestAjax}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class FolderIdTestAjax extends AbstractAJAXSession {

    private Appointment appointment;

    private FolderObject folderA;

    private FolderObject folderB;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderA = ftm.generateSharedFolder("folder A" + UUID.randomUUID().toString(), Module.CALENDAR.getFolderConstant(), getClient().getValues().getPrivateAppointmentFolder(), new int[] { getClient().getValues().getUserId(), testUser2.getAjaxClient().getValues().getUserId() });
        ftm.insertFolderOnServer(folderA);
        folderB = ftm.generateSharedFolder("folder B" + UUID.randomUUID().toString(), Module.CALENDAR.getFolderConstant(), getClient().getValues().getPrivateAppointmentFolder(), new int[] { getClient().getValues().getUserId(), testUser2.getAjaxClient().getValues().getUserId() });
        ftm.insertFolderOnServer(folderB);

        appointment = new Appointment();
        appointment.setParentFolderID(folderA.getObjectID());
        appointment.setTitle("Folder Id");
        appointment.setStartDate(D("26.12.2013 08:00"));
        appointment.setEndDate(D("27.12.2013 09:00"));
        appointment.setIgnoreConflicts(true);
        catm.insert(appointment);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testSomething() throws Exception {
        catm.setClient(testUser2.getAjaxClient());
        appointment.setParentFolderID(folderB.getObjectID());
        catm.update(folderA.getObjectID(), appointment);
        Appointment loaded = catm.get(folderB.getObjectID(), appointment.getObjectID());
        System.out.println(loaded.getTitle());
    }
}
