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
import ezvcard.property.VCardProperty;

/**
 * {@link SimpleMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class SimpleMapping<T extends VCardProperty> extends AbstractMapping {

    protected final int field;
    protected final Class<T> propertyClass;

    /**
     * Initializes a new {@link SimpleMapping}.
     *
     * @param field The mapped contact column identifier
     * @param propertyClass The vCard property class
     * @param propertyNames The affected vCard property names
     * @param contactFields The affected contact fields
     */
    protected SimpleMapping(int field, Class<T> propertyClass, String[] propertyNames, ContactField...contactFields) {
        super(propertyNames, contactFields);
        this.field = field;
        this.propertyClass = propertyClass;
    }

    /**
     * Initializes a new {@link SimpleMapping}.
     *
     * @param field The mapped contact column identifier
     * @param propertyClass The vCard property class
     * @param propertyName The affected vCard property name
     * @param contactFields The affected contact fields
     */
    protected SimpleMapping(int field, Class<T> propertyClass, String propertyName, ContactField...contactFields) {
        this(field, propertyClass, new String[] { propertyName }, contactFields);
    }

    protected abstract void exportProperty(Contact contact, T property, List<OXException> warnings);

    protected abstract T exportProperty(Contact contact, List<OXException> warnings);

    protected abstract void importProperty(T property, Contact contact, List<OXException> warnings);

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        T existingProperty = getFirstProperty(vCard);
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
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        T existingProperty = getFirstProperty(vCard);
        if (null == existingProperty) {
            contact.set(field, null);
        } else {
            importProperty(existingProperty, contact, warnings);
        }
    }

    protected T getFirstProperty(VCard vCard) {
        return getFirstProperty(vCard.getProperties(propertyClass));
    }

}
