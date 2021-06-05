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
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.user.actions.UpdateRequest;
import com.openexchange.ajax.user.actions.UpdateResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug55703Test}
 *
 * "oxadmin" account information can be altered by users
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug55703Test extends AbstractManagedContactTest {

    /**
     * Initializes a new {@link Bug55703Test}.
     *
     * @param name The test name
     */
    public Bug55703Test() {
        super();
    }

    @Test
    public void testUpdateOXAdmin() throws Exception {
        /*
         * get current contact for the admin (assume oxadmin always has contact identifier '1')
         */
        Contact originalContact = cotm.getAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, 1);
        /*
         * try and update the contact through the 'user' module
         */
        Contact modifiedContact = new Contact();
        modifiedContact.setParentFolderID(originalContact.getParentFolderID());
        modifiedContact.setObjectID(originalContact.getObjectID());
        modifiedContact.setInternalUserId(originalContact.getInternalUserId());
        modifiedContact.setNote(UUID.randomUUID().toString());
        modifiedContact.setLastModified(originalContact.getLastModified());
        UpdateResponse updateResponse = getClient().execute(new UpdateRequest(modifiedContact, null, false));
        assertTrue("No errors when updating the admin contact",  updateResponse.hasError());
        assertEquals("Unexpected error code", "CON-0176", updateResponse.getException().getErrorCode());
        /*
         * check that the admin contact was not modified
         */
        Contact reloadedContact = cotm.getAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, 1);
        assertEquals("Note was modified", originalContact.getNote(), reloadedContact.getNote());
    }

}
