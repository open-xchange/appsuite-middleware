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

package com.openexchange.contact.vcard.impl.mapping;

import java.util.List;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import ezvcard.VCard;
import ezvcard.property.StructuredName;

/**
 * {@link StructuredNameMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class StructuredNameMapping extends AbstractMapping {

    /**
     * Initializes a new {@link StructuredNameMapping}.
     */
    public StructuredNameMapping() {
        super("N", ContactField.SUR_NAME, ContactField.GIVEN_NAME, ContactField.MIDDLE_NAME, ContactField.TITLE, ContactField.SUFFIX);
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        StructuredName property = vCard.getStructuredName();
        if (containsStructuredName(contact)) {
            if (null == property) {
                property = new StructuredName();
                vCard.setStructuredName(property);
            }
            property.setFamily(contact.getSurName());
            property.setGiven(contact.getGivenName());
            property.getAdditionalNames().clear();
            String middleName = contact.getMiddleName();
            if (Strings.isNotEmpty(middleName)) {
                property.getAdditionalNames().add(middleName);
            }
            property.getPrefixes().clear();
            String title = contact.getTitle();
            if (Strings.isNotEmpty(title)) {
                property.getPrefixes().add(title);
            }
            property.getSuffixes().clear();
            String suffix = contact.getSuffix();
            if (Strings.isNotEmpty(suffix)) {
                property.getSuffixes().add(suffix);
            }
        } else if (null != property) {
            vCard.removeProperty(property);
        }
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        String surName = null;
        String givenName = null;
        String middleName = null;
        String title = null;
        String suffix = null;
        StructuredName property = vCard.getStructuredName();
        if (null != property) {
            surName = property.getFamily();
            givenName = property.getGiven();
            List<String> additional = property.getAdditionalNames();
            middleName = null != additional && 0 < additional.size() ? Strings.join(additional, " ") : null;
            List<String> prefixes = property.getPrefixes();
            title = null != prefixes && 0 < prefixes.size() ? Strings.join(prefixes, " ") : null;
            List<String> suffixes = property.getSuffixes();
            suffix = null != suffixes && 0 < suffixes.size() ? Strings.join(suffixes, " ") : null;
        }
        contact.setSurName(surName);
        contact.setGivenName(givenName);
        contact.setMiddleName(middleName);
        contact.setTitle(title);
        contact.setSuffix(suffix);
    }

    private static boolean containsStructuredName(Contact contact) {
        return hasOneOf(contact, Contact.SUR_NAME, Contact.GIVEN_NAME, Contact.MIDDLE_NAME, Contact.TITLE, Contact.SUFFIX);
    }

}
