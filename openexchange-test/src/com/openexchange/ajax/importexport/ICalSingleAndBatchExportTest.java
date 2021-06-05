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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Date;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalExportRequest;
import com.openexchange.ajax.importexport.actions.ICalExportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;


/**
 * {@link ICalSingleAndBatchExportTest}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10
 */
public class ICalSingleAndBatchExportTest extends ManagedAppointmentTest {

    @Test
    public void testICalSingleAppointment() throws OXException, IOException, JSONException {
        final String title = "testExportICalAppointment" + System.currentTimeMillis();
        int folderID = folder.getObjectID();
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle(title);
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date());
        appointmentObj.setShownAs(Appointment.RESERVED);
        appointmentObj.setParentFolderID(folderID);
        appointmentObj.setIgnoreConflicts(true);
        int objId = catm.insert(appointmentObj).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, objId));
        String body = array.toString();

        ICalExportResponse response = getClient().execute(new ICalExportRequest(-1, -1, body));

        String iCal = response.getICal();
        assertTrue(iCal.contains(title));
        assertFileName(response.getHttpResponse(), appointmentObj.getTitle()+".ics");
    }

    @Test
    public void testICalMultipleExport() throws JSONException, OXException, IOException {
        final String title = "testExportICalAppointment" + System.currentTimeMillis();
        int folderID = folder.getObjectID();
        Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle(title);
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date());
        appointmentObj.setShownAs(Appointment.RESERVED);
        appointmentObj.setParentFolderID(folderID);
        appointmentObj.setIgnoreConflicts(true);

        int objId = catm.insert(appointmentObj).getObjectID();

        final String title2 = "testExportICalAppointment" + System.currentTimeMillis();
        int secondFolderID = folder.getObjectID();
        appointmentObj = new Appointment();
        appointmentObj.setTitle(title2);
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date());
        appointmentObj.setShownAs(Appointment.RESERVED);
        appointmentObj.setParentFolderID(folderID);
        appointmentObj.setIgnoreConflicts(true);

        int secondObjId = catm.insert(appointmentObj).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, objId));
        array.put(addRequestIds(secondFolderID, secondObjId));
        String body = array.toString();

        ICalExportResponse response = getClient().execute(new ICalExportRequest(-1, -1, body));

        String iCal = response.getICal();
        assertTrue(iCal.contains(title));
        assertTrue(iCal.contains(title2));
        assertFileName(response.getHttpResponse(), folder.getFolderName()+".ics");
    }

    @Test
    public void testICalFileNameExport() throws OXException, IOException, JSONException {
        //testing for whitespace characters
        final String title = "ICal Test Appointment for testing file name encoding";
        int folderID = folder.getObjectID();
        Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle(title);
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date());
        appointmentObj.setShownAs(Appointment.RESERVED);
        appointmentObj.setParentFolderID(folderID);
        appointmentObj.setIgnoreConflicts(true);

        int objId = catm.insert(appointmentObj).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, objId));
        String body = array.toString();

        ICalExportResponse response = getClient().execute(new ICalExportRequest(-1, -1, body));

        String iCal = response.getICal();
        assertTrue(iCal.contains(title));
        assertFileName(response.getHttpResponse(), title+".ics");
    }

    protected void assertFileName(HttpResponse httpResp, String expectedFileName) {
        Header[] headers = httpResp.getHeaders("Content-Disposition");
        for (Header header : headers) {
            assertNotNull(header.getValue());
            assertTrue(header.getValue().contains(expectedFileName));
        }
    }

    protected JSONObject addRequestIds(int folderId, int objectId) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", objectId);
        json.put("folder_id", folderId);
        return json;
    }

}
