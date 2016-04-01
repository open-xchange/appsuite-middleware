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
