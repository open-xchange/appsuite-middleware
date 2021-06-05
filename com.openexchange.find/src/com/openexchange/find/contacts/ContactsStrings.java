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

package com.openexchange.find.contacts;

import com.openexchange.i18n.LocalizableStrings;

/**
 * Contact-specific strings are potentially displayed in client applications and should therefore be localized.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public class ContactsStrings implements LocalizableStrings {

    public static final String FACET_TYPE_CONTACT_TYPE = "Type";

    public static final String FACET_TYPE_CONTACT = "Contact";

    // Context: Searching in contacts.
    // Displayed as: [Search for] 'user input' in names.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_NAME = "in names";

    // Context: Searching in contacts.
    // Displayed as: [Search for] 'user input' in e-mail addresses.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_EMAIL = "in e-mail addresses";

    // Context: Searching in contacts.
    // Displayed as: [Search for] 'user input' in phone numbers.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_PHONE = "in phone numbers";

    // Context: Searching in contacts.
    // Displayed as: [Search for] 'user input' in addresses.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_ADDRESS = "in addresses";

    // Context: Searching in contacts.
    // Displayed as: [Search for] 'user input' in departments.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_DEPARTMENT = "in departments";

    // Context: Searching in contacts.
    // Displayed as: [Search for] 'user input' in user fields.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_USER_FIELDS = "in user fields";

    public static final String CONTACT_TYPE_CONTACT = "Contact";

    public static final String CONTACT_TYPE_DISTRIBUTION_LIST = "Distribution List";

}
