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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.user.UserTools;
import com.openexchange.groupware.container.Contact;

public class GetTest extends ContactTest {

    @Test
    public void testGet() throws Exception {
        final Contact contactObj = createContactObject("testGet");
        final int objectId = cotm.newAction(contactObj).getObjectID();

        loadContact(getClient(), objectId, contactFolderId);
    }

    @Test
    public void testGetWithAllFields() throws Exception {
        final Contact contactObject = createCompleteContactObject();

        final int objectId = cotm.newAction(contactObject).getObjectID();

        final Contact loadContact = loadContact(getClient(), objectId, contactFolderId);

        contactObject.setObjectID(objectId);
        compareObject(contactObject, loadContact);
    }

    @Test
    public void testGetWithAllFieldsOnUpdate() throws Exception {
        Contact contactObject = new Contact();
        contactObject.setSurName("testGetWithAllFieldsOnUpdate");
        contactObject.setParentFolderID(contactFolderId);

        final int objectId = cotm.newAction(contactObject).getObjectID();

        contactObject = createCompleteContactObject();

        final Contact loadContact = cotm.updateAction(contactFolderId, contactObject);

        contactObject.setObjectID(objectId);
        compareObject(contactObject, loadContact);
    }

    @Test
    public void testGetUser() throws Exception {
        Contact loadContact = UserTools.getUserContact(getClient(), userId);
        assertNotNull("contact object is null", loadContact);
        assertEquals("user id is not equals", userId, loadContact.getInternalUserId());
        assertTrue("object id not set", loadContact.getObjectID() > 0);
        com.openexchange.ajax.user.actions.GetResponse response = getClient().execute(new com.openexchange.ajax.user.actions.GetRequest(userId, getClient().getValues().getTimeZone()));
        loadContact = response.getContact();
        assertNotNull("contact object is null", loadContact);
        assertEquals("user id is not equals", userId, loadContact.getInternalUserId());
        assertTrue("object id not set", loadContact.getObjectID() > 0);
    }

    // Node 2652    @Test
    @Test
    public void testLastModifiedUTC() throws Exception {
        final Contact contactObj = createContactObject("testNew");
        final int objectId = cotm.newAction(contactObj).getObjectID();
        final GetRequest req = new GetRequest(contactFolderId, objectId, getClient().getValues().getTimeZone());

        final AbstractAJAXResponse response = Executor.execute(getClient(), req);
        final JSONObject contact = (JSONObject) response.getResponse().getData();
        assertTrue(contact.has("last_modified_utc"));
    }
}
