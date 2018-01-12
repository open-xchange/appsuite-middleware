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

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.VCardExportRequest;
import com.openexchange.ajax.importexport.actions.VCardExportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link VCardSingleAndBatchExportTest}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10
 */
public class VCardSingleAndBatchExportTest extends AbstractManagedContactTest {

    public VCardSingleAndBatchExportTest() {
        super();
    }

    @Test
    public void testVCardSingleExport() throws OXException, IOException, JSONException {
        Contact contact = generateContact("Singlecontact");
        int contactId = cotm.newAction(contact).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, contactId));
        String body = array.toString();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(-1, true, true, body));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("One vCard expected!", 1, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), contact.getGivenName()+" "+contact.getSurName()+".vcf");
    }

    @Test
    public void testVCardSingleDistributionListExport() throws OXException, IOException, JSONException {
        Contact list = generateContact("Distribution list");
        list.setDistributionList(new DistributionListEntryObject[] { new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
        });

        int distlistId = cotm.newAction(list).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, distlistId));
        String body = array.toString();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(-1, true, true, body));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("One vCard expected!", 1, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), list.getDisplayName()+".vcf");
    }

    @Test
    public void testVCardMultipleExport() throws OXException, IOException, JSONException {
        Contact firstContact = generateContact("First Contact");
        int firstId = cotm.newAction(firstContact).getObjectID();

        Contact secondContact = generateContact("Second Contact");
        int secondId = cotm.newAction(secondContact).getObjectID();

        Contact list = generateContact("Distribution list");
        list.setDistributionList(new DistributionListEntryObject[] { new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
        });
        int thirdId = cotm.newAction(list).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, firstId));
        array.put(addRequestIds(folderID, secondId));
        array.put(addRequestIds(folderID, thirdId));
        String body = array.toString();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(-1, true, true, body));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("Three vCard expected!", 3, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), folderName1+".vcf");
    }

    @Test
    public void testVCardOldFolderExport() throws OXException, IOException, JSONException {
        Contact firstContact = generateContact("First Contact");
        cotm.newAction(firstContact).getObjectID();

        Contact secondContact = generateContact("Second Contact");
        cotm.newAction(secondContact).getObjectID();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(folderID, false));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("Two vCards expected!", 2, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), folderName1+".vcf");
    }

    @Test
    public void testCrossFolderBatchExportTest() throws OXException, IOException, JSONException {
        Contact firstContact = generateContact("First Contact");
        int firstId = cotm.newAction(firstContact).getObjectID();

        Contact secondContact = generateContact("Second Contact");
        int secondId = cotm.newAction(secondContact).getObjectID();

        Contact thirdContact = generateContact("Third Contact", secondFolderID);
        int thirdId = cotm.newAction(thirdContact).getObjectID();

        Contact list = generateContact("Distribution list", secondFolderID);
        list.setDistributionList(new DistributionListEntryObject[] { new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
        });
        int fourthId = cotm.newAction(list).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, firstId));
        array.put(addRequestIds(folderID, secondId));
        array.put(addRequestIds(secondFolderID, thirdId));
        array.put(addRequestIds(secondFolderID, fourthId));
        String body = array.toString();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(-1, true, true, body));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("Four vCards expected!", 4, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), "Contacts.vcf");
    }

    @Test
    public void testInvalidFileNameExport() throws JSONException, OXException, IOException {
        Contact firstContact = generateContact("First \"Contact\" Contact");
        int firstId = cotm.newAction(firstContact).getObjectID();

        JSONArray array = new JSONArray();
        array.put(addRequestIds(folderID, firstId));

        String body = array.toString();

        VCardExportResponse vcardExportResponse = getClient().execute(new VCardExportRequest(-1, true, true, body));
        String vcard = (String) vcardExportResponse.getData();
        String[] result = vcard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("One vCards expected!", 1, result.length);
        assertFileName(vcardExportResponse.getHttpResponse(), "Export.vcf");
    }

}
