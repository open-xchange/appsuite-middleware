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
import java.io.InputStream;
import java.util.Map.Entry;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.importexport.formats.Format;
import com.openexchange.test.OXTestToolkit;

/**
 * Tests the VCard imports and exports by using the servlets.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class VCardImportExportServletTest extends AbstractImportExportServletTest {

    private int folderId;

    public VCardImportExportServletTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderId = createFolder("vcard-contact-roundtrip-" + System.currentTimeMillis(), FolderObject.CONTACT);
    }

    public void testVCardRoundtrip() throws Exception {
        //test: import
        InputStream is = new ByteArrayInputStream(IMPORT_VCARD.getBytes());
        WebConversation webconv = getWebConversation();
        WebRequest req = new PostMethodWebRequest(getUrl(IMPORT_SERVLET, folderId, Format.VCARD), true);
        req.selectFile("file", "contact.vcf", is, Format.VCARD.getMimeType());
        WebResponse webRes = webconv.getResource(req);

        extractFromCallback(webRes.getText());

        //test: export
        webconv = getWebConversation();
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

    public void testMultiVCardRoundtrip() throws Exception {
        //test: import
        InputStream is = new ByteArrayInputStream((IMPORT_VCARD + IMPORT_VCARD_2).getBytes());
        WebConversation webconv = getWebConversation();
        WebRequest req = new PostMethodWebRequest(getUrl(IMPORT_SERVLET, folderId, Format.VCARD), true);
        req.selectFile("file", "contact.vcf", is, Format.VCARD.getMimeType());
        WebResponse webRes = webconv.getResource(req);

        extractFromCallback(webRes.getText());

        //test: export
        webconv = getWebConversation();
        req = new GetMethodWebRequest(getUrl(EXPORT_SERVLET, folderId, Format.VCARD));
        webRes = webconv.sendRequest(req);
        is = webRes.getInputStream();
        String resultingVCard = OXTestToolkit.readStreamAsString(is);
        System.out.println(resultingVCard);
        String[] resultingVCards = resultingVCard.split("END:VCARD\\r?\\nBEGIN:VCARD");
        assertEquals("Expected two vCards.", 2, resultingVCards.length);
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

    @Override
    public void tearDown() throws Exception {
        removeFolder(folderId);
        super.tearDown();
    }

}
