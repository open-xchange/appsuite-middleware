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
 * {@link ExtendedStringPropertyMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ExtendedStringPropertyMapping extends ExtendedPropertyMapping {

    private final String[] alternativeNames;

    /**
     * Initializes a new {@link ExtendedStringPropertyMapping}.
     *
     * @param field The contact field
     * @param propertyName The vCard property name
     * @param alternativeNames Optional alternative property names that should be used during mapping
     * @param contactField The corresponding contact field
     */
    public ExtendedStringPropertyMapping(int field, String propertyName, ContactField contactField, String...alternativeNames) {
        super(field, propertyName, contactField);
        this.alternativeNames = alternativeNames;
    }

    @Override
    protected void exportProperty(Contact contact, RawProperty property, List<OXException> warnings) {
        Object value = contact.get(field);
        if (false == String.class.isInstance(value)) {
            throw new IllegalArgumentException("No string field: " + field);
        }
        property.setValue((String) value);
    }

    @Override
    protected void importProperty(RawProperty property, Contact contact, List<OXException> warnings) {
        contact.set(field, property.getValue());
    }

    @Override
    protected RawProperty exportProperty(Contact contact, List<OXException> warnings) {
        RawProperty property = new RawProperty(propertyName, null);
        exportProperty(contact, property, warnings);
        return property;
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters options, List<OXException> warnings) {
        /*
         * apply current value
         */
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
        /*
         * apply for any alternative representations of the property, too
         */
        if (null != alternativeNames && 0 < alternativeNames.length) {
            for (String alternativeName : alternativeNames) {
                RawProperty alternativeProperty = getFirstProperty(vCard.getExtendedProperties(alternativeName));
                if (null != alternativeProperty) {
                    if (has(contact, field)) {
                        exportProperty(contact, alternativeProperty, warnings);
                    } else {
                        vCard.removeProperty(alternativeProperty);
                    }
                }
            }
        }
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        /*
         * import property
         */
        RawProperty existingProperty = getFirstProperty(vCard);
        if (null == existingProperty) {
            contact.set(field, null);
        } else {
            importProperty(existingProperty, contact, warnings);
        }
        /*
         * try to import first matching alternative property as fallback
         */
        if (null == existingProperty && null != alternativeNames && 0 < alternativeNames.length) {
            for (String alternativeName : alternativeNames) {
                RawProperty alternativeProperty = getFirstProperty(vCard.getExtendedProperties(alternativeName));
                if (null != alternativeProperty) {
                    importProperty(alternativeProperty, contact, warnings);
                    break;
                }
            }
        }
    }

    @Override
    protected RawProperty getFirstProperty(VCard vCard) {
        return getFirstProperty(vCard.getExtendedProperties(propertyName));
    }

}
