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

package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.SearchRequest;
import com.openexchange.ajax.appointment.action.SearchResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.Appointment;

public class SearchTest extends AppointmentTest {

    @Test
    public void testSimpleSearch() throws Exception {
        final Appointment appointmentObj = new Appointment();
        final String date = String.valueOf(System.currentTimeMillis());
        appointmentObj.setTitle("testSimpleSearch" + date);
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        catm.insert(appointmentObj).getObjectID();
        
        final Appointment[] appointmentArray = catm.searchAppointment("testSimpleSearch" + date, appointmentFolderId, new Date(), new Date(), APPOINTMENT_FIELDS);
        assertTrue("appointment array size is 0", appointmentArray.length > 0);
    }

    // Node 2652    @Test
    @Test
    public void testLastModifiedUTC() throws Exception {
        final int cols[] = new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.LAST_MODIFIED_UTC };

        final Appointment appointmentObj = createAppointmentObject("testShowLastModifiedUTC");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
        appointmentObj.setIgnoreConflicts(true);
        catm.insert(appointmentObj).getObjectID();
        final SearchRequest searchRequest = new SearchRequest("testShowLastModifiedUTC", appointmentFolderId, cols, true);
        final SearchResponse response = Executor.execute(getClient(), searchRequest);
        final JSONArray arr = (JSONArray) response.getResponse().getData();

        assertNotNull(arr);
        final int size = arr.length();
        assertTrue(size > 0);

        for (int i = 0; i < size; i++) {
            final JSONArray objectData = arr.optJSONArray(i);
            assertNotNull(objectData);
            assertNotNull(objectData.opt(2));
        }
    }
}
