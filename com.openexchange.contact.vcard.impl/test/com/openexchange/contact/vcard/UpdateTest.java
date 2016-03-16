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

package com.openexchange.contact.vcard;

import java.util.regex.Pattern;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.UUIDs;
import ezvcard.VCard;

/**
 * {@link UpdateTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UpdateTest extends VCardTest {

    /**
     * Initializes a new {@link UpdateTest}.
     */
    public UpdateTest() {
        super();
    }

    public void testSetPropertyInContact() throws Exception {
        for (int field : getMappedStringFields()) {
            /*
             * export original, empty contact as vCard
             */
            Contact originalContact = new Contact();
            VCard vCard = getMapper().exportContact(originalContact, null, null, null);
            /*
             * update property in contact & export him again
             */
            Contact updatedContact = originalContact.clone();
            String setValue = UUIDs.getUnformattedStringFromRandom();
            updatedContact.set(field, setValue);
            VCard updatedVCard = getMapper().exportContact(updatedContact, vCard, null, null);
            /*
             * import the updated vCard as new contact & check the property
             */
            Contact importedContact = getMapper().importVCard(updatedVCard, null, null, null);
            assertEquals("Field " + field + " not set", setValue, importedContact.get(field));
            /*
             * import the updated contact as update of the original contact & check the property
             */
            importedContact = getMapper().importVCard(updatedVCard, originalContact, null, null);
            assertEquals("Field " + field + " not set", setValue, importedContact.get(field));
        }
    }

    public void testUpdatePropertyInContact() throws Exception {
        for (int field : getMappedStringFields()) {
            /*
             * export original contact as vCard
             */
            Contact originalContact = new Contact();
            String originalValue = UUIDs.getUnformattedStringFromRandom();
            originalContact.set(field, originalValue);
            VCard vCard = getMapper().exportContact(originalContact, null, null, null);
            /*
             * update property in contact & export him again
             */
            Contact updatedContact = originalContact.clone();
            String updatedValue = UUIDs.getUnformattedStringFromRandom();
            updatedContact.set(field, updatedValue);
            VCard updatedVCard = getMapper().exportContact(updatedContact, vCard, null, null);
            /*
             * import the updated vCard as new contact & check the property
             */
            Contact importedContact = getMapper().importVCard(updatedVCard, null, null, null);
            assertEquals("Field " + field + " not updated", updatedValue, importedContact.get(field));
            /*
             * import the updated contact as update of the original contact & check the property
             */
            importedContact = getMapper().importVCard(updatedVCard, originalContact, null, null);
            assertEquals("Field " + field + " not updated", updatedValue, importedContact.get(field));
        }
    }

    public void testUpdatePropertyInVCard() throws Exception {
        for (int field : getMappedStringFields()) {
            /*
             * export original contact as vCard
             */
            Contact originalContact = new Contact();
            String originalValue = UUIDs.getUnformattedStringFromRandom();
            originalContact.set(field, originalValue);
            VCard vCard = getMapper().exportContact(originalContact, null, null, null);
            /*
             * update property in vCard & parse it again
             */
            String vCardText = dump(vCard);
            String updatedValue = UUIDs.getUnformattedStringFromRandom();
            String updatedVCardText = vCardText.replace(originalValue, updatedValue);
            VCard updatedVCard = parse(updatedVCardText);
            /*
             * import the updated vCard as new contact & check the property
             */
            Contact importedContact = getMapper().importVCard(updatedVCard, null, null, null);
            assertEquals("Field " + field + " not updated", updatedValue, importedContact.get(field));
            /*
             * import the updated contact as update of the original contact & check the property
             */
            importedContact = getMapper().importVCard(updatedVCard, originalContact, null, null);
            assertEquals("Field " + field + " not updated", updatedValue, importedContact.get(field));
        }
    }

    public void testRemovePropertyInContact() throws Exception {
        for (int field : getMappedStringFields()) {
            /*
             * export original contact as vCard
             */
            Contact originalContact = new Contact();
            String originalValue = UUIDs.getUnformattedStringFromRandom();
            originalContact.set(field, originalValue);
            VCard vCard = getMapper().exportContact(originalContact, null, null, null);
            /*
             * remove property in contact & export him again
             */
            Contact updatedContact = originalContact.clone();
            updatedContact.set(field, null);
            VCard updatedVCard = getMapper().exportContact(updatedContact, vCard, null, null);
            /*
             * import the updated vCard as new contact & check the property
             */
            Contact importedContact = getMapper().importVCard(updatedVCard, null, null, null);
            assertEquals("Field " + field + " not removed", null, importedContact.get(field));
            /*
             * import the updated contact as update of the original contact & check the property
             */
            importedContact = getMapper().importVCard(updatedVCard, originalContact, null, null);
            assertEquals("Field " + field + " not removed", null, importedContact.get(field));
        }
    }

    public void testRemovePropertyInVCard() throws Exception {
        for (int field : getMappedStringFields()) {
            /*
             * export original contact as vCard
             */
            Contact originalContact = new Contact();
            String originalValue = UUIDs.getUnformattedStringFromRandom();
            originalContact.set(field, originalValue);
            VCard vCard = getMapper().exportContact(originalContact, null, null, null);
            /*
             * remove property in vCard & parse it again
             */
            String vCardText = dump(vCard);
            Pattern parameterPattern = Pattern.compile("^.+" + originalValue + ".*$", Pattern.MULTILINE);
            String updatedVCardText = parameterPattern.matcher(vCardText).replaceFirst("");
            VCard updatedVCard = parse(updatedVCardText);
            /*
             * import the updated vCard as new contact & check the property
             */
            Contact importedContact = getMapper().importVCard(updatedVCard, null, null, null);
            assertEquals("Field " + field + " not removed", null, importedContact.get(field));
            /*
             * import the updated contact as update of the original contact & check the property
             */
            importedContact = getMapper().importVCard(updatedVCard, originalContact, null, null);
            assertEquals("Field " + field + " not removed", null, importedContact.get(field));
        }
    }

}
