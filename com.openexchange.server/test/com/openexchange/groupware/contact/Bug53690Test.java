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

package com.openexchange.groupware.contact;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Locale;
import org.junit.Test;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;

/**
 * {@link Bug53690Test}
 *
 * Fields considered for sorting / categorizing contacts inconsistent
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug53690Test {

    @Test
    public void testSortWithYomiFirstNameOnly() {
        Contact contact = new Contact();
        contact.setYomiFirstName("test");
        assertFalse("No sort name", Strings.isEmpty(contact.getSortName()));
    }

    @Test
    public void testSortWithGivenNameOnly() {
        Contact contact = new Contact();
        contact.setGivenName("test");
        assertFalse("No sort name", Strings.isEmpty(contact.getSortName()));
    }

    @Test
    public void testSortYomiNamesFirst() {
        Contact contact = new Contact();
        contact.setYomiFirstName("YomiFirstName");
        contact.setYomiLastName("YomiLastName");
        contact.setSurName("SurName");
        contact.setGivenName("GivenName");
        assertFalse("No sort name", Strings.isEmpty(contact.getSortName(Locale.JAPANESE)));
        assertTrue("Yomi names not first", contact.getSortName(Locale.JAPANESE).startsWith("YomiLastName_YomiFirstName"));
    }
}
