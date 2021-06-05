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

package com.openexchange.contact.storage.rdb.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * {@link RdbContactStorageTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class RdbContactStorageTest {

    private RdbContactStorage rdbContactStorage;

    @Before
    public void setUp() {
        rdbContactStorage = new RdbContactStorage();
    }

     @Test
     public void testSupports_allSupported_returnTrue() {
        boolean supports = rdbContactStorage.supports(ContactField.SUR_NAME, ContactField.TITLE);
        assertTrue(supports);
    }

     @Test
     public void testSupports_providedOneSupported_returnTrue() {
        boolean supports = rdbContactStorage.supports(ContactField.CELLULAR_TELEPHONE1);
        assertTrue(supports);
    }

     @Test
     public void testSupports_providedOneNotSupported_returnFalse() {
        boolean supports = rdbContactStorage.supports(ContactField.IMAGE1_URL);
        assertFalse(supports);
    }

     @Test
     public void testSupports_multipleProvidedOneNotSupported_returnFlase() {
        boolean supports = rdbContactStorage.supports(ContactField.CELLULAR_TELEPHONE1, ContactField.CITY_HOME, ContactField.USERFIELD15, ContactField.TELEPHONE_CALLBACK, ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, ContactField.DEFAULT_ADDRESS, ContactField.TELEPHONE_PRIMARY);
        assertFalse(supports);
    }

     @Test
     public void testSupports_allFieldsProvidedThatContainNotSupportedOnes_returnFalse() {
        boolean supports = rdbContactStorage.supports(ContactField.values());
        assertFalse(supports);
    }

}
