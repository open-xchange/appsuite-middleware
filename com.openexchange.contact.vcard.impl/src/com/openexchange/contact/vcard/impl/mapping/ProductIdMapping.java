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
import com.openexchange.contact.vcard.impl.internal.VCardServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.version.VersionService;
import ezvcard.VCard;
import ezvcard.property.ProductId;
import ezvcard.property.RawProperty;


/**
 * {@link ProductIdMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ProductIdMapping extends AbstractMapping {

    /**
     * Initializes a new {@link ProductIdMapping}.
     */
    public ProductIdMapping() {
        super("PRODID");
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        vCard.removeProperties(ProductId.class);
        vCard.addProperty(new RawProperty("PRODID", getValue()));
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        // nothing to do
    }

    private static String getValue() {
        VersionService versionService = VCardServiceLookup.getOptionalService(VersionService.class);
        String versionString = null;
        if (null == versionService) {
            versionString = "<unknown version>";
        } else {
            versionString = versionService.getVersionString();
        }
        return "-//" + VersionService.NAME + "//" + versionString + "//EN";
    }

}
