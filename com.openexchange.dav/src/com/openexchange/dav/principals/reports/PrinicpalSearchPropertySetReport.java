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

package com.openexchange.dav.principals.reports;

import static com.openexchange.webdav.protocol.Protocol.DAV_NS;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.actions.PROPFINDAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link PrinicpalSearchPropertySetReport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PrinicpalSearchPropertySetReport extends PROPFINDAction {

    public static final String NAMESPACE = DAV_NS.getURI();
    public static final String NAME = "principal-search-property-set";

    /**
     * Initializes a new {@link PrinicpalSearchPropertySetReport}.
     *
     * @param protocol The protocol
     */
    public PrinicpalSearchPropertySetReport(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest req, WebdavResponse res) throws WebdavProtocolException {
        if (0 != req.getDepth(0) || false == req.getResource().isCollection()) {
            throw WebdavProtocolException.generalError(req.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        /*
         * prepare response
         */
        Element responseElement = new Element("principal-search-property-set", DAV_NS);
        for (Namespace namespace : protocol.getAdditionalNamespaces()) {
            responseElement.addNamespaceDeclaration(namespace);
        }
        /*
         * add supported search properties (currently name / email related)
         */
        Element[] searchPropertyElements = new Element[] {
            new Element("displayname", DAV_NS),
            new Element("email-address-set", DAVProtocol.CALENDARSERVER_NS),
            new Element("first-name", DAVProtocol.CALENDARSERVER_NS),
            new Element("last-name", DAVProtocol.CALENDARSERVER_NS),
            new Element("calendar-user-address-set", DAVProtocol.CAL_NS),
        };
        for (Element element : searchPropertyElements) {
            Element principalSearchProperty = new Element("principal-search-property", DAV_NS);
            Element propElement = new Element("prop", DAV_NS);
            propElement.addContent(element);
            principalSearchProperty.addContent(propElement);
            responseElement.addContent(principalSearchProperty);
        }
        /*
         * render response
         */
        sendXMLResponse(res, new Document(responseElement), HttpServletResponse.SC_OK);
    }

}
