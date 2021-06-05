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

package com.openexchange.dav.actions;

import static com.openexchange.webdav.protocol.Protocol.DAV_NS;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;

/**
 * {@link PROPFINDAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class PROPFINDAction extends DAVAction {

    /**
     * Initializes a new {@link PROPFINDAction}.
     *
     * @param protocol The underlying WebDAV protocol
     */
    public PROPFINDAction(DAVProtocol protocol) {
        super(protocol);
    }

    /**
     * Prepares an XML element ready to be used as root element for a multistatus response, containing any additionally defined
     * namespace declarations of the underlying protocol.
     *
     * @return A new multistatus element
     */
    @Override
    protected Element prepareMultistatusElement() {
        Element multistatusElement = new Element("multistatus", DAV_NS);
        for (Namespace namespace : protocol.getAdditionalNamespaces()) {
            multistatusElement.addNamespaceDeclaration(namespace);
        }
        return multistatusElement;
    }

    /**
     * Sends a multistatus response.
     *
     * @param response The WebDAV response to write to
     * @param multistatusElement The root element for the multistatus response
     */
    @Override
    protected void sendMultistatusResponse(WebdavResponse response, Element multistatusElement) {
        sendXMLResponse(response, new Document(multistatusElement), Protocol.SC_MULTISTATUS);
    }

}
