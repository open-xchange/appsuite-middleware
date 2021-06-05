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
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug16515Test extends AbstractAJAXSession {

    private TimeZone tz;
    private Contact contact;

    private final String FILE_AS_VALUE = "I'm the file_as field of Herbert Meier";

    public Bug16515Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        tz = getClient().getValues().getTimeZone();
        contact = createContact();
    }

    @Test
    public void testFileAs() throws Exception {
        GetRequest getContactReq = new GetRequest(contact.getParentFolderID(), contact.getObjectID(), tz);
        GetResponse getContactResp = getClient().execute(getContactReq);
        final Contact toCompare = getContactResp.getContact();

        assertEquals("File as has changed after creating contact.", contact.getFileAs(), toCompare.getFileAs());
    }

    public Contact createContact() throws Exception {
        final Contact contact = new Contact();
        contact.setTitle("Herr");
        contact.setSurName("Meier");
        contact.setGivenName("Herbert");
        contact.setDisplayName("Herbert Meier");
        contact.setStreetBusiness("Franz-Meier Weg 17");
        contact.setCityBusiness("Test Stadt");
        contact.setStateBusiness("NRW");
        contact.setCountryBusiness("Deutschland");
        contact.setTelephoneBusiness1("+49112233445566");
        contact.setCompany("Internal Test AG");
        contact.setEmail1("hebert.meier@open-xchange.com");
        contact.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        contact.setFileAs(FILE_AS_VALUE);

        InsertRequest insertContactReq = new InsertRequest(contact);
        InsertResponse insertContactResp = getClient().execute(insertContactReq);
        insertContactResp.fillObject(contact);

        return contact;
    }
}
