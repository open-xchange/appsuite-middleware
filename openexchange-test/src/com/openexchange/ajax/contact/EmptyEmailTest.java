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

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.UpdateRequest;
import com.openexchange.groupware.container.Contact;

/**
 * {@link EmptyEmailTest} - Tests the use an empty String or null for email[1-3] during creation or update of a contact.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class EmptyEmailTest extends AbstractContactTest {

    private static final String GIVEN_NAME = "Hans";
    private static final String SUR_NAME = "Schmidt";
    private static final String EMAIL1 = "email1@open-xchange.com";
    private static final String EMAIL2 = "email2@open-xchange.com";
    private static final String EMAIL3 = "email3@open-xchange.com";
    
    /**
     * {@link EmptyEmailUpdateRequest} - Private inner class that let's us set empty emails during updates. Workaround needed because
     * DataWriter will always replace empty Strings with null which results in the field not being set in the JSONObject
     * 
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     */
    private class EmptyEmailUpdateRequest extends UpdateRequest {

        private JSONObject modifiedBody;

        /**
         * Initializes a new {@link EmptyEmailUpdateRequest}.
         * 
         * @param contactObj
         */
        public EmptyEmailUpdateRequest(Contact contactObj) {
            super(contactObj);
        }

        public EmptyEmailUpdateRequest(Contact co, JSONObject jo) {
            super(co);
            modifiedBody = jo;
        }

        @Override
        public Object getBody() throws JSONException {
            return modifiedBody;
        }

    }

    /**
     * Initializes a new {@link EmptyEmailTest}.
     * 
     * @param name
     */
    public EmptyEmailTest() {
        super();
    }

    /**
     * Own version of insertContact, expanding the one from {@link AbstractContactTest} with email fields.
     * 
     * @param contactObj The contact to insert
     * @param email1 email1
     * @param email2 email2
     * @param email3 email3
     * @return Id of the created contact
     * @throws Exception
     */
    public int insertContact(final Contact contactObj, String email1, String email2, String email3) throws Exception {
        final InsertRequest request = new InsertRequest(contactObj);
        // manipulate body of the insertRequest
        JSONObject jsonObject = (JSONObject) request.getBody();
        jsonObject = setEmail(jsonObject, email1, email2, email3);
        // and create new InsertRequest with modified body
        final InsertRequest modifiedRequest = new InsertRequest(jsonObject.toString());
        final InsertResponse response = getClient().execute(modifiedRequest);
        response.fillObject(contactObj);
        return contactObj.getObjectID();
    }

    /**
     * Own version of updateContact, expanding the one from {@link AbstractContactTest} with email fields and using
     * {@link EmptyEmailUpdateRequest}
     * 
     * @param contactObj The contact to update
     * @param inFolder the folder
     * @param email1 email1
     * @param email2 email2
     * @param email3 email3
     * @throws Exception
     */
    public void updateContact(final Contact contactObj, final int inFolder, String email1, String email2, String email3) throws Exception {
        final UpdateRequest request = new UpdateRequest(inFolder, contactObj, true);
        JSONObject jsonObject = (JSONObject) request.getBody();
        jsonObject = setEmail(jsonObject, email1, email2, email3);
        EmptyEmailUpdateRequest modifiedRequest = new EmptyEmailUpdateRequest(contactObj, jsonObject);
        getClient().execute(modifiedRequest);
    }

    /**
     * Set the email fields in a contact already converted to a JSONObject.
     * 
     * @param jo JSONObject
     * @param email1 email1
     * @param email2 email2
     * @param email3 email3
     * @return The modified contact in JSONObject form
     * @throws JSONException
     */
    private JSONObject setEmail(final JSONObject jo, final String email1, final String email2, final String email3) throws JSONException {
        jo.put("email1", email1 == null ? JSONObject.NULL : email1);
        jo.put("email2", email2 == null ? JSONObject.NULL : email3);
        jo.put("email3", email3 == null ? JSONObject.NULL : email3);
        return jo;
    }

    /*
     * Tests
     */

    /**
     * Create new contact with email[1-3] set to null.
     * 
     * @throws Exception
     */
    @Test
    public void testNewNull() throws Exception {
        final Contact contactObj = createContactObject();
        contactObj.setGivenName(GIVEN_NAME);
        contactObj.setSurName(SUR_NAME);
        insertContact(contactObj, null, null, null);
    }

    /**
     * Create a new contact with email[1-3] set to the empty String.
     * 
     * @throws Exception
     */
    @Test
    public void testNewEmpty() throws Exception {
        final Contact contactObj = createContactObject();
        contactObj.setGivenName(GIVEN_NAME);
        contactObj.setSurName(SUR_NAME);
        insertContact(contactObj, "", "", "");
    }

    /**
     * Create a new contact with email[1-3] set to the empty String.
     * 
     * @throws Exception
     */
    @Test
    public void testNewSpacy() throws Exception {
        final Contact contactObj = createContactObject();
        contactObj.setGivenName(GIVEN_NAME);
        contactObj.setSurName(SUR_NAME);
        insertContact(contactObj, "   ", "   ", "   ");
    }

    /**
     * Update an existing contact to hold nulls in email[1-3].
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateNull() throws Exception {
        final Contact contactObj = createContactObject();
        contactObj.setGivenName(GIVEN_NAME);
        contactObj.setSurName(SUR_NAME);
        contactObj.setEmail1(EMAIL1);
        contactObj.setEmail2(EMAIL2);
        contactObj.setEmail3(EMAIL3);
        int id = insertContact(contactObj);
        Contact loadedContact = loadContact(id, contactFolderId);
        updateContact(loadedContact, contactFolderId, null, null, null);
    }

    /**
     * Update an existing contact to hold empty strings in email[1-3].
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateEmpty() throws Exception {
        final Contact contactObj = createContactObject();
        contactObj.setGivenName(GIVEN_NAME);
        contactObj.setSurName(SUR_NAME);
        contactObj.setEmail1(EMAIL1);
        contactObj.setEmail2(EMAIL2);
        contactObj.setEmail3(EMAIL3);
        int id = insertContact(contactObj);
        Contact loadedContact = loadContact(id, contactFolderId);
        updateContact(loadedContact, contactFolderId, "", "", "");
    }

    /**
     * Update an existing contact to hold empty strings in email[1-3].
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateSpacy() throws Exception {
        final Contact contactObj = createContactObject();
        contactObj.setGivenName(GIVEN_NAME);
        contactObj.setSurName(SUR_NAME);
        contactObj.setEmail1(EMAIL1);
        contactObj.setEmail2(EMAIL2);
        contactObj.setEmail3(EMAIL3);
        int id = insertContact(contactObj);
        Contact loadedContact = loadContact(id, contactFolderId);
        updateContact(loadedContact, contactFolderId, "   ", "   ", "   ");
    }

}
