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
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug15937Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15937Test extends AbstractAJAXSession {

    private Contact contact;

    public Bug15937Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        contact = new Contact();
        contact.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        contact.setDisplayName("Test for bug 15937");
        contact.setNumberOfAttachments(42);
        InsertRequest request = new InsertRequest(contact);
        InsertResponse response = getClient().execute(request);
        response.fillObject(contact);
    }

    @Test
    public void testNumberOfAttachments() throws Throwable {
        GetRequest request = new GetRequest(contact, getClient().getValues().getTimeZone());
        GetResponse response = getClient().execute(request);
        Contact testContact = response.getContact();
        assertTrue("Number of attachments should be send.", testContact.containsNumberOfAttachments());
        assertEquals("Number of attachments must be zero.", 0, testContact.getNumberOfAttachments());
    }
}
