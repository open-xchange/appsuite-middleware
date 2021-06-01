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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.ContactTestManager;

/**
 * {@link Bug6825Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug6825Test extends AbstractAJAXSession {

    public Bug6825Test() {
        super();
    }

    @Test
    public void testImportVCard() throws Exception {
        /*
         * import vCard with too long field values
         */
        FolderObject folder = ftm.generatePrivateFolder(UUIDs.getUnformattedStringFromRandom(), Module.CONTACTS.getFolderConstant(), getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        folder = ftm.insertFolderOnServer(folder);
        String originalSurname = "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeeffffffffffgggggggggghhhhhhhhhhiiiiiiiiiijjjjjjjjjjkkkkkkkkkkllllllllllmmmmmmmmmmnnnnnnnnnnooooooooooppppppppppqqqqqqqqqqrrrrrrrrrrttttttttttuuuuuuuuuvvvvvvvvvwwwwwwwwwwxxxxxxxxxxyyyyyyyyyyzzzzzzzzzz00000000001111111111222222222233333333334444444444455555555556666666666777777777788888888889999999999";
        String expectedSurname = "aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeeeffffffffffgggggggggghhhhhhhhhhiiiiiiiiiijjjjjjjjjjkkkkkkkkkkllllllllllmmmmmmmm";
        String vCard = "BEGIN:VCARD\nVERSION:3.0\n\nN:" + originalSurname + ";givenName;;;\nEND:VCARD\n";
        VCardImportRequest importRequest = new VCardImportRequest(folder.getObjectID(), Streams.newByteArrayInputStream(vCard.getBytes(Charsets.UTF_8)));
        VCardImportResponse importResponse = getClient().execute(importRequest);
        /*
         * check response & imported contact
         */
        JSONArray response = (JSONArray) importResponse.getData();
        assertEquals("Unexpected number of imported contacts", 1, response.length());
        JSONObject jsonObject = response.getJSONObject(0);
        Contact contact = new ContactTestManager(getClient()).getAction(jsonObject.getInt("folder_id"), jsonObject.getInt("id"));
        assertNotNull("Imported contact not found", contact);
        assertEquals("Surname not truncated as expected", expectedSurname, contact.getSurName());
    }

    @Test
    public void testImportICal() throws Exception {
        /*
         * import iCal with too long fiel values
         */
        FolderObject folder = ftm.generatePrivateFolder(UUIDs.getUnformattedStringFromRandom(), Module.CALENDAR.getFolderConstant(), getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId());
        folder = ftm.insertFolderOnServer(folder);
        String testMailAddress = "test@example.com";
        String originalTitle = "zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... ";
        String expectedTitle = "zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen.... zwanzig zeichen";
        String iCal = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:OPEN-XCHANGE\nBEGIN:VEVENT\nCLASS:PUBLIC\nCREATED:20060519T120300Z\nDTSTART:20060519T110000Z\nDTSTAMP:20070423T063205Z\nSUMMARY:" + originalTitle + "\nDTEND:20060519T120000Z\nATTENDEE:mailto:" + testMailAddress + "\nEND:VEVENT\nEND:VCALENDAR";
        ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), iCal, false);
        ICalImportResponse importResponse = getClient().execute(importRequest);
        /*
         * check response & imported contact
         */
        assertTrue(importResponse.hasError());
        JSONObject data = ((JSONArray) importResponse.getData()).getJSONObject(0);
        assertTrue(data.has("warnings"));
        JSONArray warnings = data.getJSONArray("warnings");
        assertEquals(1, warnings.length());
        assertTrue(warnings.getJSONObject(0).toString().contains("truncated"));
        JSONArray response = (JSONArray) importResponse.getData();
        assertEquals("Unexpected number of imported appointments", 1, response.length());
        JSONObject jsonObject = response.getJSONObject(0);
        Appointment appointment = new CalendarTestManager(getClient()).get(jsonObject.getInt("folder_id"), jsonObject.getInt("id"));
        assertNotNull("Imported appointment not found", appointment);
        assertEquals("Surname not truncated as expected", expectedTitle, appointment.getTitle());
    }

}
