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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
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

	/**
	 * Initializes a new {@link Bug25701Test}.
	 *
	 * @param name The test name
	 */
	public Bug25701Test(String name) {
		super(name);
	}

    public void testCombinedTypes() throws Exception {
        /*
         * check import
         */
        String vCard =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\n\n" +
            "N:test;heinz;;;\r\n" +
            "TEL;TYPE=voice,home:43643634634\r\n" +
            "END:VCARD\r\n"
        ;
        Contact importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneHome1());
        /*
         * check roundtrip
         */
        vCard = export();
        manager.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneHome1());
        manager.deleteAction(importedContact);
    }

    public void testPreferredTelephoneHomeType() throws Exception {
        /*
         * check import
         */
        String vCard =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\n\n" +
            "N:test;heinz;;;\r\n" +
            "TEL;TYPE=voice,home:17858358734\r\n" +
            "TEL;TYPE=voice,home:23455464534\r\n" +
            "TEL;TYPE=pref,voice,home:33465472555\r\n" +
            "TEL;TYPE=pref,voice,home:47574573624\r\n" +
            "END:VCARD\r\n"
        ;
        Contact importedContact = importAndFetch(vCard);
        List<String> homeTelephoneNumbers = Arrays.asList(
            new String[] { importedContact.getTelephoneHome1(), importedContact.getTelephoneHome2()  });
        assertTrue("33465472555 not found", homeTelephoneNumbers.contains("33465472555"));
        assertTrue("47574573624 not found", homeTelephoneNumbers.contains("47574573624"));
        /*
         * check roundtrip
         */
        vCard = export();
        manager.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        homeTelephoneNumbers = Arrays.asList(
            new String[] { importedContact.getTelephoneHome1(), importedContact.getTelephoneHome2()  });
        assertTrue("33465472555 not found", homeTelephoneNumbers.contains("33465472555"));
        assertTrue("47574573624 not found", homeTelephoneNumbers.contains("47574573624"));
        manager.deleteAction(importedContact);
    }

    public void testPreferredTelephoneBusinessType() throws Exception {
        /*
         * check import
         */
        String vCard =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\n\n" +
            "N:test;heinz;;;\r\n" +
            "TEL;TYPE=voice,work:17858358734\r\n" +
            "TEL;TYPE=voice,work:23455464534\r\n" +
            "TEL;TYPE=pref,voice,work:33465472555\r\n" +
            "TEL;TYPE=pref,voice,work:47574573624\r\n" +
            "END:VCARD\r\n"
        ;
        Contact importedContact = importAndFetch(vCard);
        List<String> businessTelephoneNumbers = Arrays.asList(
            new String[] { importedContact.getTelephoneBusiness1(), importedContact.getTelephoneBusiness2()  });
        assertTrue("33465472555 not found", businessTelephoneNumbers.contains("33465472555"));
        assertTrue("47574573624 not found", businessTelephoneNumbers.contains("47574573624"));
        /*
         * check roundtrip
         */
        vCard = export();
        manager.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        businessTelephoneNumbers = Arrays.asList(
            new String[] { importedContact.getTelephoneBusiness1(), importedContact.getTelephoneBusiness2()  });
        assertTrue("33465472555 not found", businessTelephoneNumbers.contains("33465472555"));
        assertTrue("47574573624 not found", businessTelephoneNumbers.contains("47574573624"));
        manager.deleteAction(importedContact);
    }

    public void testPreferredFaxHomeType() throws Exception {
        /*
         * check import
         */
        String vCard =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\n\n" +
            "N:test;heinz;;;\r\n" +
            "TEL;TYPE=fax,home:17858358734\r\n" +
            "TEL;TYPE=fax,home:23455464534\r\n" +
            "TEL;TYPE=pref,fax,home:33465472555\r\n" +
            "END:VCARD\r\n"
        ;
        Contact importedContact = importAndFetch(vCard);
        assertEquals("33465472555 not found", "33465472555", importedContact.getFaxHome());
        /*
         * check roundtrip
         */
        vCard = export();
        manager.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("33465472555 not found", "33465472555", importedContact.getFaxHome());
        manager.deleteAction(importedContact);
    }

    public void testPreferredFaxBusinessType() throws Exception {
        /*
         * check import
         */
        String vCard =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\n\n" +
            "N:test;heinz;;;\r\n" +
            "TEL;TYPE=fax,work:17858358734\r\n" +
            "TEL;TYPE=fax,work:23455464534\r\n" +
            "TEL;TYPE=pref,fax,work:33465472555\r\n" +
            "END:VCARD\r\n"
        ;
        Contact importedContact = importAndFetch(vCard);
        assertEquals("33465472555 not found", "33465472555", importedContact.getFaxBusiness());
        /*
         * check roundtrip
         */
        vCard = export();
        manager.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("33465472555 not found", "33465472555", importedContact.getFaxBusiness());
        manager.deleteAction(importedContact);
    }

    public void testTextphoneType() throws Exception {
        /*
         * check import
         */
        String vCard =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\n\n" +
            "N:test;heinz;;;\r\n" +
            "TEL;TYPE=textphone:43643634634\r\n" +
            "END:VCARD\r\n"
        ;
        Contact importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneTTYTTD());
        /*
         * check roundtrip
         */
        vCard = export();
        manager.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneTTYTTD());
        manager.deleteAction(importedContact);
    }

    public void testPagerType() throws Exception {
        /*
         * check import
         */
        String vCard =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\n\n" +
            "N:test;heinz;;;\r\n" +
            "TEL;TYPE=pager:43643634634\r\n" +
            "END:VCARD\r\n"
        ;
        Contact importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephonePager());
        /*
         * check roundtrip
         */
        vCard = export();
        manager.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephonePager());
        manager.deleteAction(importedContact);
    }

    public void testCarType() throws Exception {
        /*
         * check import
         */
        String vCard =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\n\n" +
            "N:test;heinz;;;\r\n" +
            "TEL;TYPE=car:43643634634\r\n" +
            "END:VCARD\r\n"
        ;
        Contact importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneCar());
        /*
         * check roundtrip
         */
        vCard = export();
        manager.deleteAction(importedContact);
        importedContact = importAndFetch(vCard);
        assertEquals("43643634634", importedContact.getTelephoneCar());
        manager.deleteAction(importedContact);
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
        return manager.getAction(folderID, objectID);
	}

	private String export() throws Exception {
        VCardExportRequest exportRequest = new VCardExportRequest(folderID, false);
        VCardExportResponse exportResponse = manager.getClient().execute(exportRequest);
        return exportResponse.getVCard();
	}

}
