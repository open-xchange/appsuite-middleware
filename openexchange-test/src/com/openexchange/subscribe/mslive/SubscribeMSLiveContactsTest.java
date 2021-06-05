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

package com.openexchange.subscribe.mslive;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.groupware.container.Contact;

/**
 * {@link SubscribeMSLiveContactsTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SubscribeMSLiveContactsTest extends MSLiveSubscribeTestEnvironment {

    private int getContactTestFolderID() {
        return getTestFolderID(MSLiveSubscribeTestEnvironment.CONTACT_SOURCE_ID);
    }

    private int getTestFolderID(final String id) {
        return getTestFolders().get(id).intValue();
    }

    @Test
    public void testSubscribe() {
        Contact[] contacts = cotm.allAction(getContactTestFolderID(), Contact.ALL_COLUMNS);

        // represents a full supported contact mapping
        final String testAccount1 = "Dr. Test Testerson";

        for (Contact c : contacts) {
            if (c.getDisplayName() != null) {
                if (c.getDisplayName().equals(testAccount1)) {
                    assertEquals("given name", "Test", c.getGivenName());
                    assertEquals("sur_name", "Testerson", c.getSurName());
                    assertEquals("email1", "invalid@home-private.com", c.getEmail1());
                    assertEquals("email1", "custom@custom.invalid", c.getEmail2());
                    assertEquals("email1", "test@invalid.org", c.getEmail3());
                }
            }
        }
    }
}
