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
            if (false == Strings.isEmpty(middleName)) {
                property.getAdditionalNames().add(middleName);
            }
            property.getPrefixes().clear();
            String title = contact.getTitle();
            if (false == Strings.isEmpty(title)) {
                property.getPrefixes().add(title);
            }
            property.getSuffixes().clear();
            String suffix = contact.getSuffix();
            if (false == Strings.isEmpty(suffix)) {
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
