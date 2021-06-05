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

package com.openexchange.ajax.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.user.actions.UpdateRequest;
import com.openexchange.ajax.user.actions.UpdateResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.UserImpl;

/**
 * {@link MWB963Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public final class MWB963Test extends AbstractAJAXSession {

    @Test
    public void testInvalidTimezoneId() throws Exception {
        Contact userContact = UserTools.getUserContact(getClient(), getClient().getValues().getUserId());
        Contact updatedContact = new Contact();
        updatedContact.setInternalUserId(userContact.getInternalUserId());
        updatedContact.setLastModified(userContact.getLastModified());
        updatedContact.setFolderId(userContact.getFolderId());
        UserImpl updatedUser = new UserImpl();
        updatedUser.setId(userContact.getInternalUserId());
        updatedUser.setTimeZone("Europe/Olpe");
        UpdateResponse updateResponse = getClient().execute(new UpdateRequest(updatedContact, updatedUser, false));
        assertNotNull(updateResponse);
        assertTrue("no error in response", updateResponse.hasError());
        assertNotNull("no error in response", updateResponse.getException());
        assertEquals("unexpected error code", "USR-0024", updateResponse.getException().getErrorCode());
    }
}
