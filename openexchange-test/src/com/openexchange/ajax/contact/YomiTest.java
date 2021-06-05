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
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.UpdateRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;

/**
 * {@link YomiTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class YomiTest extends AbstractAJAXSession {

    private Contact contact;

    private Contact updateContact;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        contact = new Contact();
        contact.setTitle("Herr");
        contact.setGivenName("\u660e\u65e5\u9999");
        contact.setSurName("\u4f50\u85e4");
        contact.setDisplayName("Baab Abba");
        contact.setStreetBusiness("Franz-Meier Weg 17");
        contact.setCityBusiness("Test Stadt");
        contact.setStateBusiness("NRW");
        contact.setCountryBusiness("Deutschland");
        contact.setTelephoneBusiness1("+49112233445566");
        contact.setCompany("Internal Test AG");
        contact.setEmail1("baab.abba@open-foobar.com");
        contact.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        contact.setYomiFirstName("\u30a2\u30b9\u30ab");
        contact.setYomiLastName("\u30b5\u30c8\u30a6");
        InsertRequest insertRequest = new InsertRequest(contact);
        InsertResponse insertResponse = getClient().execute(insertRequest);
        insertResponse.fillObject(contact);

        updateContact = new Contact();
        updateContact.setYomiFirstName("\u3055\u3068\u3046");
        updateContact.setYomiLastName("\u3059\u305a\u304d");
        updateContact.setParentFolderID(getClient().getValues().getPrivateContactFolder());
    }

    @Test
    public void testYomiFields() throws Exception {
        GetRequest getRequest = new GetRequest(contact, getClient().getValues().getTimeZone());
        GetResponse getResponse = getClient().execute(getRequest);
        Contact loadedContact = getResponse.getContact();
        assertEquals("Wrong Kanji First name", "\u660e\u65e5\u9999", loadedContact.getGivenName());
        assertEquals("Wrong Kanji Last name", "\u4f50\u85e4", loadedContact.getSurName());
        assertEquals("Wrong Yomi First name", "\u30a2\u30b9\u30ab", loadedContact.getYomiFirstName());
        assertEquals("Wrong Yomi Last name", "\u30b5\u30c8\u30a6", loadedContact.getYomiLastName());

        updateContact.setObjectID(contact.getObjectID());
        updateContact.setLastModified(contact.getLastModified());
        UpdateRequest updateRequest = new UpdateRequest(updateContact);
        getClient().execute(updateRequest);
        contact.setLastModified(new Date(Long.MAX_VALUE));

        getRequest = new GetRequest(contact, getClient().getValues().getTimeZone());
        getResponse = getClient().execute(getRequest);
        Contact loadedUpdatedContact = getResponse.getContact();
        assertEquals("Wrong Kanji First name", "\u660e\u65e5\u9999", loadedUpdatedContact.getGivenName());
        assertEquals("Wrong Kanji Last name", "\u4f50\u85e4", loadedUpdatedContact.getSurName());
        assertEquals("Wrong Yomi First name", "\u3055\u3068\u3046", loadedUpdatedContact.getYomiFirstName());
        assertEquals("Wrong Yomi Last name", "\u3059\u305a\u304d", loadedUpdatedContact.getYomiLastName());
    }

}
