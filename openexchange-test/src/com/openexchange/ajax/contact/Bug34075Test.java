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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.contact.action.CopyRequest;
import com.openexchange.ajax.contact.action.CopyResponse;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug34075Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug34075Test extends AbstractManagedContactTest {

    /**
     * Initializes a new {@link Bug34075Test}.
     *
     * @param name The test name
     */
    public Bug34075Test() {
        super();
    }

    @Test
    public void testAssignNewUidDuringCopy() throws Exception {
        /*
         * create contact
         */
        Contact contact = generateContact();
        contact.setUid(UUID.randomUUID().toString());
        contact = cotm.newAction(contact);
        /*
         * copy contact
         */
        CopyResponse copyResponse = getClient().execute(new CopyRequest(contact.getObjectID(), contact.getParentFolderID(), contact.getParentFolderID(), true));
        assertNotNull("No response", copyResponse);
        assertFalse("Errors in response", copyResponse.hasError());
        JSONObject data = (JSONObject) copyResponse.getData();
        int objectID = data.getInt(DataFields.ID);
        /*
         * check copy
         */
        Contact copiedContact = cotm.getAction(contact.getParentFolderID(), objectID);
        assertNotNull("Copied contact not found", copiedContact);
        assertEquals("Last name wrong", contact.getSurName(), copiedContact.getSurName());
        assertFalse("Same UID in copied contact", contact.getUid().equals(copiedContact.getUid()));
    }

}
