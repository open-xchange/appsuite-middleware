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

package com.openexchange.dav.reports;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.actions.PROPFINDAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.xml.resources.PropfindResponseMarshaller;

/**
 * {@link ExpandPropertyReport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class ExpandPropertyReport extends PROPFINDAction {

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
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        Document requestBody = optRequestBody(request);
        if (null == requestBody) {
            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        Element rootElement = requestBody.getRootElement();
        if (null == rootElement || false == "expand-property".equals(rootElement.getName()) || false == Protocol.DAV_NS.getURI().equals(rootElement.getNamespaceURI())) {
            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        Element multistatusElement = prepareMultistatusElement();
        multistatusElement.addContent(expandProperty(request, rootElement.getChildren("property", Protocol.DAV_NS), request.getResource()));
        sendMultistatusResponse(response, multistatusElement);
    }

    private Element expandProperty(WebdavRequest request, List<Element> requestedProperties, WebdavResource resource) throws WebdavProtocolException {
        PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller(request.getURLPrefix(), request.getCharset(), request.isBrief());
        Element responseElement =  new Element("response", Protocol.DAV_NS);
        responseElement.addContent(marshaller.marshalHREF(resource.getUrl(), resource.isCollection()));
        if (false == resource.exists()) {
            responseElement.addContent(marshaller.marshalStatus(HttpServletResponse.SC_NOT_FOUND));
        } else {
            Element propstatElement = new Element("propstat", Protocol.DAV_NS);
            Element propElement = new Element("prop", Protocol.DAV_NS);
            for (Element requestedProperty : requestedProperties) {
                String name = optAttributeValue("name", requestedProperty);
                String namespace = optAttributeValue("namespace", requestedProperty);
                WebdavProperty property = null == name || null == namespace ? null : resource.getProperty(namespace, name);
                if (null != property) {
                    Element marshalledProperty = marshaller.marshalProperty(property, protocol);
                    List<Element> propertiesToExpand = requestedProperty.getChildren("property", Protocol.DAV_NS);
                    if (0 < propertiesToExpand.size()) {
                        Element expandedElement = new Element(marshalledProperty.getName(), marshalledProperty.getNamespace());
                        for (WebdavPath path : getHrefPaths(request, marshalledProperty)) {
                            WebdavResource expandedResource = request.getFactory().resolveResource(path);
                            List<Element> expandedProperties = requestedProperty.getChildren("property", Protocol.DAV_NS);
                            Element expandedProperty = expandProperty(request, expandedProperties, expandedResource);
                            expandedElement.addContent(expandedProperty);
                        }
                        propElement.addContent(expandedElement);
                    } else {
                        propElement.addContent(marshalledProperty);
                    }
                }
            }
            propstatElement.addContent(propElement);
            propstatElement.addContent(marshaller.marshalStatus(HttpServletResponse.SC_OK));
            responseElement.addContent(propstatElement);
        }
        return responseElement;
    }

    private static String optAttributeValue(String attName, Element requestedProperty) {
        Attribute attribute = requestedProperty.getAttribute(attName);
        return null == attribute ? null : attribute.getValue();
    }

}
