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
 * {@link Bug29865Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug29865Test extends AbstractMailTest {
    
    private static String attachment = readFile("attachment.base64");
    
    private static String eml = readFile("bug29865.eml");
    
    private UserValues values;

    /**
     * Initializes a new {@link Bug29865Test}.
     * @param name
     */
    public Bug29865Test(final String name) {
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
