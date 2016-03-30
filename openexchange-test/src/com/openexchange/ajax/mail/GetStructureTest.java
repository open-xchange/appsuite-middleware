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

package com.openexchange.ajax.mail;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.ajax.mail.actions.NewMailResponse;
import com.openexchange.configuration.MailConfig;
import com.openexchange.exception.OXException;

/**
 * {@link GetStructureTest}
 * Testing the /ajax/mail?action=get_structure call as requested by US57553044
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetStructureTest extends AbstractMailTest {
    
    private static final String attachment = readFile("attachment.base64");
        /*"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\n";*/

    private static String eml =
        "Date: Tue, 27 Nov 2012 21:43:24 +0100 (CET)\n" +
            "From: #ADDR#\n" +
            "To: #ADDR#\n" +
            "Message-ID: <1602561799.1706.1354049004437@open-xchange.com>\n" +
            "Subject: Simple attachment\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: multipart/mixed; boundary=\"----=_Part_1705_1673668315.1354049004384\"\n" +
            "\n" +
            "------=_Part_1705_1673668315.1354049004384\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: text/plain; charset=UTF-8\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "\n" +
            "Some text\n" +
            "------=_Part_1705_1673668315.1354049004384\n" +
            "Content-Type: image/jpeg; name=7.jpeg\n" +
            "Content-Transfer-Encoding: base64\n" +
            "Content-Disposition: attachment; filename=7.jpeg\n" +
            "\n" +
            attachment +
            "\n" +
            "------=_Part_1705_1673668315.1354049004384--";

    private UserValues values;

    public GetStructureTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetStructure() throws OXException, IOException, JSONException{

        final NewMailRequest newMailRequest = new NewMailRequest(null, eml.replaceAll("#ADDR#", values.getSendAddress()), -1, true);
        final NewMailResponse newMailResponse = getClient().execute(newMailRequest);

        assertNotNull("Missing folder in response.", newMailResponse.getFolder());
        assertNotNull("Missing ID in response.", newMailResponse.getId());
        
        final GetRequest newGetRequest = new GetRequest(newMailResponse.getFolder(), newMailResponse.getId(), true, true);
        final GetResponse newGetResponse = getClient().execute(newGetRequest);
        
        String actualAttachment = ((JSONObject)newGetResponse.getData()).getJSONArray("body").getJSONObject(1).getJSONObject("body").getString("data");
        assertEquals("Attachment has been modified", attachment.replaceAll("(\\r|\\n)", ""), actualAttachment);
    }
    
    private static String readFile(String fileName){
        try {
            @SuppressWarnings("resource")
            BufferedReader br = new BufferedReader(new FileReader(MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR) + fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
