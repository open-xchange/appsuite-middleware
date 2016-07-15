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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.contact.vcard.impl.mapping;

import java.util.List;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.exception.OXException;
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
     */
    protected SimpleMapping(int field, Class<T> propertyClass) {
        super();
        this.field = field;
        this.propertyClass = propertyClass;
    }

    /**
     * Initializes a new {@link SimpleMapping}.
     *
     * @param field The mapped contact column identifier
     * @param propertyClass The vCard property class
     * @param propertyNames The affected vCard property names
     */
    protected SimpleMapping(int field, Class<T> propertyClass, String...propertyNames) {
        super(propertyNames);
        this.field = field;
        this.propertyClass = propertyClass;
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
