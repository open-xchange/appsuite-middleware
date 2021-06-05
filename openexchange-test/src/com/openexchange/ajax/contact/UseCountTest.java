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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
import com.openexchange.test.common.test.TestClassConfig;

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
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().withUserPerContext(3).createAjaxClient().createApiClient().build();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        client = getClient();

        FolderObject folder = ftm.generatePrivateFolder("useCountTest" + UUID.randomUUID().toString(), Module.CONTACTS.getFolderConstant(), client.getValues().getPrivateContactFolder(), client.getValues().getUserId());
        folder = ftm.insertFolderOnServer(folder);
        folderId = folder.getObjectID();
        Contact c1 = ContactTestManager.generateContact(folder.getObjectID(), "UseCount");
        c1.setEmail1(testUser2.getLogin());
        c1 = cotm.newAction(c1);
        Contact c2 = ContactTestManager.generateContact(folder.getObjectID(), "UseCount");
        c2.setEmail1(testContext.acquireUser().getLogin());
        c2 = cotm.newAction(c2);

        SetRequest req = new SetRequest("io.ox/mail", "{\"contactCollectOnMailTransport\": true}", true);
        client.execute(req);

        address = c2.getEmail1();
        // If an exception occurred while sending the mail might be null
        mtm.setFailOnError(true);
        assertNotNull("Mail could not be send", mtm.send(new TestMail(client.getValues().getDefaultAddress(), address, "Test", "text/plain", "Test")));
        mtm.setFailOnError(false);
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
        assertNotNull("Missing contact", firstResult);
        assertEquals(address, firstResult.getEmail1());
    }
}
