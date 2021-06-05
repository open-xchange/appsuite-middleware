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
 * {@link ColorLabelMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ColorLabelMapping extends AbstractMapping {

    static final String X_OX_COLOR_LABEL = "X-OX-COLOR-LABEL";

    /**
     * Initializes a new {@link ColorLabelMapping}.
     */
    public ColorLabelMapping() {
        super(X_OX_COLOR_LABEL, ContactField.COLOR_LABEL);
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        RawProperty property = getFirstProperty(vCard.getExtendedProperties(X_OX_COLOR_LABEL));
        int colorLabel = contact.getLabel();
        if (Contact.LABEL_NONE != colorLabel) {
            if (null == property) {
                vCard.addExtendedProperty(X_OX_COLOR_LABEL, String.valueOf(colorLabel));
            } else {
                property.setValue(String.valueOf(colorLabel));
            }
        } else if (null != property) {
            vCard.removeProperty(property);
        }
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        RawProperty property = getFirstProperty(vCard.getExtendedProperties(X_OX_COLOR_LABEL));
        if (null != property) {
            int colorLabel;
            try {
                colorLabel = Integer.parseInt(property.getValue());
            } catch (NumberFormatException e) {
                addConversionWarning(warnings, e, X_OX_COLOR_LABEL, e.getMessage());
                return;
            }
            if (Contact.LABEL_1 <= colorLabel && colorLabel <= Contact.LABEL_10) {
                contact.setLabel(colorLabel);
            } else {
                addConversionWarning(warnings, X_OX_COLOR_LABEL, "Ignoring illegal color label: " + colorLabel);
            }
        } else {
            contact.setLabel(Contact.LABEL_NONE);
        }
    }
}
