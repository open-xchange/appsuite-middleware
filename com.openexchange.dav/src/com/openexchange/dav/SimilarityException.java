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

package com.openexchange.dav;

import static com.openexchange.webdav.protocol.Protocol.DAV_NS;
import org.jdom2.Element;
import org.jdom2.Namespace;
import com.openexchange.exception.OXException;

/**
 * {@link SimilarityException}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class SimilarityException extends OXException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8513856024355900129L;
    
    private static Namespace OX_NAMESPACE = Namespace.getNamespace("OX", "http://www.open-xchange.org");
    private static final String ELEMENT_NAME = "no-similar-contact";
    
    private final String hrefSimilarContact;
    private final String uidNewContact;
    private int httpResponse;

    /**
     * Initializes a new {@link SimilarityException}.
     * 
     * @param hrefSimilarContact The href to the similar contact
     * @param uidNewContact The uid of the new contact
     */
    public SimilarityException(String hrefSimilarContact, String uidNewContact, int httpResponse) {
        super();
        this.hrefSimilarContact = hrefSimilarContact;
        this.uidNewContact = uidNewContact;
        this.httpResponse = httpResponse;
    }

    public static Namespace getNamespace() {
        return OX_NAMESPACE;
    }

    public String getHrefSimilarContact() {
        return hrefSimilarContact;
    }

    public String getUidNewContact() {
        return uidNewContact;
    }
    
    public int getStatus() {
        return httpResponse;
    }
    
    public Element getElement() {
        Element result = new Element(ELEMENT_NAME, getNamespace());
        result.addContent(new Element("href", DAV_NS).setText(hrefSimilarContact));
        result.addContent(new Element("uid", DAVProtocol.CALENDARSERVER_NS).setText(uidNewContact));
        return result;
    }

}
