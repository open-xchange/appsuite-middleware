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

package com.openexchange.webdav.action;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.util.Utils;
import com.openexchange.webdav.xml.resources.PropertiesMarshaller;

public class WebdavProppatchAction extends AbstractAction {

    private static final Namespace DAV_NS = Namespace.getNamespace("DAV:");
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebdavProppatchAction.class);

    private PropertyAction SET = null;
    private static final PropertyAction REMOVE = new RemoveAction();

    private final XMLOutputter outputter = new XMLOutputter();
    private final Protocol protocol;

    public WebdavProppatchAction(final Protocol protocol) {
        SET = new SetAction(protocol);
        this.protocol = protocol;
    }


    @Override
    public void perform(final WebdavRequest req, final WebdavResponse res) throws WebdavProtocolException {
        try {
            final Document requestDoc = req.getBodyAsDocument();
            final Document responseDoc = new Document();
            final Element multistatus = new Element("multistatus", DAV_NS);

            List<Namespace> namespaces = protocol.getAdditionalNamespaces();
            for (Namespace namespace : namespaces) {
                multistatus.addNamespaceDeclaration(namespace);
            }

            responseDoc.addContent(multistatus);
            final Element response = new Element("response", DAV_NS);

            PropertiesMarshaller helper = new PropertiesMarshaller(req.getURLPrefix(), req.getCharset());
            Element href = helper.marshalHREF(req.getUrl(), req.getResource().isCollection());
            response.addContent(href);

            multistatus.addContent(response);

            final WebdavResource resource = req.getResource();
            for (final Element element : requestDoc.getRootElement().getChildren()) {
                PropertyAction action = null;
                if (element.getNamespace().equals(DAV_NS)) {
                    if ("set".equals(element.getName())) {
                        action = SET;
                    } else if ("remove".equals(element.getName())) {
                        action = REMOVE;
                    }
                }

                if (null == action) {
                    continue;
                }

                for (final Element prop : element.getChildren("prop", DAV_NS)) {
                    response.addContent(action.perform(prop, resource));
                }
            }
            resource.save();
            res.setStatus(Protocol.SC_MULTISTATUS);
            res.setContentType("text/xml");
            outputter.output(responseDoc, res.getOutputStream());

        } catch (final JDOMException e) {
            LOG.error("JDOMException: ", e);
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (final IOException e) {
            LOG.debug("Client gone?", e);
        }
    }

    static interface PropertyAction {

        // Propstat element
        public Element perform(Element propElement, WebdavResource resource);
    }

    private static final class SetAction implements PropertyAction {

        private final XMLOutputter outputter = new XMLOutputter();
        private final Protocol protocol;

        public SetAction(final Protocol protocol) {
            this.protocol = protocol;
        }

        @Override
        public Element perform(final Element propElement, final WebdavResource resource) {
            int status = 200;

            if (propElement.getChildren().isEmpty()) {
                final Element propstat = new Element("propstat", DAV_NS);

                final Element statusElement = new Element("status", DAV_NS);
                statusElement.setText("HTTP/1.1 " + status + " " + Utils.getStatusString(status));
                propstat.addContent(statusElement);
                return propstat;
            }

            final Element propertyElement = propElement.getChildren().get(0);
            final WebdavProperty property = new WebdavProperty();
            property.setNamespace(propertyElement.getNamespaceURI());
            property.setName(propertyElement.getName());
            if (protocol.isProtected(propertyElement.getNamespaceURI(), propertyElement.getName())) {

                status = HttpServletResponse.SC_FORBIDDEN;
            } else {

                if (propertyElement.getChildren().size() > 0) {
                    property.setXML(true);
                    try {
                        final Writer w = new AllocatingStringWriter();
                        outputter.output(propertyElement.cloneContent(), w);
                        property.setValue(w.toString());
                    } catch (final IOException e) {
                        status = 500;
                    }


                } else {
                    property.setValue(propertyElement.getText());

                    if (propertyElement.getAttributes() != null) {
                        for (Attribute attribute : propertyElement.getAttributes()) {
                            property.addAttribute(attribute.getName(), attribute.getValue());
                        }
                    }

                }

                try {
                    resource.putProperty(property);
                } catch (final WebdavProtocolException e) {
                    status = e.getStatus();
                }
            }

            final Element propstat = new Element("propstat", DAV_NS);

            final Element prop = new Element("prop", DAV_NS);
            final Element propContent = new Element(property.getName(), Namespace.getNamespace(property.getNamespace()));
            prop.addContent(propContent);

            propstat.addContent(prop);


            final Element statusElement = new Element("status", DAV_NS);
            statusElement.setText("HTTP/1.1 " + status + " " + Utils.getStatusString(status));
            propstat.addContent(statusElement);

            return propstat;
        }

    }

    private static final class RemoveAction implements PropertyAction {

        @Override
        public Element perform(final Element propElement, final WebdavResource resource) {
            int status = 200;
            if (propElement.getChildren().isEmpty()) {
                final Element propstat = new Element("propstat", DAV_NS);

                final Element statusElement = new Element("status", DAV_NS);
                statusElement.setText("HTTP/1.1 " + status + " " + Utils.getStatusString(status));
                propstat.addContent(statusElement);
                return propstat;
            }
            final Element propertyElement = propElement.getChildren().get(0);

            try {
                resource.removeProperty(propertyElement.getNamespaceURI(), propertyElement.getName());
            } catch (final WebdavProtocolException e) {
                status = e.getStatus();
            }

            final Element propstat = new Element("propstat", DAV_NS);

            final Element prop = new Element("prop", DAV_NS);
            final Element propContent = new Element(propertyElement.getName(), Namespace.getNamespace(propertyElement.getNamespaceURI()));
            prop.addContent(propContent);

            propstat.addContent(prop);


            final Element statusElement = new Element("status", DAV_NS);
            statusElement.setText("HTTP/1.1 " + status + " " + Utils.getStatusString(status));
            propstat.addContent(statusElement);

            return propstat;
        }

    }

}
