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

package com.openexchange.contact.vcard;

import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
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

    @Test
    public void testSetPropertyInContact() {
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
            Assert.assertEquals("Field " + field + " not set", setValue, importedContact.get(field));
            /*
             * import the updated contact as update of the original contact & check the property
             */
            importedContact = getMapper().importVCard(updatedVCard, originalContact, null, null);
            Assert.assertEquals("Field " + field + " not set", setValue, importedContact.get(field));
        }
    }

    @Test
    public void testUpdatePropertyInContact() {
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
            Assert.assertEquals("Field " + field + " not updated", updatedValue, importedContact.get(field));
            /*
             * import the updated contact as update of the original contact & check the property
             */
            importedContact = getMapper().importVCard(updatedVCard, originalContact, null, null);
            Assert.assertEquals("Field " + field + " not updated", updatedValue, importedContact.get(field));
        }
    }

    @Test
    public void testUpdatePropertyInVCard() {
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
            Assert.assertEquals("Field " + field + " not updated", updatedValue, importedContact.get(field));
            /*
             * import the updated contact as update of the original contact & check the property
             */
            importedContact = getMapper().importVCard(updatedVCard, originalContact, null, null);
            Assert.assertEquals("Field " + field + " not updated", updatedValue, importedContact.get(field));
        }
    }

    @Test
    public void testRemovePropertyInContact() {
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
            Assert.assertEquals("Field " + field + " not removed", null, importedContact.get(field));
            /*
             * import the updated contact as update of the original contact & check the property
             */
            importedContact = getMapper().importVCard(updatedVCard, originalContact, null, null);
            Assert.assertEquals("Field " + field + " not removed", null, importedContact.get(field));
        }
    }

    @Test
    public void testRemovePropertyInVCard() {
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
            Assert.assertEquals("Field " + field + " not removed", null, importedContact.get(field));
            /*
             * import the updated contact as update of the original contact & check the property
             */
            importedContact = getMapper().importVCard(updatedVCard, originalContact, null, null);
            Assert.assertEquals("Field " + field + " not removed", null, importedContact.get(field));
        }
    }

}
