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

import static com.openexchange.ajax.folder.Create.ocl;
import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertTrue;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.SearchRequest;
import com.openexchange.ajax.appointment.action.SearchResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug16476Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug16476Test extends AbstractAJAXSession {

    private Appointment appointment;
    private AJAXClient clientA;
    private AJAXClient clientB;
    private FolderObject folder;

    /**
     * Initializes a new {@link Bug16476Test}.
     *
     * @param name
     */
    public Bug16476Test() {
        super();

    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        clientA = getClient();
        clientB = testUser2.getAjaxClient();

        folder = Create.folder(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, "Folder to test bug 16476" + System.currentTimeMillis(), FolderObject.CALENDAR, FolderObject.PRIVATE, ocl(clientA.getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION), ocl(clientB.getValues().getUserId(), false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION));

        folder = ftm.insertFolderOnServer(folder);

        appointment = new Appointment();
        appointment.setStartDate(D("01.06.2010 08:00"));
        appointment.setEndDate(D("01.06.2010 09:00"));
        appointment.setTitle("Bug 16476 Test");
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setPrivateFlag(true);
        appointment.setIgnoreConflicts(true);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testBug16476() throws Exception {
        InsertRequest insert = new InsertRequest(appointment, clientA.getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = clientA.execute(insert);
        insertResponse.fillAppointment(appointment);

        SearchRequest search = new SearchRequest("*", folder.getObjectID(), new int[] { Appointment.OBJECT_ID });
        SearchResponse searchResponse = clientB.execute(search);
        JSONArray jsonArray = (JSONArray) searchResponse.getResponse().getData();
        assertTrue("No results expected", jsonArray.length() == 0);
    }
}
