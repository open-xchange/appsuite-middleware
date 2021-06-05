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
import ezvcard.VCard;

/**
 * {@link VCardMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface VCardMapping {

    /**
     * Exports the mapped contact attributes into the supplied vCard.
     *
     * @param contact The contact to export
     * @param vCard The target vCard
     * @param parameters Further options to use, or <code>null</code> to use to the default options
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     */
    void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings);

    /**
     * Imports the mapped vCard properties into the supplied contact
     *
     * @param vCard The vCard to import
     * @param contact The target contact
     * @param parameters Further options to use, or <code>null</code> to use to the default options
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     */
    void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings);

    /**
     * Gets the property names affected by this mapping.
     *
     * @return The property names
     */
    String[] getPropertyNames();

    /**
     * Gets the contact fields corresponding to the vCard properties affected by this mapping.
     *
     * @return The contact fields
     */
    ContactField[] getContactFields();

}
