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

import static org.junit.Assert.assertNotNull;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.groupware.container.Contact;
import ezvcard.VCard;

/**
 * {@link ColorLabelTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ColorLabelTest extends VCardTest {

    /**
     * Initializes a new {@link ColorLabelTest}.
     */
    public ColorLabelTest() {
        super();
    }

         @Test
     public void testExportColorLabel() {
        /*
         * create test contact
         */
        Contact contact = new Contact();
        contact.setDisplayName("test");
        contact.setLabel(Contact.LABEL_4);
        /*
         * export to new vCard
         */
        VCard vCard = getMapper().exportContact(contact, null, null, null);
        /*
         * verify vCard
         */
        assertNotNull("no vCard exported", vCard);
        assertNotNull("no color label exported", vCard.getExtendedProperty("X-OX-COLOR-LABEL"));
        Assert.assertEquals("wrong value for color label", String.valueOf(Contact.LABEL_4), vCard.getExtendedProperty("X-OX-COLOR-LABEL").getValue());
    }

         @Test
     public void testImportColorLabel() {
        /*
         * create test vCard
         */
        VCard vCard = new VCard();
        vCard.setFormattedName("test");
        vCard.setExtendedProperty("X-OX-COLOR-LABEL", String.valueOf(Contact.LABEL_7));
        /*
         * parse vCard & verify color label
         */
        Contact contact = getMapper().importVCard(vCard, null, null, null);
        assertNotNull("no contact imported", contact);
        Assert.assertEquals("wrong value for color label", Contact.LABEL_7, contact.getLabel());
    }

}
