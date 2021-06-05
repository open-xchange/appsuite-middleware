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
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.user.actions.ListRequest;
import com.openexchange.ajax.user.actions.ListResponse;
import com.openexchange.groupware.container.Contact;

/**
 * {@link ListTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ListTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link ListTest}.
     * 
     * @param name
     */
    public ListTest() {
        super();
    }

    @Test
    public void testListUser() throws Exception {
        final int[] userIdArray = { getClient().getValues().getUserId() };
        final int[] cols = { Contact.OBJECT_ID, Contact.SUR_NAME, Contact.DISPLAY_NAME };
        final ListRequest request = new ListRequest(userIdArray, cols);
        final ListResponse response = getClient().execute(request);
        final Contact[] contactArray = response.getUsers();
        assertEquals("check response array", userIdArray.length, contactArray.length);
    }
}
