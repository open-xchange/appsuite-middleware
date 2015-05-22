/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.contact.vcard.mapping;

import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.groupware.container.Contact;
import ezvcard.VCard;
import ezvcard.property.TextProperty;

/**
 * {@link SimpleStringMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class SimpleStringMapping<T extends TextProperty> extends AbstractMapping {

    protected final int field;
    protected final Class<T> propertyClass;

    public SimpleStringMapping(int field, Class<T> propertyClass) {
        super();
        this.field = field;
        this.propertyClass = propertyClass;
    }

    protected abstract T newProperty();

    protected void exportProperty(Contact contact, T property) {
        Object value = contact.get(field);
        if (false == String.class.isInstance(value)) {
            throw new IllegalArgumentException("No string field: " + field);
        }
        property.setValue((String) value);
    }

    protected T exportProperty(Contact contact) {
        T property = newProperty();
        exportProperty(contact, property);
        return property;
    }

    protected void importProperty(T property, Contact contact) {
        String value = property.getValue();
        contact.set(field, value);
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters options) {
        T existingProperty = getFirstProperty(vCard);
        if (null != contact.get(field)) {
            if (null == existingProperty) {
                vCard.addProperty(exportProperty(contact));
            } else {
                exportProperty(contact, existingProperty);
            }
        } else if (null != existingProperty) {
            vCard.removeProperty(existingProperty);
        }
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters options) {
        T existingProperty = getFirstProperty(vCard);
        if (null == existingProperty) {
            contact.set(field, null);
        } else {
            importProperty(existingProperty, contact);
        }
    }

    private T getFirstProperty(VCard vCard) {
        return getFirstProperty(vCard.getProperties(propertyClass));
    }

}
