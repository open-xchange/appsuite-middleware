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

import java.util.Date;
import com.openexchange.ajax.contact.action.DeleteRequest;
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

    public YomiTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
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

    @Override
    protected void tearDown() throws Exception {
        getClient().execute(new DeleteRequest(contact));
        super.tearDown();
    }

}
