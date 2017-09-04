/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
