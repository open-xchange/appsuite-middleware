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

package com.openexchange.find.util;

import java.util.Locale;
import com.openexchange.find.facet.ComplexDisplayItem;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.groupware.contact.helpers.ContactDisplayNameHelper;
import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.java.Strings;

/**
 * A helper class to create {@link DisplayItem}s for common cases.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.6.1
 */
public class DisplayItems {

    /**
     * Converts the specified {@link Contact} result into a {@link ComplexDisplayItem}
     *
     * @param contact the {@link Contact} to convert
     * @param locale the user's {@link Locale}
     * @param i18nServiceRegistry The instance of the {@link I18nServiceRegistry}
     * @return The {@link ComplexDisplayItem}
     */
    public static ComplexDisplayItem convert(Contact contact, Locale locale, I18nServiceRegistry i18nServiceRegistry) {
        String displayName = ContactDisplayNameHelper.formatDisplayName(i18nServiceRegistry, contact, locale);
        String primaryAddress = extractPrimaryMailAddress(contact);
        if (Strings.isEmpty(displayName)) {
            displayName = Strings.isEmpty(primaryAddress) ? "" : primaryAddress;
        }

        ComplexDisplayItem item = new ComplexDisplayItem(displayName, primaryAddress);
        if (0 < contact.getNumberOfImages() || contact.containsImage1() && null != contact.getImage1()) {
            item.setContactForImageData(contact);
        }
        return item;
    }

    /**
     * Extracts the primary e-mail address of the specified {@link Contact}.
     *
     * @param contact The {@link Contact} to extract the e-mail address from
     * @return The primary e-mail address
     */
    private static String extractPrimaryMailAddress(Contact contact) {
        String address = contact.getEmail1();
        if (Strings.isEmpty(address)) {
            address = contact.getEmail2();
        }
        if (Strings.isEmpty(address)) {
            address = contact.getEmail3();
        }

        return address;
    }

}
