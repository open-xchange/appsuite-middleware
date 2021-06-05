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

import static org.junit.Assert.assertNull;
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
 * {@link Bug19827Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug19827Test extends AbstractAJAXSession {

    private Contact contact;

    public Bug19827Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        contact = new Contact();
        contact.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        contact.setDisplayName("Test for bug 19827");
        contact.setStreetBusiness("Business Street 55");
        contact.setPostalCodeBusiness("54453");
        contact.setCityBusiness("Business City");
        contact.setStateBusiness("Business State");
        contact.setCountryBusiness("Business Country");
        contact.setAddressBusiness("Business Street 55\r\n54453 Business City\r\nBusiness State - Business Country");
        contact.setStreetHome("Home Street 55");
        contact.setPostalCodeHome("54453");
        contact.setCityHome("Home City");
        contact.setStateHome("Home State");
        contact.setCountryHome("Home Country");
        contact.setAddressHome("Home Street 55\r\n54453 Home City\r\nHome State - Home Country");
        contact.setStreetOther("Other Street 55");
        contact.setPostalCodeOther("54453");
        contact.setCityOther("Other City");
        contact.setStateOther("Other State");
        contact.setCountryOther("Other Country");
        contact.setAddressOther("Other Street 55\r\n54453 Other City\r\nOther State - Other Country");
        InsertRequest request = new InsertRequest(contact);
        InsertResponse response = getClient().execute(request);
        response.fillObject(contact);
    }

    @Test
    public void testInvalidateBusinessAddress() throws Throwable {
        GetResponse getResponse = getClient().execute(new GetRequest(contact, getClient().getValues().getTimeZone()));
        Contact contact = getResponse.getContact();
        contact.removeCreationDate();
        contact.setLastModified(getResponse.getTimestamp());
        contact.setPostalCodeBusiness("99999");
        getClient().execute(new UpdateRequest(contact));
        GetResponse response = getClient().execute(new GetRequest(contact, getClient().getValues().getTimeZone()));
        contact = response.getContact();
        assertNull("Business address not invalidated", contact.getAddressBusiness());
    }

    @Test
    public void testInvalidateHomeAddress() throws Throwable {
        GetResponse getResponse = getClient().execute(new GetRequest(contact, getClient().getValues().getTimeZone()));
        Contact contact = getResponse.getContact();
        contact.removeCreationDate();
        contact.setLastModified(getResponse.getTimestamp());
        contact.setStreetHome("Changed Street 88");
        getClient().execute(new UpdateRequest(contact));
        GetResponse response = getClient().execute(new GetRequest(contact, getClient().getValues().getTimeZone()));
        contact = response.getContact();
        assertNull("Home address not invalidated", contact.getAddressHome());
    }

    @Test
    public void testInvalidateOtherAddress() throws Throwable {
        GetResponse getResponse = getClient().execute(new GetRequest(contact, getClient().getValues().getTimeZone()));
        Contact contact = getResponse.getContact();
        contact.removeCreationDate();
        contact.setLastModified(getResponse.getTimestamp());
        contact.setCountryOther("Another updated country");
        getClient().execute(new UpdateRequest(contact));
        GetResponse response = getClient().execute(new GetRequest(contact, getClient().getValues().getTimeZone()));
        contact = response.getContact();
        assertNull("Other address not invalidated", contact.getAddressOther());
    }

}
