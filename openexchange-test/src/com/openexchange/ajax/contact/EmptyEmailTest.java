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

import org.json.JSONException;
import org.json.JSONObject;
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
    public EmptyEmailTest(String name) {
        super(name);
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
        final InsertResponse response = client.execute(modifiedRequest);
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
        client.execute(modifiedRequest);
    }

    /**
     * Set the email fields in a contact already converted to a JSONObject. 
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
     * @throws Exception
     */
    public void testNewNull() throws Exception {
        final Contact contactObj = createContactObject("Schmidt, Hans");
        contactObj.setGivenName("Hans");
        contactObj.setSurName("Schmidt");
        insertContact(contactObj, null, null, null);
    }

    /**
     * Create a new contact with email[1-3] set to the empty String.
     * @throws Exception
     */
    public void testNewEmpty() throws Exception {
        final Contact contactObj = createContactObject("Schmidt, Hans");
        contactObj.setGivenName("Hans");
        contactObj.setSurName("Schmidt");
        insertContact(contactObj, "", "", "");
    }
    
    /**
     * Create a new contact with email[1-3] set to the empty String.
     * @throws Exception
     */
    public void testNewSpacy() throws Exception {
        final Contact contactObj = createContactObject("Schmidt, Hans");
        contactObj.setGivenName("Hans");
        contactObj.setSurName("Schmidt");
        insertContact(contactObj, "   ", "   ", "   ");
    }

    /**
     * Update an existing contact to hold nulls in email[1-3].
     * @throws Exception
     */
    public void testUpdateNull() throws Exception {
        final Contact contactObj = createContactObject("Schmidt, Hans");
        contactObj.setGivenName("Hans");
        contactObj.setSurName("Schmidt");
        contactObj.setEmail1("email1@open-xchange.com");
        contactObj.setEmail2("email2@open-xchange.com");
        contactObj.setEmail3("email3@open-xchange.com");
        int id = insertContact(contactObj);
        Contact loadedContact = loadContact(id, contactFolderId);
        updateContact(loadedContact, contactFolderId, null, null, null);
    }

    /**
     * Update an existing contact to hold empty strings in email[1-3].
     * @throws Exception
     */
    public void testUpdateEmpty() throws Exception {
        final Contact contactObj = createContactObject("Schmidt, Hans");
        contactObj.setGivenName("Hans");
        contactObj.setSurName("Schmidt");
        contactObj.setEmail1("email1@open-xchange.com");
        contactObj.setEmail2("email2@open-xchange.com");
        contactObj.setEmail3("email3@open-xchange.com");
        int id = insertContact(contactObj);
        Contact loadedContact = loadContact(id, contactFolderId);
        updateContact(loadedContact, contactFolderId, "", "", "");
    }
    
    /**
     * Update an existing contact to hold empty strings in email[1-3].
     * @throws Exception
     */
    public void testUpdateSpacy() throws Exception {
        final Contact contactObj = createContactObject("Schmidt, Hans");
        contactObj.setGivenName("Hans");
        contactObj.setSurName("Schmidt");
        contactObj.setEmail1("email1@open-xchange.com");
        contactObj.setEmail2("email2@open-xchange.com");
        contactObj.setEmail3("email3@open-xchange.com");
        int id = insertContact(contactObj);
        Contact loadedContact = loadContact(id, contactFolderId);
        updateContact(loadedContact, contactFolderId, "   ", "   ", "   ");
    }

}
