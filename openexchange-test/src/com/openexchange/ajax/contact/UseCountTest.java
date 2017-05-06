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

package com.openexchange.ajax.contact;

import static org.junit.Assert.*;
import java.util.UUID;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.AutocompleteRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonSearchResponse;
import com.openexchange.ajax.jslob.actions.SetRequest;
import com.openexchange.ajax.mail.TestMail;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.ContactTestManager;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 * {@link UseCountTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class UseCountTest extends ContactTest {

    private String address;
    private int folderId;
    private AJAXClient client;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        client = getClient();

        FolderObject folder = ftm.generatePrivateFolder("useCountTest" + UUID.randomUUID().toString(), Module.CONTACTS.getFolderConstant(), client.getValues().getPrivateContactFolder(), client.getValues().getUserId());
        folder = ftm.insertFolderOnServer(folder);
        folderId = folder.getObjectID();
        Contact c1 = ContactTestManager.generateContact(folder.getObjectID(), "UseCount");
        c1.setEmail1(modifyMailAddress(client.getValues().getDefaultAddress()));
        c1 = cotm.newAction(c1);
        Contact c2 = ContactTestManager.generateContact(folder.getObjectID(), "UseCount");
        c2.setEmail1(modifyMailAddress(client.getValues().getDefaultAddress()));
        c2 = cotm.newAction(c2);

        SetRequest req = new SetRequest("io.ox/mail", "{\"contactCollectOnMailTransport\": true}", true);
        client.execute(req);

        address = c2.getEmail1();
        // If an exception occurred while sending the mail might be null 
        assertNotNull("Mail could not be send", mtm.send(new TestMail(client.getValues().getDefaultAddress(), address, "Test", "text/plain", "Test")));
    }

    @Test
    public void testUseCount() throws Exception {
        AutocompleteRequest request = new AutocompleteRequest("UseCount", false, String.valueOf(folderId), CONTACT_FIELDS, true);
        long until = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        Contact firstResult = null;
        CommonSearchResponse response;
        do {
            response = client.execute(request);
            assertFalse(response.getErrorMessage(), response.hasError());
            JSONArray jsonArray = (JSONArray) response.getData();
            assertNotNull(jsonArray);
            Contact[] contacts = jsonArray2ContactArray(jsonArray, CONTACT_FIELDS);
            assertTrue(0 < contacts.length);
            firstResult = contacts[0];
            if (address.equals(firstResult.getEmail1())) {
                break;
            }
            Thread.sleep(500);
        } while (System.currentTimeMillis() < until);
        assertEquals(address, firstResult.getEmail1());
    }
    
    /**
     * Modify given mail address to create new unique address
     * 
     * @param mailAdress The original mail address
     * @return unique mail address
     */
    private String modifyMailAddress(String mailAddress) {
        StringBuilder sb = new StringBuilder();
        int at;

        // Check if given mail address is 'valid'  
        if ((at = mailAddress.indexOf("@")) < 0) {
            return mailAddress;
        }

        // Generate random address
        sb.append(UUID.randomUUID().toString().replaceAll("-", ""));
        sb.append(mailAddress.substring(at));

        return sb.toString();
    }
}
