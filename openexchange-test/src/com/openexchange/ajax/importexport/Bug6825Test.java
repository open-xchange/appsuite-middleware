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

import org.json.JSONArray;
import org.json.JSONObject;
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
import com.openexchange.test.FolderTestManager;

/**
 * {@link Bug6825Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug6825Test extends AbstractAJAXSession {

	private FolderTestManager folderTestManager;

	public Bug6825Test(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		folderTestManager = new FolderTestManager(getClient());
	}

	@Override
    public void tearDown() throws Exception {
	    if (null != folderTestManager) {
	        folderTestManager.cleanUp();
	    }
	    super.tearDown();
	}

	public void testImportVCard() throws Exception {
	    /*
	     * import vCard with too long field values
	     */
        FolderObject folder = folderTestManager.generatePrivateFolder(UUIDs.getUnformattedStringFromRandom(), Module.CONTACTS.getFolderConstant(),
            getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        folder = folderTestManager.insertFolderOnServer(folder);
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

    public void testImportICal() throws Exception {
        /*
         * import iCal with too long fiel values
         */
        FolderObject folder = folderTestManager.generatePrivateFolder(UUIDs.getUnformattedStringFromRandom(), Module.CALENDAR.getFolderConstant(),
            getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId());
        folder = folderTestManager.insertFolderOnServer(folder);
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
