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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.tools.encoding.URLCoder;
import ezvcard.VCard;
import ezvcard.parameter.ImppType;
import ezvcard.property.Impp;

/**
 * {@link IMPPMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class IMPPMapping extends AbstractMapping {

    /**
     * Initializes a new {@link IMPPMapping}.
     */
    public IMPPMapping() {
        super("IMPP", ContactField.INSTANT_MESSENGER1, ContactField.INSTANT_MESSENGER2);
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        List<Impp> impps = vCard.getImpps();
        /*
         * instant_messenger1 - type "WORK"
         */
        Impp businessImpp = getPropertyWithTypes(impps, ImppType.WORK);
        if (contact.containsInstantMessenger1() && isValid(contact.getInstantMessenger1())) {
            if (null == businessImpp) {
                businessImpp = new Impp(getURI(contact.getInstantMessenger1()));
                businessImpp.getTypes().add(ImppType.WORK);
                businessImpp.getTypes().add(ImppType.PREF);
                vCard.addImpp(businessImpp);
            } else {
                businessImpp.setUri(getURI(contact.getInstantMessenger1()));
                addTypeIfMissing(businessImpp, ImppType.PREF.getValue());
            }
        } else if (null != businessImpp) {
            vCard.removeProperty(businessImpp);
        }
        /*
         * instant_messenger2 - type "HOME"
         */
        Impp homeImpp = getPropertyWithTypes(impps, ImppType.HOME);
        if (contact.containsInstantMessenger2() && isValid(contact.getInstantMessenger2())) {
            if (null == homeImpp) {
                homeImpp = new Impp(getURI(contact.getInstantMessenger2()));
                homeImpp.getTypes().add(ImppType.HOME);
                vCard.addImpp(homeImpp);
            } else {
                homeImpp.setUri(getURI(contact.getInstantMessenger2()));
            }
        } else if (null != homeImpp) {
            vCard.removeProperty(homeImpp);
        }
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        List<Impp> impps = vCard.getImpps();
        /*
         * instant_messenger1 - type "WORK"
         */
        Impp businessImpp = getPropertyWithTypes(impps, ImppType.WORK);
        contact.setInstantMessenger1(null != businessImpp ? getString(businessImpp.getUri()) : null);
        /*
         * instant_messenger2 - type "HOME"
         */
        Impp homeImpp = getPropertyWithTypes(impps, ImppType.HOME);
        contact.setInstantMessenger2(null != homeImpp ? getString(homeImpp.getUri()) : null);
    }

    private static URI getURI(String instantMessenger) {
        URI uri = null;
        if (Strings.isNotEmpty(instantMessenger)) {
            try {
                uri = new URI(null, instantMessenger, null);
            } catch (URISyntaxException e) {
                try {
                    String encoded = URLCoder.encode(instantMessenger);
                    uri = new URI(encoded);
                } catch (URISyntaxException e1) {
                    // no URI
                }
            }
        }
        return uri;
    }

    private static String getString(URI instantMessenger) {
        String string = null;
        if (null != instantMessenger) {
            string = URLCoder.decode(instantMessenger.toString());
        }
        return string;
    }

    private static boolean isValid(String instantMessenger) {
        return null != getURI(instantMessenger);
    }

}
