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
