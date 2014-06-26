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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.webdav.acl.reports;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import com.openexchange.webdav.acl.PrincipalProtocol;
import com.openexchange.webdav.action.WebdavPropfindAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link PrinicpalSearchPropertySetReport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PrinicpalSearchPropertySetReport extends WebdavPropfindAction {

    public static final String NAMESPACE = DAV_NS.getURI();
    public static final String NAME = "principal-search-property-set";

    public PrinicpalSearchPropertySetReport(Protocol protocol) {
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
        Element response = new Element("principal-search-property-set", DAV_NS);
        for (Namespace namespace : protocol.getAdditionalNamespaces()) {
            response.addNamespaceDeclaration(namespace);
        }
        /*
         * add supported search properties (currently name / email related)
         */
        Element[] searchPropertyElements = new Element[] {
            new Element("displayname", DAV_NS),
            new Element("email-address-set", PrincipalProtocol.CALENDARSERVER_NS),
            new Element("first-name", PrincipalProtocol.CALENDARSERVER_NS),
            new Element("last-name", PrincipalProtocol.CALENDARSERVER_NS),
            new Element("calendar-user-address-set", PrincipalProtocol.CAL_NS),

        };
        for (Element element : searchPropertyElements) {
            Element principalSearchProperty = new Element("principal-search-property", DAV_NS);
            Element propElement = new Element("prop", DAV_NS);
            propElement.addContent(element);
            principalSearchProperty.addContent(propElement);
            response.addContent(principalSearchProperty);
        }
        /*
         * render response
         */
        Document responseBody = new Document(response);
        try {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("text/xml; charset=UTF-8");
            outputter.output(responseBody, res.getOutputStream());
        } catch (IOException e) {
            // IGNORE
        }

    }

}
