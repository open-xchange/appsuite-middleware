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

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.importexport.Format;
import com.openexchange.test.common.test.OXTestToolkit;

/**
 * Tests the VCard imports and exports by using the servlets.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class VCardImportExportServletTest extends AbstractImportExportServletTest {

    private int folderId;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        FolderObject folder = ftm.insertFolderOnServer(ftm.generatePrivateFolder("vcard-contact-roundtrip-" + UUID.randomUUID().toString(),
            FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId()));
        folderId = folder.getObjectID();
    }

    @Test
    public void testVCardRoundtrip() throws Exception {
        //test: import
        InputStream is = new ByteArrayInputStream(IMPORT_VCARD.getBytes());
        WebConversation webconv = getClient().getSession().getConversation();
        WebRequest req = new PostMethodWebRequest(getUrl(IMPORT_SERVLET, folderId, Format.VCARD), true);
        req.selectFile("file", "contact.vcf", is, Format.VCARD.getMimeType());
        WebResponse webRes = webconv.getResource(req);

        extractFromCallback(webRes.getText());

        //test: export
        webconv = getClient().getSession().getConversation();
        req = new GetMethodWebRequest(getUrl(EXPORT_SERVLET, folderId, Format.VCARD));
        webRes = webconv.sendRequest(req);
        is = webRes.getInputStream();
        String resultingVCard = OXTestToolkit.readStreamAsString(is);
        String[] result = resultingVCard.split("\n");
        //finally: checking
        for (Entry<String, String> element : VCARD_ELEMENTS.entrySet()) {
            assertTrue("Missing element: " + element.getKey(), resultingVCard.contains(element.getKey()));
            for (String r : result) {
                if (r.startsWith(element.getKey())) {
                    assertTrue("Missing value " + element.getValue(), r.contains(element.getValue()));
                    break;
                }
            }
        }
    }

    @Test
    public void testMultiVCardRoundtrip() throws Exception {
        //test: import
        InputStream is = new ByteArrayInputStream((IMPORT_VCARD + IMPORT_VCARD_2).getBytes());
        WebConversation webconv = getClient().getSession().getConversation();
        WebRequest req = new PostMethodWebRequest(getUrl(IMPORT_SERVLET, folderId, Format.VCARD), true);
        req.selectFile("file", "contact.vcf", is, Format.VCARD.getMimeType());
        WebResponse webRes = webconv.getResource(req);

        extractFromCallback(webRes.getText());

        //test: export
        webconv = getClient().getSession().getConversation();
        req = new GetMethodWebRequest(getUrl(EXPORT_SERVLET, folderId, Format.VCARD));
        webRes = webconv.sendRequest(req);
        is = webRes.getInputStream();
        String resultingVCard = OXTestToolkit.readStreamAsString(is);
        System.out.println(resultingVCard);
        String[] resultingVCards = resultingVCard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        Assert.assertEquals("Expected two vCards.", 2, resultingVCards.length);
        String[] result0 = resultingVCards[0].split("\n");
        String[] result1 = resultingVCards[1].split("\n");

        //finally: checking
        for (Entry<String, String> element : VCARD_ELEMENTS.entrySet()) {
            assertTrue("Missing element: " + element.getKey(), resultingVCard.contains(element.getKey()));
            for (String r : result0) {
                if (r.startsWith(element.getKey())) {
                    assertTrue("Missing value " + element.getValue(), r.contains(element.getValue()));
                    break;
                }
            }
        }
        for (Entry<String, String> element : VCARD_ELEMENTS_2.entrySet()) {
            assertTrue("Missing element: " + element.getKey(), resultingVCard.contains(element.getKey()));
            for (String r : result1) {
                if (r.startsWith(element.getKey())) {
                    assertTrue("Missing value " + element.getValue(), r.contains(element.getValue()));
                    break;
                }
            }
        }
    }

}
