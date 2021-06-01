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
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.VCardExportRequest;
import com.openexchange.ajax.importexport.actions.VCardExportResponse;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Charsets;

/**
 * {@link Bug25701Test}
 *
 * Several bugs when parsing a phone number in a VCard
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug25701Test extends AbstractManagedContactTest {

    @Test
    public void testCombinedTypes() throws Exception {
        /*
         * check import
         */
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\n\n" + "N:test;heinz;;;\r\n" + "TEL;TYPE=voice,home:43643634634\r\n" + "END:VCARD\r\n";
        Contact importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneHome1());
        /*
         * check roundtrip
         */
        vCard = export();
        cotm.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneHome1());
        cotm.deleteAction(importedContact);
    }

    @Test
    public void testPreferredTelephoneHomeType() throws Exception {
        /*
         * check import
         */
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\n\n" + "N:test;heinz;;;\r\n" + "TEL;TYPE=voice,home:17858358734\r\n" + "TEL;TYPE=voice,home:23455464534\r\n" + "TEL;TYPE=pref,voice,home:33465472555\r\n" + "TEL;TYPE=pref,voice,home:47574573624\r\n" + "END:VCARD\r\n";
        Contact importedContact = importAndFetch(vCard);
        List<String> homeTelephoneNumbers = Arrays.asList(new String[] { importedContact.getTelephoneHome1(), importedContact.getTelephoneHome2() });
        assertTrue("33465472555 not found", homeTelephoneNumbers.contains("33465472555"));
        assertTrue("47574573624 not found", homeTelephoneNumbers.contains("47574573624"));
        /*
         * check roundtrip
         */
        vCard = export();
        cotm.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        homeTelephoneNumbers = Arrays.asList(new String[] { importedContact.getTelephoneHome1(), importedContact.getTelephoneHome2() });
        assertTrue("33465472555 not found", homeTelephoneNumbers.contains("33465472555"));
        assertTrue("47574573624 not found", homeTelephoneNumbers.contains("47574573624"));
        cotm.deleteAction(importedContact);
    }

    @Test
    public void testPreferredTelephoneBusinessType() throws Exception {
        /*
         * check import
         */
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\n\n" + "N:test;heinz;;;\r\n" + "TEL;TYPE=voice,work:17858358734\r\n" + "TEL;TYPE=voice,work:23455464534\r\n" + "TEL;TYPE=pref,voice,work:33465472555\r\n" + "TEL;TYPE=pref,voice,work:47574573624\r\n" + "END:VCARD\r\n";
        Contact importedContact = importAndFetch(vCard);
        List<String> businessTelephoneNumbers = Arrays.asList(new String[] { importedContact.getTelephoneBusiness1(), importedContact.getTelephoneBusiness2() });
        assertTrue("33465472555 not found", businessTelephoneNumbers.contains("33465472555"));
        assertTrue("47574573624 not found", businessTelephoneNumbers.contains("47574573624"));
        /*
         * check roundtrip
         */
        vCard = export();
        cotm.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        businessTelephoneNumbers = Arrays.asList(new String[] { importedContact.getTelephoneBusiness1(), importedContact.getTelephoneBusiness2() });
        assertTrue("33465472555 not found", businessTelephoneNumbers.contains("33465472555"));
        assertTrue("47574573624 not found", businessTelephoneNumbers.contains("47574573624"));
        cotm.deleteAction(importedContact);
    }

    @Test
    public void testPreferredFaxHomeType() throws Exception {
        /*
         * check import
         */
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\n\n" + "N:test;heinz;;;\r\n" + "TEL;TYPE=fax,home:17858358734\r\n" + "TEL;TYPE=fax,home:23455464534\r\n" + "TEL;TYPE=pref,fax,home:33465472555\r\n" + "END:VCARD\r\n";
        Contact importedContact = importAndFetch(vCard);
        assertEquals("33465472555 not found", "33465472555", importedContact.getFaxHome());
        /*
         * check roundtrip
         */
        vCard = export();
        cotm.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("33465472555 not found", "33465472555", importedContact.getFaxHome());
        cotm.deleteAction(importedContact);
    }

    @Test
    public void testPreferredFaxBusinessType() throws Exception {
        /*
         * check import
         */
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\n\n" + "N:test;heinz;;;\r\n" + "TEL;TYPE=fax,work:17858358734\r\n" + "TEL;TYPE=fax,work:23455464534\r\n" + "TEL;TYPE=pref,fax,work:33465472555\r\n" + "END:VCARD\r\n";
        Contact importedContact = importAndFetch(vCard);
        assertEquals("33465472555 not found", "33465472555", importedContact.getFaxBusiness());
        /*
         * check roundtrip
         */
        vCard = export();
        cotm.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("33465472555 not found", "33465472555", importedContact.getFaxBusiness());
        cotm.deleteAction(importedContact);
    }

    @Test
    public void testTextphoneType() throws Exception {
        /*
         * check import
         */
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\n\n" + "N:test;heinz;;;\r\n" + "TEL;TYPE=textphone:43643634634\r\n" + "END:VCARD\r\n";
        Contact importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneTTYTTD());
        /*
         * check roundtrip
         */
        vCard = export();
        cotm.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneTTYTTD());
        cotm.deleteAction(importedContact);
    }

    @Test
    public void testPagerType() throws Exception {
        /*
         * check import
         */
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\n\n" + "N:test;heinz;;;\r\n" + "TEL;TYPE=pager:43643634634\r\n" + "END:VCARD\r\n";
        Contact importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephonePager());
        /*
         * check roundtrip
         */
        vCard = export();
        cotm.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephonePager());
        cotm.deleteAction(importedContact);
    }

    @Test
    public void testCarType() throws Exception {
        /*
         * check import
         */
        String vCard = "BEGIN:VCARD\r\n" + "VERSION:3.0\n\n" + "N:test;heinz;;;\r\n" + "TEL;TYPE=car:43643634634\r\n" + "END:VCARD\r\n";
        Contact importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneCar());
        /*
         * check roundtrip
         */
        vCard = export();
        cotm.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneCar());
        cotm.deleteAction(importedContact);
    }

    private Contact importAndFetch(String vCard) throws Exception {
        VCardImportRequest importRequest = new VCardImportRequest(folderID, new ByteArrayInputStream(vCard.getBytes(Charsets.UTF_8)));
        VCardImportResponse importResponse = getClient().execute(importRequest);
        JSONArray data = (JSONArray) importResponse.getData();
        assertTrue("got no data from import request", null != data && 0 < data.length());
        JSONObject jsonObject = data.getJSONObject(0);
        assertNotNull("got no data from import request", jsonObject);
        int objectID = jsonObject.optInt("id");
        assertTrue("got no object id from import request", 0 < objectID);
        return cotm.getAction(folderID, objectID);
    }

    private String export() throws Exception {
        VCardExportRequest exportRequest = new VCardExportRequest(folderID, false);
        VCardExportResponse exportResponse = cotm.getClient().execute(exportRequest);
        return exportResponse.getVCard();
    }

}
