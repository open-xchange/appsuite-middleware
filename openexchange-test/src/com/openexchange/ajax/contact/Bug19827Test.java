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

import static org.junit.Assert.assertNull;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.DeleteRequest;
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

    @After
    public void tearDown() throws Exception {
        try {
            if (null != contact) {
                contact.setLastModified(new Date(Long.MAX_VALUE));
                getClient().execute(new DeleteRequest(contact));
            }
        } finally {
            super.tearDown();
        }
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
