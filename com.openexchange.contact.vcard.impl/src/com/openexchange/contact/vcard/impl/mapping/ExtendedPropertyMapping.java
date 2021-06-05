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
import ezvcard.property.RawProperty;

/**
 * {@link ExtendedPropertyMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ExtendedPropertyMapping extends SimpleMapping<RawProperty> {

    protected final String propertyName;

    /**
     * Initializes a new {@link ExtendedPropertyMapping}.
     *
     * @param field The contact field
     * @param propertyName The vCard property name
     * @param contactFields The corresponding contact fields
     */
    protected ExtendedPropertyMapping(int field, String propertyName, @SuppressWarnings("unused") ContactField... contactFields) {
        super(field, RawProperty.class, propertyName);
        this.propertyName = propertyName;
    }

    @Override
    protected RawProperty exportProperty(Contact contact, List<OXException> warnings) {
        RawProperty property = new RawProperty(propertyName, null);
        exportProperty(contact, property, warnings);
        return property;
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        RawProperty existingProperty = getFirstProperty(vCard);
        if (has(contact, field)) {
            if (null == existingProperty) {
                vCard.addProperty(exportProperty(contact, warnings));
            } else {
                exportProperty(contact, existingProperty, warnings);
            }
        } else if (null != existingProperty) {
            vCard.removeProperty(existingProperty);
        }
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters  parameters, List<OXException> warnings) {
        RawProperty existingProperty = getFirstProperty(vCard);
        if (null == existingProperty) {
            contact.set(field, null);
        } else {
            importProperty(existingProperty, contact, warnings);
        }
    }

    @Override
    protected RawProperty getFirstProperty(VCard vCard) {
        return getFirstProperty(vCard.getExtendedProperties(propertyName));
    }

}
