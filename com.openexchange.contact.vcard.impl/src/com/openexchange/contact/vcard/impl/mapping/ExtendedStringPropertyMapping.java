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
