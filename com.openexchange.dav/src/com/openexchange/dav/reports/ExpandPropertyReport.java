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

package com.openexchange.dav.reports;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.actions.DAVPropfindAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.xml.resources.PropertiesMarshaller;
import com.openexchange.webdav.xml.resources.PropfindPropNamesMarshaller;

/**
 * {@link ExpandPropertyReport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ExpandPropertyReport extends DAVPropfindAction {

    public static final String NAMESPACE = Protocol.DEFAULT_NAMESPACE;
    public static final String NAME = "expand-property";

    /**
     * Initializes a new {@link ExpandPropertyReport}.
     *
     * @param protocol The underlying DAV protocol
     */
    public ExpandPropertyReport(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse res) throws WebdavProtocolException {
        Document requestBody = optRequestBody(request);
        if (null == requestBody) {
            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        Element rootElement = requestBody.getRootElement();
        if (null == rootElement || false == "expand-property".equals(rootElement.getName()) || false == Protocol.DAV_NS.getURI().equals(rootElement.getNamespaceURI())) {
            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }

        Element multistatusElement = prepareMultistatusElement();
        PropertiesMarshaller marshaller = new PropfindPropNamesMarshaller(request.getURLPrefix(), request.getCharset());
        for (Element requestedProperty : rootElement.getChildren()) {
            WebdavProperty property = request.getResource().getProperty(requestedProperty.getNamespaceURI(), requestedProperty.getName());
            if (null != property) {
                Element marshalledProperty = marshaller.marshalProperty(property, protocol);
                List<Element> hrefElements = marshalledProperty.getChildren("href", Protocol.DAV_NS);
                for (Element hrefElement : hrefElements) {
                    WebdavResource expandedResource = request.getFactory().resolveResource(hrefElement.getValue());
                    multistatusElement.addContent(marshaller.marshal(expandedResource));
                }
            }
        }

        sendMultistatusResponse(res, multistatusElement);
    }

}
