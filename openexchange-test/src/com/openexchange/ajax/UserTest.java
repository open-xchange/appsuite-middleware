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

package com.openexchange.ajax;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.user.UserTools;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;

public class UserTest extends AbstractAJAXSession {

    public final static int[] CONTACT_FIELDS = { DataObject.OBJECT_ID, Contact.INTERNAL_USERID, Contact.EMAIL1, Contact.GIVEN_NAME, Contact.SUR_NAME, Contact.DISPLAY_NAME
    };

    @Test
    public void testSearch() throws Exception {
        final com.openexchange.user.User users[] = UserTools.searchUser(getClient(), "*");
        assertTrue("user array size > 0", users.length > 0);
    }

    @Test
    public void testList() throws Exception {
        com.openexchange.user.User users[] = UserTools.searchUser(getClient(), "*");
        assertTrue("user array size > 0", users.length > 0);

        final int[] id = new int[users.length];
        for (int a = 0; a < id.length; a++) {
            id[a] = users[a].getId();
        }

        users = UserTools.listUser(getClient(), id);
        assertTrue("user array size > 0", users.length > 0);
    }

    @Test
    public void testSearchUsers() throws Exception {
        final com.openexchange.user.User users[] = UserTools.searchUser(getClient(), "*");
        assertTrue("user array size > 0", users.length > 0);
    }

    @Test
    public void testGet() throws Exception {
        final com.openexchange.user.User users[] = UserTools.searchUser(getClient(), "*");
        assertTrue("user array size > 0", users.length > 0);
        UserTools.getUser(getClient(), users[0].getId());
    }
}
