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
import ezvcard.property.Classification;

/**
 * {@link ClassMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ClassMapping extends SimpleMapping<Classification> {

    /**
     * Initializes a new {@link ClassMapping}.
     */
    public ClassMapping() {
        super(Contact.PRIVATE_FLAG, Classification.class, "CLASS", ContactField.PRIVATE_FLAG);
    }

    @Override
    protected void exportProperty(Contact contact, Classification property, List<OXException> warnings) {
        property.setValue(contact.getPrivateFlag() ? "PRIVATE" : "PUBLIC");
    }

    @Override
    protected Classification exportProperty(Contact contact, List<OXException> warnings) {
        return new Classification(contact.getPrivateFlag() ? "PRIVATE" : "PUBLIC");
    }

    @Override
    protected void importProperty(Classification property, Contact contact, List<OXException> warnings) {
        contact.setPrivateFlag("PRIVATE".equals(property.getValue()) || "CONFIDENTIAL".equals(property.getValue()));
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        Classification existingProperty = getFirstProperty(vCard);
        if (contact.getPrivateFlag()) {
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
        Classification existingProperty = getFirstProperty(vCard);
        if (null == existingProperty) {
            contact.setPrivateFlag(false);
        } else {
            importProperty(existingProperty, contact, warnings);
        }
    }

}
