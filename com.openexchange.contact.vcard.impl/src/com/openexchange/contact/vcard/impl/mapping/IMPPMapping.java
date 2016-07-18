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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.exception.OXException;
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
        super("IMPP");
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
        if (false == Strings.isEmpty(instantMessenger)) {
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
